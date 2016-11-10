/**
 * 2016-4-8
 * Case.java
 * TODO MainUi item case
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.util.LogUtils;

/**
 * @author liunianliang
 */
public abstract class Case {
    protected String mTAG = "Case";
    protected int mTestTime = 0;
    protected int mTestCount = 0;
    protected int mId = 0;
    protected int mCategoryId = -1;
    protected Intent mIntent = null;
    protected Context mContext = null;

    protected boolean mflag_usetime = true;
    protected boolean mbFinish = true;
    protected boolean mbStarted = false;

    protected int mResult = 1;
    protected String mFailReason = null;

    public Case(String tag) {
        this(tag, -1, -1);
    }

    public Case(String tag, int id, int categoryId) {
        mTAG = tag;
        mId = id;
        mCategoryId = categoryId;
        mContext = AutoCycleTestConfig.getInstance().getContext();
        LogUtils.i(mTAG, "Case create Enter,mTAG = " + mTAG + "  mId = " + mId + "  mCategoryId="
                + mCategoryId);
    }

    public int getResult() {
        return mResult;
    }

    public int getId() {
        return mId;
    }
    
    public int getCategoryId() {
        return mCategoryId;
    }

    public String getFailReason() {
        return mFailReason;
    }

    public void saveResult(int result, String fail_reason) {
        mResult = result;
        if (mResult == 0) {
            mFailReason = fail_reason;
        }
    }

    public void setFinished(boolean value) {
        mbFinish = value;
    }

    public boolean isFinished() {
        return mbFinish;
    }

    public String getTag() {
        return mTAG;
    }
    
    public int getCycleCount() {
        return AutoCycleTestConfig.getInstance().getCurrentCategoryTestCount(mCategoryId);
    }

    public String getCaseName() {
        LogUtils.e(mTAG, "getCaseName --> mCategoryId = " + mCategoryId + "  mId =" + mId);
        return AutoCycleTestConfig.getInstance().getAllItemTest()[mId];
    }

    public abstract int getTestTime();

    public abstract int getTestCount();

    public abstract int startTest();

    public abstract int stopTest();

    public abstract void setHandler(Handler handler);

    public abstract String getDesp();
}
