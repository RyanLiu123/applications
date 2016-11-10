/**
 * 2016-4-9
 * TestCamera.java
 * TODO  test camera
 * liunianliang
 */

package com.huaqin.autocycletest.test.camera;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.huaqin.autocycletest.R;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.test.Testerthread.TesterInterface;
import com.huaqin.autocycletest.util.LogUtils;

import java.io.IOException;

/**
 * @author liunianliang
 */
public class TestCamera extends Activity implements TesterInterface, SurfaceHolder.Callback {

    private static String TAG = "TestCamera";
    public SurfaceView mSurfaceView = null;

    private Camera mCamera = null;

    private static int MSG_BEGIN_CAMERA_TEST = 400;
    private static int MSG_STOP_CAMERA_TEST = 401;
    private static int MSG_STOP_CAMERA_PREVIEW = 402;

    private static int MSG_NOTIFY_TEST_RESULT = 403;

    private static int MSG_STOP_CAMERA_TEST_FROM_NOTIFY = 410;

    private SurfaceHolder mSurfaceHolder = null;

    private RawPreviewCallback mRawPreviewCallback = new RawPreviewCallback();

    private Testerthread mTesterthread = null;

    private static Handler mCommHandler = null;
    private boolean mbPaused = false;
    private boolean mbUserPressBack = false;

    private int mNumberofCameras = 0;
    private int mCameraIndex = 0;
    private long EVERY_CAMERA_PREVIEW_TIME = 5 * 1000;

    private static String ACTION_STOP_CAMERATEST = "com.huaqin.runtime.cameratest.stop";

    private Handler mInnerHandler = new Handler() {
        public void handleMessage(Message msg) {

            LogUtils.d(TAG, "handleMessage:msg:" + msg.what);

            if (msg.what == MSG_BEGIN_CAMERA_TEST) {
                openCamera(mCameraIndex);
                mCameraIndex++;
            } else if (msg.what == MSG_STOP_CAMERA_PREVIEW) {
                LogUtils.d(TAG, "handleMessage:STOP_CAMERA_PREVIEW:index:" + mCameraIndex
                        + ",number:" + mNumberofCameras);
                takePicture();
                if (mCameraIndex == mNumberofCameras) {
                    stopCamera();
                    mInnerHandler.removeMessages(MSG_NOTIFY_TEST_RESULT);
                    mInnerHandler.sendEmptyMessageDelayed(MSG_NOTIFY_TEST_RESULT,
                            onsleepbeforestart());
                } else {
                    stopCamera();
                    mInnerHandler.removeMessages(MSG_BEGIN_CAMERA_TEST);
                    mInnerHandler.sendEmptyMessageDelayed(MSG_BEGIN_CAMERA_TEST, 0);
                }
            } else if (msg.what == MSG_NOTIFY_TEST_RESULT) {
                notifyTestResult(1, null);
            }
            else if (msg.what == MSG_STOP_CAMERA_TEST) {
                stopCamera();
                // clear MSG_NOTIFY_TEST_RESULT message(may already in the message queue
                mInnerHandler.removeMessages(MSG_NOTIFY_TEST_RESULT);
                finish();
            }
            else if (msg.what == MSG_STOP_CAMERA_TEST_FROM_NOTIFY) {
                // release camera(do not notify test result)
                stopCamera();
                // clear MSG_NOTIFY_TEST_RESULT message(may already in the message queue
                mInnerHandler.removeMessages(MSG_NOTIFY_TEST_RESULT);
                finish();// quit testcamera ui.
            }
        }
    };

