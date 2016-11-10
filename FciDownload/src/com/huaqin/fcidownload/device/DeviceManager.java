/**
 * 2016-3-15
 * DeviceManager.java
 * TODO device info
 * zhouhui
 */

package com.huaqin.fcidownload.device;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

/**
 * @author liunianliang
 */
public class DeviceManager {

    private static String mAction = "querynewfirmware";
    private static String mCurfirmwarever = null;
    private static String mDeviceId = null;
    private static String mDevicemodel = null;
    private static String mLocale = null;
    private static String mOptionalparameter = null;

    public static void setAction(String action) {
        mAction = action;
    }

    public static void setDeviceId(String id) {
        mDeviceId = id;
    }

    public static String getAction() {
        return mAction;
    }

    public static String getCurfirmwarever() {
        return SystemProperties.get("ro.lenovo_fci_version",
                SystemProperties.get("ro.build.version.incremental"));
    }

    public static String getDeviceId(Context context, int slotId) {
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager != null) {
            String imei = telephonyManager.getDeviceId(slotId);
            return !TextUtils.isEmpty(mDeviceId) ? mDeviceId : imei;
        }
        return mDeviceId;
    }

    public static String getDevicemodel() {
        return SystemProperties.get("ro.product.fci.model",
                SystemProperties.get("ro.product.ota.model", "UNKONW")).replaceAll(" ", "");
    }

    public static String getLocale(Context context) {
        String mLocale = context.getResources().getConfiguration().locale.getLanguage();
        return mLocale;
    }

    public static String getOptionalparameter() {
        return "%7B%22ro.lenovo.easyimage.code%22%3A%22US%22%7D";
    }

    public static String getDeviceInfo(Context context, int slotId) {
        return "action=" + getAction() + "&" + "curfirmwarever=" +
                getCurfirmwarever() + "&" + "deviceid=" + getDeviceId(context, slotId) + "&"
                + "devicemodel=" + getDevicemodel() +
                "&" + "locale=" + getLocale(context) + "&" + "optionalparameter="
                + getOptionalparameter();
    }
}
