package com.zhg.bluetoothL.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.zhg.bluetoothL.entity.ErrorModel;
import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;
import com.zhg.bluetoothL.ifc.BluetoothErrorListener;

/**
 * Created by zhanghuagang on 2017/11/15.
 */

public class BLeUtil {

    private final String TAG = this.getClass().getSimpleName()+"---";

    private static BLeUtil instance = null;

    private BLeUtil(){
    }

    public static BLeUtil getInstance() {
        synchronized (BLeUtil.class) {
            if (instance == null) {
                instance = new BLeUtil();
            }
        }
        return instance;
    }

    public static void destroy(){
        if (null!=instance) {
            instance = null;
        }
    }

    /**
     * 判断是否有蓝牙权限
     * @param context Activity.this
     * @param adapter BluetoothAdapter
     */
    public void isBLeEnabled(Context context, BluetoothAdapter adapter){
        if (!adapter.isEnabled()) {
            if (!adapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
            }
        }
    }

    /**
     * 把BluetoothDevice 转换成GGBLEDeviceEntity
     * @param deviceType 设备类型
     * @param device {@link BluetoothDevice}
     * @return {@link ZHGBLEDeviceEntity}
     */
    public ZHGBLEDeviceEntity BLEDeviceToGGBLEEntity(String deviceType, BluetoothDevice device) {
        ZHGBLEDeviceEntity entity = new ZHGBLEDeviceEntity();
        entity.setConnectStatus(ZHGBluetoothProfile.STATE_DISCONNECTED);
        entity.setDeviceName(device.getName());
        entity.setDeviceType(deviceType);
        entity.setDeviceAddress(device.getAddress());
        entity.setConnectMessage("");
        return entity;
    }

    /**
     * 监听错误信息
     * @param listener {@link BluetoothErrorListener}
     * @param deviceType 设备类型
     * @param errorCode 错误信息Code {@link ZHGBluetoothProfile}
     * @param errorMessage 错误信息
     */
    public void setError(BluetoothErrorListener listener, String deviceType, int errorCode, String errorMessage){
        ErrorModel errorModel = new ErrorModel();
        errorModel.setDeviceType(deviceType);
        errorModel.setErrorCode(errorCode);
        errorModel.setErrorMessage(errorMessage);
        if (null != listener) {
            listener.onMeasureError(errorModel);
        } else {
            ZHGLog.e(TAG+72, "setError:\nErrorListener is null");
        }
    }

}
