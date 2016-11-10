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

package com.huaqin.mmitest.led;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

import com.huaqin.mmitest.MMITestService;
import com.huaqin.mmitest.util.LogUtil;
import com.huaqin.mmitest.util.ShellExe;

import android.os.SystemProperties;

/**
 * @author liunianliang
 * 
 */
public class Led {

	private static String TAG = "Led";
	private Context mContext;
	private String mType;
	private int mPercent;
	private int mCurrentlight;
	private String mCurrentType;

	private final String KEY_BOARD_LIGHT = "/sys/class/leds/keyboard-backlight/brightness";
	private final String LED_BREATH_LIGHT_RED = "/sys/class/leds/red/brightness";
	private final String LED_BREATH_LIGHT_GREEN = "/sys/class/leds/green/brightness";
	private final String LED_BREATH_LIGHT_BLUE = "/sys/class/leds/blue/brightness";
	private final String LED_BREATH_LIFHTS[] = { LED_BREATH_LIGHT_RED,
			LED_BREATH_LIGHT_GREEN, LED_BREATH_LIGHT_BLUE };

	private final int MAX_LIGHT = 255;

	public Led(MMITestService context) {
		mContext = context;
	}

	public void light(MMITestService context, String value, String type) {
		mType = type;
		mPercent = Integer.parseInt(value);
		mCurrentlight = MAX_LIGHT * (mPercent / 100);
		if (!mType.equals(context.MMI_LED_KEYBOARD)) {
			if (mCurrentlight > 0) {
				mCurrentlight = 1;
			} else {
				mCurrentlight = 0;
			}
		}
		handlerType(context);
	}

	private void handlerType(MMITestService cx) {

		LogUtil.e(TAG, " mType = " + mType);
		LogUtil.e(TAG, " mPercent = " + mPercent);
		LogUtil.e(TAG, " mCurrentlight = " + mCurrentlight);

		if (mType.equals(cx.MMI_LED_KEYBOARD)) {
			mCurrentType = KEY_BOARD_LIGHT;
			if (!isSupportKeyBoard()) {
				LogUtil.e(TAG, " don't support keyboard test !!!");
				SendResult("pass");
				return;
			}
		} else if (mType.equals(cx.MMI_LED_BREATH_RED)) {
			mCurrentType = LED_BREATH_LIGHT_RED;
		} else if (mType.equals(cx.MMI_LED_BREATH_GREEN)) {
			mCurrentType = LED_BREATH_LIGHT_GREEN;
		} else if (mType.equals(cx.MMI_LED_BREATH_BLUE)) {
			mCurrentType = LED_BREATH_LIGHT_BLUE;
		}
		if (!mType.equals(cx.MMI_LED_KEYBOARD) && !isSupportBreathLight()) {
			SendResult("pass");
			LogUtil.e(TAG, " don't support breathlight test !!!");
			return;
		}
		LogUtil.e(TAG, " mCurrentType = " + mCurrentType);

		try {
			if (!mCurrentType.equals(KEY_BOARD_LIGHT)) {
				if (mCurrentlight == 1) {
					for (String type : LED_BREATH_LIFHTS) {
						if (type.equals(mCurrentType)) {
							ShellExe.execCommand("echo " + 1 + " > "
									+ type);
							LogUtil.i(TAG, "echo " + 1 + " > " + type);
						} else {
							ShellExe.execCommand("echo " + 0 + " > "
									+ type);
							LogUtil.i(TAG, "echo " + 0 + " > " + type);
						}
					}
				} else {
					for (String type : LED_BREATH_LIFHTS) {
						ShellExe.execCommand("echo " + mCurrentlight + " > "
								+ type);
						LogUtil.i(TAG, "echo " + 0 + " > " + type);
					}
				}
			} else {
				ShellExe.execCommand("echo " + mCurrentlight + " > "
						+ mCurrentType);
				LogUtil.i(TAG, "echo " + mCurrentlight + " > " + mCurrentType);
			}
			SendResult("pass");
			LogUtil.e(TAG, "change light of " + mType + " pass !!!");
		} catch (IOException e) {
			handleLight();
			e.printStackTrace();
		}
	}

	private void handleLight() {
		FileOutputStream lightclose = null;
		try {
			lightclose = new FileOutputStream(mCurrentType);
			lightclose.write((byte) mCurrentlight);
			lightclose.close();
			SendResult("pass");
		} catch (Exception e) {
			LogUtil.d(TAG, "setBrightness close failed  " + e.toString());

			SendResult("fail");
		}
		if (lightclose != null) {
			try {
				lightclose.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void SendResult(String result) {
		Intent intent = new Intent("com.mmi.helper.response");
		intent.putExtra("type", mType);
		intent.putExtra("value", result);
		mContext.sendBroadcast(intent);
		LogUtil.i(TAG, " " + mType + " result is " + result);
	}

	private boolean isSupportKeyBoard() {
		return SystemProperties.getInt("ro.mtk_support_keyboard", 0) == 1;
	}

	private boolean isSupportBreathLight() {
		// return SystemProperties.getInt("ro.mtk_support_breathlight",0) == 1;
		return true;// al817
	}
}
