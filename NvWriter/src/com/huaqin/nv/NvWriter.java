/**
 * 2016-08-18
 * NvWriter.java
 * TODO API for read and write nv
 * liunianliang
 */
package com.huaqin.nv;

import android.util.Log;
import java.util.HashMap;
/**
 * @author liunianliang
 *
 */
public class NvWriter {

    private static final String TAG = "NvWriter-TAG";
    private static NvWriter sInstance = new NvWriter();

    /*HQ QCOM FLAG,DON'T MODIFY*/
    public static final int RF_CALIBRATION_FLAG = 0;
    public static final int RF_COMPREHENSIVE_TEST_FLAG = 1;
    public static final int RF_COUPLING_FLAG = 2;
    public static final int FACTORY_PCBATEST_MMI_FLAG = 3;
    public static final int FACTORY_FULLTEST_MMI_FLAG = 4;
    public static final int FACTORY_KBTEST_MMI_FLAG = 5;
    public static final int WIFI_GPS_BT_CONDUCT_FLAG = 6;
    public static final int WIFI_GPS_BT_ANTENNA_FLAG = 7;
    public static final int RUNIN_TEST_FLAG = 8;
    public static final int FACTORYRESET_FLAG = 9;
    public static final int BATTERY_TEST_FLAG = 10;
    public static final int CAMERA_TEST_FLAG = 11;
    public static final int AUDIO_TEST_FLAG = 12;
    public static final int FINGERPRINT_TEST_FLAG = 13;
    public static final int NFC_TEST_FLAG = 14;
    public static final int NETWORK_LOCK_FLAG = 15;
    public static final int EXT_FLAG = 16;

    /*INT TO STRING*/
    public static HashMap<Integer,String> FLAGMAP = new HashMap<Integer,String>();
    static {
      FLAGMAP.put(RF_CALIBRATION_FLAG,"RF_CALIBRATION_FLAG");
      FLAGMAP.put(RF_COMPREHENSIVE_TEST_FLAG,"RF_COMPREHENSIVE_TEST_FLAG");
      FLAGMAP.put(RF_COUPLING_FLAG,"RF_COUPLING_FLAG");
      FLAGMAP.put(FACTORY_PCBATEST_MMI_FLAG,"FACTORY_PCBATEST_MMI_FLAG");
      FLAGMAP.put(FACTORY_FULLTEST_MMI_FLAG,"FACTORY_FULLTEST_MMI_FLAG");
      FLAGMAP.put(FACTORY_KBTEST_MMI_FLAG,"FACTORY_KBTEST_MMI_FLAG");
      FLAGMAP.put(WIFI_GPS_BT_CONDUCT_FLAG,"WIFI_GPS_BT_CONDUCT_FLAG");
      FLAGMAP.put(WIFI_GPS_BT_ANTENNA_FLAG,"WIFI_GPS_BT_ANTENNA_FLAG");
      FLAGMAP.put(RUNIN_TEST_FLAG,"RUNIN_TEST_FLAG");
      FLAGMAP.put(FACTORYRESET_FLAG,"FACTORYRESET_FLAG");
      FLAGMAP.put(BATTERY_TEST_FLAG,"BATTERY_TEST_FLAG");
      FLAGMAP.put(CAMERA_TEST_FLAG,"CAMERA_TEST_FLAG");
      FLAGMAP.put(AUDIO_TEST_FLAG,"AUDIO_TEST_FLAG");
      FLAGMAP.put(FINGERPRINT_TEST_FLAG,"FINGERPRINT_TEST_FLAG");
      FLAGMAP.put(NFC_TEST_FLAG,"NFC_TEST_FLAG");
      FLAGMAP.put(NETWORK_LOCK_FLAG,"NETWORK_LOCK_FLAG");
      FLAGMAP.put(EXT_FLAG,"EXT_FLAG");

      System.loadLibrary("hqnvwriter_jni");
    }

    /*FLAG RESULT*/
    public static final char PASS = 'P';
    public static final char FAIL = 'F';
    public static final char NA = ' ';

    private native String native_readflag_NV();
    private native String native_readSN_NV();
    private native void native_writeflag_NV(int index,char result);
    private native void native_sync();

    public static NvWriter getInstance() {
      if (sInstance == null) {
        sInstance = new NvWriter();
      }
      return sInstance;
    }

    public void sync() {
      native_sync();
    }

    public String readFlagNV() {
      String mFlagNv = native_readflag_NV();
      Log.i(TAG,"readFlagNV: mFlagNv = " + mFlagNv);
      return mFlagNv;
    }

    public String readSNNV() {
      String mSnPnNV = native_readSN_NV();
      Log.i(TAG,"readSNNV: mSnPnNV = " + mSnPnNV);
      return mSnPnNV;
    }

    public void writeFlagNV(int index,char result) {
      Log.i(TAG,"WRITE NV FLAG: NAME = " + FLAGMAP.get(index) + " VALUES = " + result);
      native_writeflag_NV(index,result);
    }

    public char getFlag(int index) {
      String mFlagNv = readFlagNV();
      char flag = NA;
      if (mFlagNv != null && mFlagNv.length() >= index) {
        flag = mFlagNv.charAt(index);
      }
      Log.i(TAG,FLAGMAP.get(index) + ": flag = " + flag);
      return flag;
    }

    public boolean isFlagPass(int index) {
      return getFlag(index) == PASS;
    }

    public String getPN() {
      String mSNPNFlag = readSNNV();
      String mPN = "";
      if (mSNPNFlag != null && mSNPNFlag.length() > 63) {
        mPN = mSNPNFlag.substring(0,63).trim();
      }
      Log.i(TAG,"mPN = " + mPN);
      return mPN;
    }

    public String getSN() {
      String mSNPNFlag = readSNNV();
      String mSN = "";
      if (mSNPNFlag != null && mSNPNFlag.length() > 64) {
        mSN = mSNPNFlag.substring(64,mSNPNFlag.length()-1).trim();
      }
      Log.i(TAG,"mSN = " + mSN);
      return mSN;
    }
}
