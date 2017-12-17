package com.zhg.bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.zhg.bluetoothL.db.DatabaseBLEManager;
import com.zhg.bluetoothL.entity.ErrorModel;
import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;
import com.zhg.bluetoothL.ifc.BluetoothErrorListener;
import com.zhg.bluetoothL.ifc.BluetoothInfo;
import com.zhg.bluetoothL.ifc.ConnectListener;
import com.zhg.bluetoothL.ifc.ScanDeviceResultListener;
import com.zhg.bluetoothL.service.ZHGBleServiceFactory;
import com.zhg.bluetoothL.util.BluetoothFactory;
import com.zhg.bluetoothL.util.ZHGBluetoothProfile;

import java.util.ArrayList;
import java.util.List;

public class BluetoothListActivity extends AppCompatActivity implements ConnectListener, ScanDeviceResultListener, BluetoothErrorListener ,View.OnClickListener{

    private final String TAG = this.getClass().getSimpleName() + "---";
    private final int PERMISSION_LOCATION = 100;
    private final int H_DISCONNECTED = 0x0;
    private final int H_CONNECTED = 0x01;
    private final int H_SCAN_RESULT = 0x03;
    private final int H_BLE_ERROR = 0x04;
    Button btnScan;
    ListView lvBluetooth;
    Button btnStopScan;
    EditText etPinCode;

    private ZHGBleServiceFactory mFactory;
    private BLEAdapter mAdapter;
//    private List<BLEEntity> mBLEEntityList;
    private List<ZHGBLEDeviceEntity> mDeviceEntities;
    private Context mContext;



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= 23 && !AppPackageUtil.getInstance().isLocationOpen(this)) {
                        Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(enableLocate);
                    }
                } else {
                    Toast.makeText(this, "位置权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseBLEManager.getInstance().saveBLEDeviceList(mDeviceEntities);
        mDeviceEntities.clear();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_list);
        mContext = this;
        requestPermission();
        initListener();
        initBluetooth();
        initUI();
        initData();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
        }
    }

    private void initUI() {
        btnScan = findViewById(R.id.btn_scan);
        lvBluetooth=findViewById(R.id.lv_bluetooth);
        btnStopScan = findViewById(R.id.btn_stopScan);
        etPinCode = findViewById(R.id.et_pinCode);
        btnScan.setOnClickListener(this);
        btnStopScan.setOnClickListener(this);

        lvBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ZHGBLEDeviceEntity bleEntity;
                bleEntity = mDeviceEntities.get(position);
                mFactory.setPinCode(etPinCode.getText().toString());
                mFactory.connectBluetooth(mContext, bleEntity.getDeviceType(), bleEntity.getDeviceAddress());
                mDeviceEntities.get(position).setConnectStatus(ZHGBluetoothProfile.STATE_CONNECTING);
                mAdapter.notifyDataSetChanged();
            }
        });
        lvBluetooth.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                ZHGBLEDeviceEntity bleEntity;
                bleEntity = mDeviceEntities.get(position);
//                mFactory.connectBluetooth(mContext, bleEntity.getDeviceType(), bleEntity.getDeviceAddress());
                mFactory.disConnect(bleEntity.getDeviceType());
                /*删除蓝牙信息*/
