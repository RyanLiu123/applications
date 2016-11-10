/*
 *  file create by liunianliang for fcidownload
 */

package com.huaqin.fcidownload;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.huaqin.fcidownload.util.LogUtil;
import com.huaqin.fcidownload.util.PreferencesUtils;

import java.io.File;
import java.text.DecimalFormat;

public class FCIDownLoad {

    private static final String TAG = "FCIDownLoad";

    public static final String DOWNLOAD_FOLDER_NAME = "fci";
    public static final String DOWNLOAD_FILE_NAME = "UpdateFCI.zip";
    public static final String FCI_DOWNLOAD_ID = "fciDownloadId";
    public static final File INTERNAL_SD_FILE = Environment
            .getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
    public static final Uri CONTENT_URI = Uri.parse("content://downloads/my_downloads");
    public static final String mBaseUri = "http://fus.lenovomm.com/firmware/3.1/updateservlet";

    private Context context;
    private String url;
    private Handler handler;
    private String notificationTitle;
    private String notificationDescription;

    private DownloadManager downloadManager;
    private CompleteReceiver completeReceiver;

    private String FilePath;

    /**
     * @param context
     * @param url 下载FCI的url
     * @param notificationTitle 通知栏标题
     * @param notificationDescription 通知栏描述
     */
    public FCIDownLoad(Context context, String url, Handler handler, String notificationTitle,
            String notificationDescription) {
        super();
        this.context = context;
        this.url = url;
        this.handler = handler;
        this.notificationTitle = notificationTitle;
        this.notificationDescription = notificationDescription;

        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        completeReceiver = new CompleteReceiver();

        context.getContentResolver().registerContentObserver(CONTENT_URI, true,
                new DownloadChangeObserver(null));

        /** register download success broadcast **/
        context.registerReceiver(completeReceiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void execute() {

        // 清除已下载的内容重新下载
        long downloadId = PreferencesUtils.getLong(context, FCI_DOWNLOAD_ID);
        if (downloadId != -1) {
            downloadManager.remove(downloadId);
            PreferencesUtils.removeSharedPreferenceByKey(context, FCI_DOWNLOAD_ID);
        }

        Request request = new Request(Uri.parse(url));
        // 设置Notification中显示的文字
        request.setTitle(notificationTitle);
        request.setDescription(notificationDescription);
        // 设置可用的网络类型
        request.setAllowedNetworkTypes(Request.NETWORK_MOBILE | Request.NETWORK_WIFI);
        // 设置状态栏中显示Notification
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 不显示下载界面
        request.setVisibleInDownloadsUi(false);
        // 设置下载后文件存放的位置
        File folder = new File(INTERNAL_SD_FILE.getAbsolutePath());
        // Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER_NAME);
        FilePath = new StringBuilder(INTERNAL_SD_FILE.getAbsolutePath())
                .append(File.separator)
                .append(DOWNLOAD_FILE_NAME).toString();
        LogUtil.e(TAG, "FilePath = " + FilePath);
        if (FilePath.contains("storage/emulated/0/")) {
            FilePath = FilePath.replace("storage/emulated/0/", "data/media/0/");
        }
        File f = new File(FilePath);
        if (f.exists()) {
            f.delete();
        }
        LogUtil.e(TAG, "FilePath1 = " + FilePath);
        LogUtil.e(TAG, "folder = " + folder.getAbsolutePath());
        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER_NAME, DOWNLOAD_FILE_NAME);
        // 设置文件类型
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
                .getFileExtensionFromUrl(url));
        request.setMimeType(mimeString);
        // 保存返回唯一的downloadId
        PreferencesUtils.putLong(context, FCI_DOWNLOAD_ID, downloadManager.enqueue(request));
    }

    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver(Handler handler) {
            super(handler);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onChange(boolean selfChange) {
            queryDownloadStatus(downloadManager, PreferencesUtils.getLong(context, FCI_DOWNLOAD_ID));
        }

    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * get the id of download which have download success, if the id is my id and it's
             * status is successful, then install it
             **/
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            long downloadId = PreferencesUtils.getLong(context, FCI_DOWNLOAD_ID);

            if (completeDownloadId == downloadId) {

                // if download successful
                if (queryDownloadStatus(downloadManager, downloadId) == DownloadManager.STATUS_SUCCESSFUL) {

                    // clear downloadId
                    PreferencesUtils.removeSharedPreferenceByKey(context, FCI_DOWNLOAD_ID);

                    // unregisterReceiver
                    context.unregisterReceiver(completeReceiver);
                }
            }
        }
    };

    /** 查询下载状态 */
    public int queryDownloadStatus(DownloadManager downloadManager, long downloadId) {
        int result = -1;
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = null;
        try {
            c = downloadManager.query(query);
            if (c != null && c.moveToFirst()) {
                result = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                int fileSizeIdx =
                        c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                int bytesDLIdx =
                        c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                int fileSize = c.getInt(fileSizeIdx);
                int bytesDL = c.getInt(bytesDLIdx);
                String filename = c.getString(c
                        .getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
                if (!TextUtils.isEmpty(filename)) {
                    FilePath = filename.replace("storage/emulated/0/", "data/media/0/");
                }
                switch (status) {
                    case DownloadManager.STATUS_RUNNING:
                    case DownloadManager.STATUS_SUCCESSFUL:
                        LogUtil.e(
                                TAG,
                                "download .... "
                                        + (new DecimalFormat("#")
                                                .format((double) ((float) (bytesDL) / (float) fileSize) * 100))
                                        + "%");
                        LogUtil.e(TAG, "download .... " + bytesDL + "/" + fileSize);
                        break;
                    case DownloadManager.STATUS_FAILED:
                        LogUtil.e(TAG, "download failed !!");
                        break;
                    default:
                        break;
                }
                sendMsg(status, bytesDL, fileSize);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return result;
    }

    public void cancelDownload() {
        long downloadId = PreferencesUtils.getLong(context, FCI_DOWNLOAD_ID);
        if (downloadId != -1) {
            downloadManager.remove(downloadId);
        }
    }

    private void sendMsg(int status, int current, int total) {
        Bundle bundle = new Bundle();
        Message msg = new Message();
        String precent = (new DecimalFormat("#")
                .format((double) ((float) (current) / (float) total) * 100)) + "%";
        if (status == DownloadManager.STATUS_RUNNING) {
            msg.what = MainActivity.MSG_IS_DOWNLOADING;
        } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
            msg.what = MainActivity.MSG_FINISH_DOWNLOAD;
        } else if (status == DownloadManager.STATUS_FAILED) {
            // nothing
        }
        bundle.putInt("current", current);
        bundle.putInt("total", total);
        bundle.putString("precent", precent);
        bundle.putString("filepath", FilePath);
        LogUtil.e(TAG, "sendMsg --> precent = " + precent + "  FilePath=" + FilePath);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
}
