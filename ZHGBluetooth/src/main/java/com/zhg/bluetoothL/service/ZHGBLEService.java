package com.zhg.bluetoothL.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;
import com.zhg.bluetoothL.entity.ZHGUUIDEntity;
import com.zhg.bluetoothL.ifc.BluetoothErrorListener;
import com.zhg.bluetoothL.ifc.BluetoothInfo;
import com.zhg.bluetoothL.ifc.ScanDeviceResultListener;
import com.zhg.bluetoothL.util.BLeUtil;
import com.zhg.bluetoothL.util.BluetoothFactory;
import com.zhg.bluetoothL.util.BluetoothValueUtil;
import com.zhg.bluetoothL.util.ZHGLog;
import com.zhg.bluetoothL.util.ZHGBluetoothProfile;
import com.zhg.bluetoothL.util.InvokeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zhanghuagang on 2017/11/15.
 */

public class ZHGBLEService extends Service {

    private String TAG = this.getClass().getSimpleName() + "---";

    private final String BLUETOOTH_MAC_TABLE = "bleMacTable";

    /**
     * 扫描方法类型
     */
    private int scanType = 0;
    /**
     * android系统小于5.0时，蓝牙扫描结果处理
     */
    private final int SCAN_RESULT_TYPE_ONE = 0x01;
    /**
     * android系统大于5.0时，蓝牙扫描结果处理
     */
    private final int SCAN_RESULT_TYPE_TWO = 0x02;
    /**
     * 当android系统扫描大于5.0时，扫描失败，使用通知方式扫描结果处理
     */
    private final int SCAN_RESULT_TYPE_THREE = 0x03;

    private final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
    private final int PAIRING_VARIANT_PIN_16_DIGITS = 7;//BluetoothDevice @hide
    private final int PAIRING_VARIANT_PASSKEY = 1;//BluetoothDevice @hide

    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothInfo> mBluetoothInfoList;//蓝牙详情监听
    private List<String> deviceNameList = new ArrayList<>();//防止蓝牙重复添加
    private List<String> mAddressList = new ArrayList<>();//链接过的蓝牙 Mac 地址
    private List<BluetoothGattDescriptor> mDescriptorList = new ArrayList<>();// 写入writeDescriptor方法是异步，用它进行顺序write
    private List<BluetoothGattCharacteristic> mReadCharacteristic = new ArrayList<>();// readCharacteristic方法是异步调用，用它进行顺序write
    private ScanCallback mScanCallback;
    private BluetoothErrorListener mErrorListener;
    private ScanDeviceResultListener mResultListener;
    private String bondDeviceType = "";
    private String bondPinCode = "";

