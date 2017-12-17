package com.zhg.bluetooth;

import android.app.Application;

import com.zhg.bluetoothL.service.ZHGBleServiceFactory;

/**
 * Created by zhanghuagang on 2017/12/17.
 */

public class BaseApplication extends Application {
    private static BaseApplication instance = null;
    
    public BaseApplication(){
    }
    
    public static BaseApplication getInstance() {
        synchronized (BaseApplication.class) {
            if (instance == null) {
                instance = new BaseApplication();
            }
        }
        return instance;
    }
    
    public static void destroy(){
        if (null!=instance) {
            instance = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ZHGBleServiceFactory.getInstance().initBLEService(this);
    }

    public static BaseApplication getContext() {
        synchronized (BaseApplication.class) {
            if (instance == null) {
                instance = new BaseApplication();
            }
        }
        return instance;
    }
}
