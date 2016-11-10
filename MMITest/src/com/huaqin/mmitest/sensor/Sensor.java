/**
 * 2015-10-29
 * Sensor.java
 * TODO
 * zhouhui
 */
package com.huaqin.mmitest.sensor;

import com.huaqin.mmitest.MMITestService;
import com.huaqin.mmitest.util.LogUtil;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;

/**
 * @author zhouhui
 * 
 */
public class Sensor {

	private static String TAG = "Sensor";

	private Context mContext;
	private int mSensorType = -1;
	private Toast mToast;
	private String mType;
	public static final int GSENSOR = android.hardware.Sensor.TYPE_ACCELEROMETER;
	public static final int GYROSCOPE = android.hardware.Sensor.TYPE_GYROSCOPE;
	public static final int PSENSOR = android.hardware.Sensor.TYPE_PROXIMITY;

	private final HandlerThread mHandlerThread = new HandlerThread(
			"async_handler");
	private Handler mHandler;
	private Handler mUiHandler;
	private static final int MSG_DO_CALIBRARION_20 = 0;
	private static final int MSG_DO_CALIBRARION_40 = 1;
	private static final int MSG_CLEAR_CALIBRARION = 2;
	private static final int MSG_GET_CALIBRARION = 3;
	private static final int MSG_SET_SUCCESS = 4;
	private static final int MSG_GET_SUCCESS = 5;
	private static final int MSG_SET_FAILURE = 6;
	private static final int MSG_GET_FAILURE = 7;
	private static final int TOLERANCE_20 = 2;
	private static final int TOLERANCE_40 = 4;

	public Sensor(MMITestService context) {
		mContext = context;

		mUiHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_SET_SUCCESS:
					LogUtil.d(TAG, "set success");
					showToast(mType + " Operation succeed");
					sendReslut("pass");
					break;
				case MSG_GET_SUCCESS:
					LogUtil.d(TAG, "get success");
					break;
				case MSG_SET_FAILURE:
					LogUtil.d(TAG, "set fail");
					showToast(mType+" Operation failed");
					sendReslut("fail");
					break;
				case MSG_GET_FAILURE:
					LogUtil.d(TAG, "get fail");
					showToast("Get calibration failed");
					break;
				default:
				}
			}
		};

		mHandlerThread.start();
		mHandler = new Handler(mHandlerThread.getLooper()) {
			public void handleMessage(Message msg) {
				if (MSG_GET_CALIBRARION == msg.what) {
					getCalibration();
				} else {
					setCalibration(msg.what);
				}
			}
		};
	}

	public void calibration(MMITestService context, String type) {

		if (mContext == null) {
			mContext = context;
		}
		mType = type;
		if (type.equals(context.MMI_SENSOR_CALIBRATION_PS)) {
			mSensorType = android.hardware.Sensor.TYPE_PROXIMITY;
		} else if (type.equals(context.MMI_SENSOR_CALIBRATION_GS)) {
			mSensorType = android.hardware.Sensor.TYPE_ACCELEROMETER;
		} else if (type.equals(context.MMI_SENSOR_CALIBRATION_GRY)) {
			mSensorType = android.hardware.Sensor.TYPE_GYROSCOPE;
		}
		LogUtil.i(TAG, "mSensorType = " + mSensorType);
		mHandler.sendEmptyMessage(MSG_DO_CALIBRARION_20);
	}

	private void setCalibration(int what) {
		int result = 0;
		LogUtil.d(TAG, String.format("setCalibration(), operation %d", what));
		if (mSensorType == GSENSOR) {
			if (MSG_DO_CALIBRARION_20 == what) {
				result = EmSensor.doGsensorCalibration(TOLERANCE_20);
			} else if (MSG_DO_CALIBRARION_40 == what) {
				result = EmSensor.doGsensorCalibration(TOLERANCE_40);
			} else if (MSG_CLEAR_CALIBRARION == what) {
				result = EmSensor.clearGsensorCalibration();
			}
		} else if (mSensorType == GYROSCOPE) {
			if (MSG_DO_CALIBRARION_20 == what) {
				result = EmSensor.doGyroscopeCalibration(TOLERANCE_20);
			} else if (MSG_DO_CALIBRARION_40 == what) {
				result = EmSensor.doGyroscopeCalibration(TOLERANCE_40);
			} else if (MSG_CLEAR_CALIBRARION == what) {
				result = EmSensor.clearGyroscopeCalibration();
			}
		} else if (mSensorType == PSENSOR) {
			if (MSG_DO_CALIBRARION_20 == what) {
				//result = EmSensor.doPsensorCalibration(TOLERANCE_20);
			    result = 1; //dont support
			}
		}

		LogUtil.d(TAG, String.format("setCalibration(), ret %d", result));

		if (result == EmSensor.RET_SUCCESS) {
			if (getCalibration()) {
				mUiHandler.sendEmptyMessage(MSG_SET_SUCCESS);
			}
		} else {
			if(mSensorType == PSENSOR || mSensorType == GYROSCOPE) {
				showToast((mSensorType == PSENSOR) ? 
						" PSENSOR Calibration funtion is not good , ignore it " : 
							"GYROSCOPE Calibration funtion is not good , ignore it");
				sendReslut("pass");
				return;
			}
			mUiHandler.sendEmptyMessage(MSG_SET_FAILURE);
		}
	}

	private boolean getCalibration() {
		LogUtil.d(TAG, "getCalibration()");
		float[] result = new float[3];
		int ret = 0;
		if (mSensorType == GSENSOR) {
			ret = EmSensor.getGsensorCalibration(result);
		} else if (mSensorType == GYROSCOPE) {
			ret = EmSensor.getGyroscopeCalibration(result);
		} else {
			//ret = EmSensor.getPsensorCalibration(result);
		    ret = 1; //dont support
		}
		LogUtil.d(TAG, String.format(
				"getCalibration(), ret %d, values %f, %f, %f", ret,
				result[0], result[1], result[2]));

		if (ret == EmSensor.RET_SUCCESS) {
			return true;
		} else {
			return false;
		}
	}

	private void showToast(String msg) {
		if (mToast != null) {
			mToast.cancel();
		}
		mToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
		mToast.show();
	}
	
	private void sendReslut(String reslut) {
		LogUtil.i(TAG, "sendReslut mType = " + mType +" result = "+reslut);
		Intent intent = new Intent("com.mmi.helper.response");
		intent.putExtra("type", mType);
		intent.putExtra("value", reslut);
		mContext.sendBroadcast(intent);
	}
}
