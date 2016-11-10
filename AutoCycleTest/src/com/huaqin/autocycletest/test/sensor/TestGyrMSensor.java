/**
 * 2016-4-11
 * TestGyrMSensor.java
 * TODO test gyr and mSensor
 * liunianliang
 */

package com.huaqin.autocycletest.test.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.test.Testerthread.TesterInterface;
import com.huaqin.autocycletest.util.LogUtils;

/**
 * @author liunianliang
 */
public class TestGyrMSensor implements TesterInterface {
    private static String TAG = "TestGyrMSensor";

    private Testerthread mTesterthread = null;
    private Context mContext = null;
    private int msgDelay;
    private SensorManager mSensorManager;

    private Sensor mGyrSensor;
    private Sensor mMsensor;

    private boolean mGyr_pass;
    private boolean mMsensor_pass;

    private final int MSG_RIGSTER_ALLSENSOR = 1;
    private final int MSG_UNRIGSTER_ALLSENSOR = 2;
    private final int MSG_CHECK_SENSORDATA = 3;

    private Handler mInnerHandler = new Handler() {
        /*
         * 2016-4-11
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_RIGSTER_ALLSENSOR:
                    registerSensor();
                    removeMessages(MSG_UNRIGSTER_ALLSENSOR);
                    sendEmptyMessageDelayed(MSG_UNRIGSTER_ALLSENSOR, msgDelay / 2 * 1000);
                    break;
                case MSG_UNRIGSTER_ALLSENSOR:
                    unregisterSensor();
                    removeMessages(MSG_CHECK_SENSORDATA);
                    sendEmptyMessageDelayed(MSG_CHECK_SENSORDATA, msgDelay / 2 * 1000);
                    break;
                case MSG_CHECK_SENSORDATA:
                    check();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public TestGyrMSensor(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
        mContext = AutoCycleTestConfig.getInstance().getContext();
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        mGyrSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mMsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        
        if (mGyrSensor == null) {
            mGyr_pass = true;
        }
        if (mMsensor == null) {
            mMsensor_pass = true;
        }
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstartTest() {
        mInnerHandler.sendEmptyMessage(MSG_RIGSTER_ALLSENSOR);
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstopTest() {
        unregisterSensor();
    }

    /*
     * 2016-4-11
     */
    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    /**
     * @return
     */
    public Testerthread getTesterthread() {
        return mTesterthread;
    }

    /**
     * @param oneRoundtime
     */
    public void setDelayTime(int oneRoundtime) {
        msgDelay = oneRoundtime;
    }

    private SensorEventListener mGyrSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            mGyr_pass = true;
            LogUtils.i(TAG, "mGyrSensorListener  onSensorChanged...");
        }
    };

    private SensorEventListener mMSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            mMsensor_pass = true;
            LogUtils.i(TAG, "mMSensorListener  onSensorChanged...");
        }
    };

    private void registerSensor() {
        LogUtils.i(TAG, "registerSensor for " + msgDelay / 2 + "s");
        mSensorManager.registerListener(mGyrSensorListener, mGyrSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mMSensorListener, mMsensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensor() {
        LogUtils.i(TAG, "unregisterSensor for " + msgDelay / 2 + "s");
        mSensorManager.unregisterListener(mGyrSensorListener);
        mSensorManager.unregisterListener(mMSensorListener);
    }

    private void check() {
        if (mGyr_pass && mMsensor_pass) {
            notifyTestResult(1, null);
        } else {
            notifyTestResult(0, "A sensor is not good !");
        }
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
