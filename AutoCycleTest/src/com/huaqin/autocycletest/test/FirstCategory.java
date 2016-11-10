/**
 * 2016-4-8
 * FirstCategory.java
 * TODO Function video Test
 * liunianliang
 */

package com.huaqin.autocycletest.test;

import com.huaqin.autocycletest.test.video.CaseVideo;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author liunianliang
 */
public class FirstCategory extends Category {

    private static String TAG = "FirstCategory";
    private CaseVideo caseVideo;
    private HashMap<Integer,Case> mLocalecaselist = new HashMap<Integer,Case>();

    public FirstCategory() {
        super(TAG,AutoCycleTestUtils.LIST_ITEM_0);
        caseVideo = new CaseVideo(AutoCycleTestUtils.LIST_ITEM_0);
        mCases.add(caseVideo);
        mLocalecaselist.put(caseVideo.getId(),caseVideo);
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
        return "FirstCategory test is finished";
    }

    /*
     * 2016-4-8
     */
    @Override
    public String getDescription() {
        return "play alt_autocycle_video.mp4 for 30 min";
    }

    /* 
     * 2016-4-8
     */
    @Override
    public HashMap<Integer,Case> getLocaleCase() {
        return mLocalecaselist;
    }


}
