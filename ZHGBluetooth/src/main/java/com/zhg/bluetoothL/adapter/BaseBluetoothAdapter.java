package com.zhg.bluetoothL.adapter;


import com.zhg.bluetoothL.common.BLECommon;
import com.zhg.bluetoothL.db.DatabaseBLEManager;
import com.zhg.bluetoothL.entity.CompleteModel;
import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;
import com.zhg.bluetoothL.entity.MeasuringModel;
import com.zhg.bluetoothL.ifc.BluetoothErrorListener;
import com.zhg.bluetoothL.ifc.BluetoothInfo;
import com.zhg.bluetoothL.ifc.ConnectListener;
import com.zhg.bluetoothL.ifc.MeasuredCompleteListener;
import com.zhg.bluetoothL.ifc.MeasuringListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseBluetoothAdapter implements BluetoothInfo {

    private List<MeasuringListener> mChangedListenerList;
    private List<MeasuredCompleteListener> mCompleteListenerList;
    private List<BluetoothErrorListener> mErrorListenerList;
    private List<ConnectListener> mConnectListenerList;

    public BaseBluetoothAdapter() {
        mChangedListenerList = new ArrayList<>();
        mCompleteListenerList = new ArrayList<>();
        mErrorListenerList = new ArrayList<>();
        mConnectListenerList = new ArrayList<>();
    }

    protected void fireMeasuringCharacteristicValue(MeasuringModel measuringModel) {
        for (MeasuringListener changedListener : mChangedListenerList) {
            if (null == changedListener) continue;
            changedListener.onCharacteristicChanged(measuringModel);
        }
    }

    protected void fireMeasuredCharacteristicCompleteValue(CompleteModel completeModel) {
        for (MeasuredCompleteListener completeListener : mCompleteListenerList) {
            if (null == completeListener) continue;
            completeListener.onMeasureComplete(completeModel);
        }
        DatabaseBLEManager.getInstance().saveComplete(completeModel);
        BLECommon.getInstance().setIsMeasuringMap(completeModel.getDeviceType(), true);
    }

    protected void fireConnectStatus(ZHGBLEDeviceEntity connectModel) {
        for (ConnectListener connectListener : mConnectListenerList) {
            if (null == connectListener) continue;
            connectListener.onBluetoothConnect(connectModel);
        }
    }

    @Override
    public void addMeasuringListener(MeasuringListener listener) {
        if (mChangedListenerList.contains(listener)) return;
        mChangedListenerList.add(listener);
    }

    @Override
    public void removeMeasuringListener(MeasuringListener listener) {
        if (mChangedListenerList.contains(listener)) mChangedListenerList.remove(listener);
    }

    @Override
    public void addCompleteListener(MeasuredCompleteListener listener) {
        if (mCompleteListenerList.contains(listener)) return;
        mCompleteListenerList.add(listener);
    }

    @Override
    public void removeCompleteListener(MeasuredCompleteListener listener) {
        if (mCompleteListenerList.contains(listener)) mCompleteListenerList.remove(listener);
    }

    @Override
    public void addErrorListener(BluetoothErrorListener listener) {
        if (mErrorListenerList.contains(listener)) return;
        mErrorListenerList.add(listener);
    }

    @Override
    public void removeErrorListener(BluetoothErrorListener listener) {
        if (mErrorListenerList.contains(listener)) mErrorListenerList.remove(listener);
    }

    @Override
    public void addConnectListener(ConnectListener listener) {
        if (mConnectListenerList.contains(listener)) return;
        mConnectListenerList.add(listener);
    }

    @Override
    public void removeConnectListener(ConnectListener listener) {
        if (mConnectListenerList.contains(listener)) mConnectListenerList.remove(listener);
    }

}
