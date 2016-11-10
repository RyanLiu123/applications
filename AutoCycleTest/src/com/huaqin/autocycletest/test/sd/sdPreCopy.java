/**
 * 2016-4-11
 * sdPreCopy.java
 * TODO pre copy sd test file
 * liunianliang
 */

package com.huaqin.autocycletest.test.sd;

import android.content.res.AssetManager;
import android.os.AsyncTask;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.util.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author liunianliang
 */
public class sdPreCopy extends AsyncTask<Boolean, Void, Void> {

    private static String TAG = "sdPreCopy";
    private static String mBaseDir = "/sdcard/alt_autocycle";

    /*
     * 2016-4-11
     */
    @Override
    protected Void doInBackground(Boolean... arg0) {
        if (arg0[0]) {
            preCopyAssentfilesToSdcard();
        } else {
            delete();
        }
        return null;
    }

    public boolean preCopyAssentfilesToSdcard() {
        File mSrcDir = new File(mBaseDir);
        AssetManager mAssetManager = AutoCycleTestConfig.getInstance().getContext().getAssets();
        if (!mSrcDir.exists()) {
            mSrcDir.mkdir();
        }
        try {
            for (String file : TestSd.getFileList()) {
                LogUtils.e(TAG, "start preCopy " + file);
                File destinationFile = new File(mSrcDir, file);
                InputStream source = mAssetManager.open(file);
                destinationFile.getParentFile().mkdirs();
                OutputStream destination = new FileOutputStream(destinationFile);
                byte[] buffer = new byte[1024];
                int nread;
                while ((nread = source.read(buffer)) != -1) {
                    if (nread == 0) {
                        nread = source.read();
                        if (nread < 0)
                            break;
                        destination.write(nread);
                        continue;
                    }
                    destination.write(buffer, 0, nread);
                }
                source.close();
                destination.flush();
                destination.close();
                LogUtils.d(
                        TAG,
                        "finish preCopy file {" + file + "} to "
                                + destinationFile.getAbsolutePath()
                                + " done !");
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(TAG, "error happened when preCopy. " + e.toString());
        }
        return true;
    }

    private void delete() {
        File srcDir = new File(mBaseDir);
        LogUtils.e(TAG, " start to delete ...");
        try {
            if (srcDir.exists() && srcDir.isDirectory()) {
                File[] files = srcDir.listFiles();
                for (File f : files) {
                    if (f.delete()) {
                        LogUtils.e(TAG, f + " deleted !");
                    } else {
                        LogUtils.e(TAG, f + " delete failed !");
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "delete file failed !!!  e = " + e.toString());
        }
    }
}
