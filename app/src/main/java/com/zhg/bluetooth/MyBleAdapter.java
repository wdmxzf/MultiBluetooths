package com.zhg.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import com.zhg.bluetoothL.adapter.BaseBluetoothAdapter;
import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;
import com.zhg.bluetoothL.entity.ZHGUUIDEntity;
import com.zhg.bluetoothL.ifc.BluetoothErrorListener;
import com.zhg.bluetoothL.util.BluetoothValueUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zhanghuagang on 2017/11/15.
 */

public class MyBleAdapter extends BaseBluetoothAdapter {
    private final String TAG = "MyBleAdapter---";

    @Override
    public String getDeviceName() {
        return "bluetooth name *";
    }

    @Override
    public String getDeviceType() {
        return "自定义 deviceType *";
    }

    @Override
    public String getStartCMD() {
        return null;
    }

    @Override
    public String getStopCMD() {
        return null;
    }

    @Override
    public boolean isPinCode() {
        return true;
    }

    @Override
    public List<ZHGUUIDEntity> getUUIDInfo() {
        List<ZHGUUIDEntity> list = new ArrayList<>();
        //可以天写多个，如果不填，默认为公开UUID 可以自行匹配

        ZHGUUIDEntity entity = new ZHGUUIDEntity(
                "service UUID——1"
                , "notification characteristic uuid "
                , "descriptor uuid and  default is 00002902-0000-1000-8000-00805f9b34fb"
                , null
        );
        ZHGUUIDEntity entity1 = new ZHGUUIDEntity(
                "service UUID——2"
                , "notification characteristic uuid "
                , null
                , null
        );
        ZHGUUIDEntity entity2 = new ZHGUUIDEntity(
                "service UUID——N"
                , "notification characteristic uuid "
                , "00002902-0000-1000-8000-00805f9b34fb"
                , "read characteristic uuid and  default null"
        );
        list.add(entity);
        list.add(entity1);
        list.add(entity2);
        return list;
    }


    @Override
    public void onReceiveData(BluetoothGatt writeGatt, BluetoothGattCharacteristic writeCharacteristic, BluetoothErrorListener listener) {

        // 数据接受方法，在这里进行数据处理，将处理好的数据 调用方法fireMeasuredCharacteristicCompleteValue(completeModel);

        Log.w(TAG + 43, "onCharacteristicChanged status is " + "\n characteristic is " + writeCharacteristic.getUuid() + "\n service is " + writeCharacteristic.getService().getUuid()
                + "value is " + Arrays.toString(BluetoothValueUtil.getInstance().bytesToDemicals(writeCharacteristic.getValue()))
        );
    }

    @Override
    public void onBluetoothConnectStatus(ZHGBLEDeviceEntity connectModel) {
        //连接状态监控
        connectModel.setDeviceType(getDeviceType());
        fireConnectStatus(connectModel);
    }

}
