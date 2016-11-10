/**
 * 2016-4-11
 * CaseALPSGsensor.java
 * TODO test alps and g sensor
 * liunianliang
 */

package com.huaqin.autocycletest.test.sensor;

import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseGyrMSensor extends Case {
    private static String TAG = "CaseGyrMSensor";
    private TestGyrMSensor mTestGyrMSensor = null;
    private Testerthread mTesterthread = null;
    private int mTimeOneRound;

    /**
     * @param tag
     */
    public CaseGyrMSensor(int categoryId) {
        super(TAG, AutoCycleTestUtils.GYRMAGSENSOR_TEST, categoryId);
        mTestGyrMSensor = new TestGyrMSensor(AutoCycleTestUtils.GYRMAGSENSOR_TEST,
                Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestGyrMSensor.getTesterthread();
    }

    /*
     * 2016-4-11
     */
    @Override
    public int getTestTime() {
        return 30 * 60;
    }

    /*
     * 2016-4-11
     */
    @Override
    public int getTestCount() {
        if (AutoCycleTestConfig.DEBUG) {
            return 1 * getCycleCount();
        }
        return getTestTime() / getOneRoundtime() * getCycleCount();
    }

    /*
     * 2016-4-11
     */
    @Override
    public int startTest() {
        mTesterthread.startTesterThread();
        return 0;
    }

    /*
     * 2016-4-11
     */
    @Override
    public int stopTest() {
        mTesterthread.stopTesterThreadFromMainUI();
        return 0;
    }

    /*
     * 2016-4-11
     */
    @Override
    public void setHandler(Handler handler) {
        mTesterthread.setoutHandler(handler);
        mTestGyrMSensor.setDelayTime(getOneRoundtime());
    }

    /*
     * 2016-4-11
     */
    @Override
    public String getDesp() {
        return getOneRoundtime() / 2 + "s register and listen," + getOneRoundtime() / 2
                + "s unregister";
    }

    public void setTestTimeOneRound(int oneRoundtime) {
        mTimeOneRound = oneRoundtime;
    }

    private int getOneRoundtime() {
        return 60;
    }
}