    /*
     * 2016-4-9
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        getWindow().setFormat(0);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Intent intent = getIntent();
        int testcount = intent.getIntExtra("count", 10);
        int caseid = intent.getIntExtra("caseid", 0);
        int category = intent.getIntExtra("category", -1);

        // get camera number
        mNumberofCameras = Camera.getNumberOfCameras();
        mCameraIndex = 0;
        LogUtils.d(TAG, "onCreate:mNumberofCameras:" + mNumberofCameras);

        // register filter for processing stop event sent from runtimetestservice(mostly for battery
        // test failed)
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_STOP_CAMERATEST);

        registerReceiver(mReceiver, filter);

        mTesterthread = new Testerthread(TAG, caseid, Testerthread.TESTTYPE_COUNT, testcount,
                category);
        mTesterthread.setTesterInterface(this);
        mTesterthread.setoutHandler(mCommHandler);
    }

    /*
     * 2016-4-9
     */
    @Override
    protected void onDestroy() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
        // mTesterthread.stopTesterThread();
        super.onDestroy();
    }

    /*
     * 2016-4-13
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            mbUserPressBack = true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /*
     * 2016-4-13
     */
    @Override
    protected void onPause() {
        if (mbUserPressBack) {
            mTesterthread.stopTesterThread();
        }
        super.onPause();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtils.d(TAG, "mReceiver: action:" + action);
            if (action.equals(ACTION_STOP_CAMERATEST)) {
                mInnerHandler.sendEmptyMessage(MSG_STOP_CAMERA_TEST_FROM_NOTIFY);
            }
        }

    };

    private void openCamera(int camera_index) {
        LogUtils.d(TAG, "openCamera:index" + camera_index);
        try {
            mCamera = Camera.open(camera_index);
        } catch (Exception e) {
            notifyTestResult(0, "open error");
        }
        mCamera.setDisplayOrientation(90);
        if (mCamera == null) {
            notifyTestResult(0, "open error");
            return;
        }

        mCamera.setPreviewCallback(mRawPreviewCallback);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            notifyTestResult(0, "setpreview error" + exception);
            return;
        }
        mCamera.startPreview();
        mInnerHandler.removeMessages(MSG_STOP_CAMERA_PREVIEW);
        mInnerHandler.sendEmptyMessageDelayed(MSG_STOP_CAMERA_PREVIEW, EVERY_CAMERA_PREVIEW_TIME);
    }

    private void stopCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        mInnerHandler.removeMessages(MSG_STOP_CAMERA_PREVIEW);
        mInnerHandler.removeMessages(MSG_STOP_CAMERA_TEST);
        mInnerHandler.removeMessages(MSG_STOP_CAMERA_TEST_FROM_NOTIFY);
    }

    private void takePicture() {
        LogUtils.i(TAG, "takePicture");
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.autoFocus(null);
                mCamera.takePicture(mShutterCallback, rawPictureCallback,
                        jpegCallback);
            } catch (Exception e) {

            }
        }
    }

    private ShutterCallback mShutterCallback = new ShutterCallback() {
        public void onShutter() {
            LogUtils.i(TAG, "onShutter");
        }
    };

    private PictureCallback rawPictureCallback = new PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
        }
    };

    private PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
        }
    };

    private final class RawPreviewCallback implements PreviewCallback {
        public void onPreviewFrame(byte[] rawData, Camera camera) {
            LogUtils.v(TAG, "Preview callback start");
            mCamera.setPreviewCallback(null);
        }
    };

    private void notifyTestResult(int result, String extra) {
        Handler workingHandler = mTesterthread.getworkingHandler();
        Message msg = workingHandler
                .obtainMessage((result == 1) ? Testerthread.MSG_ONEROUND_FINISHED
                        : Testerthread.MSG_ONEROUND_FAILED);
        Bundle bundle = new Bundle();
        bundle.putInt("result", result);
        if (result != 1) {
            bundle.putString("error_code", extra);
        }
        msg.setData(bundle);
        workingHandler.sendMessage(msg);
    }

    /*
     * 2016-4-9
     */
    @Override
    public void onstartTest() {
        mCameraIndex = 0; // reset camera index.
        mInnerHandler.sendEmptyMessage(MSG_BEGIN_CAMERA_TEST);
    }

    /*
     * 2016-4-9
     */
    @Override
    public void onstopTest() {
        mInnerHandler.sendEmptyMessage(MSG_STOP_CAMERA_TEST);
    }

    /*
     * 2016-4-9
     */
    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    /**
     * @param handler
     */
    public static void setCommHandler(Handler handler) {
        mCommHandler = handler;
    }

    /*
     * 2016-4-9
     */
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    /*
     * 2016-4-9
     */
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        LogUtils.d(TAG, "surfaceCreated:mbPaued:" + mbPaused);
        if (mbPaused == false) {
            mTesterthread.startTesterThread();
        } else {
            mbPaused = false;
        }

    }

    /*
     * 2016-4-9
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

}
