/**
 * 2016-4-9
 * TestLcd.java
 * TODO test lcd
 * liunianliang
 */

package com.huaqin.autocycletest.test.lcd;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.R;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.test.Testerthread.TesterInterface;
import com.huaqin.autocycletest.util.LogUtils;

/**
 * @author liunianliang
 */
public class TestLcd extends Activity implements TesterInterface {

    private static String TAG = "TestLcd";

    private int mColorIndex = 0;
    private static int MSG_BEGIN_LCD_TEST = 100;
    private static int MSG_STOP_LCD_TEST = 101;
    private static int MSG_LCD_TEST_INTERVAL = 102;
    private static int MSG_SHOW_WHITE_COLOR = 103;
    private static int MSG_DISPLAY_BORDR = 104;
    private static int MSG_STOP_LCD_TEST_FROM_NOTIFY = 105;

    private PowerManager.WakeLock mWakeLock;
    private static Handler mCommHandler = null;
    private Testerthread mTesterthread = null;
    private MyView mView = null;
    private boolean mbUserPressBack;
    private int COLOR_EVERY_TIME = AutoCycleTestConfig.DEBUG ? 15 * 1000 : 2 * 60 * 1000;

    private int[] colorArrays = {
            Color.rgb(0, 255, 255),
            Color.rgb(255, 0, 255),
            Color.rgb(128, 128, 128),
            Color.rgb(0, 128, 0),
            Color.rgb(0, 255, 0),
            Color.rgb(128, 0, 0),
            Color.rgb(0, 0, 128),
            Color.rgb(128, 128, 0),
            Color.rgb(128, 0, 128),
            Color.rgb(255, 0, 0),
            Color.rgb(192, 192, 192),
            Color.rgb(0, 128, 128),
            Color.rgb(255, 255, 255),
            Color.rgb(255, 255, 0),
            Color.rgb(0, 0, 255)
    };

