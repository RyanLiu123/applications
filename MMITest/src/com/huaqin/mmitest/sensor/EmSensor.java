/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.huaqin.mmitest.sensor;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.huaqin.mmitest.util.LogUtil;
import com.mediatek.ALSPS.ALSPSNative;

@SuppressLint("NewApi")
public class EmSensor {
    private static final String TAG = "EmSensor";
    public static final int RET_SUCCESS = 1;
    public static final int RET_FAILED = 0;
    private static final String SALESTRACKER_NVRAM_KEY = "/data/nvram/APCFG/APRDEB/PRODUCT_INFO";
    private static final int offset = 120;
    private static final String SERVICE_KEY = "NvRAMAgent";

    public static int doGsensorCalibration(int tolerance) {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_DO_GSENSOR_CALIBRATION, 1,
                tolerance);
        if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }

    public static int getGsensorCalibration(float[] result) {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_GET_GSENSOR_CALIBRATION, 0);
        if (ret.length >= 4 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            try {
                result[0] = Float.parseFloat(ret[1]);
                result[1] = Float.parseFloat(ret[2]);
                result[2] = Float.parseFloat(ret[3]);
                return RET_SUCCESS;
            } catch (NumberFormatException e) {
                return RET_FAILED;
            }
        }
        return RET_FAILED;
    }

    public static int clearGsensorCalibration() {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_CLEAR_GSENSOR_CALIBRATION, 0);
        if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }

    public static int doGyroscopeCalibration(int tolerance) {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_DO_GYROSCOPE_CALIBRATION, 1,
                tolerance);
        if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }

    public static int getGyroscopeCalibration(float[] result) {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_GET_GYROSCOPE_CALIBRATION, 0);
        if (ret.length >= 4 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            try {
                result[0] = Float.parseFloat(ret[1]);
                result[1] = Float.parseFloat(ret[2]);
                result[2] = Float.parseFloat(ret[3]);
                return RET_SUCCESS;
            } catch (NumberFormatException e) {
                return RET_FAILED;
            }
        }
        return RET_FAILED;
    }

    public static int clearGyroscopeCalibration() {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_CLEAR_GYROSCOPE_CALIBRATION, 0);
        if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }

    public static int doPsensorCalibration(int min) {
        int[] mCali = ALSPSNative.get_alsps();
        for(int s:mCali){
            LogUtil.d(TAG, "doPsensorCalibration s: = " + s);

        }
        writeData(mCali[0], mCali[1], mCali[2], mCali[3]);
        if (ALSPSNative.set_alsps(mCali[0], mCali[1], mCali[2], mCali[3])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }
    
    public static int getPsensorCalibration(float[] results) {
    	int[] result = ALSPSNative.get_ps_threshold();
        for(int s=0;s< result.length;s++){
            LogUtil.d(TAG, "getPsensorCalibration s: = " + s);
            //results[s] = Float.intBitsToFloat(result[s]);
        }  
        
        if (result.length >= 2) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }

    public static int setPsensorThreshold(int high, int low) {
        String[] ret = runCmdInEmSvr(
                AFMFunctionCallEx.FUNCTION_EM_SENSOR_SET_THRESHOLD, 2,
                high, low);
        if (ret.length > 0 && String.valueOf(RET_SUCCESS).equals(ret[0])) {
            return RET_SUCCESS;
        }
        return RET_FAILED;
    }

    public static native int getPsensorData();

    public static native int getPsensorThreshold(int[] result);

    public static native int calculatePsensorMinValue();

    public static native int getPsensorMinValue();

    public static native int calculatePsensorMaxValue();

    public static native int getPsensorMaxValue();

    public static String[] runCmdInEmSvr(int index, int paramNum, int... param) {
        ArrayList<String> arrayList = new ArrayList<String>();
        AFMFunctionCallEx functionCall = new AFMFunctionCallEx();
        boolean result = functionCall.startCallFunctionStringReturn(index);
        functionCall.writeParamNo(paramNum);
        for (int i : param) {
            functionCall.writeParamInt(i);
        }
        if (result) {
            FunctionReturn r;
            do {
                r = functionCall.getNextResult();
                if (r.mReturnString.isEmpty()) {
                    break;
                }
                arrayList.add(r.mReturnString);
            } while (r.mReturnCode == AFMFunctionCallEx.RESULT_CONTINUE);
            if (r.mReturnCode == AFMFunctionCallEx.RESULT_IO_ERR) {
                LogUtil.d(TAG, "AFMFunctionCallEx: RESULT_IO_ERR");
                arrayList.clear();
                arrayList.add("ERROR");
            }
        } else {
        	LogUtil.d(TAG, "AFMFunctionCallEx return false");
            arrayList.clear();
            arrayList.add("ERROR");
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    static {
        System.loadLibrary("mmi_test_sensor_jni");
    }
    
    /**
     * @Description:Write NvRam data
     */
    public static void writeData(int value0,int value1,int value2,int value3) {
        IBinder binder = ServiceManager.getService(SERVICE_KEY);
        NvRAMAgent agent = NvRAMAgent.Stub.asInterface(binder);
        try {
            // write buffer to NvRam
            byte[] buff = agent.readFileByName(SALESTRACKER_NVRAM_KEY);
            LogUtil.w(TAG, "value0 = " + value0 + " value1 = " + value1 + " value2 = " + value2 + " value3 = " + value3);
            byte[] writeBuff = getBytes(value0,value1,value2,value3);
            for (int i = 0; i < writeBuff.length; i++) {
                buff[offset + i] = writeBuff[i];
            }
            int flag = agent.writeFileByName(SALESTRACKER_NVRAM_KEY, buff);
            if (flag > 0) {
            	LogUtil.w(TAG, "NvRam write success!");
            } else {
            	LogUtil.w(TAG, "NvRam write failed!");
            }
        } catch (RemoteException e) {
        	LogUtil.w(TAG, "writeFile error!");
            e.printStackTrace();
        } catch (Exception e) {
        	LogUtil.w(TAG, "writeFile error!,e = " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static byte[] getBytes(int data0,int data1,int data2,int data3){
        byte[] bytes = new byte[20];

        bytes[0] = (byte)(0x13818000&0xff);
        bytes[1] = (byte)((0x13818000&0xff00)>>8);
        bytes[2] = (byte)((0x13818000&0xff0000)>>16);
        bytes[3] = (byte)((0x13818000&0xff000000)>>24);

        bytes[4] = (byte)(data0&0xff);
        bytes[5] = (byte)((data0&0xff00)>>8);
        bytes[6] = (byte)((data0&0xff0000)>>16);
        bytes[7] = (byte)((data0&0xff000000)>>24);
        bytes[8] = (byte)(data1&0xff);
        bytes[9] = (byte)((data1&0xff00)>>8);
        bytes[10] = (byte)((data1&0xff0000)>>16);
        bytes[11] = (byte)((data1&0xff000000)>>24);
        bytes[12] = (byte)(data2&0xff);
        bytes[13] = (byte)((data2&0xff00)>>8);
        bytes[14] = (byte)((data2&0xff0000)>>16);
        bytes[15] = (byte)((data2&0xff000000)>>24);
        bytes[16] = (byte)(data3&0xff);
        bytes[17] = (byte)((data3&0xff00)>>8);
        bytes[18] = (byte)((data3&0xff0000)>>16);
        bytes[19] = (byte)((data3&0xff000000)>>24);

        return bytes;
    }
}
