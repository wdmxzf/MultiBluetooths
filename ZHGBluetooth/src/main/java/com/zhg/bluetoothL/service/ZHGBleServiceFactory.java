package com.zhg.bluetoothL.service;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;

import com.zhg.bluetoothL.common.BLECommon;
import com.zhg.bluetoothL.db.DatabaseBLEManager;
import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;
import com.zhg.bluetoothL.ifc.BluetoothErrorListener;
import com.zhg.bluetoothL.ifc.BluetoothInfo;
import com.zhg.bluetoothL.ifc.ScanDeviceResultListener;
import com.zhg.bluetoothL.util.BluetoothFactory;
import com.zhg.bluetoothL.util.BluetoothValueUtil;

import org.litepal.LitePal;

import java.util.List;

/**
 * Created by zhanghuagang on 2017/11/17.
 */

public class ZHGBleServiceFactory {
    private static ZHGBleServiceFactory instance = null;
    private static ZHGBLEService sService = null;

    private ZHGBleServiceFactory() {
    }

    public static ZHGBleServiceFactory getInstance() {
        synchronized (ZHGBleServiceFactory.class) {
            if (instance == null) {
                instance = new ZHGBleServiceFactory();
            }
        }
        return instance;
    }

    public static void destroy() {
        if (null != instance) {
            instance = null;
        }
    }

    /**
     * 自行设置BLEService的 action
     *
     * @param context activity.this | Application.getContext
     * @param action  用户自己在 manifest中 设定的 service 的唯一标识 action
     * @return {@link ZHGBLEService}
     */
    public ZHGBLEService initBLEService(Context context, String action) {
        if (null != action) {
            BLECommon.getInstance().setBLEServiceAction(action);
        }

        if (null == sService) {
            LitePal.initialize(context);
            sService = new ZHGBLEService();
            Intent intent = new Intent(context, sService.getClass());
            intent.setAction(BLECommon.getInstance().getBle_service_action());
            context.startService(intent);
            sService.initBLE(context);
        }
        return sService;
    }

    /**
     * 启动默认的BLE Service 在 manifests 里面注册BLEService的action为 com.zhg.bluetoothL.service.ZHGBLEService
     *
     * @param context activity.this | Application.getContext
     * @return {@link ZHGBLEService}
     */
    public ZHGBLEService initBLEService(Context context) {
        return initBLEService(context, null);
    }

    /**
     * 停止服务
     *
     * @param context activity.this | Application.getContext
     */
    public void stopBLEService(Context context) {
        if (null != sService) {
            getService().removeAll();
            Intent intent = new Intent(context, sService.getClass());
            intent.setAction(BLECommon.getInstance().getBle_service_action());
            context.stopService(intent);
            sService = null;
            destroy();
            DatabaseBLEManager.destroy();
            BLECommon.destroy();
            BluetoothFactory.destroy();
            BluetoothValueUtil.destroy();
        }
    }

    private ZHGBLEService getService() {
        if (null == sService) {
            throw new NullPointerException(this.getClass().getCanonicalName() + " line 71 " + "ZHGBLEService is null");
        }
        return sService;
    }

    /**
     * 设置扫描结果监听
     *
     * @param listener {@link ScanDeviceResultListener}
     */
    public void setScanResultListener(ScanDeviceResultListener listener) {
        getService().setScanDeviceResultListener(listener);
    }

    /**
     * 设置 ZHGBLEService 错误监听
     *
     * @param listener {@link BluetoothErrorListener}
     */
    public void setBLEErrorListener(BluetoothErrorListener listener) {
        getService().setBluetoothErrorListener(listener);
    }

    /**
     * 初始化蓝牙BluetoothAdapter
     *
     * @param context 开启蓝牙 用的上下文
     */
    public void initBluetoothAdapter(Context context) {
        getService().initBLE(context);
    }

    /**
     * 扫描蓝牙
     *
     * @param context activity.this
     */
    public void scanBle(Context context) {
        getService().startScan(context);
    }

