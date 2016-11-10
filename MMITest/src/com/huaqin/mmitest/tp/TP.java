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

package com.huaqin.mmitest.tp;

import com.huaqin.mmitest.MMITestService;
import com.huaqin.mmitest.util.LogUtil;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;

/**
 * @author liunianliang
 *
 */
public class TP {

	private static String TAG = "TP";
	private Context mContext;

	public TP(MMITestService context) {
	    mContext = context;
	}

	public void test(MMITestService context,String value) {
		if(mContext == null) {
			mContext = context;
		}
		LogUtil.i(TAG, "Enter tp test ..");

		if(!isSupportTpAutoTest()) {

			 Intent i = new Intent(); i.setClass(mContext,
			 TpTestActivity.class); i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 mContext.startActivity(i);

			/*
			 * LogUtil.i(TAG, "Do not support Tp auto Test ..");
			 * sendResult("pass");
			 */
		}else {
			//do Tp test,here need TP vendor support.
		}
	}

	private boolean isSupportTpAutoTest() {
		return SystemProperties.getInt("ro.mtk_support_tpautotest",0) == 1;
	}

	private void sendResult(String result) {
		Intent intent = new Intent("com.mmi.helper.response");
		intent.putExtra("type", "tp_test");
		intent.putExtra("value", result);
		mContext.sendBroadcast(intent);
		LogUtil.d(TAG, "result = "+result);
	}
}
