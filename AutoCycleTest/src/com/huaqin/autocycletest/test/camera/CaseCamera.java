/**
 * 2016-4-9
 * CaseCamera.java
 * TODO test camera
 * liunianliang
 */

package com.huaqin.autocycletest.test.camera;

import android.content.Intent;
import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.test.audio.TestAudio;
import com.huaqin.autocycletest.test.video.TestVideo;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseCamera extends Case {

    private static String TAG = "CaseCamera";
    private int mCategory;

    public CaseCamera(int categoryId) {
        super(TAG, AutoCycleTestUtils.CAMEAR_TEST, categoryId);
        mCategory = categoryId;
    }

    /*
     * 2016-4-9
     */
    @Override
    public int getTestTime() {
        if (AutoCycleTestConfig.DEBUG) {
            return 10;
        }
        return 30 * 60;
    }

    /*
     * 2016-4-9
     */
    @Override
    public int getTestCount() {
        return (getTestTime() / 10) * getCycleCount();
    }

    /*
     * 2016-4-9
     */
    @Override
    public int startTest() {
        Intent intent = new Intent(mContext, TestCamera.class);
        intent.putExtra("count", getTestCount());
        intent.putExtra("caseid", AutoCycleTestUtils.CAMEAR_TEST);
        intent.putExtra("category", mCategory);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return 0;
    }

    /*
     * 2016-4-9
     */
    @Override
    public int stopTest() {
        mContext.sendBroadcast(new Intent("com.huaqin.autoTest.cameratest.stop"));
        return 0;
    }

    /*
     * 2016-4-9
     */
    @Override
    public void setHandler(Handler handler) {
        TestCamera.setCommHandler(handler);
    }

    /*
     * 2016-4-9
     */
    @Override
    public String getDesp() {

        return null;
    }
}
