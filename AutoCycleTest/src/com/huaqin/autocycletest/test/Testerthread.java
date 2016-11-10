
package com.huaqin.autocycletest.test;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.AutoCycleTestMassage;
import com.huaqin.autocycletest.util.LogUtils;

public class Testerthread {
    public static final int TESTTYPE_COUNT = 0;
    public static final int TESTTYPE_TIME = 1;

    private String mTAG = null;

    private int mtestType = TESTTYPE_COUNT;
    private int mcaseId = 0;
    private int mTestCount = 0;
    private int mCategoryId = -1;
    private int mTestTime = 0;
    private int mCountIndex = 0;
    protected Handler moutHandler = null;
    private TesterInterface mtesterInterface;

    private HandlerThread mworkingThread = null;
    private WorkingHandler mworkingHandler = null;

    public static final int MSG_BEGIN_TEST_THREAD = 500;
    public static final int MSG_STOP_TEST_THREAD = 501;
    public static final int MSG_STOP_TEST_THREAD_FROM_MAIN = 502;
    public static final int MSG_STOP_TEST_THREAD_FROM_NOTIFY = 503;

    public static final int MSG_ONEROUND_FINISHED = 510;
    public static final int MSG_ONEROUND_FAILED = 511;
    public static final int MSG_ONEROUND_ABORTED = 512;
    public static final int MSG_TEST_TIME_OVER = 513;
    public static final int MSG_TEST_FINISHED = 514;
    public static final int MSG_TEST_INTERVAL_TIMEUP = 520;

    public interface TesterInterface {
        void onstartTest();

        void onstopTest();

        int onsleepbeforestart();
    };

