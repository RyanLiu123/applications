/**
 * 2016-4-7
 * AutoCycleTestTesting.java
 * TODO for AutoTesting
 * liunianliang
 */

package com.huaqin.autocycletest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huaqin.autocycletest.adapter.MyExpandableListAdapter;
import com.huaqin.autocycletest.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class AutoCycleTestTesting extends Activity {

    private static String TAG = "AutoCycleTestTesting";

    private ArrayList<String> mTestItem = new ArrayList<String>();
    private HashMap<Integer, Integer> mAdapterItem = new HashMap<Integer, Integer>();
    private ArrayList<Integer> mchecked_list = new ArrayList<Integer>();
    private ExpandableListView mlistView;
    private MyExpandableListAdapter mAdapter;
    private TextView mTextTestResult;
    private LinearLayout mResultLayout;
    private PowerManager mPM = null;
    private PowerManager.WakeLock mwl = null;
    private Context mContext;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AutoCycleTestMassage.MSG_START_TEST_SERVICE:
                    startTestService();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            String action = arg1.getAction();
            if (action.equals(AutoCycleTestMassage.NOTIFY_TEST_FINISHED)) {
                if (mTextTestResult != null && mResultLayout != null) {
                    mResultLayout.setBackgroundColor(Color.GREEN);
                    mTextTestResult.setText(mContext.getResources().getString(
                            R.string.caseresult_success) + "\nlasting:" +
                            getEndTime());
                }
            } else if (action.equals(AutoCycleTestMassage.NOTIFY_TEST_FAILED)) {
                if (mTextTestResult != null && mResultLayout != null) {
                    mResultLayout.setBackgroundColor(Color.RED);
                    mTextTestResult.setText(mContext.getResources().getString(
                            R.string.casestate_failed) + "\nlasting:" +
                            getEndTime());
                }
            } else if (action.equals(AutoCycleTestMassage.NOTIFY_TEST_CASE_STARTED)) {
                // do nothing
            } else if (action.equals(AutoCycleTestMassage.NOTIFY_TEST_CASE_ABORTED)) {
                Bundle b = arg1.getBundleExtra("data");
                int categoryId = b.getInt("categoryId");
                int caseId = b.getInt("caseid");
                LogUtils.i(TAG, "NOTIFY_TEST_CASE_ABORTED categoryId " + categoryId + "  caseId ="
                        + caseId);
                if (mTextTestResult != null && mResultLayout != null) {
                    mResultLayout.setBackgroundColor(Color.RED);
                    mTextTestResult.setText(mContext.getResources().getString(
                            R.string.casestate_aborted) + "\nlasting:" +
                            getEndTime());
                }
            } else if (action.equals(AutoCycleTestMassage.NOTIFY_TEST_CASE_FINISHED)) {
                // do nothing
            } else if (action.equals(AutoCycleTestMassage.REFRESH_ADAPTER_LIST)) {
                LogUtils.i(TAG, "AutoCycleTestMassage.REFRESH_ADAPTER_LIST");
                refresh();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_testing);
        init();
        mPM = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mwl = mPM.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "AutoCycleTestWakeLock");
        if (!mwl.isHeld()) {
            mwl.acquire();
        }
        AutoCycleTestConfig.getInstance().setHandler(mHandler);
        mContext = AutoCycleTestConfig.getInstance().getContext();
        if (mContext == null) {
            mContext = getApplicationContext();
            AutoCycleTestConfig.getInstance().initConfig(mContext);
        }
        mHandler.sendEmptyMessageDelayed(AutoCycleTestMassage.MSG_START_TEST_SERVICE, 500);

        IntentFilter filter = new IntentFilter();
        filter.addAction(AutoCycleTestMassage.NOTIFY_TEST_FINISHED);
        filter.addAction(AutoCycleTestMassage.NOTIFY_TEST_FAILED);
        filter.addAction(AutoCycleTestMassage.NOTIFY_TEST_CASE_STARTED);
        filter.addAction(AutoCycleTestMassage.NOTIFY_TEST_CASE_FINISHED);
        filter.addAction(AutoCycleTestMassage.NOTIFY_TEST_CASE_ABORTED);
        filter.addAction(AutoCycleTestMassage.REFRESH_ADAPTER_LIST);
        mContext.registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        if (mwl.isHeld()) {
            mwl.release();
        }
        stopService(new Intent(mContext, AutoCycleTestService.class));
        // AutoCycleTestConfig.getInstance().setCycleTest(false);
        super.onDestroy();
    }

    private void init() {
        mTestItem.clear();
        mlistView = (ExpandableListView) findViewById(R.id.testing_list);
        mchecked_list = AutoCycleTestConfig.getInstance().getCheckLists();
        mTextTestResult = (TextView) findViewById(R.id.result_test);
        mResultLayout = (LinearLayout) findViewById(R.id.result_text_layout);
        mlistView.setAdapter(mAdapter = new MyExpandableListAdapter(this));

        String[] testItem = getResources().getStringArray(R.array.main_list_item);
        for (Integer i : mchecked_list) {
            mTestItem.add(testItem[i]);
        }
        for (int i = 0; i < mchecked_list.size(); i++) {
            mAdapterItem.put(i, mchecked_list.get(i));
        }
        LogUtils.i(TAG, "mTestItem size = " + mTestItem.size());
        AutoCycleTestConfig.getInstance().setTestingAdapterGroupNames(mTestItem);
        AutoCycleTestConfig.getInstance().setTestingAdapterGroupChilds(mAdapterItem);
        refresh();
    }

    private void refresh() {
        ((MyExpandableListAdapter) mAdapter).notifyDataSetChanged();
        for (int i = 0; i < mAdapter.getGroupCount(); i++) {
            mlistView.expandGroup(i);
        }
    }

    private void startTestService() {
        AutoCycleTestConfig.getInstance().setStartTime(SystemClock.currentTimeMicro());
        Intent intent = new Intent(mContext, AutoCycleTestService.class);
        intent.setAction("com.huaqin.start_service_from_testing");
        startService(intent);
    }

    private String getEndTime() {
        long endtime = SystemClock.currentTimeMicro();
        LogUtils.i(TAG, "endtime = " + endtime);
        long duration = endtime - AutoCycleTestConfig.getInstance().getStartTime();
        long seconds = (duration / 1000000);
        long hours = seconds / 3600;
        long mins = (seconds % 3600) / 60;
        if (mins == 0) {
            return seconds + " s";
        } else if (hours == 0) {
            return mins + " min " + seconds % 60 + " s";
        } else {
            return hours + " h " + mins + " min";
        }
    }

}
