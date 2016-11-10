/**
 * 2016-4-8
 * ThirdCategory.java
 * TODO Parallel Camera Earpiece Prox Light Accele SD Test
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import com.huaqin.autocycletest.test.camera.CaseCamera;
import com.huaqin.autocycletest.test.earpiece.CaseEarpiece;
import com.huaqin.autocycletest.test.sd.CaseSd;
import com.huaqin.autocycletest.test.sensor.CaseALPSGsensor;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class ThirdCategory extends Category {

    private static String TAG = "ThirdCategory";

    private HashMap<Integer, Case> mLocalecaselist = new HashMap<Integer, Case>();
    private CaseCamera caseCamera;
    private CaseSd caseSd;
    private CaseEarpiece caseEarpiece;
    private CaseALPSGsensor caseALPSGsensor;

    /**
     * @param tag
     */
    public ThirdCategory() {
        super(TAG, AutoCycleTestUtils.LIST_ITEM_2);
        mCases.add(caseEarpiece = new CaseEarpiece(AutoCycleTestUtils.LIST_ITEM_2));
        mCases.add(caseSd = new CaseSd(AutoCycleTestUtils.LIST_ITEM_2));
        mCases.add(caseCamera = new CaseCamera(AutoCycleTestUtils.LIST_ITEM_2));
        caseALPSGsensor = new CaseALPSGsensor(AutoCycleTestUtils.LIST_ITEM_2);
        caseALPSGsensor.setTestTimeOneRound(100);
        mCases.add(caseALPSGsensor);
        mLocalecaselist.put(caseEarpiece.getId(), caseEarpiece);
        mLocalecaselist.put(caseSd.getId(), caseSd);
        mLocalecaselist.put(caseCamera.getId(), caseCamera);
        mLocalecaselist.put(caseALPSGsensor.getId(), caseALPSGsensor);
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
