/**
 * 2015-10-28
 * LogUtil.java
 * TODO
 * liunianliang
 */
package com.huaqin.fcidownload.util;

import android.util.Log;

/**
 * @author liunianliang
 *
 */
public class LogUtil {

	private static boolean DEBUG = true;
	private static String TAG = "FCIDownLoad";
	
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
