/**
 * 2016-4-11
 * TestALPSGsensor.java
 * TODO test alps and gsensor
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
public class TestALPSGsensor implements TesterInterface {
    private static String TAG = "TestALPSGsensor";

    private Testerthread mTesterthread = null;
    private Context mContext = null;
    private int msgDelay;
    private SensorManager mSensorManager;

    private Sensor mLightSensor;
    private Sensor mPSensor;
    private Sensor mGSensor;

    private boolean mLightSensor_pass;
    private boolean mPSensor_pass;
    private boolean mGSensor_pass;

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

    public TestALPSGsensor(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
        mContext = AutoCycleTestConfig.getInstance().getContext();
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mPSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mGSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        if (mLightSensor == null) {
            mLightSensor_pass = true;
        }
        if (mPSensor == null) {
            mPSensor_pass = true;
        }
        if (mGSensor == null) {
            mGSensor_pass = true;
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

    private SensorEventListener mLightSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            mLightSensor_pass = true;
            LogUtils.i(TAG, "mLightSensorListener  onSensorChanged...");
        }
    };

    private SensorEventListener mPSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            mPSensor_pass = true;
            LogUtils.i(TAG, "mPSensorListener  onSensorChanged...");
        }
    };

    private SensorEventListener mGSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            mGSensor_pass = true;
            LogUtils.i(TAG, "mGSensorListener  onSensorChanged...");
        }
    };

    private void registerSensor() {
        LogUtils.i(TAG, "registerSensor for " + msgDelay / 2 + "s");
        mSensorManager.registerListener(mLightSensorListener, mLightSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mPSensorListener, mPSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mGSensorListener, mGSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensor() {
        LogUtils.i(TAG, "unregisterSensor for " + msgDelay / 2 + "s");
        mSensorManager.unregisterListener(mLightSensorListener);
        mSensorManager.unregisterListener(mPSensorListener);
        mSensorManager.unregisterListener(mGSensorListener);
    }

    private void check() {
        if (mLightSensor_pass && mPSensor_pass && mGSensor_pass) {
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