//                BLEStorageUtil.getInstance().deleteBLE(bleEntity);
//                mFactory.deleteBle(BaseApplication.getContext(), bleEntity.getDeviceType(), bleEntity.getDeviceAddress());
                mFactory.deleteBLE(BaseApplication.getContext(),bleEntity);
                mDeviceEntities.remove(bleEntity);

                mFactory.disConnect(bleEntity.getDeviceType());
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });

    }

    private void initData() {
        mDeviceEntities = new ArrayList<>();
//        mBLEEntityList = BLEStorageUtil.getInstance().getALlBLE();
        mDeviceEntities = DatabaseBLEManager.getInstance().getAllBLEDevice();
        addBLE();
        mAdapter = new BLEAdapter(this, mDeviceEntities);
        lvBluetooth.setAdapter(mAdapter);
    }

    private void addBLE(){

        Log.i(TAG,"BLEList size is "+mDeviceEntities.size());

        List<ZHGBLEDeviceEntity> bleDeviceEntities = mFactory.getAllConnectedDevice();
        List<String> connectedAddressList = new ArrayList<>();
        List<String> DBAddressList = new ArrayList<>();
        /*收集连接的蓝牙Mac地址*/
        if (null != bleDeviceEntities && bleDeviceEntities.size() > 0){
            for (ZHGBLEDeviceEntity entity : bleDeviceEntities){
                if (!connectedAddressList.contains(entity.getDeviceAddress())){
                    connectedAddressList.add(entity.getDeviceAddress());
                }
            }
        }
        Log.i(TAG,"connectedAddressList size is "+mDeviceEntities.size());

        /*收集数据存储的蓝牙的mac地址*/
        if (null != mDeviceEntities && mDeviceEntities.size()>0){
            for (int j =0 ; j< mDeviceEntities.size();j++){
                mDeviceEntities.get(j).setConnectStatus(ZHGBluetoothProfile.STATE_DISCONNECTED);
                if (!DBAddressList.contains(mDeviceEntities.get(j).getDeviceAddress())){
                    DBAddressList.add(mDeviceEntities.get(j).getDeviceAddress());
                }
            }
        }
        Log.i(TAG,"DBAddressList size is "+mDeviceEntities.size());

        /*判断数据存储是否包含连接的蓝牙设备*/
        for (int i= 0; i< connectedAddressList.size(); i++){
            if (!DBAddressList.contains(connectedAddressList.get(i))){
                assert bleDeviceEntities != null;
                mDeviceEntities.add(bleDeviceEntities.get(i));
            }else {
                for (int j =0 ; j< mDeviceEntities.size();j++){
                    if (mDeviceEntities.get(j).getDeviceAddress().equals(connectedAddressList.get(i))){
                        mDeviceEntities.get(j).setConnectStatus(ZHGBluetoothProfile.STATE_CONNECTED);
                    }
                }
            }
        }

    }

    private void initBluetooth() {
        mFactory = ZHGBleServiceFactory.getInstance();
        mFactory.setScanResultListener(this);
        mFactory.setBLEErrorListener(this);
        mFactory.setPinCode("166909");
    }

    private void initListener() {
        List<BluetoothInfo> bluetoothInfoList = BluetoothFactory.getInstance().createBluetoothInfo(this);
        for (BluetoothInfo info : bluetoothInfoList) {
            info.addConnectListener(this);
        }
    }

    private void scanResult(ZHGBLEDeviceEntity entity){
                    /*mBLEList不包含的蓝牙添加*/
        List<String> addressList = new ArrayList<>();
        for (int i = 0; i<mDeviceEntities.size();i++){
            addressList.add(mDeviceEntities.get(i).getDeviceAddress());
        }
        if (!addressList.contains(entity.getDeviceAddress())){
            mDeviceEntities.add(entity);
        }
        mAdapter.notifyDataSetChanged();
    }

    private Message mMessage = new Message();
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case H_DISCONNECTED:
                    ZHGBLEDeviceEntity model = (ZHGBLEDeviceEntity) msg.obj;
                    for (int i = 0; i < mDeviceEntities.size(); i++) {
                        if (model.getDeviceAddress().equals(mDeviceEntities.get(i).getDeviceAddress())) {
                            mDeviceEntities.get(i).setConnectStatus(model.getConnectStatus());
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                    break;
                case H_CONNECTED:
                    ZHGBLEDeviceEntity connectModel = (ZHGBLEDeviceEntity) msg.obj;
                    for (int i = 0; i < mDeviceEntities.size(); i++) {
                        if (connectModel.getDeviceAddress().equals(mDeviceEntities.get(i).getDeviceAddress())) {
                            mDeviceEntities.get(i).setConnectStatus(connectModel.getConnectStatus());
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                    break;
                case H_SCAN_RESULT:
                    ZHGBLEDeviceEntity entity = (ZHGBLEDeviceEntity) msg.obj;
                    scanResult(entity);
                    break;
                case H_BLE_ERROR:
                    ErrorModel errorModel = (ErrorModel) msg.obj;
                    Log.e(TAG, errorModel == null ? "error" : errorModel.getErrorMessage());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onBluetoothConnect(ZHGBLEDeviceEntity connectModel) {
        switch (connectModel.getConnectStatus()) {
            case ZHGBluetoothProfile.STATE_CONNECTED:
                Log.i(TAG, "device is connected " + connectModel.getDeviceType());
                mMessage = mHandler.obtainMessage();
                mMessage.what = H_CONNECTED;
                mMessage.obj = connectModel;
                mHandler.sendMessage(mMessage);
                break;
            case ZHGBluetoothProfile.STATE_DISCONNECTED:
                Log.e(TAG, "device is disConnect");
                mMessage = mHandler.obtainMessage();
                mMessage.what = H_DISCONNECTED;
                mMessage.obj = connectModel;
                mHandler.sendMessage(mMessage);
                break;
            default:
                break;
        }
    }

    @Override
    public void onScanResult(ZHGBLEDeviceEntity entity) {
        Log.i(TAG,"onScanResult");
        mMessage = mHandler.obtainMessage();
        mMessage.what = H_SCAN_RESULT;
        mMessage.obj = entity;
        mHandler.sendMessage(mMessage);
    }

    @Override
    public void onMeasureError(ErrorModel errorModel) {
        mMessage = mHandler.obtainMessage();
        mMessage.what = H_BLE_ERROR;
        mMessage.obj = errorModel;
        mHandler.sendMessage(mMessage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                mFactory.scanBle(this);
                break;
            case R.id.btn_stopScan:
                mFactory.stopScan();
                break;
            default:
                break;
        }
    }
}
