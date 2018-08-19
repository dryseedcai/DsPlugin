package com.dryseed.assetsmultidexloader.utils;


import com.orhanobut.logger.Logger;

/**
 * @author caiminming
 */
public class LogUtil {
    private static final String TAG = "MMM";

    public static boolean isDebug() {
        return true;
    }

    public static void d(String msg) {
        Logger.d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        Logger.d(tag, msg);
    }

    public static void i(String msg) {
        Logger.i(TAG, msg);
    }

    public static void i(String tag, String msg) {
        Logger.i(tag, msg);
    }

    public static void e(String msg) {
        Logger.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        Logger.e(tag, msg);
    }


}
