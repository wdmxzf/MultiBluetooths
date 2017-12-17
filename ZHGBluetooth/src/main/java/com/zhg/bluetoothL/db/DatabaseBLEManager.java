package com.zhg.bluetoothL.db;

import android.content.ContentValues;

import com.zhg.bluetoothL.entity.CompleteModel;
import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;

import org.litepal.crud.DataSupport;

import java.util.List;

public class DatabaseBLEManager {

    private static DatabaseBLEManager instance = null;

    private DatabaseBLEManager() {
    }

    public static DatabaseBLEManager getInstance() {
        synchronized (DatabaseBLEManager.class) {
            if (instance == null) {
                instance = new DatabaseBLEManager();
            }
        }
        return instance;
    }

    public static void destroy() {
        if (null != instance) {
            instance = null;
        }


    }

    /*-数据存储-----------*/
    /*蓝牙设备存储*/

    /**
     * 保存蓝牙设备（单个）
     *
     * @param entity  蓝牙单个设备
     */
    public void saveBLEDeviceEntity(ZHGBLEDeviceEntity entity) {
        entity.save();
    }

    /**
     * 保存 扫描到的所有设备
     *
     * @param list    设备列表
     */
    public void saveBLEDeviceList(List<ZHGBLEDeviceEntity> list) {
        DataSupport.saveAll(list);
    }

    /**
     * 获取所有的蓝牙设备
     *
     * @return List<ZHGBLEDeviceEntity>
     */
    public List<ZHGBLEDeviceEntity> getAllBLEDevice() {
        return DataSupport.findAll(ZHGBLEDeviceEntity.class);
    }

    /**
     * 删除单个蓝牙设备
     */
    public void deleteBLEDeviceEntity(ZHGBLEDeviceEntity entity) {
        entity.delete();
    }

    /**
     * 删除所有蓝牙设备
     *
     */
    public void deleteAllBLEDevice() {
        DataSupport.deleteAll(ZHGBLEDeviceEntity.class);
    }

    /*测量结果存储*/

    /**
     * 保存测量结果
     *
     * @param completeModel 测量数据的结果
     */
    public void saveComplete(CompleteModel completeModel) {
        completeModel.save();
    }

    /**
     * 更新测量数据为已上传
     *
     * @param completeModelList 上传成功的测量数据
     */
    public void updateCompleteToIsUpdate( List<CompleteModel> completeModelList) {
        ContentValues values = new ContentValues();
        values.put("isUpdate", true);
        DataSupport.updateAll(CompleteModel.class, values, "isUpdate = ?", "0");
    }

    /**
     * 获取说有测量数据
     *
     * @return List<CompleteModel>
     */
    public List<CompleteModel> getAllComplete() {
        return DataSupport.findAll(CompleteModel.class);
    }

    /**
     * 获取所有没有上传的测量数据
     *
     * @return List<CompleteModel>
     */
    public List<CompleteModel> getAllCompleteOfNotUpdate() {
        return DataSupport.where("isUpdate = ?", "0").order("measuredTime").find(CompleteModel.class);
    }

    public List<CompleteModel> getAllNotUpdatedForDeviceType(String deviceType) {
        return DataSupport.where("deviceType = ? and isUpdate = ?", deviceType, "0").order("measuredTime").find(CompleteModel.class);
    }

    public List<CompleteModel> getAllCompleteUpdated() {
        return DataSupport.where("isUpdate = ?", "1").order("measuredTime").find(CompleteModel.class);
    }

    public List<CompleteModel> getAllCompleteUpdatedForDeviceType(String deviceType) {
        return DataSupport.where("deviceType = ? and isUpdate = ?", deviceType, "1").order("measuredTime").find(CompleteModel.class);
    }

    /**
     * 根据设备类型获取相应的测量结果数据
     *
     * @param deviceType 蓝牙设备类型
     * @return List<CompleteModel>
     */
    public List<CompleteModel> getAllCompleteOfDeviceType(String deviceType) {
        return DataSupport.where("deviceType = ?", deviceType).find(CompleteModel.class);

    }

    /**
     * 删除一类型的测量结果数据
     *
     * @param deviceType 蓝牙设备类型
     */
    public void deleteCompleteOfDeviceType(String deviceType) {
        DataSupport.deleteAll(CompleteModel.class, "deviceType = ?", deviceType);
    }

    /**
     * 删除所有的测量数据
     *
     */
    public void deleteAllComplete() {
        DataSupport.deleteAll(CompleteModel.class);
    }

}
