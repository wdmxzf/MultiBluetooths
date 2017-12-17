package com.zhg.bluetoothL.entity;

import com.zhg.bluetoothL.util.ZHGBluetoothProfile;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

public class ZHGBLEDeviceEntity extends DataSupport implements Serializable{

    private static final long serialVersionUID = 1L;

    /**
     *设备类型，界面更新时用来区分设备返回的数据
     */
    private String deviceType;
    /**
     * 蓝牙设备连接状态
     */
    private int connectStatus;
    /**
     * 蓝牙设备连接状态说明
     */
    private String connectMessage;

    /**
     * 蓝牙设备名字
     */
    private String deviceName;
    /**
     * 蓝牙设备MAC地址
     */
    private String deviceAddress;

    public int getConnectStatus() {
        return connectStatus;
    }

    /**
     * @param connectStatus
     * or{@link ZHGBluetoothProfile#STATE_DISCONNECTED}
     * or{@link ZHGBluetoothProfile#STATE_CONNECTING}
     * or{@link ZHGBluetoothProfile#STATE_CONNECTED}
     * or{@link ZHGBluetoothProfile#STATE_DISCONNECTING}
     * or{@link ZHGBluetoothProfile#STATE_DISCOVERY_FAIL}
     */
    public void setConnectStatus(int connectStatus) {
        this.connectStatus = connectStatus;
    }

    public String getConnectMessage() {
        return connectMessage;
    }

    public void setConnectMessage(String connectMessage) {
        this.connectMessage = connectMessage;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }
}
