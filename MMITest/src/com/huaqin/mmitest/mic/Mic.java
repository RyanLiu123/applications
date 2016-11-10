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

package com.huaqin.mmitest.mic;

import java.io.File;
import java.io.IOException;

import com.huaqin.mmitest.MMITestService;
import com.huaqin.mmitest.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.AudioSystem;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;

import com.mediatek.storage.StorageManagerEx;

/**
 * @author liunianliang
 * 
 */
public class Mic {

	private static String TAG = "Mic";
	private Context mContext;
	private int MIC_ID = 0;

	private static final int MSG = 3000;
	static final long ANIMATION_INTERVAL = 70;

	private Handler mOutHandler = null;

	private boolean mIsDeviceOpen = false;
	// audio manager instance use to set volume stream type and use audio focus
	private AudioManager mAudioManager = null;
	private HeadsetBroadcastReceiver mBroadcastReceiver = null;

	private boolean mIsAudioFocusHeld = false;

	private int ear_state = 0;
	private int mic_state = 0;
	private char MIC_ON = 1;
	private char MIC_OFF = 0;

	private boolean isRecordering = false;
	private boolean isPlaying = false;

	private MediaPlayer mMediaPlayer = null;
	private MediaRecorder mMediaRecorder = null;
	private String mPath = null;
	private int mAmplitude = 0;
	private Object mlock = new Object();

