package com.zhg.bluetoothL.common;


import java.util.HashMap;
import java.util.Map;

public class BLECommon {

    private String ble_service_action = "com.zhg.bluetoothL.service.ZHGBLEService";
    private String ble_db_name = "GG_BLE_DB_NAME";
    private int ble_db_version = 1;

    /**
     * true 是已开始测量了
     */
    private Map<String,Boolean> isMeasuringMap = new HashMap<>();

    private static BLECommon instance = null;

    private BLECommon() {
    }

    public static BLECommon getInstance() {
        synchronized (BLECommon.class) {
            if (instance == null) {
                instance = new BLECommon();
            }
        }
        return instance;
    }

    public static void destroy() {
        if (null != instance) {
            instance = null;
        }
    }

    public void setBLEServiceAction(String action) {
        if (null != action && "".equals(action)) {
            this.ble_service_action = action;
        }
    }

    public String getBle_service_action() {
        return ble_service_action;
    }

    public String getBle_db_name() {
        return ble_db_name;
    }

    public int getBle_db_version() {
        return ble_db_version;
    }

    public void setIsMeasuringMap(String deviceType, Boolean isMeasuring){
        isMeasuringMap.put(deviceType, isMeasuring);
    }

    public Boolean isMeasuring(String deviceType){
        if (null == isMeasuringMap || null == isMeasuringMap.get(deviceType)) return false;
        return isMeasuringMap.get(deviceType);
    }
}
