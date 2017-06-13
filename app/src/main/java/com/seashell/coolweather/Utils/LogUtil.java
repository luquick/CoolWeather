package com.seashell.coolweather.Utils;

import android.util.Log;

/**
 * Created by Luquick on 2017/6/13.
 */

public class LogUtil {
    /**
     * Log switch
     *
     * VERBOSE--------> on
     * ASSERT---------> off
     *
     */
    private static int LIVE = Log.VERBOSE;

    public static void v(String tag,String msg) {
        if (LIVE <= Log.VERBOSE) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag,String msg) {
        if (LIVE <= Log.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag,String msg) {
        if (LIVE <= Log.INFO) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag,String msg) {
        if (LIVE <= Log.WARN) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag,String msg) {
        if (LIVE <= Log.ERROR) {
            Log.e(tag, msg);
        }
    }
}
