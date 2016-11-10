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

import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import com.huaqin.mmitest.util.LogUtil;

public class MMITestReceiver extends BroadcastReceiver {

	private String TAG = "MMITestReceiver";

	private String mPermissControlPkgName = "com.mediatek.security";

	/*
	 * 2015-10-28
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		LogUtil.e(TAG, "action = " + action);
		if ("com.mmi.helper.request".equals(action) || "com.mmi.helper.requestFM".equals(action)) {
			Intent it = new Intent(intent);
			it.setClass(context, MMITestService.class);
			context.startService(it);
			LogUtil.e(TAG, "start service : MMITestService ");
		} else if (Intent.ACTION_BOOT_COMPLETED.equals(action) && !get_cit()) {
			LogUtil.e(TAG, "MMI test mode,disablePermissControlApk");
			//disablePermissControlApk(context);
		}
	}

	private void disablePermissControlApk(Context context) {
		if (hasPkg(context, mPermissControlPkgName)) {
			context.getPackageManager().setApplicationEnabledSetting(
					mPermissControlPkgName,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
		}
	}

	private boolean get_cit() {
		String serialno = SystemProperties.get("gsm.serial");
		if (serialno.length() < 55) {
			return false;
		} else {
			char[] barcode = serialno.toCharArray();
			if (barcode[55] == '1') {
				return true;
			} else {
				return false;
			}
		}
	}

	private boolean hasPkg(Context context, String pkg) {
		boolean hasPkg = true;
		try {
			if (context.getPackageManager().getPackageInfo(pkg, 0) == null) {
				hasPkg = false;
			}
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			hasPkg = false;
		}
		LogUtil.e(TAG, "hasPkg: " + pkg + " is " + hasPkg);
		return hasPkg;
	}

}
