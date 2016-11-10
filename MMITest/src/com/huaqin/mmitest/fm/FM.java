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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioSystem;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;

import com.huaqin.mmitest.MMITestService;
import com.huaqin.mmitest.util.LogUtil;

import com.mediatek.fmradio.FmRadioNative;

/**
 * @author liunianliang
 * 
 */
public class FM {

	private static String TAG = "FM";
	private Context mContext;
	private float mValue;
	private String mType;

	private boolean mIsDeviceOpen = false;
	private boolean mIsPownUp = false;
	private int mValueHeadSetPlug = 1;
	private boolean mIsSpeakerUsed = true;
	private boolean mIsEarUsed = true;
	private static final int HEADSET_PLUG_IN = 1;
	private AudioManager mAudioManager = null;
	private MediaPlayer mFMPlayer = null;
	private List<Float> mListFrequency = new ArrayList<Float>();
	public static final int DEFAULT_STATION = 1000;
	private FMBroadcastReceiver mBroadcastReceiver = null;
	private boolean mIsAudioFocusHeld = false;
	public static final int CONVERT_RATE = 10;
	private int mForcedUseForMedia;
	private static final int FOR_PROPRIETARY = 5;

	private class FMBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.d(TAG, ">>> TestFM.onReceive");
			String action = intent.getAction();
			if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
				mValueHeadSetPlug = (intent.getIntExtra("state", -1) == HEADSET_PLUG_IN) ? 0
						: 1;
				if (!mIsPownUp && mValueHeadSetPlug == 1) {
					LogUtil.w(TAG,
							"ACTION_HEADSET_PLUG: FM not powered up yet!!");
					return;
				}
				int state = intent.getIntExtra("state", -1);

				LogUtil.i(TAG, "state: " + state);
				LogUtil.i(TAG, "fmopen: " + mIsDeviceOpen);