    private class WorkingHandler extends Handler {
        WorkingHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_BEGIN_TEST_THREAD:
                    if (mtestType == TESTTYPE_COUNT) {
                        if (mCountIndex < mTestCount) {
                            mtesterInterface.onstartTest();
                        }
                    } else if (mtestType == TESTTYPE_TIME) {
                        mtesterInterface.onstartTest();
                        mworkingHandler.sendEmptyMessageDelayed(MSG_TEST_TIME_OVER,
                                mTestTime * 1000);
                    }
                    break;
                case MSG_STOP_TEST_THREAD:
                    LogUtils.i(mTAG, "workinghandler:stop test from:" + mTAG);
                    mworkingThread.getLooper().quit();
                    if (moutHandler != null) {
                        Message msg_abort = moutHandler
                                .obtainMessage(AutoCycleTestMassage.MSG_TESTCASE_ABORTED);
                        Bundle abort_data = new Bundle();
                        abort_data.putInt("caseid", mcaseId);
                        abort_data.putInt("categoryId", mCategoryId);
                        msg_abort.setData(abort_data);
                        moutHandler.sendMessage(msg_abort);
                    }
                    break;
                case MSG_STOP_TEST_THREAD_FROM_NOTIFY:
                    LogUtils.i(mTAG, "workinghandler:stop test from notifity:" + mTAG);
                    if (mtestType == TESTTYPE_TIME) {
                        mworkingHandler.removeMessages(MSG_TEST_TIME_OVER);
                    }
                    mworkingThread.getLooper().quit(); // stop the working thread.
                    break;
                case MSG_STOP_TEST_THREAD_FROM_MAIN:
                    LogUtils.i(mTAG, "workinghandler:stop test from main ui");
                    mtesterInterface.onstopTest();
                    mworkingThread.getLooper().quit();
                    break;
                case MSG_TEST_TIME_OVER:
                    LogUtils.i(mTAG, "workinghandler: test time over");
                    if (mworkingThread.isAlive()) {
                        mtesterInterface.onstopTest();
                    }
                    if (moutHandler != null) {
                        Message msg_finish = moutHandler
                                .obtainMessage(AutoCycleTestMassage.MSG_TESTCASE_FINISHED);
                        Bundle finish_data = new Bundle();
                        finish_data.putInt("caseid", mcaseId);
                        finish_data.putInt("categoryId", mCategoryId);
                        msg_finish.setData(finish_data);
                        moutHandler.sendMessage(msg_finish);
                    }
                    mworkingThread.getLooper().quit();
                    break;
                case MSG_TEST_FINISHED:
                    LogUtils.i(mTAG, "workinghandler: test is finished");
                    if (moutHandler != null) {
                        Message msg_finish = moutHandler
                                .obtainMessage(AutoCycleTestMassage.MSG_TESTCASE_FINISHED);
                        Bundle finish_data = new Bundle();
                        finish_data.putInt("caseid", mcaseId);
                        finish_data.putInt("categoryId", mCategoryId);
                        msg_finish.setData(finish_data);
                        moutHandler.sendMessage(msg_finish);
                    }
                    mworkingThread.getLooper().quit();
                    break;
                case MSG_ONEROUND_FINISHED:
                    if (mtestType == TESTTYPE_COUNT) {
                        mCountIndex++;
                        LogUtils.i(mTAG, "ONEROUND finshed:mTestCount:" + mTestCount
                                + ", mCountIndex:" + mCountIndex);

                        if (mCountIndex < mTestCount) {

                            mtesterInterface.onstartTest();
                        }
                        else {
                            mtesterInterface.onstopTest();

                            if (moutHandler != null) {
                                Message msg_finish = moutHandler
                                        .obtainMessage(AutoCycleTestMassage.MSG_TESTCASE_FINISHED);
                                Bundle finish_data = new Bundle();
                                finish_data.putInt("caseid", mcaseId);
                                finish_data.putInt("categoryId", mCategoryId);
                                msg_finish.setData(finish_data);

                                moutHandler.sendMessage(msg_finish);
                            }
                            mworkingThread.getLooper().quit();
                        }
                    }
                    break;
                case MSG_ONEROUND_FAILED:
                    if (mtestType == TESTTYPE_COUNT) {
                        LogUtils.i(mTAG, "ONEROUND failed:mTestCount:" + mTestCount
                                + ", mCountIndex:" + mCountIndex);

                        mtesterInterface.onstopTest();

                        if (moutHandler != null) {
                            Message msg_fail = moutHandler
                                    .obtainMessage(AutoCycleTestMassage.MSG_TESTCASE_FAILED);
                            Bundle fail_data = new Bundle();
                            fail_data.putInt("caseid", mcaseId);
                            fail_data.putInt("categoryId", mCategoryId);
                            fail_data.putString("fail_reason", msg.getData()
                                    .getString("error_code")
                                    + ",index/total:"
                                    + mCountIndex
                                    + "/"
                                    + mTestCount);

                            msg_fail.setData(fail_data);
                            moutHandler.sendMessage(msg_fail);
                        }
                        mworkingThread.getLooper().quit();

                    }
                    break;
                case MSG_ONEROUND_ABORTED:
                    break;
                case MSG_TEST_INTERVAL_TIMEUP:
                    if (mCountIndex < mTestCount) {
                        LogUtils.i(mTAG, "MSG_TEST_INTERVAL_TIMEUP: begin new round test");

                        mtesterInterface.onstartTest();
                    }
                    break;
                default:
                    throw new IllegalStateException(mTAG + "unhandled message: " + msg.what);
            }
        }
    }

    public Testerthread(String tag, int caseid, int testtype, int value, int categoryId) {
        mTAG = tag;
        mtestType = testtype;
        mcaseId = caseid;
        mCategoryId = categoryId;

        if (testtype == TESTTYPE_COUNT) {
            mTestCount = value;
        }
        else {
            mTestTime = value;
        }

        // create working thread and handler.
        mworkingThread = new HandlerThread("TesterWorkingThread");
        mworkingThread.start();
        mworkingHandler = new WorkingHandler(mworkingThread.getLooper());
    }

    private void resetTestIndex() {
        mCountIndex = 0;
    }

    public int getTestIndex() {
        return mCountIndex;
    }
    
    public int getCategoryTestCount() {
        return AutoCycleTestConfig.getInstance().getCurrentCategoryTestCount(mCategoryId);
    }

    // call by specific tester
    public void setTesterInterface(TesterInterface testerInterface) {
        mtesterInterface = testerInterface;
    }

    public Handler getworkingHandler() {
        return mworkingHandler;
    }

    public void startTesterThread() {
        resetTestIndex();
        mworkingHandler.sendEmptyMessage(MSG_BEGIN_TEST_THREAD);
    }

    public void stopTesterThread() {
        mworkingHandler.sendEmptyMessage(MSG_STOP_TEST_THREAD);
    }

    public void stopTesterThreadFromNotify() {
        mworkingHandler.sendEmptyMessage(MSG_STOP_TEST_THREAD_FROM_NOTIFY);
    }

    public void stopTesterThreadFromMainUI() {
        mworkingHandler.sendEmptyMessage(MSG_STOP_TEST_THREAD_FROM_MAIN);
    }

    public void setoutHandler(Handler outHandler) {
        moutHandler = outHandler;
    }

    public Handler getoutHandler() {
        return moutHandler;
    }

}
