package com.zhg.bluetoothL.entity;


import com.zhg.bluetoothL.util.ZHGBluetoothProfile;

public class ErrorModel {

    /**
     * 设备类型，界面更新时用来区分设备返回的数据
     */
    private String deviceType;
    /**
     * 错误码Code
     */
    private int errorCode;
    /**
     * 错误信息
     */
    private String errorMessage;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(@ZHGBluetoothProfile.Error int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
