/**
 * 2016-4-9
 * CaseSd.java
 * TODO test SD copy
 * liunianliang
 */

package com.huaqin.autocycletest.test.sd;

import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.test.vibrator.TestVibrator;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseSd extends Case {
    private static String TAG = "CaseSd";
    private TestSd mTestSd = null;
    private Testerthread mTesterthread = null;

    /**
     * @param tag
     */
    public CaseSd(int categoryId) {
        super(TAG, AutoCycleTestUtils.SD_TEST, categoryId);
        mTestSd = new TestSd(AutoCycleTestUtils.SD_TEST, Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestSd.getTesterthread();
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
        return 1 * getCycleCount();
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
        StringBuilder sb = new StringBuilder();
        sb.append("1 times every 30 min\n");
        sb.append("Delete old *.surf1\",\"*.surf2\" file\n");
        sb.append("Copy \n");
        sb.append("\"/sdcard/alt_autocycle/alt_autocycle_video.3gp\" to\n");
        sb.append("\"/sdcard/alt_autocycle/alt_autocycle_video.3gp.surf1\" \"/sdcard/alt_autocycle/alt_autocycle_video.3gp.surf2\"\n");
        sb.append("\"/sdcard/alt_autocycle/alt_audio_350_to_450.wav\" to\n");
        sb.append("\"/sdcard/alt_autocycle/alt_audio_350_to_450.wav.surf1\" \"/sdcard/alt_autocycle/alt_audio_350_to_450.wav.surf2\"\n");
        sb.append("\"/sdcard/alt_autocycle/alt_picture.jpg\" to\n");
        sb.append("\"/sdcard/alt_autocycle/alt_picture.jpg.surf1\", \"/sdcard/alt_autocycle/alt_picture.jpg.surf2\"\n");
        sb.append("\"/sdcard/alt_autocycle/alt_tex_file.txt\" to\n");
        sb.append("\"/sdcard/alt_autocycle/alt_tex_file.txt.surf1\", \"/sdcard/alt_autocycle/alt_tex_file.txt.surf2\"\n");
        return sb.toString();
    }
}
