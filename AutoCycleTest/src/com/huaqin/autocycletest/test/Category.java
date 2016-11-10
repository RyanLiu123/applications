/**
 * 2016-4-8
 * Category.java
 * TODO MainUi item
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import com.huaqin.autocycletest.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public abstract class Category {
    protected String mTAG = "Gategory";
    protected int mCategoryID = -1;

    protected ArrayList<Case> mCases = new ArrayList<Case>();
    protected HashMap<Integer,ArrayList<Case>> CategoryCaselist = new HashMap<Integer,ArrayList<Case>>();

    public Category(String tag, int categoryID) {
        mTAG = tag;
        mCategoryID = categoryID;
        LogUtils.i(mTAG, "new Category Enter !!");
    }

    public abstract ArrayList<Case> getCasesList();
    
    //public abstract HashMap<Integer,ArrayList<Case>> getCategoryCaselist();

    public abstract String getTestResult();

    public abstract String getDescription();
    
    public abstract HashMap<Integer, Case> getLocaleCase();
}
