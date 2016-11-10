/**
 * 2016-4-8
 * TestVideo.java
 * TODO TestVideo
 * liunianliang
 */

package com.huaqin.autocycletest.test.video;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.VideoView;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.R;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.util.LogUtils;

/**
 * @author liunianliang
 */
public class TestVideo extends Activity implements Testerthread.TesterInterface {
    private static String TAG = "TestVideo";
    private static Handler mCommHandler = null;
    private MyVideoView mVideoView = null;
    private int mWIDTH = 0;
    private int mHEIGHT = 0;
    private AudioManager mAm;
    private Testerthread mTesterthread = null;
    private boolean mbUserPressBack;
    private int mCylceCount;

    private static int MSG_BEGIN_VIDEO_TEST = 300;
    private static int MSG_STOP_VIDEO_TEST = 301;
    private static int MSG_STOP_VIDEO_TEST_TIMEOVER = 302;
    private static int MSG_STOP_VIDEO_TEST_FROM_NOTIFY = 303;

    private static String ACTION_STOP_VIDEOTEST = "com.huaqin.autoTest.videotest.stop";

    private final long VIDEO_TESTTIME_ONEROUND = AutoCycleTestConfig.DEBUG ? 15 * 1000
            : 30 * 60 * 1000;

    private Handler mInnerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_BEGIN_VIDEO_TEST) {
                if (mVideoView != null) {
                    mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                            + R.raw.alt_autocycle_video));
                    mVideoView.requestFocus();
                    mVideoView.setOnErrorListener(mErrorListner);
                    mVideoView.setOnCompletionListener(mCompleteListner);
                    // set volume to MAX value.
                    mAm.setStreamVolume(AudioManager.STREAM_MUSIC,
                            mAm.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                            AudioManager.FLAG_PLAY_SOUND);
                    mVideoView.start();
                    LogUtils.v(TAG, "VideoPlayer play started!");
                }
                mInnerHandler.removeMessages(MSG_STOP_VIDEO_TEST_TIMEOVER);
                mInnerHandler.sendEmptyMessageDelayed(MSG_STOP_VIDEO_TEST_TIMEOVER,
                        VIDEO_TESTTIME_ONEROUND * mCylceCount);
            } else if (msg.what == MSG_STOP_VIDEO_TEST) {
                try {
                    if (mVideoView != null) {
                        mVideoView.stopPlayback();
                        mVideoView.setOnCompletionListener(null);
                        mVideoView.setOnErrorListener(null);
                    }
                } catch (Exception localException) {
                    localException.printStackTrace();
                    LogUtils.v(TAG, "VideoPlayer play stop Exception!");
                }
            } else if (msg.what == MSG_STOP_VIDEO_TEST_FROM_NOTIFY) {
                try {
                    if (mVideoView != null) {
                        mVideoView.stopPlayback();
                        mVideoView.setOnCompletionListener(null);
                        mVideoView.setOnErrorListener(null);
                    }

                } catch (Exception localException) {
                    localException.printStackTrace();
                    LogUtils.v(TAG, "VideoPlayer play stop Exception!");
                }
                finish();
            } else if (msg.what == MSG_STOP_VIDEO_TEST_TIMEOVER) {
                notifyTestResult(1, 0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWIDTH = dm.widthPixels;
        mHEIGHT = dm.heightPixels;
        LogUtils.i(TAG, "mWIDTH = " + mWIDTH + "  mHEIGHT = " + mHEIGHT);
        mVideoView = new MyVideoView(this);
        setContentView(mVideoView);
        mAm = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP_VIDEOTEST);
        registerReceiver(mReceiver, filter);

        Intent intent = getIntent();
        int testcount = intent.getIntExtra("count", 30);
        int caseid = intent.getIntExtra("caseid", 0);
        int category = intent.getIntExtra("category", -1);
        mCylceCount = intent.getIntExtra("cylceCount", 1);

        mTesterthread = new Testerthread(TAG, caseid, Testerthread.TESTTYPE_COUNT, testcount,
                category);
        mTesterthread.setTesterInterface(this);
        mTesterthread.setoutHandler(mCommHandler);
        mTesterthread.startTesterThread();
    }

    /*
     * 2016-4-8
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    /*
     * 2016-4-13
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            mbUserPressBack = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * 2016-4-13
     */
    @Override
    protected void onPause() {
        if (mbUserPressBack) {
            mTesterthread.stopTesterThread();
        }
        super.onPause();
    }

    private class MyVideoView extends VideoView {
        public MyVideoView(Context context) {
            super(context);
        }

        public MyVideoView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = getDefaultSize(mWIDTH, widthMeasureSpec);
            int height = getDefaultSize(mHEIGHT, heightMeasureSpec);
            setMeasuredDimension(width, height);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "mReceiver: action:" + action);
            if (action.equals(ACTION_STOP_VIDEOTEST)) {
                mInnerHandler.sendEmptyMessage(MSG_STOP_VIDEO_TEST_FROM_NOTIFY);
            }
        }
    };

    private OnErrorListener mErrorListner = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            LogUtils.e(TAG, "Error occurred while playing video:what:" + what);
            notifyTestResult(0, what);
            return true;
        }
    };

    // mediaplayer comple listener
    private OnCompletionListener mCompleteListner = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            LogUtils.i(TAG, "video file play completed.");
            mVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/"
                    + R.raw.alt_autocycle_video));
            mVideoView.start();
        }
    };

    private void notifyTestResult(int result, int extra) {
        // tell tester thread that one round is done,may begin a new round.
        Handler workingHandler = mTesterthread.getworkingHandler();
        Message msg = workingHandler.obtainMessage(Testerthread.MSG_ONEROUND_FINISHED);
        Bundle bundle = new Bundle();
        bundle.putInt("result", result); // 0, failed.1,success.
        bundle.putInt("error_code", extra);
        msg.setData(bundle);

        workingHandler.sendMessage(msg); // send message to workingthread.
    }

    @Override
    public void onstartTest() {
        mInnerHandler.sendEmptyMessage(MSG_BEGIN_VIDEO_TEST);
    }

    @Override
    public void onstopTest() {
        mInnerHandler.sendEmptyMessage(MSG_STOP_VIDEO_TEST);
    }

    @Override
    public int onsleepbeforestart() {
        return 100;
    }

    public static void setCommHandler(Handler handler) {
        mCommHandler = handler;
    }
}
