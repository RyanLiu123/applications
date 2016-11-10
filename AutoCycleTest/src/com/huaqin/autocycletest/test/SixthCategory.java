/**
 * 2016-4-8
 * SixthCategory.java
 * TODO Parallel Earpiece Prox Light Accele Vibrator BT WiFi Test
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import com.huaqin.autocycletest.test.bluetooth.CaseBluetooth;
import com.huaqin.autocycletest.test.earpiece.CaseEarpiece;
import com.huaqin.autocycletest.test.sensor.CaseALPSGsensor;
import com.huaqin.autocycletest.test.vibrator.CaseVibrator;
import com.huaqin.autocycletest.test.wifi.CaseWifi;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class SixthCategory extends Category {

    private static String TAG = "SixthCategory";
    private HashMap<Integer, Case> mLocalecaselist = new HashMap<Integer, Case>();

    private CaseEarpiece caseEarpiece;
    private CaseALPSGsensor caseALPSGsensor;
    private CaseBluetooth casebluetooth;
    private CaseWifi caseWifi;
    private CaseVibrator caseVibrator;

    /**
     * @param tag
     */
    public SixthCategory() {
        super(TAG, AutoCycleTestUtils.LIST_ITEM_5);
        mCases.add(caseEarpiece = new CaseEarpiece(AutoCycleTestUtils.LIST_ITEM_5));
        caseALPSGsensor = new CaseALPSGsensor(AutoCycleTestUtils.LIST_ITEM_5);
        caseALPSGsensor.setTestTimeOneRound(30);
        mCases.add(caseALPSGsensor);
        mCases.add(casebluetooth = new CaseBluetooth(AutoCycleTestUtils.LIST_ITEM_5));
        mCases.add(caseWifi = new CaseWifi(AutoCycleTestUtils.LIST_ITEM_5));
        mCases.add(caseVibrator = new CaseVibrator(AutoCycleTestUtils.LIST_ITEM_5));

        mLocalecaselist.put(caseEarpiece.getId(), caseEarpiece);
        mLocalecaselist.put(caseALPSGsensor.getId(), caseALPSGsensor);
        mLocalecaselist.put(casebluetooth.getId(), casebluetooth);
        mLocalecaselist.put(caseWifi.getId(), caseWifi);
        mLocalecaselist.put(caseVibrator.getId(), caseVibrator);
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
        return mLocalecaselist;
    }

}
