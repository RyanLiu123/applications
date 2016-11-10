/**
 * 2016-4-8
 * AutoCycleTestConfig.java
 * TODO testconfig
 * liunianliang
 */

package com.huaqin.autocycletest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.huaqin.autocycletest.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class AutoCycleTestConfig {

    private static String TAG = "AutoCycleTestConfig";
    private String[] allItemTest;
    private String[] allCategoryTestString;
    private Handler mHandler;
    private static Context mContext;
    public static boolean DEBUG = false;

    private ArrayList<String> groupNames = new ArrayList<String>();
    private ArrayList<Integer> mchecked_list = new ArrayList<Integer>();
    private HashMap<Integer, Integer> mAdapterItem = new HashMap<Integer, Integer>();
    private HashMap<Integer, int[]> mTestMap = new HashMap<Integer, int[]>();
    private static HashMap<String, String> mResultMap = new HashMap<String, String>();

    private static AutoCycleTestConfig sInstance;

    public static AutoCycleTestConfig getInstance() {
        if (sInstance == null) {
            sInstance = new AutoCycleTestConfig();
        }
        return sInstance;
    }

    public void initConfig(Context context) {
        mContext = context;
        allItemTest = mContext.getResources().getStringArray(R.array.item_test);
        allCategoryTestString = mContext.getResources().getStringArray(R.array.main_list_item);
    }

    public void clean() {
        allItemTest = null;
        mTestMap = null;
        groupNames = null;
        mAdapterItem = null;
        mchecked_list = null;
    }

    public Context getContext() {
        return mContext;
    }

    public String[] getAllItemTest() {
        return allItemTest;
    }

    public String[] getAllCategoryTestString() {
        return allCategoryTestString;
    }

    public void saveTestItem(HashMap<Integer, int[]> map) {
        mTestMap = map;
    }

    public HashMap<Integer, int[]> getTestMap() {
        return mTestMap;
    }

    public void setTestingAdapterGroupNames(ArrayList<String> names) {
        groupNames = names;
    }

    public ArrayList<String> getTestingAdapterGroupNames() {
        return groupNames;
    }

    public void setTestingAdapterGroupChilds(HashMap<Integer, Integer> childs) {
        mAdapterItem = childs;
    }

    public HashMap<Integer, Integer> getTestingAdapterGroupChilds() {
        return mAdapterItem;
    }

    public void setCheckLits(ArrayList<Integer> list) {
        mchecked_list = list;
    }

    public ArrayList<Integer> getCheckLists() {
        return mchecked_list;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public long getStartTime() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        long result = sp.getLong("start_time", 0);
        LogUtils.i(TAG, "getStartTime = " + result);
        return result;
    }

    public void setStartTime(long starttime) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("start_time", starttime);
        editor.commit();
    }

    public void saveResult(int categoryId, String caseName, String caseResult) {
        LogUtils.i(TAG, "saveResult:categoryId=" + categoryId + "  caseName=" + caseName
                + " caseResult=" + caseResult);
        String key = categoryId + caseName;
        mResultMap.put(key, caseResult);
        mContext.sendBroadcast(new Intent(AutoCycleTestMassage.REFRESH_ADAPTER_LIST));
    }

    public void cleanResult() {
        mResultMap.clear();
    }

    public HashMap<String, String> getResultMap() {
        return mResultMap;
    }

    public int getCurrentCategoryTestCount(int categoryId) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int count = sp.getInt(allCategoryTestString[categoryId] + "_count", 1);
        return count;
    }

    public void setCycleTest(boolean isCycleTest) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("mIsCycleTest", isCycleTest);
        editor.commit();
    }

    public boolean isCycleTest() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean mIsCycleTest = sp.getBoolean("mIsCycleTest", false);
        return mIsCycleTest;
    }
}
