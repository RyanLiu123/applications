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

package com.huaqin.mmitest.util;

import android.util.Log;

/**
 * @author liunianliang
 *
 */
public class LogUtil {

	private static boolean DEBUG = true;
	private static String TAG = "HqMMITest";
	
    public static void v(String tag, String msg) {
    	if(isLoggable()) {
    		Log.v(TAG,tag + " --> "+msg);
    	}
    }

    public static void d(String tag, String msg) {
    	if(isLoggable()) {
    		Log.d(TAG,tag + " --> "+msg);
    	}
    }

    public static void i(String tag, String msg) {
    	if(isLoggable()) {
    		Log.i(TAG,tag + " --> "+msg);
    	}
    }

    public static void w(String tag, String msg) {
    	if(isLoggable()) {
    		Log.w(TAG,tag + " --> "+msg);
    	}
    }

    public static void e(String tag, String msg) {
    	if(isLoggable()) {
    		Log.e(TAG,tag + " --> "+msg);
    	}
    }

    public static void e(String tag, String msg,Exception e) {
        if(isLoggable()) {
            Log.e(TAG,tag + ":"+msg, e);
        }
    }

    public static boolean isLoggable() {
    	return DEBUG;
    }
}
