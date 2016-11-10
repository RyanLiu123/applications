/*
 * Copyright (C) 2016 liunianliang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaqin.mmitest.fm;

import android.app.Service;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.content.Context;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;

import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.AudioSystem;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.media.AudioDevicePort;
import android.media.AudioDevicePortConfig;
import android.media.AudioFormat;
import android.media.AudioManager.OnAudioPortUpdateListener;
import android.media.AudioMixPort;
import android.media.AudioPatch;
import android.media.AudioPort;
import android.media.AudioPortConfig;
import android.media.AudioRecord;
import android.media.AudioSystem;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.HandlerThread;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.huaqin.mmitest.MMITestService;
import com.huaqin.mmitest.util.LogUtil;
import com.mediatek.fmradio.FmRadioNative;

public class FM_M
{
    private static String TAG = "FM";

    static final int MSGID_SWITCH_ANTENNA = 4;
    // Fm main
    static final int MSGID_POWERUP_FINISHED = 9;
    static final int MSGID_POWERDOWN_FINISHED = 10;
    // Audio focus related
    static final int MSGID_AUDIOFOCUS_CHANGED = 30;
    String KEY_AUDIOFOCUS_CHANGED = "key_audiofocus_changed";
    String SWITCH_ANTENNA_VALUE = "switch_antenna_value";

    public static int POWER_UP = 0;
    public static int DURING_POWER_UP = 1;
    public static int POWER_DOWN = 2;

    // Headset
    private static final int HEADSET_PLUG_IN = 1;

    private static Context mContext = null;
    private AudioManager mAudioManager = null;

    // default station frequency
    public static final int DEFAULT_STATION = 879;
    private float mCurrentStation = computeFrequency((float) DEFAULT_STATION);

    // Record whether is speaker used
    private boolean mIsSpeakerUsed = false;
    // Record whether device is open
    private boolean mIsDeviceOpen = false;
    // Record Power Status
    private int mPowerStatus = POWER_DOWN;

    private String mRds = "-1";

    private boolean mIsMuted = false;
    // Audio focus is held or not
    private boolean mIsAudioFocusHeld = false;

    private static final int FOR_PROPRIETARY = 1;

    // RDS events
    // PS
    private static final int RDS_EVENT_PROGRAMNAME = 0x0008;
    // RT
    private static final int RDS_EVENT_LAST_RADIOTEXT = 0x0040;
    // AF
    private static final int RDS_EVENT_AF = 0x0080;
    private Thread mRdsThread = null;
    // record whether RDS thread exit
    private boolean mIsRdsThreadExit = false;

    // Forced Use value
    private int mForcedUseForMedia;
    private int mValueHeadSetPlug = 1;

    private FmServiceBroadcastReceiver mBroadcastReceiver = null;
    private FmRadioServiceHandler mFmServiceHandler;

    private static final String FM_FREQUENCY = "frequency";

    // Audio Patch
    private AudioPatch mAudioPatch = null;

    private Object mRenderLock = new Object();

    private Thread mRenderThread = null;
    private AudioTrack mAudioTrack = null;
    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int RECORD_BUF_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE,
            CHANNEL_CONFIG, AUDIO_FORMAT);
    private boolean mIsRender = false;

    AudioDevicePort mAudioSource = null;
    AudioDevicePort mAudioSink = null;

    public static final int CONVERT_RATE = 10;
    private FMTuneThread mFMTuneThread;
    private boolean mHideResultButton = true;
    
    private float mValue;
    private String mType;

    private class FmServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String command = intent.getStringExtra("command");
            LogUtil.d(TAG, "onReceive, action = " + action + " / command = " + command);
            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                // switch antenna should not impact audio focus status
                mValueHeadSetPlug = (intent.getIntExtra("state", -1) == HEADSET_PLUG_IN) ? 0 : 1;
                switchAntennaAsync(mValueHeadSetPlug);

                int state = intent.getIntExtra("state", -1);
                LogUtil.d(TAG, "onReceive, state = " + state);
                /*
                 * If ear phone insert and activity is foreground. power up FM automatic
                 */
                if (0 == mValueHeadSetPlug) {
                    if (mValue != 0) {
                        if (mFMTuneThread != null && mFMTuneThread.isAlive()) {
                            try {
                                mFMTuneThread.interrupt();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        mFMTuneThread = new FMTuneThread(TAG);
                        mFMTuneThread.start();
                    }
                    // powerUpAsync(mCurrentStation);
                } else if (1 == mValueHeadSetPlug) {
                    mFmServiceHandler.removeMessages(
                            MSGID_POWERDOWN_FINISHED);
                    mFmServiceHandler.removeMessages(
                            MSGID_POWERUP_FINISHED);
                    focusChanged(AudioManager.AUDIOFOCUS_LOSS);
                    setForceUse(false);
                }
            }
        }
    }

    public FM_M(MMITestService context) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    public void play(MMITestService context, String value, String type) {

        if (context == null)
            return;
        mContext = context;
        if (value != null) {
            mValue = Float.parseFloat(value);
        }
        mType = type;
        LogUtil.i(TAG, "play");

        if (type.equals(context.MMI_FM_PLAY)) {
            startTest();
        } else if (type.equals(context.MMI_FM_STOP)) {
            stopTest();
        }

    }
    
    public void startTest() {

        openDevice();
        // set speaker to default status, avoid setting->clear data.
        setForceUse(mIsSpeakerUsed);
        initAudioRecordSink();

        HandlerThread handlerThread = new HandlerThread("FmRadioServiceThread");
        handlerThread.start();
        mFmServiceHandler = new FmRadioServiceHandler(handlerThread.getLooper());

        registerFmBroadcastReceiver();
        registerAudioPortUpdateListener();

        if (!isAntennaAvailable()) {
            LogUtil.d(TAG, ">>> !isHeadsetOn()");
        } else {
            if (mValue != 0) {
                if (mFMTuneThread != null && mFMTuneThread.isAlive()) {
                    try {
                        mFMTuneThread.interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mFMTuneThread = new FMTuneThread(TAG);
                mFMTuneThread.start();
            }
        }
    }

    public void stopTest() {
        if (mFMTuneThread != null && mFMTuneThread.isAlive()) {
            try {
                mFMTuneThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        enableFmAudio(false);
        abandonAudioFocus();
        unregisterFmBroadcastReceiver();
        unregisterAudioPortUpdateListener();
        mPowerStatus = POWER_DOWN;
        closeDevice();
        LogUtil.i(TAG,"stop Test !");
        sendResult("pass");
    }

    public float computeFrequency(float station) {
        return (float) station / CONVERT_RATE;
    }

    private boolean openDevice() {
        LogUtil.d(TAG, "openDevice");
        if (!mIsDeviceOpen) {
            mIsDeviceOpen = FmRadioNative.openDev();
        }
        return mIsDeviceOpen;
    }

    private boolean closeDevice() {
        LogUtil.d(TAG, "closeDevice");
        boolean isDeviceClose = false;
        if (mIsDeviceOpen) {
            isDeviceClose = FmRadioNative.closeDev();
            mIsDeviceOpen = !isDeviceClose;
        }
        // quit looper
        mFmServiceHandler.getLooper().quit();
        return isDeviceClose;
    }

    private void registerFmBroadcastReceiver() {
        LogUtil.d(TAG, "registerFmBroadcastReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        mBroadcastReceiver = new FmServiceBroadcastReceiver();
        mContext.registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterFmBroadcastReceiver() {
        LogUtil.d(TAG, "unregisterFmBroadcastReceiver");
        if (null != mBroadcastReceiver) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private void registerAudioPortUpdateListener() {
        LogUtil.d(TAG, "registerAudioPortUpdateListener");
        if (mAudioPortUpdateListener == null) {
            mAudioPortUpdateListener = new FmOnAudioPortUpdateListener();
            mAudioManager.registerAudioPortUpdateListener(mAudioPortUpdateListener);
        }
    }

    private void unregisterAudioPortUpdateListener() {
        LogUtil.d(TAG, "unregisterAudioPortUpdateListener");
        if (mAudioPortUpdateListener != null) {
            mAudioManager.unregisterAudioPortUpdateListener(mAudioPortUpdateListener);
            mAudioPortUpdateListener = null;
        }
    }

    private void setForceUse(boolean isSpeaker) {
        LogUtil.d(TAG, "setForceUse isSpeaker:" + isSpeaker);
        mForcedUseForMedia = isSpeaker ? AudioSystem.FORCE_SPEAKER : AudioSystem.FORCE_NONE;
        AudioSystem.setForceUse(FOR_PROPRIETARY, mForcedUseForMedia);
        mIsSpeakerUsed = isSpeaker;
    }

    private synchronized void initAudioRecordSink() {
        LogUtil.d(TAG, "initAudioRecordSink");
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, RECORD_BUF_SIZE, AudioTrack.MODE_STREAM);
    }

    private synchronized void createAudioPatchByEarphone() {
        LogUtil.d(TAG, "createAudioPatchByEarphone");
        if (mAudioPatch != null) {
            LogUtil.d(TAG, "createAudioPatch, mAudioPatch is not null, return");
            return;
        }

        mAudioSource = null;
        mAudioSink = null;
        ArrayList<AudioPort> ports = new ArrayList<AudioPort>();
        mAudioManager.listAudioPorts(ports);
        for (AudioPort port : ports) {
            if (port instanceof AudioDevicePort) {
                int type = ((AudioDevicePort) port).type();
                String name = AudioSystem.getOutputDeviceName(type);
                if (type == AudioSystem.DEVICE_IN_FM_TUNER) {
                    mAudioSource = (AudioDevicePort) port;
                } else if (type == AudioSystem.DEVICE_OUT_WIRED_HEADSET ||
                        type == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE) {
                    mAudioSink = (AudioDevicePort) port;
                }
            }
        }
        if (mAudioSource != null && mAudioSink != null) {
            AudioDevicePortConfig sourceConfig = (AudioDevicePortConfig) mAudioSource
                    .activeConfig();
            AudioDevicePortConfig sinkConfig = (AudioDevicePortConfig) mAudioSink.activeConfig();
            AudioPatch[] audioPatchArray = new AudioPatch[] {
                    null
            };
            mAudioManager.createAudioPatch(audioPatchArray,
                    new AudioPortConfig[] {
                        sourceConfig
                    },
                    new AudioPortConfig[] {
                        sinkConfig
                    });
            mAudioPatch = audioPatchArray[0];
        }
    }

    private synchronized void createAudioPatchBySpeaker() {
        LogUtil.d(TAG, "createAudioPatchBySpeaker");
        if (mAudioPatch != null) {
            LogUtil.d(TAG, "createAudioPatch, mAudioPatch is not null, return");
            return;
        }

        mAudioSource = null;
        mAudioSink = null;
        ArrayList<AudioPort> ports = new ArrayList<AudioPort>();
        mAudioManager.listAudioPorts(ports);
        for (AudioPort port : ports) {
            if (port instanceof AudioDevicePort) {
                int type = ((AudioDevicePort) port).type();
                String name = AudioSystem.getOutputDeviceName(type);
                if (type == AudioSystem.DEVICE_IN_FM_TUNER) {
                    mAudioSource = (AudioDevicePort) port;
                } else if (type == AudioSystem.DEVICE_OUT_SPEAKER) {
                    mAudioSink = (AudioDevicePort) port;
                }
            }
        }
        if (mAudioSource != null && mAudioSink != null) {
            AudioDevicePortConfig sourceConfig = (AudioDevicePortConfig) mAudioSource
                    .activeConfig();
            AudioDevicePortConfig sinkConfig = (AudioDevicePortConfig) mAudioSink.activeConfig();
            AudioPatch[] audioPatchArray = new AudioPatch[] {
                    null
            };
            mAudioManager.createAudioPatch(audioPatchArray,
                    new AudioPortConfig[] {
                        sourceConfig
                    },
                    new AudioPortConfig[] {
                        sinkConfig
                    });
            mAudioPatch = audioPatchArray[0];
        }
    }

    public void switchAntennaAsync(int antenna) {
        LogUtil.d(TAG, "switchAntennaAsync:" + antenna);
        final int bundleSize = 1;
        mFmServiceHandler.removeMessages(MSGID_SWITCH_ANTENNA);

        Bundle bundle = new Bundle(bundleSize);
        bundle.putInt(SWITCH_ANTENNA_VALUE, antenna);
        Message msg = mFmServiceHandler.obtainMessage(MSGID_SWITCH_ANTENNA);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    public void powerUpAsync(float frequency) {
        LogUtil.d(TAG, "powerUpAsync:" + frequency);
        final int bundleSize = 1;
        mFmServiceHandler.removeMessages(MSGID_POWERUP_FINISHED);
        mFmServiceHandler.removeMessages(MSGID_POWERDOWN_FINISHED);
        Bundle bundle = new Bundle(bundleSize);
        bundle.putFloat(FM_FREQUENCY, frequency);
        Message msg = mFmServiceHandler.obtainMessage(MSGID_POWERUP_FINISHED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    public void powerDownAsync() {
        LogUtil.d(TAG, "powerDownAsync");
        // if power down Fm, should remove message first.
        // not remove all messages, because such as recorder message need
        // to execute after or before power down
        mFmServiceHandler.removeMessages(MSGID_POWERDOWN_FINISHED);
        mFmServiceHandler.removeMessages(MSGID_POWERUP_FINISHED);
        mFmServiceHandler.sendEmptyMessage(MSGID_POWERDOWN_FINISHED);
    }

    private boolean firstPlaying(float frequency) {
        if (mPowerStatus != POWER_UP) {
            LogUtil.w(TAG, "firstPlaying, FM is not powered up");
            return false;
        }
        LogUtil.d(TAG, "firstPlaying");
        boolean isSeekTune = FmRadioNative.tune(frequency);
        if (isSeekTune) {
            playFrequency(frequency);
        }
        return isSeekTune;
    }

    private boolean tuneStation(float frequency) {
        LogUtil.d(TAG, "tuneStation:" + frequency + "  mPowerStatus = "+mPowerStatus);
        if (mPowerStatus == POWER_UP) {
            setRds(false);
            boolean bRet = FmRadioNative.tune(frequency);
            if (bRet) {
                sendResult("pass");
                setRds(true);
                mCurrentStation = frequency;
            } else {
                sendResult("fail");
            }
            setMute(false);
            return bRet;
        }

        // if earphone is not insert, not power up
        LogUtil.d(TAG, "isAntennaAvailable = "+isAntennaAvailable());
        if (!isAntennaAvailable()) {
            return false;
        }

        // if not power up yet, should powerup first
        boolean tune = false;

        if (powerUp(frequency)) {
            tune = playFrequency(frequency);
        }
        return tune;
    }

    private void handlePowerUp(Bundle bundle) {
        LogUtil.d(TAG, "handlePowerUp");
        boolean isPowerUp = false;
        boolean isSwitch = true;
        float curFrequency = bundle.getFloat(FM_FREQUENCY);

        if (!isAntennaAvailable()) {
            LogUtil.d(TAG, "handlePowerUp, earphone is not ready");
            return;
        }
        if (powerUp(curFrequency)) {
            isPowerUp = playFrequency(curFrequency);
        }
    }

    private void handlePowerDown() {
        LogUtil.d(TAG, "handlePowerDown");
        boolean isPowerdown = powerDown();
    }

    private boolean powerUp(float frequency) {
        LogUtil.d(TAG, "powerUp:" + frequency);
        if (mPowerStatus == POWER_UP) {
            return true;
        }
        LogUtil.d(TAG, "powerUp success");
        if (!requestAudioFocus()) {
            // activity used for update powerdown menu
            mPowerStatus = POWER_DOWN;
            return false;
        }

        mPowerStatus = DURING_POWER_UP;

        // if device open fail when chip reset, it need open device again before
        // power up
        if (!mIsDeviceOpen) {
            openDevice();
        }

        if (!FmRadioNative.powerUp(frequency)) {
            mPowerStatus = POWER_DOWN;
            return false;
        }
        mPowerStatus = POWER_UP;
        // need mute after power up
        setMute(true);

        return (mPowerStatus == POWER_UP);
    }

    private boolean powerDown() {
        LogUtil.d(TAG, "powerDown start");
        if (mPowerStatus == POWER_DOWN) {
            return true;
        }
        LogUtil.d(TAG, "powerDown success");
        setMute(true);
        setRds(false);
        enableFmAudio(false);

        if (!FmRadioNative.powerDown(0)) {

            if (isRdsSupported()) {
                stopRdsThread();
            }


            return false;
        }
        // activity used for update powerdown menu
        mPowerStatus = POWER_DOWN;

        if (isRdsSupported()) {
            stopRdsThread();
        }

        return true;
    }

    private void stopRdsThread() {
        LogUtil.d(TAG, "stopRdsThread");
        if (null != mRdsThread) {
            // Must call closedev after stopRDSThread.
            mIsRdsThreadExit = true;
            mRdsThread = null;
        }
    }

    public boolean isAntennaAvailable() {
        return mAudioManager.isWiredHeadsetOn();
    }

    public int setMute(boolean mute) {
        LogUtil.d(TAG, "setMute:" + mute);
        if (mPowerStatus != POWER_UP) {
            LogUtil.w(TAG, "setMute, FM is not powered up");
            return -1;
        }
        int iRet = FmRadioNative.setMute(mute);
        mIsMuted = mute;
        return iRet;
    }

    public boolean requestAudioFocus() {
        LogUtil.d(TAG, "requestAudioFocus");
        if (mIsAudioFocusHeld) {
            return true;
        }

        int audioFocus = mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        mIsAudioFocusHeld = (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioFocus);
        return mIsAudioFocusHeld;
    }

    public void abandonAudioFocus() {
        LogUtil.d(TAG, "abandonAudioFocus");
        mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
        mIsAudioFocusHeld = false;
    }

    private void focusChanged(int focusState) {
        LogUtil.d(TAG, "focusChanged:" + focusState);
        mIsAudioFocusHeld = false;
        // using handler thread to update audio focus state
        updateAudioFocusAync(focusState);
    }

    private synchronized void updateAudioFocusAync(int focusState) {
        LogUtil.d(TAG, "updateAudioFocusAync:" + focusState);
        final int bundleSize = 1;
        Bundle bundle = new Bundle(bundleSize);
        bundle.putInt(KEY_AUDIOFOCUS_CHANGED, focusState);
        Message msg = mFmServiceHandler.obtainMessage(MSGID_AUDIOFOCUS_CHANGED);
        msg.setData(bundle);
        mFmServiceHandler.sendMessage(msg);
    }

    /**
     * Use to interact with other voice related app
     */
    private final OnAudioFocusChangeListener mAudioFocusChangeListener =
            new OnAudioFocusChangeListener() {
                /**
                 * Handle audio focus change ensure message FIFO
                 * 
                 * @param focusChange audio focus change state
                 */
                @Override
                public void onAudioFocusChange(int focusChange) {
                    LogUtil.d(TAG, "onAudioFocusChange " + focusChange);
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            synchronized (this) {
                                mAudioManager.setParameters("AudioFmPreStop=1");
                                setMute(true);
                                focusChanged(AudioManager.AUDIOFOCUS_LOSS);
                            }
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            synchronized (this) {
                                mAudioManager.setParameters("AudioFmPreStop=1");
                                setMute(true);
                                focusChanged(AudioManager.AUDIOFOCUS_LOSS_TRANSIENT);
                            }
                            break;

                        case AudioManager.AUDIOFOCUS_GAIN:
                            synchronized (this) {
                                updateAudioFocusAync(AudioManager.AUDIOFOCUS_GAIN);
                            }
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            synchronized (this) {
                                updateAudioFocusAync(
                                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);
                            }
                            break;

                        default:
                            break;
                    }
                }
            };

    private boolean playFrequency(float frequency) {
        LogUtil.d(TAG, "playFrequency:" + frequency);
        mCurrentStation = frequency;

        // Start the RDS thread if RDS is supported.
        if (isRdsSupported()) {
            startRdsThread();
        }

        if (mIsSpeakerUsed != isSpeakerPhoneOn()) {
            setForceUse(mIsSpeakerUsed);
        }
        enableFmAudio(true);

        setRds(true);
        setMute(false);

        return (mPowerStatus == POWER_UP);
    }

    private boolean isSpeakerPhoneOn() {
        return (mForcedUseForMedia == AudioSystem.FORCE_SPEAKER);
    }

    public boolean isRdsSupported() {
        boolean isRdsSupported = (FmRadioNative.isRdsSupport() == 1);
        LogUtil.e(TAG,"isRdsSupported = "+isRdsSupported);
        return isRdsSupported;
    }

    private void startRdsThread() {
        LogUtil.d(TAG, "startRdsThread");
        mIsRdsThreadExit = false;
        if (null != mRdsThread) {
            return;
        }
        mRdsThread = new Thread() {
            public void run() {
                while (true) {
                    if (mIsRdsThreadExit) {
                        break;
                    }

                    int iRdsEvents = FmRadioNative.readRds();
                    if (iRdsEvents != 0) {
                        LogUtil.d(TAG, "startRdsThread, is rds events: " + iRdsEvents);
                    }

                    if (RDS_EVENT_PROGRAMNAME == (RDS_EVENT_PROGRAMNAME & iRdsEvents)) {
                        byte[] bytePS = FmRadioNative.getPs();
                        if (null != bytePS) {
                            String ps = new String(bytePS).trim();
                            // setPs(ps);
                            // TODO
                        }
                    }

                    if (RDS_EVENT_LAST_RADIOTEXT == (RDS_EVENT_LAST_RADIOTEXT & iRdsEvents)) {
                        byte[] byteLRText = FmRadioNative.getLrText();
                        if (null != byteLRText) {
                            String rds = new String(byteLRText).trim();
                            mRds = rds;
                        }
                    }
                    // Do not handle other events.
                    // Sleep 500ms to reduce inquiry frequency
                    try {
                        final int hundredMillisecond = 500;
                        Thread.sleep(hundredMillisecond);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mRdsThread.start();
    }

    private void enableFmAudio(boolean enable) {
        LogUtil.d(TAG, "enableFmAudio:" + enable);
        if (enable) {
            if ((mPowerStatus != POWER_UP) || !mIsAudioFocusHeld) {
                LogUtil.d(TAG, "enableFmAudio, current not available return.mIsAudioFocusHeld:"
                        + mIsAudioFocusHeld);
                return;
            }

            startAudioTrack();
            ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
            mAudioManager.listAudioPatches(patches);
            if (mAudioPatch == null) {
                if (isPatchMixerToEarphone(patches)) {
                    stopAudioTrack();
                    stopRender();
                    createAudioPatchByEarphone();
                } else if (isPatchMixerToSpeaker(patches)) {
                    stopAudioTrack();
                    stopRender();
                    createAudioPatchBySpeaker();
                } else {
                    startRender();
                }
            }
        } else {
            releaseAudioPatch();
            stopRender();
        }
    }

    private void startAudioTrack() {
        LogUtil.d(TAG, "startAudioTrack");
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
            ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
            mAudioManager.listAudioPatches(patches);
            mAudioTrack.play();
        }
    }

    private void stopAudioTrack() {
        LogUtil.d(TAG, "stopAudioTrack");
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }
    }

    private synchronized void startRender() {
        LogUtil.d(TAG, "startRender " + AudioSystem.getForceUse(FOR_PROPRIETARY));

        if (mAudioTrack != null) {
            mAudioTrack.stop();
        }
        initAudioRecordSink();

        mIsRender = true;
        synchronized (mRenderLock) {
            mRenderLock.notify();
        }
    }

    private synchronized void stopRender() {
        LogUtil.d(TAG, "stopRender");
        mIsRender = false;
    }

    // Make sure patches count will not be 0
    private boolean isPatchMixerToEarphone(ArrayList<AudioPatch> patches) {
        LogUtil.d(TAG, "isPatchMixerToEarphone");
        int deviceCount = 0;
        int deviceEarphoneCount = 0;
        for (AudioPatch patch : patches) {
            AudioPortConfig[] sources = patch.sources();
            AudioPortConfig[] sinks = patch.sinks();
            AudioPortConfig sourceConfig = sources[0];
            AudioPortConfig sinkConfig = sinks[0];
            AudioPort sourcePort = sourceConfig.port();
            AudioPort sinkPort = sinkConfig.port();
            LogUtil.d(TAG, "isPatchMixerToEarphone " + sourcePort + " ====> " + sinkPort);
            if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                deviceCount++;
                int type = ((AudioDevicePort) sinkPort).type();
                if (type == AudioSystem.DEVICE_OUT_WIRED_HEADSET ||
                        type == AudioSystem.DEVICE_OUT_WIRED_HEADPHONE) {
                    deviceEarphoneCount++;
                }
            }
        }
        if (deviceEarphoneCount == 1 && deviceCount == deviceEarphoneCount) {
            return true;
        }
        return false;
    }

    // Make sure patches count will not be 0
    private boolean isPatchMixerToSpeaker(ArrayList<AudioPatch> patches) {
        LogUtil.d(TAG, "isPatchMixerToSpeaker");
        int deviceCount = 0;
        int deviceEarphoneCount = 0;
        for (AudioPatch patch : patches) {
            AudioPortConfig[] sources = patch.sources();
            AudioPortConfig[] sinks = patch.sinks();
            AudioPortConfig sourceConfig = sources[0];
            AudioPortConfig sinkConfig = sinks[0];
            AudioPort sourcePort = sourceConfig.port();
            AudioPort sinkPort = sinkConfig.port();
            LogUtil.d(TAG, "isPatchMixerToSpeaker " + sourcePort + " ====> " + sinkPort);
            if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                deviceCount++;
                int type = ((AudioDevicePort) sinkPort).type();
                if (type == AudioSystem.DEVICE_OUT_SPEAKER) {
                    deviceEarphoneCount++;
                }
            }
        }
        if (deviceEarphoneCount == 1 && deviceCount == deviceEarphoneCount) {
            return true;
        }
        return false;
    }

    private boolean isPatchMixerToDeviceRemoved(ArrayList<AudioPatch> patches) {
        LogUtil.d(TAG, "isPatchMixerToDeviceRemoved");
        boolean noMixerToDevice = true;
        for (AudioPatch patch : patches) {
            AudioPortConfig[] sources = patch.sources();
            AudioPortConfig[] sinks = patch.sinks();
            AudioPortConfig sourceConfig = sources[0];
            AudioPortConfig sinkConfig = sinks[0];
            AudioPort sourcePort = sourceConfig.port();
            AudioPort sinkPort = sinkConfig.port();

            if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort) {
                noMixerToDevice = false;
                break;
            }
        }
        return noMixerToDevice;
    }

    private boolean isOutputDeviceChanged(ArrayList<AudioPatch> patches) {
        LogUtil.d(TAG, "isOutputDeviceChanged");

        AudioPortConfig[] origSources = null;
        AudioPortConfig[] origSinks = null;
        synchronized (this) {
            // need synchronized to avoid NPE of mAudioPatch, which
            // is reassigned to null in releaseAudioPatch().
            if (mAudioPatch == null) {
                LogUtil.d(TAG, "isOutputDeviceChanged, mAudioPatch is null, return");
                return false;
            }
            origSources = mAudioPatch.sources();
            origSinks = mAudioPatch.sinks();
        }
        AudioPort origSrcPort = origSources[0].port();
        AudioPort origSinkPort = origSinks[0].port();
        LogUtil.d(TAG, "DEBUG " + origSinkPort);

        for (AudioPatch aPatch : patches) {
            AudioPortConfig[] sources = aPatch.sources();
            AudioPortConfig[] sinks = aPatch.sinks();
            AudioPortConfig sourceConfig = sources[0];
            AudioPortConfig sinkConfig = sinks[0];
            AudioPort sourcePort = sourceConfig.port();
            AudioPort sinkPort = sinkConfig.port();
            LogUtil.d(TAG, "DEBUG " + sourcePort + " sink: " + sinkPort + " origPort: " + origSinkPort);
            if (sourcePort instanceof AudioMixPort && sinkPort instanceof AudioDevicePort &&
                    origSinkPort instanceof AudioDevicePort) {
                if (((AudioDevicePort) sinkPort).type() != ((AudioDevicePort) origSinkPort).type()) {
                    return true;
                }
            }
        }
        return false;
    }

    private synchronized void releaseAudioPatch() {
        if (mAudioPatch != null) {
            LogUtil.d(TAG, "releaseAudioPatch");
            mAudioManager.releaseAudioPatch(mAudioPatch);
            mAudioPatch = null;
        }
        mAudioSource = null;
        mAudioSink = null;
    }

    private int setRds(boolean on) {
        LogUtil.d(TAG, "setRds:" + on);
        if (mPowerStatus != POWER_UP) {
            return -1;
        }
        int ret = -1;
        if (isRdsSupported()) {
            ret = FmRadioNative.setRds(on);
        }
        return ret;
    }

    private void updateAudioFocus(int focusState) {
        LogUtil.d(TAG, "updateAudioFocus:" + focusState);
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_LOSS:
                handlePowerDown();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                handlePowerDown();
                break;

            case AudioManager.AUDIOFOCUS_GAIN:
                if ((mPowerStatus != POWER_UP)) {
                    final int bundleSize = 1;
                    mFmServiceHandler.removeMessages(MSGID_POWERUP_FINISHED);
                    mFmServiceHandler.removeMessages(MSGID_POWERDOWN_FINISHED);
                    Bundle bundle = new Bundle(bundleSize);
                    bundle.putFloat(FM_FREQUENCY, mCurrentStation);
                    handlePowerUp(bundle);
                }
                setMute(false);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                setMute(true);
                break;

            default:
                break;
        }
    }

    private FmOnAudioPortUpdateListener mAudioPortUpdateListener = null;

    private class FmOnAudioPortUpdateListener implements OnAudioPortUpdateListener {
        /**
         * Callback method called upon audio port list update.
         * 
         * @param portList the updated list of audio ports
         */
        @Override
        public void onAudioPortListUpdate(AudioPort[] portList) {
            // Ingore audio port update
        }

        /**
         * Callback method called upon audio patch list update.
         * 
         * @param patchList the updated list of audio patches
         */
        @Override
        public void onAudioPatchListUpdate(AudioPatch[] patchList) {
            if (mPowerStatus != POWER_UP) {
                LogUtil.d(TAG, "onAudioPatchListUpdate, not power up");
                return;
            }

            if (!mIsAudioFocusHeld) {
                LogUtil.d(TAG, "onAudioPatchListUpdate no audio focus");
                return;
            }
            LogUtil.d(TAG, "onAudioPatchListUpdate");

            if (mAudioPatch != null) {
                ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
                mAudioManager.listAudioPatches(patches);
                // When BT or WFD is connected, native will remove the patch (mixer -> device).
                // Need to recreate AudioRecord and AudioTrack for this case.
                if (isPatchMixerToDeviceRemoved(patches)) {
                    LogUtil.d(TAG, "onAudioPatchListUpdate reinit for BT or WFD connected");
                    initAudioRecordSink();
                    startRender();
                    return;
                }
                if (isPatchMixerToEarphone(patches)) {
                    stopRender();
                    if (isOutputDeviceChanged(patches)) {
                        LogUtil.d(TAG, "DEBUG outputDeviceChanged: re-create audio patch");
                        releaseAudioPatch();
                        createAudioPatchByEarphone();
                    }
                } else if (isPatchMixerToSpeaker(patches)) {
                    stopRender();
                    if (isOutputDeviceChanged(patches)) {
                        LogUtil.d(TAG, "DEBUG outputDeviceChanged: re-create audio patch");
                        releaseAudioPatch();
                        createAudioPatchBySpeaker();
                    }
                } else {
                    releaseAudioPatch();
                    startRender();
                }
            } else if (mIsRender) {
                ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
                mAudioManager.listAudioPatches(patches);
                if (isPatchMixerToEarphone(patches)) {
                    stopAudioTrack();
                    stopRender();
                    createAudioPatchByEarphone();
                } else if (isPatchMixerToSpeaker(patches)) {
                    stopAudioTrack();
                    stopRender();
                    createAudioPatchBySpeaker();
                }
            }
        }

        /**
         * Callback method called when the mediaserver dies
         */
        @Override
        public void onServiceDied() {
            enableFmAudio(false);
        }
    }

    class FmRadioServiceHandler extends Handler {
        public FmRadioServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            boolean isPowerup = false;
            boolean isSwitch = true;

            LogUtil.d(TAG, "handleMessage:" + msg.what);

            switch (msg.what) {

            // power up
                case MSGID_POWERUP_FINISHED:
                    bundle = msg.getData();
                    handlePowerUp(bundle);
                    break;

                // power down
                case MSGID_POWERDOWN_FINISHED:
                    handlePowerDown();
                    break;

                // switch antenna
                case MSGID_SWITCH_ANTENNA:
                    bundle = msg.getData();
                    int value = bundle.getInt(SWITCH_ANTENNA_VALUE);

                    // if ear phone insert, need dismiss plugin earphone
                    // dialog
                    // if earphone plug out and it is not play recorder
                    // state, show plug diaLogUtil.
                    if (0 == value) {
                        // TODO
                    } else {

                    }
                    break;
                case MSGID_AUDIOFOCUS_CHANGED:
                    bundle = msg.getData();
                    int focusState = bundle.getInt(KEY_AUDIOFOCUS_CHANGED);
                    updateAudioFocus(focusState);
                    break;

                default:
                    break;
            }
        }

    }

    class FMTuneThread extends Thread {
        private float frequency = 0;

        public FMTuneThread(String name) {
            super(name);
        }

        public void run() {
            try {
                frequency = mValue;
                tuneStation(frequency);
                LogUtil.d(TAG, ">>> TestFM  frequency " + frequency);
            } catch (Throwable t) {
                LogUtil.d(TAG,
                        "[mRecordListener: mRecordListener " + t.getMessage());
                return;
            }
        }
    }

    private void sendResult(String result) {
        Intent intent = new Intent("com.mmi.helper.response");
        intent.putExtra("type", mType);
        intent.putExtra("value", result);
        mContext.sendBroadcast(intent);
        LogUtil.d(TAG, "send result = " + result);
    }
}
