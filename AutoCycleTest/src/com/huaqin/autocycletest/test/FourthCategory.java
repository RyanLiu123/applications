/**
 * 2016-4-8
 * FourthCategory.java
 * TODO Parallel LCD Earpiece Gyro Mag Test
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import com.huaqin.autocycletest.test.earpiece.CaseEarpiece;
import com.huaqin.autocycletest.test.lcd.CaseLcd;
import com.huaqin.autocycletest.test.sensor.CaseGyrMSensor;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class FourthCategory extends Category {

    private static String TAG = "FourthCategory";
    private HashMap<Integer,Case> mLocalecaselist = new HashMap<Integer,Case>();
    private CaseLcd caseLcd;
    private CaseGyrMSensor caseGyrMsensor;
    private CaseEarpiece caseEarpiece;
    /**
     * @param tag
     */
    public FourthCategory() {
        super(TAG,AutoCycleTestUtils.LIST_ITEM_3);
        caseLcd = new CaseLcd(AutoCycleTestUtils.LIST_ITEM_3);
        caseGyrMsensor = new CaseGyrMSensor(AutoCycleTestUtils.LIST_ITEM_3);
        caseGyrMsensor.setTestTimeOneRound(60);
        caseEarpiece = new CaseEarpiece(AutoCycleTestUtils.LIST_ITEM_3);
        mCases.add(caseLcd);
        mCases.add(caseGyrMsensor);
        mCases.add(caseEarpiece);
        mLocalecaselist.put(caseLcd.getId(), caseLcd);
        mLocalecaselist.put(caseGyrMsensor.getId(), caseGyrMsensor);
        mLocalecaselist.put(caseEarpiece.getId(), caseEarpiece);
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
    public HashMap<Integer,Case> getLocaleCase() {
        // TODO Auto-generated method stub
        return mLocalecaselist;
    }

}
