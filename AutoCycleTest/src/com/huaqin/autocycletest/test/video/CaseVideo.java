/**
 * 2016-4-8
 * CaseVideo.java
 * TODO test video
 * liunianliang
 */

package com.huaqin.autocycletest.test.video;

import android.content.Intent;
import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseVideo extends Case {

    private static String TAG = "CaseVideo";
    private int mCategory;

    public CaseVideo(int category) {
        super(TAG,AutoCycleTestUtils.VIDEO_TEST,category);
        mCategory = category;
    }

    /*
     * 2016-4-8
     */
    @Override
    public int getTestTime() {
        if (AutoCycleTestConfig.DEBUG) {
            return 5;
        }
        return 30*60*60;
    }

    /*
     * 2016-4-8
     */
    @Override
    public int getTestCount() {
        return 1;
    }

    /*
     * 2016-4-8
     */
    @Override
    public int startTest() {
        Intent intent = new Intent(mContext,TestVideo.class);
        intent.putExtra("count", getTestCount());
        intent.putExtra("caseid", AutoCycleTestUtils.VIDEO_TEST);
        intent.putExtra("category", mCategory);
        intent.putExtra("cylceCount", getCycleCount());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        return 0;
    }

    /*
     * 2016-4-8
     */
    @Override
    public int stopTest() {
        mContext.sendBroadcast(new Intent("com.huaqin.autoTest.videotest.stop"));
        return 0;
    }

    /*
     * 2016-4-8
     */
    @Override
    public void setHandler(Handler handler) {
        TestVideo.setCommHandler(handler);
    }

    /*
     * 2016-4-8
     */
    @Override
    public String getDesp() {

        return "play alt_autocycle_video.mp4 for 30 min";
    }

}
