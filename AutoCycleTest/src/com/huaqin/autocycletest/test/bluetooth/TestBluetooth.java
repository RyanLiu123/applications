/**
 * 2016-4-8
 * TestBluetooth.java
 * TODO test TestBluetooth
 * liunianliang
 */

package com.huaqin.autocycletest.test.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.LogUtils;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author liunianliang
 */
public class TestBluetooth implements Testerthread.TesterInterface {
    public static String TAG = "TestBluetooth";

    private Testerthread mTesterthread = null;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<String> mDeviceList = new ArrayList<String>();
    private Set<BluetoothDevice> bondedDevices;
    private static final int TIMEOUT_MSEC = 60 * 1000;

    private static final int MSG_ENABLE_BLUETOOTH_ONEMIN = 1;
    private static final int MSG_DISABLE_BLUETOOTH_ONEMIN = 2;
    private static final int MSG_INIT_BLUETOOTH_TEST = 3;
    private static final int MSG_CHECK_BLUETOOTH_RESULT = 4;

    private Handler mInnerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENABLE_BLUETOOTH_ONEMIN:
                    openBtAndScanFor60s();
                    break;
                case MSG_DISABLE_BLUETOOTH_ONEMIN:
                    closedBtFor60s();
                    break;
                case MSG_INIT_BLUETOOTH_TEST:
                    sendEmptyMessage(MSG_ENABLE_BLUETOOTH_ONEMIN);
                    break;
                case MSG_CHECK_BLUETOOTH_RESULT:
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

    public TestBluetooth(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            LogUtils.d(TAG, "Obtain BluetoothAdapter OK");
        } else {
            LogUtils.e(TAG, "Can't Obtain BluetoothAdapter !!");
        }
        initTestStatus();
    }

    public Testerthread getTesterthread() {
        return mTesterthread;
    }

    @Override
    public void onstartTest() {
        LogUtils.d(TAG, "BT onstartTest!");
        registerBroadcast();
        // mDeviceList.clear();
        mInnerHandler.sendEmptyMessage(MSG_ENABLE_BLUETOOTH_ONEMIN);
    }

    @Override
    public void onstopTest() {
        LogUtils.d(TAG, "  onstopTest!");
        initTestStatus();
        AutoCycleTestConfig.getInstance().getContext().unregisterReceiver(mBTReceiver);
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
        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        AutoCycleTestConfig.getInstance().getContext().registerReceiver(mBTReceiver, filter);

    }

    private final BroadcastReceiver mBTReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                LogUtils.i(TAG, " find a device !");
                mDeviceList.add(device.getName());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogUtils.i(TAG, " discovery finished ");
                for (String name : mDeviceList) {
                    LogUtils.i(TAG, "name = " + name);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogUtils.i(TAG, " start to discovery... ");
                startScan();
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                 //do nothing
            }
        }
    };

    private void closedBtFor60s() {
        LogUtils.i(TAG, " closedBtFor60s...");
        mBluetoothAdapter.disable();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        mInnerHandler.removeMessages(MSG_CHECK_BLUETOOTH_RESULT);
        mInnerHandler.sendEmptyMessageDelayed(MSG_CHECK_BLUETOOTH_RESULT,
                TIMEOUT_MSEC - 1000);
    }

    private void openBtAndScanFor60s() {
        LogUtils.i(TAG, " openBtAndScanFor60s... ");
        mBluetoothAdapter.enable();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        mBluetoothAdapter.startDiscovery();
        mInnerHandler.removeMessages(MSG_DISABLE_BLUETOOTH_ONEMIN);
        mInnerHandler.sendEmptyMessageDelayed(MSG_DISABLE_BLUETOOTH_ONEMIN,
                TIMEOUT_MSEC - 2000);
    }

    private void initTestStatus() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
        mInnerHandler.removeMessages(MSG_DISABLE_BLUETOOTH_ONEMIN);
        mInnerHandler.removeMessages(MSG_ENABLE_BLUETOOTH_ONEMIN);
    }

    private void startScan() {
        mDeviceList.clear();
        bondedDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : bondedDevices) {
            String deviceName = device.getName();
            mDeviceList.add(deviceName);
        }
    }
}
