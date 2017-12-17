package com.zhg.bluetoothL.entity;


public class MeasuringModel{

    /**
     * 蓝牙正在执行中的数据
     */
    private int measuringData;
    /**
     * 设备类型，界面更新时用来区分设备返回的数据
     */
    private String deviceType;

    public int getMeasuringData() {
        return measuringData;
    }

    public void setMeasuringData(int measuringData) {
        this.measuringData = measuringData;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
