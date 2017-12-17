package com.zhg.bluetoothL.util;


import android.content.Context;

import com.zhg.bluetoothL.entity.ClassNameEntity;
import com.zhg.bluetoothL.ifc.BluetoothInfo;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BluetoothFactory {

    private static BluetoothFactory instance = null;
    private static List<BluetoothInfo> sBluetoothInfoList = null;

    private BluetoothFactory() {
    }

    public static BluetoothFactory getInstance() {
        synchronized (BluetoothFactory.class) {
            if (instance == null) {
                instance = new BluetoothFactory();
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
     * 从Xml文件里获取BluetoothInfo集合
     *
     * @param context Activity.this
     * @return 蓝牙信息集合
     */
    public List<BluetoothInfo> createBluetoothInfo(Context context) {

        if (null != sBluetoothInfoList) {
            return sBluetoothInfoList;
        }

        sBluetoothInfoList = new ArrayList<>();
        ClassNameEntity bluetoothEntity = new ClassNameEntity();
        XMLAnalysisUtil analysisUtil = new XMLAnalysisUtil();
        try {
            bluetoothEntity = analysisUtil.getBluetoothInfo(context);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }

        for (String className : bluetoothEntity.getClassNameList()) {
            Class clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            BluetoothInfo bluetoothInterface = null;
            assert clazz != null;
            try {
                bluetoothInterface = (BluetoothInfo) clazz.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (!sBluetoothInfoList.contains(bluetoothInterface)) {
                sBluetoothInfoList.add(bluetoothInterface);
            }

        }

        return sBluetoothInfoList;
    }
}
