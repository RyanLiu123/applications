/**
 * 2016-4-8
 * CaseLcd.java
 * TODO test lcd
 * liunianliang
 */

package com.huaqin.autocycletest.test.lcd;

import android.content.Intent;
import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseLcd extends Case {

    private static String TAG = "CaseVideo";
    private int mCategory;

    public CaseLcd(int category) {
        super(TAG, AutoCycleTestUtils.LCD_TEST, category);
        mCategory = category;
    }

    /*
     * 2016-4-8
     */
    @Override
    public int getTestTime() {
        if (AutoCycleTestConfig.DEBUG) {
            return 6 * getCycleCount();
        }
        return 30 * getCycleCount();
    }

    /*
     * 2016-4-8
     */
    @Override
    public int getTestCount() {
        return getTestTime() / 6;
    }

    /*
     * 2016-4-8
     */
    @Override
    public int startTest() {
        Intent intent = new Intent(mContext, TestLcd.class);
        intent.putExtra("count", getTestCount());
        intent.putExtra("caseid", AutoCycleTestUtils.LCD_TEST);
        intent.putExtra("category", mCategory);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return 0;
    }

    /*
     * 2016-4-8
     */
    @Override
    public int stopTest() {
        mContext.sendBroadcast(new Intent("com.huaqin.autoTest.lcdtest.stop"));
        return 0;
    }

    /*
     * 2016-4-8
     */
    @Override
    public void setHandler(Handler handler) {
        TestLcd.setCommHandler(handler);
    }

    /*
     * 2016-4-8
     */
    @Override
    public String getDesp() {

        return "2 min color pattern (0, 255, 255)(255, 0, 255)(128, 128, 128)(0, 128, 0)" +
                "(0, 255, 0)(128, 0, 0)(0, 0, 128)(128, 128, 0)(128, 0, 128)(255, 0, 0)" +
                "(192, 192, 192)(0, 128, 128)(255, 255, 255)(255, 255, 0)(0, 0, 255) " +
                "2 min set white background 2 min set display border";
    }

}
