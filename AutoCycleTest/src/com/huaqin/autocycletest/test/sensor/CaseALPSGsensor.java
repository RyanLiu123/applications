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
import com.huaqin.autocycletest.test.sd.TestSd;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseALPSGsensor extends Case {
    private static String TAG = "CaseALPSGsensor";
    private TestALPSGsensor mTestALPSGsensor = null;
    private Testerthread mTesterthread = null;
    private int mTimeOneRound;
    private int mCategoryId;

    /**
     * @param tag
     */
    public CaseALPSGsensor(int categoryId) {
        super(TAG, AutoCycleTestUtils.ALPSGSENSOR_TEST, categoryId);
        mCategoryId = categoryId;
        mTestALPSGsensor = new TestALPSGsensor(AutoCycleTestUtils.ALPSGSENSOR_TEST,
                Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestALPSGsensor.getTesterthread();
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
        mTestALPSGsensor.setDelayTime(getOneRoundtime());
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
        return mCategoryId == AutoCycleTestUtils.LIST_ITEM_2 ? 50 : 30;
    }
}
