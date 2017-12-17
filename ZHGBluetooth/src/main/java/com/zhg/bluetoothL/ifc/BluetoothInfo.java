package com.zhg.bluetoothL.ifc;


import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;
import com.zhg.bluetoothL.entity.ZHGUUIDEntity;

import java.util.List;

/**
 * 蓝牙设备信息回调接口
 */
public interface  BluetoothInfo {

    /**
     * 获取蓝牙设备名称
     * @return String 设备名
     */
    String getDeviceName();

    /**
     * 用来区分蓝牙连接设备类型
     * @return String
     */
    String getDeviceType();

    /**
     * 开始执行命令
     * @return String
     */
    String getStartCMD();

    /**
     * 停止执行命令
     * @return String
     */
    String getStopCMD();

    /**
     * 设置配对需要的 pinCode
     * @return String
     */
    boolean isPinCode();

    /**
     *  设置蓝牙所需 服务，通知通道等 uuid
     * @return {@link List< ZHGUUIDEntity >}
     */
    List<ZHGUUIDEntity> getUUIDInfo();

    /**
     * 蓝牙返回数据
     * @param writeGatt BluetoothGatt
     * @param writeCharacteristic 向蓝牙写入命令的特征值
     */
//    void onReceiveData(BluetoothGatt writeGatt, BluetoothGattCharacteristic writeCharacteristic, byte[] bytes);
    void onReceiveData(BluetoothGatt writeGatt, BluetoothGattCharacteristic writeCharacteristic, BluetoothErrorListener listener);
    /**
     * 蓝牙链接状态
     * @param connectModel 链接状态
     *               BluetoothProfile.STATE_CONNECTED 已链接
     *               BluetoothProfile.STATE_CONNECTING 正在链接
     *               BluetoothProfile.STATE_DISCONNECTING 正在断开
     *               BluetoothProfile.STATE_DISCONNECTED 已断开
     */
    void onBluetoothConnectStatus(ZHGBLEDeviceEntity connectModel);

    /**
     * 注册正在测量中数据监听
     * @param listener 测量中数据回调
     */
    void addMeasuringListener(MeasuringListener listener);

    /**
     * 移除测量中的监听
     * @param listener 测量中数据回调
     */
    void removeMeasuringListener(MeasuringListener listener);

    /**
     * 注册测量完成后的监听
     * @param listener 测量完成后回调
     */
    void addCompleteListener(MeasuredCompleteListener listener);

    /**
     * 移除测量完成后监听
     * @param listener 测量完成后回调
     */
    void removeCompleteListener(MeasuredCompleteListener listener);

    /**
     * 注册测量过程中错误监听
     * @param listener 发生错误时回调
     */
    void addErrorListener(BluetoothErrorListener listener);

    /**
     * 移除错误监听
     * @param listener 发生错误时回调
     */
    void removeErrorListener(BluetoothErrorListener listener);

    /**
     * 注册蓝牙链接状态监听
     * @param listener 蓝牙链接状态发生改变时回调
     */
    void addConnectListener(ConnectListener listener);

    /**
     * 移除链接状态监听
     * @param listener 蓝牙链接状态发生改变时的回调
     */
    void removeConnectListener(ConnectListener listener);

}
