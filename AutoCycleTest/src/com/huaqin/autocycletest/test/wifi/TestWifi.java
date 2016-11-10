/**
 * 2016-4-8
 * TestWifi.java
 * TODO test TestWifi
 * liunianliang
 */

package com.huaqin.autocycletest.test.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liunianliang
 */
public class TestWifi implements Testerthread.TesterInterface {
    public static String TAG = "TestWifi";

    private Testerthread mTesterthread = null;
    private Context mContext = null;
    private WiFiBroadcastReceiver mReceiver = null;

    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private List<ScanResult> wifiScanResult;
    private WifiManager mWifiManager;
    private WifiLock mWifiLock;
    private static final int TIMEOUT_MSEC = 60 * 1000;

    private static final int MSG_ENABLE_WIFI_ONEMIN = 1;
    private static final int MSG_DISABLE_WIFI_ONEMIN = 2;
    private static final int MSG_INIT_WIFI_TEST = 3;
    private static final int MSG_CHECK_WIFI_TESTRESULT = 4;

    private boolean isEnabled = false;
    private int mCount = 0;

    private Handler mInnerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_WIFI_ONEMIN:
                    openWifiAndScanFor60s();
                    break;
                case MSG_DISABLE_WIFI_ONEMIN:
                    getScanResult();
                    closedWifiFor60s();
                    break;
                case MSG_INIT_WIFI_TEST:
                    initWifiTest();
                    sendEmptyMessageDelayed(MSG_ENABLE_WIFI_ONEMIN, 1000);
                    break;
                case MSG_CHECK_WIFI_TESTRESULT:
                    if (mDeviceList.size() > 0) {
                        notifyTestResult(1, null);
                    } else {
                        notifyTestResult(0, "failed do scan !");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public TestWifi(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
        mContext = AutoCycleTestConfig.getInstance().getContext();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        // default close wifi
        mWifiManager.setWifiEnabled(false);
    }

    public Testerthread getTesterthread() {
        return mTesterthread;
    }

    @Override
    public void onstartTest() {
        LogUtils.d(TAG, "WIFI onstartTest!");
        registerBroadcast();
        mWifiLock = mWifiManager.createWifiLock(
                WifiManager.WIFI_MODE_SCAN_ONLY, "WiFi");
        if (false == mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }
        mInnerHandler.sendEmptyMessage(MSG_INIT_WIFI_TEST);
    }

    @Override
    public void onstopTest() {
        LogUtils.d(TAG, "  onstopTest!");
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        initWifiTest();
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    private void notifyTestResult(int value, String extra) {
        Handler workingHandler = mTesterthread.getworkingHandler();
        Message msg = (value == 1) ? workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FINISHED) : workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FAILED);
        Bundle bundle = new Bundle();
        bundle.putInt("result", value); // 0, failed.1,success.

        if (value != 1) {
            bundle.putString("error_code", "error_code:" + extra);
        }
        msg.setData(bundle);
        workingHandler.sendMessage(msg); // send message to workingthread.
    }

    private void registerBroadcast() {
        mReceiver = new WiFiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(mReceiver, filter);
    }

    private class WiFiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            LogUtils.i(TAG, "recevie action:" + intent.getAction() + "  "
                    + mWifiManager.getWifiState());
            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent
                    .getAction())) {
                getScanResult();
            }
        }
    };

    private void closedWifiFor60s() {
        LogUtils.i(TAG, " closedWifiFor60s... ");
        mWifiManager.setWifiEnabled(false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        mInnerHandler.removeMessages(MSG_CHECK_WIFI_TESTRESULT);
        mInnerHandler.sendEmptyMessageDelayed(MSG_CHECK_WIFI_TESTRESULT, TIMEOUT_MSEC - 1000);
    }

    private void openWifiAndScanFor60s() {
        LogUtils.i(TAG, " openWifiAndScanFor60s... ");
        mWifiManager.setWifiEnabled(true);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        mWifiManager.startScan();
        mInnerHandler.removeMessages(MSG_DISABLE_WIFI_ONEMIN);
        mInnerHandler.sendEmptyMessageDelayed(MSG_DISABLE_WIFI_ONEMIN, TIMEOUT_MSEC - 2000);
    }

    private void initWifiTest() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        mInnerHandler.removeMessages(MSG_DISABLE_WIFI_ONEMIN);
        mInnerHandler.removeMessages(MSG_ENABLE_WIFI_ONEMIN);
    }

    private void getScanResult() {
        wifiScanResult = mWifiManager.getScanResults();
        String s = "";
        LogUtils.i(TAG, "wifiScanResult.size() = " + wifiScanResult.size());
        if (wifiScanResult != null && wifiScanResult.size() > 0) {
            for (int i = 0; i < wifiScanResult.size(); i++) {
                s = "AP";
                s += " " + i + ": " + wifiScanResult.get(i).SSID;
                mDeviceList.add(s);
                LogUtils.i(TAG, "s = " + s);
            }
        }
    }
}
