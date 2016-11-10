/**
 * 2016-4-9
 * TestSd.java
 * TODO testSd
 * liunianliang
 */

package com.huaqin.autocycletest.test.sd;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author liunianliang
 */
public class TestSd implements Testerthread.TesterInterface {
    private static String TAG = "TestSd";
    private static String[] mFilelist;
    private static String[] mDefaultSuffix;
    private static String mBaseDir = "/sdcard/alt_autocycle";

    private Testerthread mTesterthread = null;
    private int copyCount = 0;

    private final int MSG_STRAT_COPY = 0;
    private final int MSG_COPY_DONE = 1;
    private final int MSG_DELETE_TESTFILE = 2;

    private long ONEROUND_TIME = AutoCycleTestConfig.DEBUG ? 20 * 1000 : 30 * 60 * 1000;

    static {
        mFilelist = new String[] {
                "alt_autocycle_video.3gp", "alt_audio_350_to_450.wav", "alt_picture.jpg",
                "alt_tex_file.txt"
        };
        mDefaultSuffix = new String[] {
                ".surf1", ".surf2"
        };
    }

    private Handler mInnerHandler = new Handler() {
        /*
         * 2016-4-11
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STRAT_COPY:
                    copy();
                    break;
                case MSG_COPY_DONE:
                    done();
                    break;
                case MSG_DELETE_TESTFILE:
                    delete();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public TestSd(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstartTest() {
        mInnerHandler.sendEmptyMessage(MSG_STRAT_COPY);
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstopTest() {
        mInnerHandler.sendEmptyMessage(MSG_DELETE_TESTFILE);
    }

    /*
     * 2016-4-11
     */
    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    /**
     * @return Testerthread
     */
    public Testerthread getTesterthread() {
        return mTesterthread;
    }

    public static String[] getFileList() {
        return mFilelist;
    }

    public static String[] getSuffix() {
        return mDefaultSuffix;
    }

    private void notifyTestResult(int value, String extra) {
        Handler workingHandler = mTesterthread.getworkingHandler();
        Message msg = (value == 1) ? workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FINISHED) : workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FAILED);
        Bundle bundle = new Bundle();
        bundle.putInt("result", value); // 0, failed.1,success.
        if (value != 1) {
            bundle.putString("error_code", "error_code:" + extra);
        }
        msg.setData(bundle);
        workingHandler.sendMessage(msg); // send message to workingthread.
    }

    private void copy() {
        long startTime = System.currentTimeMillis();
        File srcDir = new File(mBaseDir);
        if (!srcDir.exists()) {
            notifyTestResult(0, "preCopy have been not run ?");
        }
        try {
            for (String file : mFilelist) {
                File sourceFile = new File(srcDir, file);
                LogUtils.i(TAG, "copy sourceFile = " + sourceFile.getAbsolutePath());
                if (sourceFile.exists()) {
                    for (String suffix : mDefaultSuffix) {
                        InputStream source = new FileInputStream(sourceFile);
                        File destfile = new File(srcDir, file + suffix);
                        LogUtils.i(TAG, "copy destfile = " + destfile.getAbsolutePath());
                        FileOutputStream fs = new FileOutputStream(destfile);
                        byte[] buffer = new byte[1024];
                        int nread;
                        while ((nread = source.read(buffer)) != -1) {
                            if (nread == 0) {
                                nread = source.read();
                                if (nread < 0)
                                    break;
                                fs.write(nread);
                                continue;
                            }
                            fs.write(buffer, 0, nread);
                        }
                        source.close();
                        fs.flush();
                        fs.close();
                        copyCount++;
                    }
                } else {
                    notifyTestResult(0, file + " not exists ?");
                }
            }
            long endTime = System.currentTimeMillis();
            long spendTime = endTime - startTime;
            mInnerHandler.removeMessages(MSG_COPY_DONE);
            mInnerHandler.sendEmptyMessageDelayed(MSG_COPY_DONE, ONEROUND_TIME - spendTime);
        } catch (Exception e) {
            copyCount--;
            notifyTestResult(0, "preCopy not run ?");
            LogUtils.e(TAG, "error happened when copy file. e =" + e.toString());
        }
        srcDir.setWritable(true);
    }

    private void done() {
        if (copyCount == mFilelist.length * mDefaultSuffix.length) {
            LogUtils.e(TAG, " Sd test successfully ! ");
            notifyTestResult(1, null);
        }
        copyCount = 0;
    }

    private void delete() {
        File srcDir = new File(mBaseDir);
        try {
            if (srcDir.exists() && srcDir.isDirectory()) {
                File[] files = srcDir.listFiles();
                for (File f : files) {
                    if (f.getName().endsWith(mDefaultSuffix[0])
                            || f.getName().endsWith(mDefaultSuffix[1])) {
                        if (f.delete()) {
                            LogUtils.e(TAG, f + " deleted !");
                        } else {
                            LogUtils.e(TAG, f + " delete failed !");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "delete file failed !!!  e = " + e.toString());
        }
    }
}
