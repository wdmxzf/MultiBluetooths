package com.zhg.bluetoothL.util;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;


import com.zhg.bluetoothL.entity.ClassNameEntity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;

class XMLAnalysisUtil {

    private String TAG = this.getClass().getName()+">>>";

    private String BLUETOOTH_XML = "bluetooth.xml";
    private String MAPPING = "mapping";
    private String CLASS = "class";

    /**
     * 获取xml文件内的内容
     * @param context Activity.this
     * @return 内容
     * @throws IOException 抛出异常
     */
    private InputStream getBluetoothXMLInputStream(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();
        if (null == assetManager){
            Toast.makeText(context,"程序异常，请重新启动",Toast.LENGTH_SHORT).show();
            throw new NullPointerException("context.getAssets(); == null");
        }
        String[] fileNames = assetManager.list("");
        if (fileNames != null && fileNames.length > 0) {
            for (String fileName : fileNames) {
                if (BLUETOOTH_XML.equalsIgnoreCase(fileName)) {
                    return assetManager.open(fileName, AssetManager.ACCESS_BUFFER);
                }
            }
        }
        Log.e(TAG, BLUETOOTH_XML +"可能不存在或解析失败");
        throw new RuntimeException("没有找到"+BLUETOOTH_XML+"文件");

    }

    /**
     * 从xml中获取信息
     * @param context  Activity.this
     * @return 返回类名称集合
     */
    ClassNameEntity getBluetoothInfo(Context context) throws IOException, XmlPullParserException {
        ClassNameEntity entity = new ClassNameEntity();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser xmlPullParser = factory.newPullParser();
        xmlPullParser.setInput(getBluetoothXMLInputStream(context), "UTF-8");
        int eventType = xmlPullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String nodeName = xmlPullParser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if (MAPPING.equals(nodeName)){
                        String className = xmlPullParser.getAttributeValue("", CLASS);
                        entity.addClassName(className);
                    }
                    break;
                default:
                    break;
            }
            eventType = xmlPullParser.next();
        }
        return entity;
    }

}
