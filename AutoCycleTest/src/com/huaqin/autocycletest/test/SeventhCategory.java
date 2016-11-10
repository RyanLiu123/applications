/**
 * 2016-4-8
 * SeventhCategory.java
 * TODO Power cycle
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import com.huaqin.autocycletest.util.AutoCycleTestUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class SeventhCategory extends Category {

    private static String TAG = "SeventhCategory";
    private HashMap<Integer,Case> mLocalecaselist = new HashMap<Integer,Case>();
    /**
     * @param tag
     */
    public SeventhCategory() {
        super(TAG,AutoCycleTestUtils.LIST_ITEM_6);
    }

    /*
     * 2016-4-8
     */
    @Override
    public ArrayList<Case> getCasesList() {

        return null;
    }

    /*
     * 2016-4-8
     */
    @Override
    public String getTestResult() {

        return null;
    }

    /*
     * 2016-4-8
     */
    @Override
    public String getDescription() {

        return null;
    }

    /* 
     * 2016-4-8
     */
    @Override
    public HashMap<Integer,Case> getLocaleCase() {
        // TODO Auto-generated method stub
        return mLocalecaselist;
    }

}