    private Map<String, BluetoothInfo> mInfoMap = new HashMap<>();//根据deviceType 存储蓝牙信息
    private Map<String, BluetoothGatt> mGattMap = new HashMap<>();//根据deviceType 存储蓝牙的BluetoothGatt
    private Map<BluetoothGatt, String> mTypeMap = new HashMap<>();//根据BluetoothGatt 存储deviceType
    private Map<String, BluetoothGattCharacteristic> mCharacterMap = new HashMap<>();//根据deviceType 存储写入命令的characteristic
    private Map<String, ZHGBLEDeviceEntity> mConnectedDeviceMap = new HashMap<>();//根绝deviceType，存储已链接的设备

    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    ZHGLog.i(TAG + 100, "BroadcastReceiver:\n" + "action 1 is ACTION_STATE_CHANGED");
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    ZHGLog.i(TAG + 103, "BroadcastReceiver:\n" + "action 2 is ACTION_ACL_CONNECTED");
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    ZHGLog.i(TAG + 106, "BroadcastReceiver:\n" + "action 3 is ACTION_ACL_DISCONNECTED");
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    ZHGLog.i(TAG + 109, "BroadcastReceiver:\n" + "action 4 is ACTION_BOND_STATE_CHANGED");
                    sendBondStateChangedMessage(intent);
                    break;
                case ACTION_PAIRING_REQUEST:
                    ZHGLog.i(TAG + 113, "BroadcastReceiver:\n" + "action 5 is PAIRING_REQUEST");
                    if (null != bondPinCode && !"".equals(bondPinCode)) {
                        sendParingPinKey(intent, bondPinCode);
                    }
                    break;
                default:
                    ZHGLog.e(TAG + 119, "BroadcastReceiver:\n action is" + action);
                    break;
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAll();
        ZHGLog.i(TAG, "destroy");
        unRegisterReseiver();
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 初始化蓝牙Adapter，判断是否打开蓝牙
     *
     * @param context Activity.this
     */
    void initBLE(Context context) {
        mBluetoothInfoList = BluetoothFactory.getInstance().createBluetoothInfo(context);
        setAddressList(context, mBluetoothInfoList);
        if (null != mBluetoothAdapter) {
            return;
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        if (null != manager) {
            mBluetoothAdapter = manager.getAdapter();
        }
        if (null == mBluetoothAdapter) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (null == mBluetoothAdapter) {
            throw new NullPointerException(TAG + 157 + "initBLE:\n BluetoothAdapter is null");
        }
        registerReceiver(context);
        startScan(context);
    }

    /**
     * 扫描设备结果监听
     *
     * @param listener {@link ScanDeviceResultListener}
     */
    void setScanDeviceResultListener(ScanDeviceResultListener listener) {
        this.mResultListener = listener;
    }

    /**
     * 在对蓝牙一些操作或者使用过程中错误的监听
     *
     * @param listener {@link BluetoothErrorListener}
     */
    void setBluetoothErrorListener(BluetoothErrorListener listener) {
        this.mErrorListener = listener;
    }

    /**
     * 开始扫描
     */
    void startScan(Context context) {
        if (mBluetoothAdapter == null) {
            ZHGLog.e(TAG + 193, "startScan:\n BluetoothAdapter is null");
            return;
        }
        BLeUtil.getInstance().isBLeEnabled(context, mBluetoothAdapter);
        if (!mBluetoothAdapter.isEnabled()){
            return;
        }
        stopScan();
        if (mBluetoothAdapter.isEnabled()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
            /*需要sdk版本在21以上才可以定义使用*/
                mScanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            scanType = SCAN_RESULT_TYPE_TWO;
                            scanResult(result.getDevice(), SCAN_RESULT_TYPE_TWO);
                        }
                    }

                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        super.onBatchScanResults(results);
                        ZHGLog.i(TAG+219, "onBatchScanResults is " + results.toString());
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        ZHGLog.e(TAG+224, "Scan error");
                        mBluetoothAdapter.startDiscovery();
                    }
                };
                mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);

            }
        }
    }

    /**
     * 使用 BluetoothDevice 链接设备 并保存mac地址
     *
     * @param context    Activity.this
     * @param deviceType 设备类型
     * @param device     {@link BluetoothDevice}
     */
    void connectBLE(Context context, String deviceType, BluetoothDevice device) {
        BluetoothInfo info = mInfoMap.get(deviceType);
        if (null == info) {
            for (BluetoothInfo bluetoothInfo : mBluetoothInfoList) {
                if (bluetoothInfo.getDeviceName().equals(device.getName()) && deviceType.equals(bluetoothInfo.getDeviceType())) {
                    info = bluetoothInfo;
                    mInfoMap.put(deviceType, bluetoothInfo);
                }
            }
        }
        assert info != null;
        boolean isBond = info.isPinCode();
        ZHGLog.i(TAG+253, "connectBLE:\nisBond is "+isBond);
        if (isBond) {
            //需要pinCode
            createBond(device, deviceType);
        } else {
            //不需要pinCode
            connect(device, deviceType);
        }
        saveMacAddress(context, deviceType, device.getAddress());

    }

    /**
     * 根据蓝牙 Mac 地址链接蓝牙，并保存 mac
     *
     * @param context    Activity.this
     * @param deviceType 设备类型
     * @param address    设备的 mac 地址
     */
    void connectBLE(Context context, String deviceType, String address) {
        if (null == mBluetoothAdapter) {
            ZHGLog.e(TAG + 274, "connectBLE:\n BluetoothAdapter is null");
            return;
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (null == device) {
            ZHGLog.e(TAG + 279, "connectBLE:\n BluetoothDevice is null");
        }
        Log.w(TAG+281,"connectBLE:\n device bond is "+device.getBondState());
        connectBLE(context, deviceType, device);
    }

    /**
     * 不保存 Mac 地址链接
     *
     * @param deviceType 设备类型
     * @param device     {@link BluetoothDevice}
     */
    void connectBLE(String deviceType, BluetoothDevice device) {
        connectBLE(null, deviceType, device);
    }

    /**
     * 不保存 Mac 地址链接
     *
     * @param deviceType 设备类型
     * @param macAddress 蓝牙Mac地址
     */
    void connectBLE(String deviceType, String macAddress) {
        connectBLE(null, deviceType, macAddress);
    }

    /**
     * 开始配对绑定
     *
     * @param device     蓝牙设备
     * @param deviceType 设备类型
     */
    void createBond(BluetoothDevice device, String deviceType) {
        this.bondDeviceType = deviceType;
        if (null == device) {
            ZHGLog.e(TAG + 314, "createBond:\n device is null");
            return;
        }
        ZHGLog.w(TAG+317,"createBond:\n bond is "+device.getBondState());

        if (BluetoothDevice.BOND_BONDED == device.getBondState()) {
            connect(device, deviceType);
        } else {
            InvokeUtil.getInstance().createBond(device);
        }
    }


    /**
     * 停止扫描
     */
    void stopScan() {
        switch (scanType) {
            case SCAN_RESULT_TYPE_ONE:
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                break;
            case SCAN_RESULT_TYPE_TWO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
                }
                break;
            case SCAN_RESULT_TYPE_THREE:
                mBluetoothAdapter.cancelDiscovery();
                break;
            default:
                break;
        }
    }

    /**
     * 设置pinCode
     *
     * @param pinCode 配对信息
     */
    void setPinCode(String pinCode) {
        this.bondPinCode = pinCode;
    }

    /**
     * 断开蓝牙链接
     *
     * @param deviceType 蓝牙设备类型
     */
    void disConnect(String deviceType) {
        BluetoothGatt gatt = mGattMap.get(deviceType);
        if (null != gatt) {
            gatt.disconnect();
        } else {
            ZHGLog.e(TAG + 367, "disConnect:\n BluetoothGatt is null");
        }
    }

    /**
     * 向蓝牙写入 16 进制 String 类型命令
     *
     * @param deviceType 设备类型
     * @param hex        16 进制字符串
     */
    void writeCharacteristic(String deviceType, String hex) {
        BluetoothGattCharacteristic characteristic = mCharacterMap.get(deviceType);
        BluetoothGatt gatt = mGattMap.get(deviceType);
        if (null != gatt && null != characteristic) {
            characteristic.setValue(BluetoothValueUtil.getInstance().HexStringToBytes(hex));
            gatt.writeCharacteristic(characteristic);
        }
    }

    /**
     * 向蓝牙写入 byte[]
     *
     * @param deviceType 设备类型
     * @param value      byte[] 类型的命令
     */
    void writeCharacteristic(String deviceType, byte[] value) {
        BluetoothGattCharacteristic characteristic = mCharacterMap.get(deviceType);
        BluetoothGatt gatt = mGattMap.get(deviceType);
        if (null != gatt && null != characteristic) {
            characteristic.setValue(value);
            gatt.writeCharacteristic(characteristic);
        }
    }

    /**
     * 获取单个设备的连接状态
     *
     * @param deviceType 设备类型
     * @return {@link ZHGBLEDeviceEntity}
     */
    ZHGBLEDeviceEntity getConnectedDevice(String deviceType) {
        return mConnectedDeviceMap.get(deviceType);
    }

    /**
     * 获取所有的连接设备
     *
     * @return List<ZHGBLEDeviceEntity>
     */
    List<ZHGBLEDeviceEntity> getAllConnectedDevice() {
        List<ZHGBLEDeviceEntity> connectModelList = new ArrayList<>();
        for (String type : mConnectedDeviceMap.keySet()) {
            connectModelList.add(mConnectedDeviceMap.get(type));
        }
        return connectModelList;
    }

    BluetoothInfo getBluetoothInfo(String deviceType) {
        return mInfoMap.get(deviceType);
    }

    void removeMacAddress(Context context, ZHGBLEDeviceEntity entity) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(BLUETOOTH_MAC_TABLE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(entity.getDeviceType());
        editor.apply();
    }

    /**
     * 清除所有
     */
    void removeAll() {
        mTypeMap.clear();
        mInfoMap.clear();
        mCharacterMap.clear();
        mGattMap.clear();
        mConnectedDeviceMap.clear();
        mAddressList.clear();
        mBluetoothInfoList.clear();
        deviceNameList.clear();
        mDescriptorList.clear();
        mReadCharacteristic.clear();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            scanType = SCAN_RESULT_TYPE_ONE;
            scanResult(device, SCAN_RESULT_TYPE_ONE);
        }
    };

    private void scanResult(BluetoothDevice device, int scanType) {
        if (null == device) {
            ZHGLog.e(TAG + 461, "scanResult:\n device is null");
            return;
        }
        String name = device.getName();
        ZHGLog.i(TAG + 465, "scanResult:\nscanType is " + scanType + "\n device name ==" + name);
        if (null == name) {
            ZHGLog.e(TAG + 467, "scanResult:\n device name is null");
            return;
        }

        for (BluetoothInfo info : mBluetoothInfoList) {
            String deviceName = info.getDeviceName();
            if (!name.equals(deviceName) || deviceNameList.contains(name)) {
                continue;
            }
            if (!deviceNameList.contains(name)) {
                deviceNameList.add(name);
            }
            mInfoMap.put(info.getDeviceType(), info);
            ZHGBLEDeviceEntity entity = BLeUtil.getInstance().BLEDeviceToGGBLEEntity(info.getDeviceType(), device);
            Log.i(TAG+481,"scanResult:\n bluetooth device bond is "+device.getBondState());
            if (null != mResultListener) {
                mResultListener.onScanResult(entity);
            } else {
                BLeUtil.getInstance().setError(mErrorListener, info.getDeviceType(), ZHGBluetoothProfile.ERROR_NULL, "BluetoothErrorListener is null");
            }

            if (null != mAddressList && mAddressList.size() > 0) {
                for (String address : mAddressList) {
                    if (device.getAddress().equals(address)) {
                        ZHGLog.i(TAG+491,"scanResult:\n "+device.getName()+" address is "+ address);
                        connectBLE(null, info.getDeviceType(), device);
                    }
                }
            }
        }
    }

    private void connect(BluetoothDevice device, String deviceType) {

        if (null != mGattMap.get(deviceType)){
            mGattMap.get(deviceType).connect();
            mTypeMap.put(mGattMap.get(deviceType), deviceType);
        }else {

            BluetoothGatt gatt = device.connectGatt(this, false, mCallback);
            if (null != gatt) {
                mGattMap.put(deviceType, gatt);
                mTypeMap.put(gatt, deviceType);
            } else {
                ZHGLog.e(TAG + 511, "connectBLE:\n bluetoothGatt is null");
            }
        }

    }

    private void registerReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        context.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void unRegisterReseiver() {
        if (null != mBroadcastReceiver) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    private void sendBondStateChangedMessage(Intent intent) {
        int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
        switch (bondState) {
            case BluetoothDevice.BOND_NONE:
                ZHGLog.i(TAG + 537, "sendBondStateChangedMessage bondState is EVT_BOND_NONE");
                break;
            case BluetoothDevice.BOND_BONDING:
                ZHGLog.i(TAG + 540, "sendBondStateChangedMessage bondState is BOND_BONDING");
                break;
            case BluetoothDevice.BOND_BONDED:
                ZHGLog.i(TAG + 543, "sendBondStateChangedMessage bondState is BOND_BONDED");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                connect(device, bondDeviceType);
                break;
            default:
                Log.e(TAG + 548, "sendBondStateChangedMessage bondState is EVT_BOND_NONE");
                break;
        }
    }

    private void sendParingPinKey(Intent intent, String pinCode) {
        int variant = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_VARIANT", -1);
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        ZHGLog.i(TAG + 556, "variant is " + String.valueOf(variant));
        ZHGLog.i(TAG+557,"sendParingPinKey:\n pinCode is "+pinCode);
        switch (variant) {
            case 0:
            case PAIRING_VARIANT_PIN_16_DIGITS:
                InvokeUtil.getInstance().setPinCode(device, pinCode);
                break;
            case PAIRING_VARIANT_PASSKEY:
                InvokeUtil.getInstance().setPasskey(device, pinCode);
                break;
            default:
                break;
        }
    }

    private BluetoothGattCallback mCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            ZHGLog.w(TAG + 575, "BluetoothGattCallback:\nonConnectionStateChange:\n newState is " + newState);
            switch (newState) {
                case ZHGBluetoothProfile.STATE_CONNECTED:
                    if (null != gatt) {
                        gatt.discoverServices();
                        stopScan();
                    } else {
                        ZHGLog.e(TAG + 582, "BluetoothGattCallback:\nonConnectionStateChange:\n BluetoothGatt is null");
                    }
                    break;
                case ZHGBluetoothProfile.STATE_DISCONNECTED:
                    ZHGBLEDeviceEntity deviceEntity = new ZHGBLEDeviceEntity();
                    deviceEntity.setConnectMessage("设备断开链接");
                    deviceEntity.setConnectStatus(newState);
                    deviceEntity.setDeviceAddress(gatt.getDevice().getAddress());
                    deviceEntity.setDeviceName(gatt.getDevice().getName());
                    deviceEntity.setDeviceType(mTypeMap.get(gatt));
                    InvokeUtil.getInstance().removeBond(gatt.getDevice());
                    if (mInfoMap.size() > 0 && mInfoMap.get(mTypeMap.get(gatt)) != null) {
                        mInfoMap.get(mTypeMap.get(gatt)).onBluetoothConnectStatus(deviceEntity);
                    }

                    removeConnected(mTypeMap.get(gatt));
                    mTypeMap.remove(gatt);
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            displayGattServices(gatt);
            ZHGLog.w(TAG + 607, "BluetoothGattCallback:\nonServicesDiscovered:\n BluetoothGatt is " + gatt.getServices() + " status is " + status);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            ZHGLog.w(TAG + 613, "BluetoothGattCallback:\nonCharacteristicRead:\n status is " + status + "\n characteristic is " + characteristic.getUuid() + "\n service is " + characteristic.getService().getUuid()
                    + "value is " + Arrays.toString(characteristic.getValue())
            );
            mReadCharacteristic.remove(characteristic);
            if (mReadCharacteristic.size() > 0) {
                gatt.readCharacteristic(mReadCharacteristic.get(mReadCharacteristic.size() - 1));
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            ZHGLog.w(TAG + 625, "BluetoothGattCallback:\nonCharacteristicWrite:\n status is " + status + "\n characteristic is " + characteristic.getUuid() + "\n service is " + characteristic.getService().getUuid()
                    + "value is " + Arrays.toString(characteristic.getValue())
            );
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            ZHGLog.w(TAG + 633, "BluetoothGattCallback:\nonCharacteristicChanged:\n  status is " + "\n characteristic is " + characteristic.getUuid() + "\n service is " + characteristic.getService().getUuid()
                    + "value is " + Arrays.toString(characteristic.getValue())
            );
            mInfoMap.get(mTypeMap.get(gatt)).onReceiveData(gatt, characteristic, mErrorListener);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            ZHGLog.w(TAG + 642, "BluetoothGattCallback:\nonDescriptorRead:\n status is " + status + "\ndescriptor characteristic is " + descriptor.getUuid() + "\n service is " + descriptor.getCharacteristic().getService().getUuid()
                    + "\n value is " + Arrays.toString(descriptor.getValue())
            );
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            ZHGLog.w(TAG + 650, "BluetoothGattCallback:\nonDescriptorWrite:\n status is " + status + "\ndescriptor characteristic is " + descriptor.getUuid() + "\n service is " + descriptor.getCharacteristic().getService().getUuid()
                    + "\n value is " + Arrays.toString(descriptor.getValue())
            );
            mDescriptorList.remove(descriptor);
            if (mDescriptorList.size() > 0) {
                writeDescriptor(gatt, mDescriptorList.get(mDescriptorList.size() - 1));
            }
            if (mDescriptorList.size() == 0){
                if (mReadCharacteristic.size() > 0) {
                    gatt.readCharacteristic(mReadCharacteristic.get(mReadCharacteristic.size() - 1));
                }
            }

        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
            ZHGLog.w(TAG + 668, "BluetoothGattCallback:\nonReliableWriteCompleted:\n status is " + status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            ZHGLog.w(TAG + 674, "BluetoothGattCallback:\nonReadRemoteRssi:\n rssi is " + rssi + "\n status is " + status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            ZHGLog.w(TAG + 680, "BluetoothGattCallback:\nonMtuChanged:\n mtu is " + mtu + "\n status is " + status);
        }
    };

    private void displayGattServices(BluetoothGatt gatt) {
        List<BluetoothGattService> serviceList = gatt.getServices();
        if (null == serviceList || serviceList.size() == 0) {
            ZHGLog.e(TAG + 687, "BluetoothGattServiceList is null or size = 0");
            return;
        }

        BluetoothInfo info = mInfoMap.get(mTypeMap.get(gatt));
        List<ZHGUUIDEntity> gguuidEntityList = info.getUUIDInfo();

        for (BluetoothGattService service : serviceList) {
            ZHGLog.i(TAG + 695, "service uuid is " + service.getUuid().toString());

            if (null != gguuidEntityList && gguuidEntityList.size() > 0){
                notificationServiceNew(gatt, service, gguuidEntityList);
            }else {
                notificationServiceOld(gatt, service);
            }

        }

        ZHGBLEDeviceEntity connectModel = new ZHGBLEDeviceEntity();
        connectModel.setConnectMessage("设备已链接");
        connectModel.setConnectStatus(ZHGBluetoothProfile.STATE_CONNECTED);
        connectModel.setDeviceName(gatt.getDevice().getName());
        connectModel.setDeviceAddress(gatt.getDevice().getAddress());
        connectModel.setDeviceType(mTypeMap.get(gatt));
        mInfoMap.get(mTypeMap.get(gatt)).onBluetoothConnectStatus(connectModel);
        mConnectedDeviceMap.put(mTypeMap.get(gatt), connectModel);
    }

    private void setNotificationEnable(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        setNotificationEnable(gatt, characteristic,"00002902-0000-1000-8000-00805f9b34fb");
    }

    private void setNotificationEnable(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, String descriptorUuid){
        if (null == descriptorUuid || "".equals(descriptorUuid)){
            descriptorUuid = "00002902-0000-1000-8000-00805f9b34fb";
        }
        BluetoothGattDescriptor dp = characteristic.getDescriptor(UUID.fromString(descriptorUuid));
        setDescriptorEnable(gatt, characteristic, dp);
    }

    private void setDescriptorEnable(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor) {
        int properties = characteristic.getProperties();
        byte[] value;
        boolean ret;

        if ((properties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0
                || (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            mCharacterMap.put(mTypeMap.get(gatt), characteristic);
        }

        if (BluetoothGattCharacteristic.PROPERTY_INDICATE == (properties & BluetoothGattCharacteristic.PROPERTY_INDICATE)) {
            value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE;
        } else if (BluetoothGattCharacteristic.PROPERTY_NOTIFY == (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
            value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        } else {
            ZHGLog.e(TAG + 742, "setDescriptorEnable:\n  properties is " + properties);
            return;
        }

        if (null == descriptor){
            ZHGLog.e(TAG+747,"setDescriptorEnable:\n descriptor is null");
            return;
        }

        ret = descriptor.setValue(value);
        if (ret) {
            ZHGLog.i(TAG + 753, "descriptor set value is true");
        } else {
            ZHGLog.e(TAG + 755, "descriptor set value is false");
        }

        ret = gatt.setCharacteristicNotification(characteristic, true);

        if (ret) {
            ZHGLog.i(TAG + 761, "setNotificationEnable:\n setCharacteristicNotification is true");
        } else {
            ZHGLog.e(TAG + 763, "setNotificationEnable:\n setCharacteristicNotification is false\n"
                    + "\n characteristic value is " + Arrays.toString(characteristic.getValue())
                    + "\n uuid is " + characteristic.getUuid()
                    + "\n service is " + characteristic.getService().getUuid()
                    + "\n properties is" + characteristic.getWriteType() + characteristic.getProperties()
                    + "\n type is " + characteristic.getWriteType());
        }
        mDescriptorList.add(descriptor);
        if (1 == mDescriptorList.size()) {
            writeDescriptor(gatt, descriptor);
        }

    }

    private void writeDescriptor(BluetoothGatt gatt, BluetoothGattDescriptor descriptor) {
        boolean ret;
        ret = gatt.writeDescriptor(descriptor);
        if (ret) {
            ZHGLog.i(TAG + 781, "writeDescriptor:\n write descriptor is true"
                    + "\n characteristic is " + descriptor.getCharacteristic().getUuid());
        } else {
            ZHGLog.e(TAG + 784, "writeDescriptor:\n writeDescriptor is failed"
                    + "\n descriptor service is" + descriptor.getCharacteristic().getService().getUuid()
                    + "\n characteristic is " + descriptor.getCharacteristic().getUuid()
                    + "\n value is " + Arrays.toString(descriptor.getValue())
                    + "\n permission is " + descriptor.getPermissions());
        }
    }

    private void readCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        int properties = characteristic.getProperties();
        if ((properties & BluetoothGattCharacteristic.PROPERTY_READ) > 0
                || (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
            mReadCharacteristic.add(characteristic);
        }
    }

    private void saveMacAddress(Context context, String deviceType, String macAddress) {
        if (null == context) {
            ZHGLog.e(TAG + 802, "saveMacAddress:\n context is null");
            return;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(BLUETOOTH_MAC_TABLE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(deviceType, macAddress);
        editor.apply();
    }

    private String getMacAddress(Context context, String deviceType) {
        if (null == context) {
            ZHGLog.e(TAG + 813, "getMacAddress:\n context is null");
            return "";
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences(BLUETOOTH_MAC_TABLE, Context.MODE_PRIVATE);
        return sharedPreferences.getString(deviceType, "");
    }

    private void setAddressList(Context context, List<BluetoothInfo> infoList) {
        for (BluetoothInfo info : infoList) {
            String address = getMacAddress(context, info.getDeviceType());
            if (!"".equals(address) && !mAddressList.contains(address)) {
                mAddressList.add(address);
            }
        }
    }

    private void removeConnected(String deviceType) {
        if (null != mConnectedDeviceMap && mConnectedDeviceMap.size() > 0) {
            mConnectedDeviceMap.remove(deviceType);
        } else {
            ZHGLog.e(TAG + 833, "removeConnected mConnectedDeviceMap is null or size is zero");
        }
    }

    private void notificationServiceNew(BluetoothGatt gatt, BluetoothGattService service, List<ZHGUUIDEntity> gguuidEntityList){
        String serviceUuid = service.getUuid().toString();
        for (ZHGUUIDEntity entity : gguuidEntityList){
            if (null == entity){
                continue;
            }
            String sUuid = entity.getServiceUUID();
            String notiUuid = entity.getNotificationCharacteristic();
            String descriptorUuid = entity.getDescriptor();
            String readUuid = entity.getReadCharacteristic();
            if (null == sUuid || "".equals(sUuid)){
                notificationServiceOld(gatt, service);
                continue;
            }

            if (serviceUuid.equals(sUuid)){
                if (null != notiUuid && !"".equals(notiUuid)) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(notiUuid));
                    setNotificationEnable(gatt, characteristic, descriptorUuid);
                }else {
                    notificationServiceOld(gatt, service);
                }

                if (null != readUuid && !"".equals(readUuid)){
                    BluetoothGattCharacteristic readCharacteristic = service.getCharacteristic(UUID.fromString(readUuid));
                    readCharacteristic(gatt, readCharacteristic);
                }
            }
        }
    }

    private void notificationServiceOld(BluetoothGatt gatt, BluetoothGattService service){
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
            setNotificationEnable(gatt, characteristic);
        }
    }

}
