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

package com.huaqin.mmitest.wirteflag;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import com.huaqin.common.featureoption.FeatureOption;
import com.huaqin.mmitest.util.LogUtil;

/**
 * @author liunianliang
 * 
 */
public class WriteFlag {

	private static String TAG = "WriteFlag";
	private String MMI_TEST_FLAG = "persist.sys.mmitest.flag";
	private Context mContext;
	private Phone[] mPhone = new Phone[2];
	private boolean MTK_GEMINI_SUPPORT = SystemProperties.get(
			"ro.mtk_gemini_support").equals("1");
	private static final int EVENT_WRITE_FTFLAG_MMI = 1000;
	private static final int EVENT_WRITE_FTFLAG_SUCCESS_MMI = EVENT_WRITE_FTFLAG_MMI + 1;
	private static final int EVENT_WRITE_FTFLAG_FAIL_MMI = EVENT_WRITE_FTFLAG_MMI + 2;

	private Handler mResponseHander = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AsyncResult ar;
			switch (msg.what) {
			case EVENT_WRITE_FTFLAG_MMI:
				ar = (AsyncResult) msg.obj;
				if (ar.exception == null) {
					mResponseHander.sendEmptyMessageDelayed(
							EVENT_WRITE_FTFLAG_SUCCESS_MMI, 4000);
				} else {
					mResponseHander.sendEmptyMessageDelayed(
							EVENT_WRITE_FTFLAG_FAIL_MMI, 4000);
				}
				break;
			case EVENT_WRITE_FTFLAG_SUCCESS_MMI:
				LogUtil.d(TAG, "Write FTFLAG_MMI successfully");
				sendReslut("pass");
				break;
			case EVENT_WRITE_FTFLAG_FAIL_MMI:
				LogUtil.d(TAG, "Write FTFLAG_MMI fail");
				sendReslut("fail");
				break;
			default:
				break;
			}
		}
	};

	public void write(Context context, String value) {
		mContext = context;
		if ("pass".equals(value) || "fail".equals(value)) {
			setFTBarcode();
		}
	}

	private void getDefaultPhone() {
		if (MTK_GEMINI_SUPPORT) {
			LogUtil.v(TAG, "MTK_GEMINI_SUPPORT getPhoneCount="
					+ TelephonyManager.getDefault().getPhoneCount());
			if (TelephonyManager.getDefault().getPhoneCount() > 1) {
				mPhone[PhoneConstants.SIM_ID_1] = PhoneFactory
						.getPhone(PhoneConstants.SIM_ID_1);
				mPhone[PhoneConstants.SIM_ID_2] = PhoneFactory
						.getPhone(PhoneConstants.SIM_ID_2);
			}
		} else {
			LogUtil.v(TAG, "mPhoneProxey");
			mPhone[0] = (Phone) PhoneFactory.getDefaultPhone();
		}
	}

	private String getSerialString() {
		String serialno = SystemProperties.get("gsm.serial");
		return serialno;
	}

	private void setFTBarcode() {
		getDefaultPhone();
		String imeiString[] = { "AT+EGMR=1,", "+EGMR" };
		String serialno = getSerialString();
		LogUtil.i(TAG, "setFTBarcode getSerialString() = " + serialno);
		if (serialno.length() < 55) {
			// do something
			sendReslut("fail");
		} else {
			char[] barcode = serialno.toCharArray();

			if (barcode[55] == '1') {
				sendReslut("pass");
			} else {
				barcode[55] = '1';
				serialno = new String(barcode);
				LogUtil.i(TAG, "getSerialString() = " + serialno);
				imeiString[0] = "AT+EGMR=1,5,\"" + serialno + "\"" + "\r\n";
				mPhone[0].invokeOemRilRequestStrings(imeiString,
						mResponseHander.obtainMessage(EVENT_WRITE_FTFLAG_MMI));
			}
		}
	}

	private void sendReslut(String reslut) {
		LogUtil.i(TAG, "sendReslut value= " + reslut);
		Intent intent = new Intent("com.mmi.helper.response");
		intent.putExtra("type", "write_mmi_flag");
		intent.putExtra("value", reslut);
		mContext.sendBroadcast(intent);
	}
}
