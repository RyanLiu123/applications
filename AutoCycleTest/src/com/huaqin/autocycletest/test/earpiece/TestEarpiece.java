/**
 * 2016-4-11
 * TestEarpiece.java
 * TODO TestEarpiece
 * liunianliang
 */

package com.huaqin.autocycletest.test.earpiece;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.R;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.test.Testerthread.TesterInterface;
import com.huaqin.autocycletest.util.LogUtils;

import java.io.IOException;

/**
 * @author liunianliang
 */
public class TestEarpiece implements TesterInterface {
    private static String TAG = "TestEarpiece";

    private Testerthread mTesterthread = null;
    private MediaPlayer mPlayer = null;
    private AssetFileDescriptor afd = null;
    private AudioManager mAm = null;
    private Context mContext = null;
    private int mDefaultMode;
    private HeadsetBroadcastReceiver mBroadcastReceiver = null;
    private int playStatusStartTest = -1;
    private int playStatusBR = -1;
    private int ear_state = 0;
    private int TEST_TIME = AutoCycleTestConfig.DEBUG ? 15 * 1000 : 30 * 60 * 1000;

    private final int MSG_TESTTIME_OVER = 0;
    private Handler mInnerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TESTTIME_OVER:
                    LogUtils.i(TAG, "audio test time over");
                    notifyTestResult(1, 0);
                    break;
                default:
                    break;
            }
        };
    };

    private class HeadsetBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>>>> TestReceiver.onReceive");
            String action = intent.getAction();
            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                ear_state = intent.getIntExtra("state", -1);
                LogUtils.i(TAG, "ear_state: " + ear_state);

                if (ear_state == 1) {
                    mPlayer.stop();
                    mPlayer.release();
                    mPlayer = null;
                    playStatusStartTest = 0;
                    LogUtils.d(TAG, "playStatusStartTest 5555--> 0");
                    playStatusBR = 0;
                } else {
                    LogUtils.d(TAG, "onReceive()-->playStatusStartTest--> " + playStatusStartTest);
                    if (playStatusStartTest != 1) {
                        mAm.setSpeakerphoneOn(false);
                        LogUtils.d(TAG, "onReceive()-->will setAudio() 1111");
                        setAudio();
                        LogUtils.i(TAG,
                                "onReceive()----------------------------------------> try to startPlay(2)");
                        int currentMode = mAm.getMode();
                        LogUtils.d(TAG, "onReceive() --> currentMode : " + currentMode);
                        playStatusBR = 1;
                        startPlay(2);
                    }
                }
            }
        }
    }

    public TestEarpiece(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
        mAm = (AudioManager) AutoCycleTestConfig.getInstance().getContext().getSystemService(
                Context.AUDIO_SERVICE);
        mContext = AutoCycleTestConfig.getInstance().getContext();
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstartTest() {
        mDefaultMode = mAm.getMode();
        LogUtils.d(TAG, "mDefaultMode -->" + mDefaultMode);
        setAudio();
        playStatusStartTest = 1;
        registerBroadcast();
        if (!isAntennaAvailable()) {
            mAm.setSpeakerphoneOn(false);
            LogUtils.i(TAG, "startTest() -----------------------------------> try to startPlay(1)");
            startPlay(1);
        }
        mInnerHandler.removeMessages(MSG_TESTTIME_OVER);
        mInnerHandler.sendEmptyMessageDelayed(MSG_TESTTIME_OVER,
                TEST_TIME * mTesterthread.getCategoryTestCount());
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstopTest() {
        if (null != mBroadcastReceiver) {
            LogUtils.i(TAG, "Unregister broadcast receiver.");
            mContext.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
        if (null != mPlayer) {
            playStatusStartTest = 0;
            LogUtils.d(TAG, "playStatusStartTest 4444--> 0");
            playStatusBR = 0;
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
        mAm.setMode(mDefaultMode);
        LogUtils.i(TAG, " stopTest");
    }

    /*
     * 2016-4-11
     */
    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    /**
     * @return Testerthread
     */
    public Testerthread getTesterthread() {
        return mTesterthread;
    }

    private boolean isAntennaAvailable() {
        return mAm.isWiredHeadsetOn();
    }

    private void setAudio() {
        mPlayer = MediaPlayer.create(mContext, R.raw.alt_audio_350_to_450);
        playStatusStartTest = 0;
        LogUtils.d(TAG, "playStatusStartTest 1111--> 0");
        playStatusBR = 0;
        mPlayer.stop();
        mAm.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mPlayer.setVolume(1.0F, 1.0F);
        mAm.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                mAm.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
    }

    private void registerBroadcast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        mBroadcastReceiver = new HeadsetBroadcastReceiver();
        LogUtils.i(TAG, "Register broadcast receiver.");
        mContext.registerReceiver(mBroadcastReceiver, filter);

    }

    private void startPlay(int source) {
        try {
            play(true, source);
            LogUtils.e(TAG, "startPlay()-->source : " + source);
        } catch (Exception e) {
            LogUtils.e(TAG, "Exception: Cannot call MediaPlayer prepare.", e);
        }
    }

    private void play(boolean enable, int source) {
        LogUtils.d(TAG, ">>> enableAudio: " + enable);
        if (!enable) {
            if (!mPlayer.isPlaying()) {
                LogUtils.d(TAG, "warning: audio is already disabled.");
                return;
            }
            LogUtils.d(TAG, "stop audio.");
            playStatusStartTest = 0;
            LogUtils.d(TAG, "playStatusStartTest 2222--> 0");
            playStatusBR = 0;
            mPlayer.stop();
            return;
        }

        if (mPlayer == null) {
            LogUtils.d(TAG, "mPlayer == null !");
            return;
        }
        LogUtils.d(TAG, "play() ----> playStatusStartTest :" + playStatusStartTest);
        LogUtils.d(TAG, "play() ----> playStatusBR :" + playStatusBR);
        LogUtils.d(TAG, "play() ----> source :" + source);
        LogUtils.d(TAG, "play() ----> isPlaying :" + mPlayer.isPlaying());
        if (source == 1) {
            if ((playStatusBR == 1)) {
                LogUtils.d(TAG, "play()----> playing now,caused by BroadcastReceiver plug out");
                return;
            }
        } else if (source == 2) {
            if ((playStatusStartTest == 1)) {
                LogUtils.d(TAG, "play()----> playing now,caused by start test");
                return;
            }
        }
        LogUtils.d(TAG, ">>>>>>>>>>>>>>>>>>>>>>> PLAYING NOW.......");
        try {
            mPlayer.setOnCompletionListener(mCompleteListner);
            mPlayer.setOnErrorListener(mErrorListner);
            mPlayer.setLooping(true);
            mPlayer.prepare();
            int currentMode = mAm.getMode();
            LogUtils.d(TAG, "play() --> currentMode 222 : " + currentMode);
            mPlayer.start();
        } catch (IOException e) {
            LogUtils.e(TAG, "Exception: Cannot call MediaPlayer prepare.", e);
        } catch (IllegalStateException e) {
            LogUtils.e(TAG, "Exception: Cannot call MediaPlayer prepare 222.", e);
        }

        LogUtils.d(TAG, "Start audio.");
    }

    private OnErrorListener mErrorListner = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtils.e(TAG, "Error occurred while playing audio:what:" + what);
            mp.stop();
            mp.release();
            notifyTestResult(0, what);
            return true;
        }
    };

    private OnCompletionListener mCompleteListner = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            LogUtils.i(TAG, "audio file play completed.");
            mp.stop();
            mp.release();
            notifyTestResult(1, 0); // success.
        }
    };

    private void notifyTestResult(int value, int extra) {
        Handler workingHandler = mTesterthread.getworkingHandler();
        Message msg = (value == 1) ? workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FINISHED) : workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FAILED);
        Bundle bundle = new Bundle();
        bundle.putInt("result", value); // 0, failed.1,success.

        if (value != 1) {
            bundle.putString("error_code", "error_code:" + extra);
        }
        msg.setData(bundle);
        workingHandler.sendMessage(msg); // send message to workingthread.
    }
}
