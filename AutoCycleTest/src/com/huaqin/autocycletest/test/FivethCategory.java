/**
 * 2016-4-8
 * FivethCategory.java
 * TODO Parallel LCD Earpiece GPS SD Test
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import com.huaqin.autocycletest.test.earpiece.CaseEarpiece;
import com.huaqin.autocycletest.test.gps.CaseGps;
import com.huaqin.autocycletest.test.lcd.CaseLcd;
import com.huaqin.autocycletest.test.sd.CaseSd;
import com.huaqin.autocycletest.test.sensor.CaseGyrMSensor;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class FivethCategory extends Category {

    private static String TAG = "FivethCategory";
    private HashMap<Integer, Case> mLocalecaselist = new HashMap<Integer, Case>();

    private CaseLcd caseLcd;
    private CaseEarpiece caseEarpiece;
    private CaseSd caseSd;
    private CaseGps caseGps;

    /**
     * @param tag
     */
    public FivethCategory() {
        super(TAG, AutoCycleTestUtils.LIST_ITEM_4);
        mCases.add(caseLcd = new CaseLcd(AutoCycleTestUtils.LIST_ITEM_4));
        mCases.add(caseEarpiece = new CaseEarpiece(AutoCycleTestUtils.LIST_ITEM_4));
        mCases.add(caseSd = new CaseSd(AutoCycleTestUtils.LIST_ITEM_4));
        mCases.add(caseGps = new CaseGps(AutoCycleTestUtils.LIST_ITEM_4));
        mLocalecaselist.put(caseLcd.getId(), caseLcd);
        mLocalecaselist.put(caseEarpiece.getId(), caseEarpiece);
        mLocalecaselist.put(caseSd.getId(), caseSd);
        mLocalecaselist.put(caseGps.getId(), caseGps);
    }

    /*
     * 2016-4-8
     */
    @Override
    public ArrayList<Case> getCasesList() {
        return mCases;
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
    public HashMap<Integer, Case> getLocaleCase() {
        // TODO Auto-generated method stub
        return mLocalecaselist;
    }

}
