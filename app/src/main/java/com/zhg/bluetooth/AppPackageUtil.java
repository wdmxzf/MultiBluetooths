package com.zhg.bluetooth;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;

/**
 * Created  zhanghuagang on 2017/3/1.
 */

public class AppPackageUtil {

    private static AppPackageUtil instance = null;

    private AppPackageUtil(){
    }

    public static AppPackageUtil getInstance() {
        synchronized (AppPackageUtil.class) {
            if (instance == null) {
                instance = new AppPackageUtil();
            }
        }
        return instance;
    }

    public static void destroy(){
        if (null!=instance) {
            instance = null;
        }
    }

    public PackageInfo getPackageInfo(){
        PackageManager packageManager = BaseApplication.getInstance().getPackageManager();
        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(BaseApplication.getInstance().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }
    public boolean isLocationOpen(final Context context){
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //gps定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider|| isNetWorkProvider;
    }
}
