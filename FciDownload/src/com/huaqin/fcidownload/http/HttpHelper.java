/**
 * 2016-3-15
 * HttpHelper.java
 * TODO parser http code
 * liunianliang
 */

package com.huaqin.fcidownload.http;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Xml;

import com.huaqin.fcidownload.util.LogUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liunianliang
 */
public class HttpHelper extends AsyncTask<String, String, String> {

    private static String TAG = "HttpHelper";

    private static String httpDownloadUri = "Unkonw";
    private static HttpURLConnection httpURLConnection = null;
    private String mUri = null;
    private Context mContext = null;
    private Object mLock = new Object();
    private static final Pattern sDownloadUriPattern =
            Pattern.compile("(http://phonedl.*.zip)");

    public HttpHelper(Context context, String uri) {
        mContext = context;
        mUri = uri;
    }

    public InputStream getInputStream(String uri) throws ProtocolException {
        InputStream inputStream = null;
        try {
            LogUtil.i(TAG, "*** getInputStream ***");
            URL url = new URL(uri);
            if (url != null) {
                try {
                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setConnectTimeout(10000);
                    httpURLConnection.setReadTimeout(10000);
                    httpURLConnection.setRequestMethod("GET");
                    // httpURLConnection.setDoInput(true);
                    // httpURLConnection.connect();
                    int responsecode = httpURLConnection.getResponseCode();
                    LogUtil.i(TAG, "*** responsecode =  ***" + responsecode);
                    if (responsecode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                    }
                } catch (IOException e) {
                    LogUtil.i(TAG, "*** getInputStream IOException  ***" + e.toString());
                    e.printStackTrace();
                } finally {
                    httpURLConnection.disconnect();
                }
            }
        } catch (MalformedURLException e) {
            LogUtil.i(TAG, "*** getInputStream MalformedURLException  ***" + e.toString());
            e.printStackTrace();
        }
        return inputStream;
    }

    /**
     * @param mRealUri
     * @param mainActivity
     * @return boolean
     */
    public boolean parserDownloadUri(String realUri, Context context) {
        LogUtil.i(TAG, "realUri = " + realUri);
        return realParser(realUri);
    }

    /**
     * @param response
     * @return
     */
    private boolean realParser(String realUri) {
        InputStream inputStream = null;
        try {
            inputStream = getInputStream(realUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream, "UTF-8"));
            String result = "";
            String line = "";
            synchronized (mLock) {
                // parserInput(inputStream);
                while (null != (line = reader.readLine())) {
                    result += line + "\n";
                }
            }
            httpDownloadUri = result;
            LogUtil.i(TAG, result);
        } catch (Exception e) {
            LogUtil.i(TAG, "*** realParser IOException  ***" + e.toString());
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * @return
     */
    public String getDownloadUri() {
        Matcher matcher = sDownloadUriPattern.matcher(httpDownloadUri);
        String mUri = "unkonw";
        if (matcher.find()) {
            mUri = matcher.group(1);
        }
        LogUtil.i(TAG, "mUri = " + mUri);
        return mUri;
    }

    private void parserInput(InputStream in) {
        XmlPullParser mXmlPullParser = Xml.newPullParser();
        try {
            mXmlPullParser.setInput(in, "UTF-8");
            int mEventType = mXmlPullParser.getEventType();
            while (mEventType != XmlPullParser.END_DOCUMENT) {
                if (mEventType == XmlPullParser.START_TAG) {
                    String tagName = mXmlPullParser.getName();
                    LogUtil.i(TAG, "tagName = " + tagName);
                } else if (mEventType == XmlPullParser.END_TAG) {

                }
                mEventType = mXmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            LogUtil.i(TAG, "*** parserInput XmlPullParserException  e = ***" + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            LogUtil.i(TAG, "*** parserInput IOException  e = ***" + e.toString());
        }
    }

    /*
     * 2016-3-15
     */
    @Override
    protected String doInBackground(String... arg0) {
        parserDownloadUri(mUri, mContext);
        return null;
    }

    /*
     * 2016-3-15
     */
    @Override
    protected void onPostExecute(String result) {
        // LogUtil.e(TAG, "result = " + result);
        super.onPostExecute(result);
    }

}
