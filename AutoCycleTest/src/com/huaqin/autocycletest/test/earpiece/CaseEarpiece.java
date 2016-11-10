/**
 * 2016-4-11
 * CaseEarpiece.java
 * TODO test earpiece
 * liunianliang
 */

package com.huaqin.autocycletest.test.earpiece;

import android.os.Handler;

import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseEarpiece extends Case {
    private static String TAG = "CaseEarpiece";
    private TestEarpiece mTestEarpiece = null;
    private Testerthread mTesterthread = null;

    /**
     * @param tag
     */
    public CaseEarpiece(int categoryId) {
        super(TAG, AutoCycleTestUtils.EARPIECE_TEST, categoryId);
        mTestEarpiece = new TestEarpiece(AutoCycleTestUtils.EARPIECE_TEST, Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestEarpiece.getTesterthread();
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
        return 1;
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
        return "play alt_audio_350_to_450.wav by CaseEarpiece";
    }
}
