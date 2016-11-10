/*
 *  file create by liunianliang for fcidownload xmlparse
 */
package com.huaqin.fcidownload.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.huaqin.fcidownload.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class XmlUtil {

    private static final String TAG = "XmlUtil";
    private static HashMap<Integer, String> mUrlMap = new HashMap<Integer, String>();
    private static final String extUrlfile = "/system/etc/url_config.xml";
    private static final String sdcardfile = "/mnt/sdcard/url_config.xml";
    private static XmlUtil sInstance = null;
    
    public static XmlUtil getInstance() {
        if (sInstance == null) {
            sInstance = new XmlUtil();
        }
        return sInstance;
    }
    
    public static XmlUtil init(Context context) {
        FileInputStream inputStream=null; 
        File file = new File(sdcardfile);
        try {
            if (file.exists() && file.canRead()) {
                LogUtil.e(TAG, "file is "+sdcardfile);
                inputStream = new FileInputStream(sdcardfile);
                loadUrlItems(context, inputStream);
            } else if ((file = new File(extUrlfile)).exists() && file.canRead()) {
                LogUtil.e(TAG, "file is "+extUrlfile);
                inputStream = new FileInputStream(extUrlfile);
                loadUrlItems(context, inputStream);
            } else {
                LogUtil.e(TAG, "file is default.");
                loadUrlItems(context);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception e ="+e.toString());
        }
        return getInstance();
    }

    public static void loadUrlItems(Context context,FileInputStream in)
            throws XmlPullParserException, IOException {
        mUrlMap.clear();
        XmlPullParser mXmlPullParser =  Xml.newPullParser();
        mXmlPullParser.setInput(in, "UTF-8");
        int mEventType = mXmlPullParser.getEventType();
        while (XmlPullParser.END_DOCUMENT != mEventType) {
            if (XmlPullParser.START_TAG == mEventType) {
                String tagName = mXmlPullParser.getName();
                LogUtil.i(TAG, "tagName:" + tagName);
                if ("UrlConfig".equals(tagName)) {
                    //do nothing
                } else if ("urlitem".equals(tagName)) {
                    int code = Integer.valueOf(mXmlPullParser.getAttributeValue(null,
                            "code"));
                    String url = mXmlPullParser.getAttributeValue(null,
                            "url");
                    //LogUtil.e(TAG, "1 code= "+ code +"  url= "+url);
                    if (code > 0 && ! TextUtils.isEmpty(url)) {
                        mUrlMap.put(code, url);
                    }
                }
            } else if (XmlPullParser.END_TAG == mEventType) {
                //do nothing
            }
            mEventType = mXmlPullParser.next();
        }
    }
    
    public static void loadUrlItems(Context context)
            throws XmlPullParserException, IOException {
        mUrlMap.clear();
        XmlPullParser mXmlPullParser =  context.getResources().getXml(getItemResId());
        int mEventType = mXmlPullParser.getEventType();
        while (XmlPullParser.END_DOCUMENT != mEventType) {
            if (XmlPullParser.START_TAG == mEventType) {
                String tagName = mXmlPullParser.getName();
                LogUtil.i(TAG, "tagName:" + tagName);
                if ("UrlConfig".equals(tagName)) {
                    //do nothing
                } else if ("urlitem".equals(tagName)) {
                    int code = Integer.valueOf(mXmlPullParser.getAttributeValue(null,
                            "code"));
                    String url = mXmlPullParser.getAttributeValue(null,
                            "url");
                    //LogUtil.e(TAG, "2 code= "+ code +"  url= "+url);
                    if (code > 0 && ! TextUtils.isEmpty(url)) {
                        mUrlMap.put(code, url);
                    }
                }
            } else if (XmlPullParser.END_TAG == mEventType) {
                //do nothing
            }
            mEventType = mXmlPullParser.next();
        }
    }

    public HashMap<Integer, String> getUrlMap() {
        return mUrlMap;
    }

    public static int getItemResId() {
        return R.xml.url_config;
    }

}
