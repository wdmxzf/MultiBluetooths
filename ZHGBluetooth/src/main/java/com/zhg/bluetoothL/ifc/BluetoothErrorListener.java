package com.zhg.bluetoothL.ifc;


import com.zhg.bluetoothL.entity.ErrorModel;

/**
 * 测量错误回调接口
 */
public interface BluetoothErrorListener {
    /**
     * 蓝牙使用过程中发生错误回调该方法进行监听
     * @param errorModel {@link ErrorModel}
     */
    void onMeasureError(ErrorModel errorModel);
}