	private Handler mHeadsetHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			LogUtil.d(TAG, "msg.what == " + msg.what + "  ear_state = "
					+ ear_state + "  isRecordering=" + isRecordering
					+ "  isPlaying = " + isPlaying);
			if (msg.what == MSG) {
				if (ear_state == 1) {
					if (isRecordering) {
						isRecordering = false;
						LogUtil.d(TAG,
								"-->isRecordering has been false");
						stopRecorder();
						LogUtil.d(TAG,
								"-->has stopped Recorder since plug in");
					}
					if (!isPlaying) {
						File mFile = new File(mPath);
						if (mFile != null && mFile.exists()) {
							startPlay();
							isPlaying = true;
							LogUtil.d(TAG, "startPlay finished "
									+ "isPlaying = " + isPlaying);
						}
					}
				} else {
					if (isPlaying) {
						stopPlay();
						isPlaying = false;
					}
					if (!isRecordering) {
						deleteRecorder();
						startRecorder();
						isRecordering = true;
						new RecordThread(TAG).start();
					}
				}
			}
			super.handleMessage(msg);
		}
	};

	private class HeadsetBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.d(TAG, ">>> TestMainMic.onReceive");
			String action = intent.getAction();
			if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
				ear_state = intent.getIntExtra("state", -1);
				mic_state = intent.getIntExtra("microphone", -1);
				LogUtil.i(TAG, "ear_state: " + ear_state);
				LogUtil.i(TAG, "mic_state: " + mic_state);
				Message msg = mHeadsetHandler.obtainMessage(MSG);
				mHeadsetHandler.sendMessage(msg);
				return;
			}
		}
	}

	public Mic(MMITestService context) {
		mContext = context;
		mPath = StorageManagerEx.getDefaultPath() + "/mmi_test.amr";
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	}

	public void open(Context context, String value) {
		MIC_ID = Integer.parseInt(value);
		if (MIC_ID != 0 && MIC_ID != 1 && MIC_ID != 2) {
			throw new RuntimeException("This Mic id {" + MIC_ID
					+ "} is not support !!!");
		}
		if(MIC_ID != 0) {
			startTest();
		}else {
			stopTest();
		}
	}

	public void startTest() {
		registerBroadcast();

		if (!isAntennaAvailable()) {
			deleteRecorder();
			isRecordering = true;
			startRecorder();
			new RecordThread(TAG).start();
		} else {
			ear_state = 0;
		}
	}

	public void stopTest() {
		// Unregister the broadcast receiver.
		if (null != mBroadcastReceiver) {
			LogUtil.i(TAG, "Unregister broadcast receiver.");
			mContext.unregisterReceiver(mBroadcastReceiver);
			mBroadcastReceiver = null;
		}

		isRecordering = false;
		new StopAudioUseThread(TAG).start();
	}

	private void registerBroadcast() {
		// Register broadcast receiver.
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_HEADSET_PLUG);
		mBroadcastReceiver = new HeadsetBroadcastReceiver();
		LogUtil.i(TAG, "Register broadcast receiver.");
		mContext.registerReceiver(mBroadcastReceiver, filter);
	}

	private class StopAudioUseThread extends Thread {

		public StopAudioUseThread(String name) {
			super(name);
		}

		public void run() {

			LogUtil.i(TAG, "StopAudioUseThread run.");
			stopRecorder();
			stopPlay();
			isRecordering = false;
			isPlaying = false;
			deleteRecorder();
		}
	}

	private boolean isAntennaAvailable() {
		return mAudioManager.isWiredHeadsetOn();
	}

	private void deleteRecorder() {
		File mFile = new File(mPath);
		if ((mFile != null) && (mFile.exists())) {
			mFile.delete();
		}
	}

	private void startRecorder() {
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		AudioSystem.setParameters("SET_MIC_CHOOSE=" + MIC_ID);
		
		Intent intent = new Intent("com.mmi.helper.response");
		intent.putExtra("type","mic");
		intent.putExtra("value","pass"); 
		mContext.sendBroadcast(intent);
		LogUtil.i(TAG, "Mic : "+MIC_ID+" open succuse !!!");
		
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mMediaRecorder.setOutputFile(mPath);
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (Exception localException) {
			LogUtil.i(TAG, "startRecording failed");
		}

		try {
			String command = "chmod 777 " + mPath;
			LogUtil.i("zyl", "command = " + command);
			Runtime runtime = Runtime.getRuntime();

			Process proc = runtime.exec(command);
		} catch (IOException e) {
			LogUtil.i("zyl", "chmod fail!!!!");
			e.printStackTrace();
		}
	}

	class RecordThread extends Thread {
		private long oldTime = System.currentTimeMillis();

		public RecordThread(String name) {
			super(name);
		}

		public void run() {
			try {

				LogUtil.i(TAG, "RecordThread run.");
				LogUtil.d(TAG, "songjiangchao-->isRecordering-->"
						+ isRecordering);
				synchronized (mlock) {

					while (isRecordering) {
						long curTime = System.currentTimeMillis();
						if (curTime > oldTime + ANIMATION_INTERVAL) {
							if (mMediaRecorder != null) {
								LogUtil.i(TAG, "synchronized mMediaRecorder ="
										+ mMediaRecorder);
								mAmplitude = mMediaRecorder.getMaxAmplitude();
								LogUtil.i(TAG,
										"mMediaRecorder.getMaxAmplitude() ="
												+ mAmplitude);
							}
							;
							oldTime = System.currentTimeMillis();
						}

					}

				}
			} catch (Throwable t) {
				LogUtil.d(TAG,
						"[mRecordListener: mRecordListener " + t.getMessage());
				return;
			}
		}
	}

	private void stopRecorder() {
		LogUtil.d(TAG, "enter stopRecorder() ");
		if (mMediaRecorder != null) {
			synchronized (mlock) {
				LogUtil.d(TAG, "prepare to release  mMediaRecorder");
				if (mMediaRecorder != null) {
					mMediaRecorder.release();
					mMediaRecorder = null;
				}
			}
			AudioSystem.setParameters("ForceUseSpecificMic=0");
		}
		LogUtil.d(TAG, "exit stopRecorder() ");
	}

	private void stopPlay() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		abandonAudioFocus();
	}

	private void startPlay() {
		mMediaPlayer = new MediaPlayer();
		try {
			mMediaPlayer.setDataSource(mPath);
		} catch (Exception localException1) {
			LogUtil.i(TAG, "startPlaying setDataSource error");
		}
		requestAudioFocus();
		try {
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setLooping(true);
		} catch (Exception localException2) {
			LogUtil.i(TAG, "startPlaying start() failed");
		}
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 11,
				AudioManager.FLAG_PLAY_SOUND);
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
}
