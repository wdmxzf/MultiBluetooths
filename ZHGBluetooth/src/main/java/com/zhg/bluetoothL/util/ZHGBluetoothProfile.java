package com.zhg.bluetoothL.util;


import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


public interface ZHGBluetoothProfile {

    /**
     * 设备传感器异常
     */
    int ERROR_TRANSDUCER = 0x01;
    /**
     * 简测不到心跳或算不出血压
     */
    int ERROR_MEASURE = 0x02;
    /**
     * 测量结果异常
     */
    int ERROR_RESULT = 0x03;
    /**
     * 测量结果异常或压力超过上限
     */
    int ERROR_RESULT_LIMITED = 0x04;
    /**
     * 腕带过松或漏气
     */
    int ERROR_BLOWING = 0x05;
    /**
     * 电量过低
     */
    int ERROR_ELECTRIC = 0x06;
    /**
     * 腕带过紧或气路堵塞
     */
    int ERROR_TIGHTEN = 0x07;

    /**
     * null 错误
     */
    int ERROR_NULL = 0x08;

    /**
     * 断开链接
     */
    int STATE_DISCONNECTED = 0;
    /**
     *正在链接
     */
    int STATE_CONNECTING = 1;
    /**
     *已链接
     */
    int STATE_CONNECTED = 2;
    /**
     *正在断开
     */
    int STATE_DISCONNECTING =3;
    /**
     *扫描设备失败
     */
    int STATE_DISCOVERY_FAIL = 4;

    /**
     * 没有发现蓝牙服务
     */
    int STATE_DISCOVER_SERVICE_FAIL = 5;


    @IntDef({ZHGBluetoothProfile.ERROR_TRANSDUCER
            , ZHGBluetoothProfile.ERROR_MEASURE
            , ZHGBluetoothProfile.ERROR_RESULT
            , ZHGBluetoothProfile.ERROR_RESULT_LIMITED
            , ZHGBluetoothProfile.ERROR_BLOWING
            , ZHGBluetoothProfile.ERROR_ELECTRIC
            , ZHGBluetoothProfile.ERROR_TIGHTEN
    })
    @Retention(RetentionPolicy.SOURCE)
     @interface Error {}

    @IntDef({ZHGBluetoothProfile.STATE_DISCONNECTED
            , ZHGBluetoothProfile.STATE_CONNECTING
            , ZHGBluetoothProfile.STATE_CONNECTED
            , ZHGBluetoothProfile.STATE_DISCONNECTING
            , ZHGBluetoothProfile.STATE_DISCOVERY_FAIL
            ,STATE_DISCOVER_SERVICE_FAIL

    })
    @Retention(RetentionPolicy.SOURCE)
     @interface ConnectState{}

}
