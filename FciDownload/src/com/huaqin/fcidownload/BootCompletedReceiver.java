/**
 * 2015-12-26
 * BootCompletedReceiver.java
 * TODO clean Notification
 * zhouhui
 */
package com.huaqin.fcidownload;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huaqin.fcidownload.util.LogUtil;
import com.huaqin.fcidownload.util.PreferencesUtils;

/**
 * @author liunianliang
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
    
    private static String TAG = "BootCompletedReceiver";
    /* 
     * 2015-12-26
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            long downloadId = PreferencesUtils.getLong(context, FCIDownLoad.FCI_DOWNLOAD_ID);
            if (downloadId != -1) {
                LogUtil.i(TAG, "clean download id (means Notification)");
                ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).remove(downloadId);
            }
        }
    }
    
}
