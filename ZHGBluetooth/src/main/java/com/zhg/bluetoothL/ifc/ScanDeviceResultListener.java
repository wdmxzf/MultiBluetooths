package com.zhg.bluetoothL.ifc;

import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;

/**
 * 蓝牙扫描结果监听
 */
public interface ScanDeviceResultListener {
    /**
     * 蓝牙扫描结果
     * @param entity 蓝牙设备监听
     */
    void onScanResult(ZHGBLEDeviceEntity entity);

}
