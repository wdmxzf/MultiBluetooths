package com.zhg.bluetoothL.entity;

/**
 * 蓝牙信息
 */

public class BluetoothInfoEntity {
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备mac 地址
     */
    private String deviceAddress;
    /**
     * 开始命令
     */
    private String startCMD;
    /**
     * 停止命令
     */
    private String stopCMD;

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


    /**
     * @return 返回可能为 null 或者 ""
     */
    public String getStartCMD() {
        return startCMD;
    }

    public void setStartCMD(String startCMD) {
        this.startCMD = startCMD;
    }

    /**
     * @return 返回可能为 null 或者 ""
     */
    public String getStopCMD() {
        return stopCMD;
    }

    public void setStopCMD(String stopCMD) {
        this.stopCMD = stopCMD;
    }
}
