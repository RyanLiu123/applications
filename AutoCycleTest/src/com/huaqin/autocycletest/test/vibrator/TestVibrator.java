/**
 * 2016-4-9
 * TestVibrator.java
 * TODO TestVibrator
 * liunianliang
 */

package com.huaqin.autocycletest.test.vibrator;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.LogUtils;

/**
 * @author liunianliang
 */
public class TestVibrator implements Testerthread.TesterInterface {
    public static String TAG = "TestVibrator";

    private Testerthread mTesterthread = null;
    private Context mContext = null;
    private Vibrator mVibrator;
    private static final long VIBRATION_OPEN_TIME = 15 * 1000;
    private static final long VIBRATION_CLOSE_TIME = 15 * 1000;

    private static final int MSG_VIBRATION_START_VIBRATION = 0;
    private static final int MSG_VIBRATION_START_CLIENT = 1;
    private static final int MSG_VIBRATION_ONEROUND = 2;

    private Handler mVibratorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VIBRATION_START_VIBRATION:
                    try {
                        if (mVibrator != null) {
                            LogUtils.d(TAG, "hasVibrator, start vibration for 15s.");
                            mVibrator.vibrate(VIBRATION_OPEN_TIME);
                            removeMessages(MSG_VIBRATION_START_CLIENT);
                            mVibratorHandler.sendEmptyMessageDelayed(MSG_VIBRATION_START_CLIENT,
                                    VIBRATION_OPEN_TIME);
                        } else {
                            notifyTestResult(0, "error: no vibrator?");
                        }
                    } catch (Exception localException) {
                        LogUtils.v(TAG, "vibrator play start Exception!" + localException);
                        notifyTestResult(0, "error: vibrator play start Exception!");
                        localException.printStackTrace();
                    }
                    break;
                case MSG_VIBRATION_START_CLIENT:
                    if (mVibrator != null) {
                        LogUtils.d(TAG, "hasVibrator, start client for 15s.");
                        mVibrator.cancel();
                        removeMessages(MSG_VIBRATION_ONEROUND);
                        mVibratorHandler.sendEmptyMessageDelayed(MSG_VIBRATION_ONEROUND,
                                VIBRATION_CLOSE_TIME);
                    } else {
                        notifyTestResult(0, "error: no vibrator?");
                    }
                    break;
                case MSG_VIBRATION_ONEROUND:
                    removeMessages(MSG_VIBRATION_ONEROUND);
                    notifyTestResult(1, "Vibrator one time");
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public TestVibrator(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
        mContext = AutoCycleTestConfig.getInstance().getContext();
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstartTest() {
        mVibratorHandler.removeMessages(MSG_VIBRATION_START_VIBRATION);
        mVibratorHandler.sendEmptyMessage(MSG_VIBRATION_START_VIBRATION);
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstopTest() {
        mVibratorHandler.removeMessages(MSG_VIBRATION_START_VIBRATION);
        mVibratorHandler.removeMessages(MSG_VIBRATION_START_CLIENT);
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    /*
     * 2016-4-11
     */
    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    /**
     * @return Testerthread
     */
    public Testerthread getTesterthread() {
        return mTesterthread;
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

}
