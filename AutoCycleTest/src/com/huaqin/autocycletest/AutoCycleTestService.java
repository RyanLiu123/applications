/**
 * 2016-4-8
 * AutoCycleTestService.java
 * TODO test service
 * liunianliang
 */

package com.huaqin.autocycletest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.huaqin.autocycletest.test.Case;
import com.huaqin.autocycletest.test.Category;
import com.huaqin.autocycletest.test.FirstCategory;
import com.huaqin.autocycletest.test.FivethCategory;
import com.huaqin.autocycletest.test.FourthCategory;
import com.huaqin.autocycletest.test.SecondCategory;
import com.huaqin.autocycletest.test.SeventhCategory;
import com.huaqin.autocycletest.test.SixthCategory;
import com.huaqin.autocycletest.test.ThirdCategory;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;
import com.huaqin.autocycletest.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class AutoCycleTestService extends Service {

    private static String TAG = "AutoCycleTestService";

    private ArrayList<Integer> mchecked_list = new ArrayList<Integer>();
    private ArrayList<Category> mCategoryList = new ArrayList<Category>();
    private HashMap<Integer, HashMap<Integer, Case>> mcaseid_index_map = new HashMap<Integer, HashMap<Integer, Case>>();

    private Context mContext;
    private int mCheckedNum;
    private int mTotalCaseNum;
    private int mTotalFinishedCaseNum;
    private int mCurrentTestingCategory;
    private int mCurrentTestingCategoryTestItemNum;
    private int mTotalAutoTestCycleNumber;
    private int mCurrentAutoTestCycleNumber;
    private boolean mCurrentTestCategoryFinished;

    private Handler mServiceHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AutoCycleTestMassage.MSG_BEGIN_AUTOTEST:
                    LogUtils.i(TAG, "--MSG_BEGIN_AUTOTEST--");
                    resetCaseIndex();
                    processCaseStart();
                    break;
                case AutoCycleTestMassage.MSG_STOP_RUNTIMETEST:
                    LogUtils.i(TAG, "--MSG_STOP_RUNTIMETEST--");
                    break;
                case AutoCycleTestMassage.MSG_TESTCASE_FINISHED:
                    LogUtils.i(TAG, "--MSG_TESTCASE_FINISHED--");
                    processCaseFinish(msg.getData());
                    break;
                case AutoCycleTestMassage.MSG_TESTCASE_FAILED:
                    LogUtils.i(TAG, "--MSG_TESTCASE_FAILED--");
                    processCaseFailed(msg.getData());
                    break;
                case AutoCycleTestMassage.MSG_TESTCASE_ABORTED:
                    LogUtils.i(TAG, "--MSG_TESTCASE_ABORTED--");
                    processCaseAborted(msg.getData());
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onCreate() {
        LogUtils.i(TAG, "AutoCycleTestService onCreate !");
        mContext = AutoCycleTestConfig.getInstance().getContext();
        if (mContext == null) {
            mContext = getApplicationContext();
            AutoCycleTestConfig.getInstance().initConfig(mContext);
        }
        mchecked_list = AutoCycleTestConfig.getInstance().getCheckLists();
        mCheckedNum = mchecked_list.size();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "AutoCycleTestService onStartCommand !");
        if (intent == null || intent.getAction() == null) {
            return START_NOT_STICKY;
        }
        if (intent.getAction().equals("com.huaqin.start_service_from_testing")) {
            LogUtils.i(TAG, "mCheckedNum = " + mCheckedNum);
            if (mCheckedNum > 0 && getCategoryList() > 0) {
                mServiceHandler.sendEmptyMessage(AutoCycleTestMassage.MSG_BEGIN_AUTOTEST);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG, "AutoCycleTestService onDestroy !");
        resetCaseIndex();
        super.onDestroy();
    }

    /**
     * resetCaseIndex
     */
    protected void resetCaseIndex() {
        mTotalFinishedCaseNum = 0;
        mCurrentTestingCategory = 0;
        mCurrentTestingCategoryTestItemNum = 0;
        mCurrentAutoTestCycleNumber = 0;
        mTotalAutoTestCycleNumber = AutoCycleTestConfig.getInstance().getCurrentCategoryTestCount(
                AutoCycleTestUtils.LIST_ITEM_6);
        mCurrentTestCategoryFinished = false;
        LogUtils.d(TAG, "resetCaseIndex --> mTotalCaseNum = " + mTotalCaseNum
                + "  mTotalAutoTestCycleNumber=" + mTotalAutoTestCycleNumber);
    }

    private int getCategoryList() {
        mTotalCaseNum = 0;
        mCategoryList.clear();
        for (Integer i : mchecked_list) {
            Category itemCategory = getCategory(i);
            if (itemCategory != null) {
                mCategoryList.add(itemCategory);
                mcaseid_index_map.put(i, itemCategory.getLocaleCase());
                mTotalCaseNum += itemCategory.getLocaleCase().size();
            }
        }
        LogUtils.i(TAG, "mCategoryList = " + mCategoryList);
        return mCategoryList.size();
    }

    private Category getCategory(Integer i) {
        Category itemCategory = null;
        switch (i) {
            case AutoCycleTestUtils.LIST_ITEM_0:
                itemCategory = new FirstCategory();
                break;
            case AutoCycleTestUtils.LIST_ITEM_1:
                itemCategory = new SecondCategory();
                break;
            case AutoCycleTestUtils.LIST_ITEM_2:
                itemCategory = new ThirdCategory();
                break;
            case AutoCycleTestUtils.LIST_ITEM_3:
                itemCategory = new FourthCategory();
                break;
            case AutoCycleTestUtils.LIST_ITEM_4:
                itemCategory = new FivethCategory();
                break;
            case AutoCycleTestUtils.LIST_ITEM_5:
                itemCategory = new SixthCategory();
                break;
            case AutoCycleTestUtils.LIST_ITEM_6:
                itemCategory = new SeventhCategory();
                break;
            default:
                break;
        }
        return itemCategory;
    }

    private void processCaseStart() {
        mCurrentTestingCategoryTestItemNum = 0;
        mCurrentTestCategoryFinished = false;
        HashMap<Integer, Case> mCurrentCategoryList = mCategoryList.get(mCurrentTestingCategory)
                .getLocaleCase();
        int mCaseIds = AutoCycleTestConfig.getInstance().getAllItemTest().length;
        for (int i = 0; i < mCaseIds; i++) {
            Case mCase = mCurrentCategoryList.get(i);
            if (mCase != null) {
                AutoCycleTestConfig.getInstance().saveResult(mCase.getCategoryId(),
                        mCase.getCaseName(),
                        mContext.getResources().getString(R.string.casestate_begin));
                LogUtils.i(TAG, "processCaseStart mCasename = " + mCase.getCaseName());
                mCase.setHandler(mServiceHandler);
                mCase.startTest();
                mCase.setFinished(false);
            }
        }
    }

    private void processCaseFinish(Bundle b) {
        int categoryId = b.getInt("categoryId");
        int caseId = b.getInt("caseid");
        Case mCase = mcaseid_index_map.get(categoryId).get(caseId);
        if (mCase != null) {
            LogUtils.i(TAG, "processCaseFinish  mCaseName = " + mCase.getCaseName());
            mCase.saveResult(1, null);
            mCase.setFinished(true);
            mCase.stopTest();
            mCase.setHandler(null);
            mTotalFinishedCaseNum++;
            mCurrentTestingCategoryTestItemNum++;
            AutoCycleTestConfig.getInstance().saveResult(categoryId, mCase.getCaseName(),
                    mContext.getResources().getString(R.string.caseresult_success));
        }
        LogUtils.i(TAG, "mTotalFinishedCaseNum = " + mTotalFinishedCaseNum);
        LogUtils.i(TAG, "mTotalCaseNum = " + mTotalCaseNum);
        LogUtils.i(TAG, "mCurrentTestingCategoryTestItemNum = "
                + mCurrentTestingCategoryTestItemNum);
        LogUtils.i(TAG, "mCategoryList000 caseSize = " + mCategoryList.get(mCurrentTestingCategory)
                .getLocaleCase()
                .size());
        LogUtils.i(TAG, "mIsCycleTest = " + AutoCycleTestConfig.getInstance().isCycleTest());
        if (mTotalFinishedCaseNum == mTotalCaseNum
                && AutoCycleTestConfig.getInstance().isCycleTest()) {
            mCurrentAutoTestCycleNumber++;
            LogUtils.i(TAG, "mCurrentAutoTestCycleNumber = " + mCurrentAutoTestCycleNumber);
            if (mCurrentAutoTestCycleNumber < mTotalAutoTestCycleNumber) {
                mServiceHandler.sendEmptyMessage(AutoCycleTestMassage.MSG_BEGIN_AUTOTEST);
            } else {
                processAllCaseFinished();
            }
        } else if (mCurrentTestingCategoryTestItemNum == mCategoryList.get(mCurrentTestingCategory)
                .getLocaleCase()
                .size() && mTotalFinishedCaseNum < mTotalCaseNum) {
            mCurrentTestCategoryFinished = true;
            mCurrentTestingCategory++;
            processCaseStart();
        } else if (mTotalFinishedCaseNum == mTotalCaseNum) {
            processAllCaseFinished();
        }
        LogUtils.i(TAG, "mCategoryList001 caseSize = " + mCategoryList.get(mCurrentTestingCategory)
                .getLocaleCase()
                .size());
    }

    private void processCaseFailed(Bundle b) {
        int categoryId = b.getInt("categoryId");
        int caseId = b.getInt("caseid");
        Case mCase = mcaseid_index_map.get(categoryId).get(caseId);
        int mCaseIds = AutoCycleTestConfig.getInstance().getAllItemTest().length;
        if (mCase != null) {
            mCase.saveResult(0, b.getString("fail_reason"));
            mTotalFinishedCaseNum++;
            mCurrentTestingCategoryTestItemNum++;
            AutoCycleTestConfig.getInstance().saveResult(categoryId, mCase.getCaseName(),
                    mContext.getResources().getString(R.string.casestate_failed));
        }

        // stop all test
        for (int i = 0; i < mCaseIds; i++) {
            Case sCase = mcaseid_index_map.get(categoryId).get(i);
            if (sCase != null && !sCase.isFinished()) {
                sCase.stopTest();
                sCase.setFinished(true);
            }
        }

        Intent intent = new Intent(AutoCycleTestMassage.NOTIFY_TEST_FAILED);
        intent.putExtra("data", b);
        mContext.sendBroadcast(intent);
    }

    private void processCaseAborted(Bundle b) {
        int categoryId = b.getInt("categoryId");
        int caseId = b.getInt("caseid");
        Case mCase = mcaseid_index_map.get(categoryId).get(caseId);
        int mCaseIds = AutoCycleTestConfig.getInstance().getAllItemTest().length;
        if (mCase != null) {
            mCase.setFinished(true);
            mCase.saveResult(0, "aborted");
            mTotalFinishedCaseNum++;
            mCurrentTestingCategoryTestItemNum++;
            AutoCycleTestConfig.getInstance().saveResult(categoryId, mCase.getCaseName(),
                    mContext.getResources().getString(R.string.casestate_aborted));
        }

        // stop all test
        for (int i = 0; i < mCaseIds; i++) {
            Case sCase = mcaseid_index_map.get(categoryId).get(i);
            if (sCase != null && !sCase.isFinished()) {
                sCase.stopTest();
                sCase.setFinished(true);
            }
        }
        Intent intent = new Intent(AutoCycleTestMassage.NOTIFY_TEST_CASE_ABORTED);
        intent.putExtra("data", b);
        mContext.sendBroadcast(intent);
    }

    private void processAllCaseFinished() {
        Intent intent = new Intent(AutoCycleTestMassage.NOTIFY_TEST_FINISHED);
        mContext.sendBroadcast(intent);
    }
}
