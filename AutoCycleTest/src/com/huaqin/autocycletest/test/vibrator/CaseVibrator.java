/**
 * 2016-4-9
 * CaseVibrator.java
 * TODO test vibrator
 * liunianliang
 */

package com.huaqin.autocycletest.test.vibrator;

import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseVibrator extends Case {
    private static String TAG = "CaseVibrator";
    private TestVibrator mTestVibrator = null;
    private Testerthread mTesterthread = null;
    private int mCategory;

    /**
     * @param tag
     */
    public CaseVibrator(int categoryId) {
        super(TAG, AutoCycleTestUtils.VIBRATOR_TEST, categoryId);
        mTestVibrator = new TestVibrator(AutoCycleTestUtils.VIBRATOR_TEST,
                Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestVibrator.getTesterthread();
        mCategory = categoryId;
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
            return 2 * getCycleCount();
        }
        return getTestTime() / 30 * getCycleCount();
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
        return "15s ON, 15s OFF";
    }
}
