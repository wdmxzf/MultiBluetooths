package com.zhg.bluetoothL.ifc;


import com.zhg.bluetoothL.entity.CompleteModel;

/**
 * 测量完成回调接口
 */
public interface MeasuredCompleteListener {

    /**
     * 测量量完成回调方法
     * @param completeModel 返回高压、低压、心率、时间
     */
    void onMeasureComplete(CompleteModel completeModel);
}
