package com.zhg.bluetoothL.ifc;

import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;

/**
 * 链接状态回调接口
 */
public interface ConnectListener {
    /**
     * 蓝牙链接状态
     * @param connectModel 蓝牙链接状态
     */
    void onBluetoothConnect(ZHGBLEDeviceEntity connectModel);
}
