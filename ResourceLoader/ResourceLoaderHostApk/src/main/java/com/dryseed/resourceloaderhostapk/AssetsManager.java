package com.dryseed.resourceloaderhostapk;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

public class AssetsManager {
    public static final String TAG = "AssetsApkLoader";

    /**
     * 从assets复制出去的apk的目标目录
     */
    public static final String APK_DIR = "third_apk";

    /**
     * 文件结尾过滤
     */
    public static final String APK_FILE_FILTER = ".apk";

    /**
     * 清除内部存储apk目录中的文件
     *
     * @param context
     * @throws Exception
     */
    public static void clearOldDexDir(Context context) throws Exception {
        File dexDir = context.getDir(AssetsManager.APK_DIR, Context.MODE_PRIVATE);
        if (dexDir.isDirectory()) {
            Log.i(TAG, "Clearing old secondary dex dir (" + dexDir.getPath()
                    + ").");
            File[] files = dexDir.listFiles();
            if (files == null) {
                Log.w(TAG, "Failed to list secondary dex dir content ("
                        + dexDir.getPath() + ").");
                return;
            }
            for (File oldFile : files) {
                Log.i(TAG, "Trying to delete old file " + oldFile.getPath()
                        + " of size " + oldFile.length());
                if (!oldFile.delete()) {
                    Log.w(TAG, "Failed to delete old file " + oldFile.getPath());
                } else {
                    Log.i(TAG, "Deleted old file " + oldFile.getPath());
                }
            }
            if (!dexDir.delete()) {
                Log.w(TAG,
                        "Failed to delete secondary dex dir "
                                + dexDir.getPath());
            } else {
                Log.i(TAG, "Deleted old secondary dex dir " + dexDir.getPath());
            }
        }
    }

    /**
     * 将资源文件中的apk文件拷贝到私有目录中
     *
     * @param context
     */
    public static void copyAllAssetsApk(Context context) {
        Log.d(TAG, "copyAllAssetsApk start");
        AssetManager assetManager = context.getAssets();
        long startTime = System.currentTimeMillis();
        try {
            File dex = context.getDir(APK_DIR, Context.MODE_PRIVATE);
            dex.mkdir();
            String[] fileNames = assetManager.list("");
            for (String fileName : fileNames) {
                if (!fileName.endsWith(APK_FILE_FILTER)) {
                    return;
                }
                copyAssetsToFile(context, fileName, dex.getAbsolutePath());
            }
            Log.i(TAG, "copyAllAssetsApk end. time = " + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 拷贝assets文件导指定文件
     *
     * @param context
     * @param assetFilename
     * @param dstName
     */
    public static void copyAssetsToFile(final Context context, final String assetFilename, final String dstName) {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {

                FileOutputStream fos = null;
                InputStream is = null;
                try {
                    AssetManager assetManager = context.getAssets();
                    String newFileName = dstName + "/" + assetFilename;
                    File dstFile = new File(newFileName);
                    Log.d(TAG, "desFile : " + dstFile.getAbsolutePath());

                    is = assetManager.open(assetFilename);
                    if (dstFile.exists() && dstFile.length() == is.available()) {
                        Log.i(TAG, assetFilename + " no change");
                        return;
                    }
                    fos = new FileOutputStream(dstFile);
                    byte[] buffer = new byte[1024 * 2];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    Log.d(TAG, newFileName + " copy over");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

    }
}