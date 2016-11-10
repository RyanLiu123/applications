/**
 * 2016-4-12
 * AutoCycleTestSetting.java
 * TODO test setting
 * liunianliang
 */

package com.huaqin.autocycletest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.huaqin.autocycletest.util.LogUtils;

/**
 * @author liunianliang
 */
public class AutoCycleTestSetting extends Activity {
    private static String TAG = "AutoCycleTestSetting";
    private int[] mCategoryTextId = {
            R.id.category1_name, R.id.category2_name, R.id.category3_name, R.id.category4_name,
            R.id.category5_name, R.id.category6_name, R.id.category7_name
    };
    private int[] mCategoryCountId = {
            R.id.category1_count, R.id.category2_count, R.id.category3_count, R.id.category4_count,
            R.id.category5_count, R.id.category6_count, R.id.category7_count
    };
    EditText mCategory1Count;
    EditText mCategory2Count;
    EditText mCategory3Count;
    EditText mCategory4Count;
    EditText mCategory5Count;
    EditText mCategory6Count;
    EditText mCategory7Count;
    private EditText[] mCategoryCountEditText = {
            mCategory1Count, mCategory2Count, mCategory3Count, mCategory4Count,
            mCategory5Count, mCategory6Count, mCategory7Count
    };

    private String[] mainList;

    private static int[] mDefaultCount = {
            1, 1, 1, 1, 1, 1, 1
    };

    private Context mContext;

    /*
     * 2016-4-12
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_settings);
        mContext = AutoCycleTestConfig.getInstance().getContext();
        init();
    }

    /*
     * 2016-4-12
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * init count
     */
    private void init() {
        mainList = AutoCycleTestConfig.getInstance().getAllCategoryTestString();
        for (int i = 0; i < mainList.length; i++) {
            ((TextView) findViewById(mCategoryTextId[i])).setText(mainList[i]);
            mCategoryCountEditText[i] = (EditText) findViewById(mCategoryCountId[i]);
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sp.edit();
        boolean binited = sp.getBoolean("inited", false);
        if (!binited) {
            editor.putBoolean("inited", true);
            for (int i = 0; i < mainList.length; i++) {
                mCategoryCountEditText[i].setText(String.valueOf(mDefaultCount[i]));
                saveCount(i, mDefaultCount[i]);
            }
            editor.commit();
        } else {
            resetoreTestCount();
        }
    }

    /**
     * resetoreTestCount
     */
    private void resetoreTestCount() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        for (int i = 0; i < mainList.length; i++) {
            int count = sp.getInt(mainList[i] + "_count", 1);
            mCategoryCountEditText[i].setText(String.valueOf(count));
        }
    }

    private void saveCount(int index, int count) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(mainList[index] + "_count", count);
        editor.commit();
    }

    public void onSaveClick(View v) {
        for (int i = 0; i < mainList.length; i++) {
            int count = Integer.parseInt(mCategoryCountEditText[i].getText().toString());
            saveCount(i,count);
        }
        finish();
    }

    public void onCancleClick(View v) {
        LogUtils.d(TAG, " onCancleClick, do nothing!");
        finish();
    }
}
