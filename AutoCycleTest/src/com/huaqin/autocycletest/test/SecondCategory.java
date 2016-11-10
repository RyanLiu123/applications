/**
 * 2016-4-8
 * SecondCategory.java
 * TODO Parallel Audio Vibrator BT WiFi Test
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import com.huaqin.autocycletest.test.audio.CaseAudio;
import com.huaqin.autocycletest.test.bluetooth.CaseBluetooth;
import com.huaqin.autocycletest.test.vibrator.CaseVibrator;
import com.huaqin.autocycletest.test.wifi.CaseWifi;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class SecondCategory extends Category {

    private static String TAG = "SecondCategory";
    private HashMap<Integer,Case> mLocalecaselist = new HashMap<Integer,Case>();
    private CaseAudio caseAudio;
    private CaseBluetooth casebluetooth;
    private CaseWifi caseWifi;
    private CaseVibrator caseVibrator;
    /**
     * @param tag
     */
    public SecondCategory() {
        super(TAG,AutoCycleTestUtils.LIST_ITEM_1);
        mCases.add(caseAudio = new CaseAudio(AutoCycleTestUtils.LIST_ITEM_1));
        mCases.add(casebluetooth = new CaseBluetooth(AutoCycleTestUtils.LIST_ITEM_1));
        mCases.add(caseWifi = new CaseWifi(AutoCycleTestUtils.LIST_ITEM_1));
        mCases.add(caseVibrator = new CaseVibrator(AutoCycleTestUtils.LIST_ITEM_1));
        mLocalecaselist.put(caseAudio.getId(),caseAudio);
        mLocalecaselist.put(casebluetooth.getId(),casebluetooth);
        mLocalecaselist.put(caseWifi.getId(),caseWifi);
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
    public HashMap<Integer,Case> getLocaleCase() {
        return mLocalecaselist;
    }

}
