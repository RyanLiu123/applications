/**
 * 2016-4-8
 * CaseWifi.java
 * TODO test WIFI
 * liuninanliang
 */

package com.huaqin.autocycletest.test.wifi;

import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liuninanliang
 */
public class CaseWifi extends Case {

    private static String TAG = "CaseBluetooth";
    private TestWifi mTestWifi = null;
    private Testerthread mTesterthread = null;

    /**
     * @param tag
     */
    public CaseWifi(int categoryId) {
        super(TAG, AutoCycleTestUtils.WIFI_TEST, categoryId);
        mTestWifi = new TestWifi(AutoCycleTestUtils.WIFI_TEST, Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestWifi.getTesterthread();
    }

    /*
     * 2016-4-9
     */
    @Override
    public int getTestTime() {
        if (AutoCycleTestConfig.DEBUG) {
            return 2;
        }
        return 30;
    }

    /*
     * 2016-4-9
     */
    @Override
    public int getTestCount() {
        return (getTestTime() / 2) * getCycleCount();
    }

    /*
     * 2016-4-9
     */
    @Override
    public int startTest() {
        mTesterthread.startTesterThread();
        return 0;
    }

    /*
     * 2016-4-9
     */
    @Override
    public int stopTest() {
        mTesterthread.stopTesterThreadFromMainUI();
        return 0;
    }

    /*
     * 2016-4-9
     */
    @Override
    public void setHandler(Handler handler) {
        mTesterthread.setoutHandler(handler);
    }

    /*
     * 2016-4-9
     */
    @Override
    public String getDesp() {
        return "Enable and discovery 1 min, Disable 1 min";
    }
}
