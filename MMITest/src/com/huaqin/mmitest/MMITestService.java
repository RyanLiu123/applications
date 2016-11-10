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

package com.huaqin.mmitest;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.huaqin.mmitest.fm.FM;
import com.huaqin.mmitest.fm.FM_M;
import com.huaqin.mmitest.led.Led;
import com.huaqin.mmitest.mic.Mic;
import com.huaqin.mmitest.sensor.Sensor;
import com.huaqin.mmitest.tp.TP;
import com.huaqin.mmitest.util.LogUtil;
import com.huaqin.mmitest.wirteflag.WriteFlag;

public class MMITestService extends Service {

	private String TAG = "MMITestService";

	public static String ACTION_MMI_TEST_REQUEST = "com.mmi.helper.request";
	public static String ACTION_MMI_TEST_REQUEST_FM = "com.mmi.helper.requestFM";
	public static String ACTION_MMI_TEST_RESPONSE = "com.mmi.helper.response";

	public static String MMI_WRITE_FLAG = "write_mmi_flag";

	public static String MMI_LED_KEYBOARD = "led_keyboard";
	public static String MMI_LED_BREATH = "led_breath";
	public static String MMI_LED_BREATH_RED = MMI_LED_BREATH + "_red";
	public static String MMI_LED_BREATH_GREEN = MMI_LED_BREATH + "_green";
	public static String MMI_LED_BREATH_BLUE = MMI_LED_BREATH + "_blue";

	public static String MMI_FM_PLAY = "fm_play";
	public static String MMI_FM_STOP = "fm_stop";

	public static String MMI_SENSOR_CALIBRATION = "calibration";
	public static String MMI_SENSOR_CALIBRATION_PS = MMI_SENSOR_CALIBRATION
			+ "_ps";
	public static String MMI_SENSOR_CALIBRATION_GS = MMI_SENSOR_CALIBRATION
			+ "_gs";
	public static String MMI_SENSOR_CALIBRATION_GRY = MMI_SENSOR_CALIBRATION
			+ "_gyr";

	public static String MMI_MIC = "mic";
	public static String MMI_TP_TEST = "tp_test";

	private final static int MMI_TEST_SEND_RESULT = 0;

	private FM_M mFM = null;
	private Led mLed = null;
	private Mic mMic = null;
	private Sensor mSensor = null;
	private TP mTp = null;
	private WriteFlag mWriteFlag = null;

	/*
	 *  Maybe use in Feature.
	 */
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Bundle bundle = (Bundle) msg.getData();
			String type = bundle.getString("type");
			String result = bundle.getString("result");
			switch (msg.what) {
			case MMI_TEST_SEND_RESULT:
				sendResult(type, result);
				break;
			}
		}
	};

	/*
	 * 2015-10-28
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/*
	 * 2015-10-28
	 */
	@Override
	public void onCreate() {
		LogUtil.e(TAG, "onCreate");
		mFM = new FM_M(this);
		mLed = new Led(this);
		mMic = new Mic(this);
		mSensor = new Sensor(this);
		mTp = new TP(this);
		mWriteFlag = new WriteFlag();
		super.onCreate();
	}

	/*
	 * 2015-10-28
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogUtil.e(TAG, "onStartCommand");
		super.onStartCommand(intent, flags, startId);
		String action = null;
		String type = null;
		String value = null;
		if (intent != null) {
			action = intent.getAction();
			type = intent.getStringExtra("type");
			value = intent.getStringExtra("value");
		}
		if (action == null || type == null) {
			LogUtil.w(TAG, "onStartCommand action is null");
			return START_NOT_STICKY;
		}
		if (ACTION_MMI_TEST_REQUEST.equals(action)) {
			handCommand(type, value, this);
		} else if (ACTION_MMI_TEST_REQUEST_FM.equals(action)) {
			mFM.play(this, value, type);
		}
		return START_STICKY;
	}

	private void handCommand(String type, String value, MMITestService i) {

		LogUtil.e(TAG, "type = " + type + " value = " + value);

		if (MMI_WRITE_FLAG.equals(type)) {
			mWriteFlag.write(i, value);
		} else if (type.contains("led")) {
			mLed.light(i, value, type);
		} else if (MMI_FM_PLAY.equals(type) || MMI_FM_STOP.equals(type)) {
			mFM.play(i, value, type);
		} else if (type.contains("calibration")) {
			mSensor.calibration(i, type);
		} else if (MMI_MIC.equals(type)) {
			mMic.open(i, value);
		} else if (MMI_TP_TEST.equals(type)) {
			mTp.test(i, value);
		}

	}

	private void sendResult(String type, String result) {
		LogUtil.i(TAG, "sendReslut mType = " + type + " result = " + result);
		Intent intent = new Intent("com.mmi.helper.response");
		if (type != null) {
			intent.putExtra("type", type);
		}
		if (result != null) {
			intent.putExtra("value", result);
		}
		sendBroadcast(intent);
	}
}
