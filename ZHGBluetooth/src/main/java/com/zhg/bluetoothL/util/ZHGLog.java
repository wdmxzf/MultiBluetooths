package com.zhg.bluetoothL.util;


import android.util.Log;

import com.zhg.bluetoothL.BuildConfig;


public class ZHGLog {
    private static boolean isLog = BuildConfig.LOG;

    public static void i(String tag, String message){
        if (isLog) Log.i(tag,message);
    }

    public static void d(String tag, String message){
        if (isLog) Log.d(tag,message);
    }

    public static void e(String tag, String message){
        if (isLog) Log.e(tag,message);
    }

    public static void w(String tag, String message){
        if (isLog) Log.w(tag,message);
    }

    public static void v(String tag, String message){
        if (isLog) Log.v(tag,message);
    }


}
