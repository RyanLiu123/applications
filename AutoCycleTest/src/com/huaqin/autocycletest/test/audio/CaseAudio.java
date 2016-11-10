/**
 * 2016-4-8
 * CaseAudio.java
 * TODO test audio
 * liunianliang
 */

package com.huaqin.autocycletest.test.audio;

import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

/**
 * @author liunianliang
 */
public class CaseAudio extends Case {
    private static String TAG = "CaseAudio";
    private TestAudio mTestAudio = null;
    private Testerthread mTesterthread = null;

    public CaseAudio(int categoryId) {
        super(TAG, AutoCycleTestUtils.AUDIO_TEST, categoryId);
        mTestAudio = new TestAudio(AutoCycleTestUtils.AUDIO_TEST, Testerthread.TESTTYPE_COUNT,
                getTestCount(), categoryId);
        mTesterthread = mTestAudio.getTesterthread();
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
        mTesterthread.startTesterThread();
        return 0;
    }

    /*
     * 2016-4-8
     */
    @Override
    public int stopTest() {
        mTesterthread.stopTesterThreadFromMainUI();
        return 0;
    }

    /*
     * 2016-4-8
     */
    @Override
    public void setHandler(Handler handler) {
        mTesterthread.setoutHandler(handler);
    }

    /*
     * 2016-4-8
     */
    @Override
    public String getDesp() {
        return "play alt_audio_350_to_450.wav for 30min!";
    }

}