    private Handler mInnerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == MSG_BEGIN_LCD_TEST) {
                LogUtils.i(TAG, "begin lcd test !");
                mColorIndex++;
                if (mView != null) {
                    mView.setColor(colorArrays[0]);
                    mView.postInvalidate();
                }
                mInnerHandler.sendEmptyMessageDelayed(MSG_LCD_TEST_INTERVAL, COLOR_EVERY_TIME
                        / colorArrays.length);
            } else if (msg.what == MSG_LCD_TEST_INTERVAL) {
                if (mColorIndex < colorArrays.length) {
                    LogUtils.i(TAG, "change color to -> " + colorArrays[mColorIndex]);
                    if (mView != null) {
                        mView.setColor(colorArrays[mColorIndex]);
                        mView.postInvalidate();
                    }
                    mInnerHandler.sendEmptyMessageDelayed(MSG_LCD_TEST_INTERVAL, COLOR_EVERY_TIME
                            / colorArrays.length);
                    mColorIndex++;
                } else {
                    mInnerHandler.sendEmptyMessage(MSG_SHOW_WHITE_COLOR);
                }
            } else if (msg.what == MSG_STOP_LCD_TEST) {
                LogUtils.i(TAG, "MSG_STOP_LCD_TEST .");
                notifyTestResult(1, 0);
            } else if (msg.what == MSG_SHOW_WHITE_COLOR) {
                LogUtils.i(TAG, "MSG_SHOW_WHITE_COLOR .");
                if (mView != null) {
                    mView.setColor(Color.WHITE);
                    mView.postInvalidate();
                }
                mInnerHandler.sendEmptyMessageDelayed(MSG_DISPLAY_BORDR, COLOR_EVERY_TIME);
            } else if (msg.what == MSG_DISPLAY_BORDR) {
                LogUtils.i(TAG, "MSG_DISPLAY_BORDR .");
                if (mView != null) {
                    mView.setColor(Color.BLACK);
                    mView.postInvalidate();
                }
                mInnerHandler.sendEmptyMessageDelayed(MSG_STOP_LCD_TEST, COLOR_EVERY_TIME);
            } else if (msg.what == MSG_STOP_LCD_TEST_FROM_NOTIFY) {
                LogUtils.i(TAG, "MSG_STOP_LCD_TEST_FROM_NOTIFY .");
                mInnerHandler.removeMessages(MSG_LCD_TEST_INTERVAL);

                // stop tester thread(do not notify aborted)
                mTesterthread.stopTesterThreadFromNotify();
                finish();
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "mReceiver: action:" + action);
            if (action.equals("com.huaqin.autoTest.lcdtest.stop")) {
                mInnerHandler.sendEmptyMessage(MSG_STOP_LCD_TEST_FROM_NOTIFY);
            }
        }
    };

    /*
     * 2016-4-9
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lcd);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        Intent intent = getIntent();
        int testcount = intent.getIntExtra("count", 30);
        int caseid = intent.getIntExtra("caseid", 0);
        int category = intent.getIntExtra("category", -1);

        mView = new MyView(this);
        RelativeLayout mLcdLayout = (RelativeLayout) findViewById(R.id.lcd_layout);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        mView.setId(caseid);
        mView.setLayoutParams(lp);
        mLcdLayout.addView(mView);

        mTesterthread = new Testerthread(TAG, caseid, Testerthread.TESTTYPE_COUNT, testcount,
                category);
        mTesterthread.setTesterInterface(this);
        mTesterthread.setoutHandler(mCommHandler);
        mTesterthread.startTesterThread();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huaqin.autoTest.lcdtest.stop");
        registerReceiver(mReceiver, filter);
    }

    /*
     * 2016-4-9
     */
    @Override
    protected void onDestroy() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        unregisterReceiver(mReceiver);
        mView = null;
        super.onDestroy();
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

    /**
     * @param handler
     */
    public static void setCommHandler(Handler handler) {
        mCommHandler = handler;
    }

    /*
     * 2016-4-9
     */
    @Override
    public void onstartTest() {
        mColorIndex = 0;
        mInnerHandler.removeMessages(MSG_BEGIN_LCD_TEST);
        mInnerHandler.sendEmptyMessage(MSG_BEGIN_LCD_TEST);
    }

    /*
     * 2016-4-9
     */
    @Override
    public void onstopTest() {
        mInnerHandler.removeMessages(MSG_BEGIN_LCD_TEST);
        mInnerHandler.removeMessages(MSG_DISPLAY_BORDR);
        mInnerHandler.removeMessages(MSG_LCD_TEST_INTERVAL);
        mInnerHandler.removeMessages(MSG_SHOW_WHITE_COLOR);
        mInnerHandler.removeMessages(MSG_STOP_LCD_TEST);
        mColorIndex = 0;
        mView = null;
        finish();
    }

    /*
     * 2016-4-9
     */
    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    class MyView extends View {

        private int currentColor;

        public MyView(Context context) {
            super(context);
        }

        public void onDraw(Canvas paramCanvas) {
            super.onDraw(paramCanvas);
            int red = (currentColor & 0xFF0000) >> 16;
            int green = (currentColor & 0x00FF00) >> 8;
            int blue = (currentColor & 0xFF);
            paramCanvas.drawRGB(red, green, blue);
        }

        public void setColor(int color) {
            currentColor = color;
        }
    }

    private void notifyTestResult(int result, int extra) {
        // tell tester thread that one round is done,may begin a new round.
        Handler workingHandler = mTesterthread.getworkingHandler();
        Message msg = result == 1 ? workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FINISHED) :
                workingHandler.obtainMessage(Testerthread.MSG_ONEROUND_FAILED);
        Bundle bundle = new Bundle();
        bundle.putInt("result", result); // 0, failed.1,success.
        bundle.putInt("error_code", extra);
        msg.setData(bundle);

        workingHandler.sendMessage(msg); // send message to workingthread.
    }
}
