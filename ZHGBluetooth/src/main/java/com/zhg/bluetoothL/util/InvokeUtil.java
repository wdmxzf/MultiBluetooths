package com.zhg.bluetoothL.util;

import android.bluetooth.BluetoothDevice;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by zhanghuagang on 2017/11/15.
 */

public class InvokeUtil {

    private final String TAG = "InvokeUtil---";

    private static InvokeUtil instance = null;

    private InvokeUtil() {
    }

    public static InvokeUtil getInstance() {
        synchronized (InvokeUtil.class) {
            if (instance == null) {
                instance = new InvokeUtil();
            }
        }
        return instance;
    }

    public static void destroy() {
        if (null != instance) {
            instance = null;
        }
    }

    public byte[] getCurrentTimeData() {
        byte[] data = new byte[10];
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        data[0] = (byte) year;
        data[1] = (byte) ((year >> 8) & 0xFF);
        data[2] = (byte) (cal.get(Calendar.MONTH) + 1);
        data[3] = (byte) cal.get(Calendar.DAY_OF_MONTH);
        data[4] = (byte) cal.get(Calendar.HOUR_OF_DAY);
        data[5] = (byte) cal.get(Calendar.MINUTE);
        data[6] = (byte) cal.get(Calendar.SECOND);
        data[7] = (byte) ((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1); // Rotate
        data[8] = (byte) (cal.get(Calendar.MILLISECOND) * 256 / 1000); // Fractions256
        data[9] = 0x01; // Adjust Reason: Manual time update

        String date = year + "/" + data[2] + "/" + data[3] + " " +
                String.format(Locale.US, "%1$02d:%2$02d:%3$02d", data[4], data[5], data[6]) +
                " (WeekOfDay:" + data[7] + " Fractions256:" + data[8] + " AdjustReason:" + data[9] + ")";
        StringBuilder sb = new StringBuilder("");
        for (byte b : data) {
            sb.append(String.format(Locale.US, "%02x,", b));
        }
        ZHGLog.i(TAG + 64, "getCurrentTimeData：：：" + Arrays.toString(data));
        return data;
    }

    public boolean createBond(BluetoothDevice device){
        boolean ret = false;
        try{
            ret = (Boolean) invokeMethod(device, "createBond", null, null);
        }catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (!ret) {
            ZHGLog.e(TAG+76,"createBond:\ncreateBond() false");
        }
        return ret;
    }

    public boolean removeBond(BluetoothDevice device){
        boolean ret = false;
        try{
            ret = (Boolean) invokeMethod(device, "removeBond", null, null);
        }catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (!ret) {
            ZHGLog.e(TAG+90,"removeBond:\nremoveBond() false");
        }
        return ret;
    }

    /**
     * 个蓝牙设置pinCode
     * @param device {@link BluetoothDevice}
     * @param pinCode 蓝牙配对的Code
     */
    public boolean setPinCode(BluetoothDevice device, String pinCode) {
        boolean ret = false;
        byte[] pinB = convertPinToBytes(device, pinCode);
//        if (Build.VERSION_CODES.KITKAT > Build.VERSION.SDK_INT){
        Method method = null;
        try {
            method = device.getClass().getMethod("setPin", byte[].class);
           ret = (Boolean) method.invoke(device, new Object[]{pinB});
        } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
//        }else {
//            mBluetoothDevice.setPin(pinB);//方法不调用，BluetoothDevice内的sService 为null
//        }
        ZHGLog.i(TAG+114,"setPinCode:\n setPinCode is "+ret);
        return ret;
    }

    /**
     * 设置蓝牙的passKey
     * @param device {@link BluetoothDevice}
     * @param pinCode 蓝牙配对的Code
     * @return true 设置成功，false 设置失败
     */
    public boolean setPasskey(BluetoothDevice device, String pinCode) {
        boolean ret = false;
        try {
            ByteBuffer converter = ByteBuffer.allocate(4);
            converter.order(ByteOrder.nativeOrder());
            converter.putInt(Integer.parseInt(pinCode));
            byte[] pin = converter.array();
            ret = (Boolean) invokeMethod(
                    invokeMethod(BluetoothDevice.class, "getService", null, null),
                    "setPasskey",
                    new Class<?>[]{BluetoothDevice.class, boolean.class, int.class, byte[].class},
                    new Object[]{device, true, pin.length, pin}
            );
        } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private byte[] convertPinToBytes(Object target, String pinCode) {
        byte[] pinB = null;

        try {
            Method method = target.getClass().getDeclaredMethod("convertPinToBytes", String.class);
            pinB = (byte[]) method.invoke(target, pinCode);
        } catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return pinB;
    }

    public static Object invokeMethod(Object target, String methodName, Class<?>[] parameterClasses, Object[] paramterValues)
            throws IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        Class<?> clazz = target.getClass();
        Method method = clazz.getDeclaredMethod(methodName, parameterClasses);
        return method.invoke(target, paramterValues);
    }

}
