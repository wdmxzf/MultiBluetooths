package com.zhg.bluetoothL.entity;

import java.io.Serializable;

/**
 * Created by zhanghuagang on 2017/11/22.
 */

public class ZHGUUIDEntity implements Serializable{

    private static final long serialVersionUID = 1L;
    /**
     * 蓝牙设备服务 uuid
     */
    private String serviceUUID;
    /**
     * 蓝牙服务下 注册通知 uuid
     */
    private String notificationCharacteristic;
    /**
     * 蓝牙服务下 读通知 uuid
     */
    private String readCharacteristic;

    /**
     * 蓝牙服务下 通知通道下 writeDescriptor 所需的 uuid
     */
    private String descriptor;

    public ZHGUUIDEntity(){
    }
    public ZHGUUIDEntity(String serviceUUID, String notificationCharacteristic, String descriptor, String readCharacteristic){
        setServiceUUID(serviceUUID);
        setNotificationCharacteristic(notificationCharacteristic);
        setDescriptor(descriptor);
        setReadCharacteristic(readCharacteristic);
    }

    public String getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public String getNotificationCharacteristic() {
        return notificationCharacteristic;
    }

    public void setNotificationCharacteristic(String notificationCharacteristic) {
        this.notificationCharacteristic = notificationCharacteristic;
    }

    public String getReadCharacteristic() {
        return readCharacteristic;
    }

    public void setReadCharacteristic(String readCharacteristic) {
        this.readCharacteristic = readCharacteristic;
    }

    public String getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(String descriptor) {
        this.descriptor = descriptor;
    }
}
