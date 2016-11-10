/**
 * 2016-4-8
 * CaseBluetooth.java
 * TODO test bluetooth
 * liuninanliang
 */

package com.huaqin.autocycletest.test.bluetooth;

import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liuninanliang
 */
public class CaseBluetooth extends Case {

    private static String TAG = "CaseBluetooth";
    private TestBluetooth mTestBluetooth = null;
    private Testerthread mTesterthread = null;

    /**
     * @param tag
     */
    public CaseBluetooth(int categoryId) {
        super(TAG, AutoCycleTestUtils.BLUETOOTH_TEST, categoryId);
        mTestBluetooth = new TestBluetooth(AutoCycleTestUtils.BLUETOOTH_TEST,
                Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestBluetooth.getTesterthread();
    }

    /*
     * 2016-4-9
     */
    @Override
    public int getTestTime() {
        if (AutoCycleTestConfig.DEBUG) {
            return 1;
        }
        return 30;
    }

    /*
     * 2016-4-9
     */
    @Override
    public int getTestCount() {
        return getTestTime() * getCycleCount();
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
