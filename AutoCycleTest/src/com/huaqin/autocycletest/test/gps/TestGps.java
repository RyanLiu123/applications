/**
 * 2016-4-11
 * TestGps.java
 * TODO test gps
 * liunianliang
 */

package com.huaqin.autocycletest.test.gps;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.test.Testerthread;
import com.huaqin.autocycletest.test.Testerthread.TesterInterface;
import com.huaqin.autocycletest.util.LogUtils;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author liunianliang
 */
public class TestGps implements TesterInterface {
    private static String TAG = "TestGps";

    private Testerthread mTesterthread = null;
    private Context mContext = null;

    private static final int MIN_COUNT = 1;
    private ArrayList<GpsSatellite> satelliteList = new ArrayList<GpsSatellite>();
    private GpsStatus mGpsStatus;
    private Iterable<GpsSatellite> mSatellites;
    private Location mLocation;
    private LocationManager mLocationManager = null;
    private static final int OPEN_TIME = 30 * 1000;
    private boolean isEnabled = false;

    private final int MSG_GPS_OPEN_30S = 1;
    private final int MSG_GPS_CLOSE_30S = 2;
    private final int MSG_CHECK_GPS_RESULT = 3;

    private Handler mInnerHandler = new Handler() {
        /*
         * 2016-4-11
         */
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GPS_OPEN_30S:
                    LogUtils.i(TAG, "--MSG_GPS_OPEN_30S--");
                    removeMessages(MSG_GPS_CLOSE_30S);
                    sendEmptyMessageDelayed(MSG_GPS_CLOSE_30S, OPEN_TIME - 10000);
                    break;
                case MSG_GPS_CLOSE_30S:
                    stopGPS();
                    LogUtils.i(TAG, "--MSG_GPS_CLOSE_30S, size");
                    removeMessages(MSG_CHECK_GPS_RESULT);
                    sendEmptyMessageDelayed(MSG_CHECK_GPS_RESULT, OPEN_TIME);
                    break;
                case MSG_CHECK_GPS_RESULT:
                    if (satelliteList.size() >= MIN_COUNT) {
                        notifyTestResult(1, null);
                    } else {
                        notifyTestResult(0, "can't find gps !");
                    }
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            LogUtils.i(TAG, "location.getTime() =" + location.getTime());
            LogUtils.i(TAG, "location.getLongitude()" + location.getLongitude());
            LogUtils.i(TAG, "location.getLatitude()" + location.getLatitude());
            LogUtils.i(TAG, "location.getAltitude()" + location.getAltitude());
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    LogUtils.i(TAG, "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    LogUtils.i(TAG, "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    LogUtils.i(TAG, "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    };

    private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {

        public void onGpsStatusChanged(int arg0) {
            switch (arg0) {
                case GpsStatus.GPS_EVENT_STARTED:
                    LogUtils.i(TAG, "GPS_EVENT_STARTED");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    LogUtils.i(TAG, "GPS_EVENT_STOPPED");
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    LogUtils.i(TAG, "GPS_EVENT_SATELLITE_STATUS");
                    mGpsStatus = mLocationManager.getGpsStatus(null);
                    mSatellites = mGpsStatus.getSatellites();
                    Iterator<GpsSatellite> it = mSatellites.iterator();
                    int count = 0;
                    satelliteList.clear();
                    while (it.hasNext()) {
                        GpsSatellite gpsS = (GpsSatellite) it.next();
                        satelliteList.add(gpsS);
                        LogUtils.i(TAG, "GPS_snr:" + gpsS.getSnr());
                        count++;
                    }
                    break;
                default:
                    break;
            }

        }

    };

    public TestGps(int caseid, int testtype, int value, int categoryId) {
        mTesterthread = new Testerthread(TAG, caseid, testtype, value, categoryId);
        mTesterthread.setTesterInterface(this);
        mContext = AutoCycleTestConfig.getInstance().getContext();
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * @return
     */
    public Testerthread getTesterthread() {
        return mTesterthread;
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstartTest() {
        satelliteList.clear();
        startGPS();
    }

    /*
     * 2016-4-11
     */
    @Override
    public void onstopTest() {
        satelliteList.clear();
        stopGPS();
    }

    /*
     * 2016-4-11
     */
    @Override
    public int onsleepbeforestart() {
        return 0;
    }

    void startGPS() {
        try {
            LogUtils.i(TAG, " startGPS() mLocationManager is not enable, so enable it");
            final ContentResolver resolver = mContext.getContentResolver();
            Settings.Secure
                    .setLocationProviderEnabled(resolver, LocationManager.GPS_PROVIDER, true);
            SleepTime(1000);
        } catch (Exception e) {
            isEnabled = true;
            LogUtils.i(TAG, "startGPS()  init fail e = "+ e.toString());
        }
        Criteria criteria;
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = mLocationManager.getBestProvider(criteria, true);
        if (provider == null) {
            // locationSate = "Fail to get GPS Provider!";
        }
        LogUtils.i(TAG, "startGPS()  provider = " + provider);
        mLocationManager.requestLocationUpdates(provider, 500, 0,
                mLocationListener);
        mLocationManager.addGpsStatusListener(gpsStatusListener);
        mLocation = mLocationManager.getLastKnownLocation(provider);
        SleepTime(9000);
        mInnerHandler.sendEmptyMessage(MSG_GPS_OPEN_30S);
    }

    void stopGPS() {
        if (!isEnabled) {
            Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(),
                    LocationManager.GPS_PROVIDER, false);
        }

        try {
            mLocationManager.removeUpdates(mLocationListener);
            mLocationManager.removeGpsStatusListener(gpsStatusListener);
        } catch (Exception e) {

        }
    }

    private void SleepTime(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    private void notifyTestResult(int value, String extra) {
        Handler workingHandler = mTesterthread.getworkingHandler();
        Message msg = (value == 1) ? workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FINISHED) : workingHandler
                .obtainMessage(Testerthread.MSG_ONEROUND_FAILED);
        Bundle bundle = new Bundle();
        bundle.putInt("result", value); // 0, failed.1,success.

        if (value != 1) {
            bundle.putString("error_code", "error_code:" + extra);
        }
        msg.setData(bundle);
        workingHandler.sendMessage(msg); // send message to workingthread.
    }

}