    /**
     * 获得 BluetoothDevice 链接蓝牙，并把蓝牙 Mac 地址保存到 SharedPreferences
     * 方法内包含配对方法
     *
     * @param context    activity.this | Application.getContext
     * @param deviceType 设备类型
     * @param device     {@link BluetoothDevice}
     */
    public void connectBluetooth(Context context, String deviceType, BluetoothDevice device) {
        getService().connectBLE(context, deviceType, device);
    }

    /**
     * 获得蓝牙 Mac 地址来链接蓝牙，并把蓝牙 Mac 地址保存到 SharedPreferences
     * 方法内包含配对方法
     *
     * @param context    activity.this | Application.getContext
     * @param deviceType 设备类型
     * @param macAddress 蓝牙 Mac 地址
     */
    public void connectBluetooth(Context context, String deviceType, String macAddress) {
        getService().connectBLE(context, deviceType, macAddress);
    }

    /**
     * 自己绑定配对设备
     *
     * @param device     {@link BluetoothDevice}
     * @param deviceType 设备类型
     */
    public void bondBluetooth(BluetoothDevice device, String deviceType) {
        getService().createBond(device, deviceType);
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        getService().stopScan();
    }

    /**
     * 断开链接
     *
     * @param deviceType 设备类型
     */
    public void disConnect(String deviceType) {
        getService().disConnect(deviceType);
    }

    /**
     * 向蓝牙写入 16 进制 String 类型命令
     *
     * @param deviceType 设备类型
     * @param hex        16 进制字符串
     */
    public void writeCharacteristic(String deviceType, String hex) {
        getService().writeCharacteristic(deviceType, hex);
    }

    /**
     * 向蓝牙写入 byte[]
     *
     * @param deviceType 设备类型
     * @param value      byte[] 类型的命令
     */
    public void writeCharacteristic(String deviceType, byte[] value) {
        getService().writeCharacteristic(deviceType, value);
    }

    /**
     * 设置蓝牙配对的 pinCode
     *
     * @param pinCode 配对码
     */
    public void setPinCode(String pinCode) {
        getService().setPinCode(pinCode);
    }

    /**
     * 获取已经链接的设备
     * @param deviceType 设备类型
     * @return {@link ZHGBLEDeviceEntity}
     */
    public ZHGBLEDeviceEntity getConnectedDevice(String deviceType){
        return getService().getConnectedDevice(deviceType);
    }

    /**
     * 获取所有已链接的设备
     * @return {@link List< ZHGBLEDeviceEntity >}
     */
    public List<ZHGBLEDeviceEntity> getAllConnectedDevice(){
        return getService().getAllConnectedDevice();
    }

    /**
     * 判断蓝牙开始测量，是否已经结束
     * @param deviceType 蓝牙设备类型
     * @return Boolean对象
     */
    public Boolean isMeasuring(String deviceType){
        return BLECommon.getInstance().isMeasuring(deviceType);
    }

    /**
     * 设置蓝牙测量状态 是否是开始点击已测量
     * @param deviceType  蓝牙设备类型
     * @param isMeasuring true 是不在测量页面，但是测量完成，false是没有开始测量，或者测量完成已看过
     */
    public void setIsMeasuring(String deviceType, Boolean isMeasuring){
        BLECommon.getInstance().setIsMeasuringMap(deviceType, isMeasuring);
    }

    /**
     * 获取蓝牙信息详情
     * @param deviceType 设备类型
     * @return {@link BluetoothInfo}
     */
    public BluetoothInfo getBluetoothInfo(String deviceType){
        return getService().getBluetoothInfo(deviceType);
    }

    /**
     * 删除蓝牙信息
     * @param context Activity.this
     * @param entity {@link ZHGBLEDeviceEntity}
     */
    public void deleteBLE(Context context, ZHGBLEDeviceEntity entity){
        DatabaseBLEManager.getInstance().deleteBLEDeviceEntity(entity);
        getService().removeMacAddress(context,entity);
    }
}