				if (state == 1) {
					if (!mIsPownUp) {
						pownUp();
						if (mValue > 0) {
							new FMTuneThread(TAG).start();
						}

					}
				} else {
					if (mIsPownUp) {
						enableFMAudio(false);
						pownDown(0);
					}
				}
			}
		}
	}

	public FM(MMITestService context) {
		mAudioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
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
		if (!isHeadsetOn()) {
			LogUtil.d(TAG, ">>> !isHeadsetOn()");
		}
		mIsPownUp = false;
		setAudio();
		registerBroadcast();
	}

	private boolean openDevice() {
		LogUtil.d(TAG, ">>> TestFM.openDevice");
		if (!mIsDeviceOpen) {
			mIsDeviceOpen = FmRadioNative.openDev();
		}
		LogUtil.d(TAG, "<<< TestFM.openDevice: " + mIsDeviceOpen);
		return mIsDeviceOpen;
	}

	private void setAudio() {

		mFMPlayer = new MediaPlayer();
		mFMPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
		mFMPlayer.setOnErrorListener(mPlayerErrorListener);
		mFMPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		try {
			mFMPlayer.setDataSource("THIRDPARTY://MEDIAPLAYER_PLAYERTYPE_FM");
		} catch (IOException ex) {
			LogUtil.e(TAG, "setDataSource: " + ex);
			return;
		} catch (IllegalArgumentException ex) {
			LogUtil.e(TAG, "setDataSource: " + ex);
			return;
		} catch (SecurityException ex) {
			LogUtil.e(TAG, "setDataSource: " + ex);
			return;
		} catch (IllegalStateException ex) {
			LogUtil.e(TAG, "setDataSource: " + ex);
			return;
		}
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
				AudioManager.FLAG_PLAY_SOUND);

	}

	private void registerBroadcast() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		mBroadcastReceiver = new FMBroadcastReceiver();
		LogUtil.i(TAG, "Register broadcast receiver.");
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}

	private void pownUp() {
		if (!requestAudioFocus()) {
			LogUtil.e(TAG, "FM can't get audio focus when power up");
			return;
		}
		boolean bRet = false;
		bRet = FmRadioNative.powerUp(mValue);
		if (!bRet) {
			LogUtil.e(TAG, "FM powerup fail");
			mIsPownUp = false;
		} else {
			mIsPownUp = true;
			setMute(true);
		}
	}

	private boolean requestAudioFocus() {
		if (mIsAudioFocusHeld) {
			return true;
		}

		int audioFocus = mAudioManager.requestAudioFocus(null,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		mIsAudioFocusHeld = (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioFocus);
		return mIsAudioFocusHeld;
	}

	public void abandonAudioFocus() {
		mAudioManager.abandonAudioFocus(null);
		mIsAudioFocusHeld = false;
	}

	private int setMute(boolean mute) {
		if (!mIsPownUp) {
			LogUtil.w(TAG, "FM is not powered up");
			return -1;
		}

		LogUtil.d(TAG, ">>> TestFM.setMute: " + mute);
		int iRet = FmRadioNative.setMute(mute);
		LogUtil.d(TAG, "<<< TestFM.setMute: " + iRet);
		return iRet;
	}

	class FMTuneThread extends Thread {
		private long oldTime = System.currentTimeMillis();
		private int iTime = 1;
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

	private void tuneStation(float frequency) {
		if (!mIsPownUp) {
			pownUp();
		}
		initDevice(frequency);
	}

	private void initDevice(float frequency) {

		if (mIsEarUsed != isEarPhoneOn()) {
			setEarPhoneOn(mIsEarUsed);
		}
		enableFMAudio(true);
		if (FmRadioNative.tune(mValue)) {
			sendResult("pass");
		} else {
			sendResult("fail");
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {

		}
		setMute(false);
	}

	private void enableFMAudio(boolean enable) {
		LogUtil.d(TAG, ">>> TestFM.enableFMAudio: " + enable);
		if ((mFMPlayer == null) || !mIsPownUp) {
			LogUtil.w(TAG, "mFMPlayer is null in Service.enableFMAudio");
			return;
		}

		if (!enable) {
			if (!mFMPlayer.isPlaying()) {
				LogUtil.d(TAG, "warning: FM audio is already disabled.");
				return;
			}
			LogUtil.d(TAG, "stop FM audio.");
			mFMPlayer.stop();
			return;
		}

		if (enable) {
			if (mFMPlayer.isPlaying()) {
				LogUtil.d(TAG, "warning: FM audio is already enabled.");
				mFMPlayer.stop();
			}
		}

		try {
			mFMPlayer.prepare();
			mFMPlayer.start();
		} catch (IOException e) {
			LogUtil.e(TAG, "Exception: Cannot call MediaPlayer prepare.", e);
		} catch (IllegalStateException e) {
			LogUtil.e(TAG, "Exception: Cannot call MediaPlayer prepare.", e);
		}

		LogUtil.d(TAG, "Start FM audio.");
		LogUtil.d(TAG, "<<< FMRadioService.enableFMAudio");
	}

	public void setEarPhoneOn(boolean isEar) {
		LogUtil.d(TAG, ">>> TestFM.isEar: " + isEar);
		mForcedUseForMedia = isEar ? AudioSystem.FORCE_HEADPHONES
				: AudioSystem.FORCE_NONE;
		AudioSystem.setForceUse(FOR_PROPRIETARY, mForcedUseForMedia);
		mIsSpeakerUsed = isEar;
		LogUtil.d(TAG, "<<< TestFM.isEar");

		LogUtil.e(TAG, "mAudioManager.setEarphoneOn: ");

	}

	private boolean isEarPhoneOn() {
		return (mForcedUseForMedia == AudioSystem.FORCE_HEADPHONES);
	}

	private final MediaPlayer.OnErrorListener mPlayerErrorListener = new MediaPlayer.OnErrorListener() {

		public boolean onError(MediaPlayer mp, int what, int extra) {

			if (MediaPlayer.MEDIA_ERROR_SERVER_DIED == what) {
				LogUtil.d(TAG, "onError: MEDIA_SERVER_DIED");
				if (null != mFMPlayer) {
					mFMPlayer.release();
					mFMPlayer = null;
				}
				mFMPlayer = new MediaPlayer();
				mFMPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
				mFMPlayer.setOnErrorListener(mPlayerErrorListener);
				try {
					mFMPlayer
							.setDataSource("MEDIATEK://MEDIAPLAYER_PLAYERTYPE_FM");
					mFMPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					if (mIsPownUp) {
						mFMPlayer.prepare();
						mFMPlayer.start();
					}
				} catch (IOException ex) {
					LogUtil.e(TAG, "setDataSource: " + ex);
					return false;
				} catch (IllegalArgumentException ex) {
					LogUtil.e(TAG, "setDataSource: " + ex);
					return false;
				} catch (IllegalStateException ex) {
					LogUtil.e(TAG, "setDataSource: " + ex);
					return false;
				}
			}

			return true;
		}
	};

	private boolean isHeadsetOn() {
		return mAudioManager.isWiredHeadsetOn();
	}

	public void stopTest() {
		if (null != mBroadcastReceiver) {
			LogUtil.i(TAG, "Unregister broadcast receiver.");
			mContext.unregisterReceiver(mBroadcastReceiver);
			mBroadcastReceiver = null;
		}
		if (mIsPownUp) {
			enableFMAudio(false);
			pownDown(0);
		}
		closeDevice();
		if (null != mFMPlayer) {
			mFMPlayer.stop();
			mFMPlayer.release();
			mFMPlayer = null;
		}
		abandonAudioFocus();
		LogUtil.i(TAG, "stop FM Test .");
		sendResult("pass");
	}

	private void pownDown(int type) {
		setMute(true);
		FmRadioNative.powerDown(0);
		mIsPownUp = false;
	}

	private boolean closeDevice() {
		LogUtil.d(TAG, ">>> TestFM.closeDevice");
		boolean isDeviceClose = false;
		if (mIsDeviceOpen) {
			isDeviceClose = FmRadioNative.closeDev();
			mIsDeviceOpen = !isDeviceClose;
		}
		LogUtil.d(TAG, "<<< TestFM.closeDevice: " + isDeviceClose);

		return isDeviceClose;
	}

	private void sendResult(String result) {
		Intent intent = new Intent("com.mmi.helper.response");
		intent.putExtra("type", mType);
		intent.putExtra("value", result);
		mContext.sendBroadcast(intent);
		LogUtil.d(TAG, "send result = " + result);
	}
}
