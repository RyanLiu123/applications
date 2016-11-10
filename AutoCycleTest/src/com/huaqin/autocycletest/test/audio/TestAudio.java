/**
 * 2016-4-8
 * TestAudio.java
 * TODO test audio
 * liunianliang
 */

package com.huaqin.autocycletest.test.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.R;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.LogUtils;

/**
 * @author liunianliang
 */
public class TestAudio implements Testerthread.TesterInterface {
    public static String TAG = "TestAudio";

    private Testerthread mTesterthread = null;
    private MediaPlayer mPlayer = null;
    private AssetFileDescriptor afd = null;
    private AudioManager mAm = null;
    private int TEST_TIME = AutoCycleTestConfig.DEBUG ? 15 * 1000 : 30 * 60 * 1000;

    private final int MSG_TESTTIME_OVER = 0;

    private Handler mInnerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TESTTIME_OVER:
                    LogUtils.i(TAG, "audio test time over");
                    notifyTestResult(1, 0);
                    break;
                default:
                    break;
            }
        };
    };

    public TestAudio(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
        mAm = (AudioManager) AutoCycleTestConfig.getInstance().getContext().getSystemService(
                Context.AUDIO_SERVICE);
    }

    public Testerthread getTesterthread() {
        return mTesterthread;
    }

    @Override
    public void onstartTest() {
        try {
            mPlayer = new MediaPlayer();
            if (mPlayer != null) {
                afd = AutoCycleTestConfig.getInstance().getContext().getResources()
                        .openRawResourceFd(R.raw.alt_audio_350_to_450);
                if (afd == null)
                    return;
                mPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                        afd.getLength());
                afd.close();
                afd = null;
                mPlayer.prepare();
                mPlayer.setVolume(1.0f, 1.0f);
                mAm.setStreamVolume(AudioManager.STREAM_MUSIC,
                        mAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                        AudioManager.FLAG_PLAY_SOUND);

                mPlayer.setOnErrorListener(mErrorListner);
                mPlayer.setOnCompletionListener(mCompleteListner);
                mPlayer.setLooping(true);
                mPlayer.start();
                LogUtils.v(TAG, "MediaPlayer play started!");
                mInnerHandler.removeMessages(MSG_TESTTIME_OVER);
                mInnerHandler.sendEmptyMessageDelayed(MSG_TESTTIME_OVER,
                        TEST_TIME * mTesterthread.getCategoryTestCount());
            }
        } catch (Exception localException) {
            LogUtils.v(TAG, "MediaPlayer play start Exception!" + localException);
            localException.printStackTrace();
        }
    }

    @Override
    public void onstopTest() {
        try {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
            }
        } catch (Exception localException) {
            localException.printStackTrace();
            LogUtils.v(TAG, "MediaPlayer play stop Exception!");
        }
    }

    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    private OnErrorListener mErrorListner = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtils.e(TAG, "Error occurred while playing audio:what:" + what);
            mp.stop();
            mp.release();
            // should save the error result
            notifyTestResult(0, what);
            return true;
        }
    };

    // mediaplayer comple listener
    private OnCompletionListener mCompleteListner = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            LogUtils.i(TAG, "audio file play completed.");
            mp.stop();
            mp.release();
            notifyTestResult(1, 0); // success.
        }
    };

    private void notifyTestResult(int value, int extra) {
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

}
