/**
 * 2016-4-11
 * CaseGps.java
 * TODO test gps
 * zhouhui
 */

package com.huaqin.autocycletest.test.gps;

import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author test gps
 */
public class CaseGps extends Case {
    private static String TAG = "CaseGps";
    private TestGps mTestGps = null;
    private Testerthread mTesterthread = null;

    /**
     * @param tag
     */
    public CaseGps(int categoryId) {
        super(TAG, AutoCycleTestUtils.GPS_TEST, categoryId);
        mTestGps = new TestGps(AutoCycleTestUtils.GPS_TEST, Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestGps.getTesterthread();
    }

    /*
     * 2016-4-11
     */
    @Override
    public int getTestTime() {
        return 1;
    }

    /*
     * 2016-4-11
     */
    @Override
    public int getTestCount() {
        if (AutoCycleTestConfig.DEBUG) {
            return 1 * getCycleCount();
        }
        return 1 * 30 * getCycleCount();
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
    }

    /*
     * 2016-4-11
     */
    @Override
    public String getDesp() {
        return "30s register and listen, 30s unregister";
    }
}
