/*
 *  file create by liunianliang for fcidownload
 */

package com.huaqin.fcidownload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.fcidownload.device.DeviceManager;
import com.huaqin.fcidownload.http.HttpHelper;
import com.huaqin.fcidownload.util.LogUtil;
import com.huaqin.fcidownload.util.XmlUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends Activity implements View.OnClickListener {

    private Context context;

    public static final int MSG_START_DOWNLOAD = 0;
    public static final int MSG_IS_DOWNLOADING = 1;
    public static final int MSG_FINISH_DOWNLOAD = 2;
    public static final int MSG_FAILED_DOWNLOAD = 3;

    public static String DOWNLOAD_URL = "";
    private static final String TAG = "MainActivity";

    private Button button;
    private TextView progress_text;
    private LinearLayout mEditCodeLayout;
    private LinearLayout mWarningLayout;
    private EditText mCodeEdit;
    private ProgressBar mProgressBar;
    private FCIDownLoad mFCIDownLoad;
    private ProgressDialog dialog;
    private HashMap<Integer, String> mUrlMap = new HashMap<Integer, String>();

    private static String mRealUri = "";
    private String mErrorMsg = "";
    private HttpHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        mUrlMap = XmlUtil.init(context).getUrlMap();
        getUri();
        (mHelper = new HttpHelper(context, mRealUri)).execute("");
        initView();
    }

    private void getUri() {
        if (!TextUtils.isEmpty(DeviceManager.getDeviceId(context, 0))) {
            mRealUri = FCIDownLoad.mBaseUri + "?" + DeviceManager.getDeviceInfo(context, 0);
        } else {
            mRealUri = FCIDownLoad.mBaseUri + "?" + DeviceManager.getDeviceInfo(context, 1);
        }
        LogUtil.e(TAG, "getUri mRealUri = " + mRealUri);
    }

    @Override
    protected void onDestroy() {
        if (mFCIDownLoad != null) {
            mFCIDownLoad.cancelDownload();
        }
        mRealUri = null;
        DOWNLOAD_URL = null;
        super.onDestroy();
    }

    private void initView() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        progress_text = (TextView) findViewById(R.id.progressBar_text);
        mCodeEdit = (EditText) findViewById(R.id.edit_edit);
        mCodeEdit.setHint(R.string.code_hint);
        (mEditCodeLayout = (LinearLayout) findViewById(R.id.edit_layout))
                .setVisibility(View.VISIBLE);
        (mWarningLayout = (LinearLayout) findViewById(R.id.warning_layout))
                .setVisibility(View.GONE);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    private String updateMessage() {

        StringBuilder sb = new StringBuilder();

        sb.append("1. Connect to server and download fci pkg；");
        sb.append("\n");
        sb.append("2. Check file name；");
        sb.append("\n");
        sb.append("3. Reboot to recovery mode and install fci pkg；");
        sb.append("\n");
        sb.append("4. After install finish,reboot to normal mode；");
        sb.append("\n");

        return sb.toString();
    }

    private String precent = "0";
    private String filepath = "";
    private int total = 100;
    private int current = 0;
    private Handler mHandler = new Handler() {
        /*
         * 2015-12-25
         */
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = (Bundle) msg.getData();
            precent = bundle.getString("precent", "0");
            filepath = bundle.getString("filepath", "");
            total = bundle.getInt("total");
            if (precent.equals("-0%")) {
                precent = "0%";
            }
            current = bundle.getInt("current");
            mProgressBar.setMax(total);
            LogUtil.e(TAG, "msg.what = " + msg.what);

            switch (msg.what) {
                case MSG_START_DOWNLOAD:
                    break;
                case MSG_IS_DOWNLOADING:
                    button.setEnabled(false);
                    break;
                case MSG_FINISH_DOWNLOAD:
                    button.setText(R.string.install);
                    button.setTextColor(Color.RED);
                    button.setEnabled(true);
                    break;
                case MSG_FAILED_DOWNLOAD:
                    break;
                default:
                    break;
            }
            mProgressBar.setProgress(current);
            progress_text.setText(precent);
            super.handleMessage(msg);
        }
    };

    /**
     * install pkg
     * 
     * @param context
     * @param filePath
     * @return whether pkg exist
     */
    public static void install(Context context, String filePath) {
        try {
            LogUtil.e(TAG, "install filePath = " + filePath);
            RecoverySystem.installPackage(context, new File(filePath));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * show the dialog of download info.
     */
    private void showDownloadDialog() {
        Dialog dialog = new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle("FCI Download")
                .setMessage(updateMessage())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        Toast.makeText(context, R.string.start_download, Toast.LENGTH_SHORT).show();
                        (mFCIDownLoad = new FCIDownLoad(getApplicationContext(), DOWNLOAD_URL,
                                mHandler,
                                "FCI",
                                Build.MANUFACTURER + " Fast Custom Image")).execute();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub
                        arg0.cancel();
                    }
                })
                .create();
        dialog.show();
    }

    private void showInstallProgressDialog() {
        if (dialog == null) {
            dialog = new ProgressDialog(context);
            dialog.setTitle(Build.BRAND + "FCI");
            dialog.setMessage("Please wait...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
        }
        dialog.show();
        new TimeCount(4000, 1000).start();
    }

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
            install(context, filepath);
        }

        @Override
        public void onTick(long arg0) {
        }
    }

    /*
     * check whether the code is right or wrong.
     */
    private boolean checkStatus() {
        // check code
        String edit = mCodeEdit.getText().toString();
        if (TextUtils.isEmpty(edit) || !edit.equals("59741")) {
            mErrorMsg = getString(R.string.code_wrong);
            return false;
        }

        // checkout network connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isAvailable()) {
            mErrorMsg = getString(R.string.code_wrong);
            return false;
        }

        int code = Integer.valueOf(edit);
        if (mUrlMap.get(code) != null && (DOWNLOAD_URL = mUrlMap.get(code).toString()) != null) {
            LogUtil.i(TAG, "DOWNLOAD_URL0 = " + DOWNLOAD_URL);
            return true;
        }

        // check uri
        if (TextUtils.isEmpty(DOWNLOAD_URL)) {
            DOWNLOAD_URL = mHelper.getDownloadUri();
            LogUtil.i(TAG, "********* DOWNLOAD_URL = " + DOWNLOAD_URL);
            return true;
        }
        LogUtil.e(TAG, "Url is null，can't download !!!");
        return false;
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.button:
                if (current == total) {
                    showInstallProgressDialog();
                } else {
                    if (mEditCodeLayout.getVisibility() == View.VISIBLE) {
                        if (checkStatus()) {
                            mEditCodeLayout.setVisibility(View.GONE);
                            mWarningLayout.setVisibility(View.VISIBLE);
                            showDownloadDialog();
                        } else {
                            Toast.makeText(this, R.string.code_wrong, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showDownloadDialog();
                    }

                }
                break;
        }
    }
}
