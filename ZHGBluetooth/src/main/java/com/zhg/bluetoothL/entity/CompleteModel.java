package com.zhg.bluetoothL.entity;


import org.litepal.crud.DataSupport;

public class CompleteModel extends DataSupport{

    /**
     * 设备类型，界面更新时用来区分设备返回的数据
     */
    private String deviceType;

    /**
     * 蓝牙返回的最后结果，复制 int[] 中，
     * 血压计高压、低压、心率一次赋值0..2
     * 厨房秤或者体重称，直接赋值int[0]
     */
    private byte[] measuredData;

    /**
     * 测量时间时间戳
     */
    private String measuredTime;

    private boolean isUpdate;


    public int[] getMeasuredData() {
        if (this.measuredData == null) return new int[]{0};
        int[] a = new int[this.measuredData.length];
        for (int i = 0; i< this.measuredData.length; i++){
            a[i] = this.measuredData[i];
        }
        return a;
    }

    public void setMeasuredData(int[] measuredData) {
        byte[] b = new byte[measuredData.length];
        for (int i=0; i< measuredData.length; i++){
            b[i] = (byte) measuredData[i];
        }
        this.measuredData = b;
    }

    public String getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(String measuredTime) {
        this.measuredTime = measuredTime;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }
}
