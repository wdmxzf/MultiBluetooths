# Bluetooth
蓝牙多设备连接，支持公开协议和私有协议

## 工具使用

把工程引入项目：
```groovy
compile project(':ZHGBluetooth')
```
### 在AndroidManifest.xml文件中声明BluetoothService
* 使用默认 action
```xml
        <service android:name="com.zhg.bluetoothL.service.ZHGBLEService"
                 android:enabled="true"
                 android:exported="false">
            <intent-filter>
                <action android:name="com.zhg.bluetoothL.service.ZHGBLEService"/>
            </intent-filter>
        </service>
```

* 使用自定义 action (xxxxxxxxx)
```xml
<service android:name="com.zhg.bluetoothL.service.ZHGBLEService"
                 android:enabled="true"
                 android:exported="false">
            <intent-filter>
                <action android:name="xxxxxxxxx"/>
            </intent-filter>
        </service>
```

### 创建类 继承 BaseBluetoothAdapter 并实现里面的方法

```java
public class MyBleAdapter extends BaseBluetoothAdapter {

    private CompleteModel completeModel = new CompleteModel();
    private ErrorModel errorModel = new ErrorModel();
    private MeasuringModel measuringModel = new MeasuringModel();
    @Override
    public String getDeviceName() {
        return "蓝牙设备名称";
    }

    @Override
    public String getStartCMD() {
        return "开始命令";
    }

    @Override
    public String getStopCMD() {
        return "停止命令";
    }

    @Override
    public String getDeviceType() {
        return "蓝牙设备类型";//随意设置，一样功能最好设置相同的type
    }

    @Override
    public void onReceiveData(BluetoothGatt writeGatt, BluetoothGattCharacteristic writeCharacteristic, byte[] bytes, BluetoothErrorListener listener) {
        //对蓝牙返回的数据进行处理
...
        fireMeasuringCharacteristicValue(measuringModel);//进行数据处理，把测量中的数据赋值给
        fireMeasuredCharacteristicCompleteValue(completeModel); //把测量结束的值赋值给
        fireMeasuredErrorCode(errorModel);//把测量错误的值赋值给
    }

    @Override
    public void onBluetoothConnectStatus(GGBLEDeviceEntity connectModel) {
        // 蓝牙连接状态监听
        connectModel.setDeviceType(getDeviceType());
        fireConnectStatus(connectModel);
    }

}
```
### 实现完adapter后，在assets文件下创建一个叫bluetooth的xml（bluetooth.xml）

```xml
<bluetooth>

    <list>
        <mapping class = "com.zhg.bluetooth.MyBleAdapter"/>
    </list>

</bluetooth>
```

### 在 assets 文件夹下创建 litepal.xml 文件，内容如下：
```xml
<litepal>
    <dbname value="ble"/>
    <version value="2"/>
    <list>
        <mapping class="com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity"/>
        <mapping class="com.zhg.bluetoothL.entity.CompleteModel"/>
    </list>
</litepal>
```

### 在 LaucherActivity 中添加：(因为需要权限判断)
`ServiceFactory.getInstance().initBLEService(this);`

或者 
`ServiceFactory.getInstance().initBLEService(this, xxxxxxxxx);`
xxxxxxxxx 为自定义的 action

对蓝牙和数据库进行初始化。

### 提供接口
|接口名称|功能|
|---|---|
| BluetoothErrorListener |蓝牙连接，测量等错误回调|
| ConnectListener |蓝牙连接状态回调|
| MeasuredCompleteListener |测量结果回调|
| MeasuringListener |测量中回调|
| ScanDeviceResultListener |扫描到的设备回调|

### 在 Activity 中使用

* 在 Activity 中实现以上任意接口

`implements ConnectListener, ScanDeviceResultListener, BluetoothErrorListener `
* 获取 ServiceFactory ：`mFactory = ServiceFactory.getInstance();`
* 设置回调监听

```java

mFactory.setScanBluetoothResultListener(this);
mFactory.setErrorListener(this);
for (BluetoothInfo bluetoothInfoInterface : bluetoothInfoList) {
            bluetoothInfoInterface.addMeasuringListener(this);
            bluetoothInfoInterface.addCompleteListener(this);
            bluetoothInfoInterface.addConnectListener(this);
}
```
* 在 Activity 页面结束时调用

```java
private void removerListener() {
        for (BluetoothInfo bluetoothInfoInterface : bluetoothInfoList) {
            bluetoothInfoInterface.removeMeasuringListener(this);
            bluetoothInfoInterface.removeCompleteListener(this);
            bluetoothInfoInterface.removeConnectListener(this);
        }
    }
```
* 对蓝牙进行操作类 GGBleServiceFactory 提供的方法为：

```java
public class GGBleServiceFactory {
    private static GGBleServiceFactory instance = null;
    private static GGBLEService sService = null;

    private GGBleServiceFactory() {
    }

    public static GGBleServiceFactory getInstance() {
        synchronized (GGBleServiceFactory.class) {
            if (instance == null) {
                instance = new GGBleServiceFactory();
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
     * 自行设置BLEService的 action
     *
     * @param context activity.this | Application.getContext
     * @param action  用户自己在 manifest中 设定的 service 的唯一标识 action
     * @return {@link GGBLEService}
     */
    public GGBLEService initBLEService(Context context, String action) {
        return sService;
    }

    /**
     * 启动默认的BLE Service 在 manifests 里面注册BLEService的action为 com.GeGee.library.bluetooth.GGBluetoothService
     *
     * @param context activity.this | Application.getContext
     * @return {@link GGBLEService}
     */
    public GGBLEService initBLEService(Context context) {
    }

    /**
     * 停止服务
     *
     * @param context activity.this | Application.getContext
     */
    public void stopBLEService(Context context) {
    }

    private GGBLEService getService() {
        return sService;
    }

    /**
     * 设置扫描结果监听
     *
     * @param listener {@link ScanDeviceResultListener}
     */
    public void setScanResultListener(ScanDeviceResultListener listener) {
    }

    /**
     * 设置 GGBLEService 错误监听
     *
     * @param listener {@link BluetoothErrorListener}
     */
    public void setBLEErrorListener(BluetoothErrorListener listener) {
    }

    /**
     * 初始化蓝牙BluetoothAdapter
     *
     * @param context 开启蓝牙 用的上下文
     */
    public void initBluetoothAdapter(Context context) {
    }

    /**
     * 扫描蓝牙
     *
     * @param context activity.this
     */
    public void scanBle(Context context) {
    }

    /**
     * 获得 BluetoothDevice 链接蓝牙，并把蓝牙 Mac 地址保存到 SharedPreferences
     * 方法内包含配对方法
     *
     * @param context    activity.this | Application.getContext
     * @param deviceType 设备类型
     * @param device     {@link BluetoothDevice}
     */
    public void connectBluetooth(Context context, String deviceType, BluetoothDevice device) {
    }

    /**
     * 获得蓝牙 Mac 地址来链接蓝牙，并把蓝牙 Mac 地址保存到 SharedPreferences
     * 方法内包含配对方法
     *
     * @param context    activity.this | Application.getContext
     * @param deviceType 设备类型
     * @param macAddress 蓝牙 Mac 地址
     */
    public void connectBluetooth(Context context, String deviceType, String macAddress) {
    }

    /**
     * 自己绑定配对设备
     *
     * @param device     {@link BluetoothDevice}
     * @param deviceType 设备类型
     */
    public void bondBluetooth(BluetoothDevice device, String deviceType) {
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
    }

    /**
     * 断开链接
     *
     * @param deviceType 设备类型
     */
    public void disConnect(String deviceType) {
    }

    /**
     * 向蓝牙写入 16 进制 String 类型命令
     *
     * @param deviceType 设备类型
     * @param hex        16 进制字符串
     */
    public void writeCharacteristic(String deviceType, String hex) {
    }

    /**
     * 向蓝牙写入 byte[]
     *
     * @param deviceType 设备类型
     * @param value      byte[] 类型的命令
     */
    public void writeCharacteristic(String deviceType, byte[] value) {
    }

    /**
     * 设置蓝牙配对的 pinCode
     *
     * @param pinCode 配对码
     */
    public void setPinCode(String pinCode) {
    }

    /**
     * 获取已经链接的设备
     * @param deviceType 设备类型
     * @return {@link GGBLEDeviceEntity}
     */
    public GGBLEDeviceEntity getConnectedDevice(String deviceType){
    }

    /**
     * 获取所有已链接的设备
     * @return {@link List<GGBLEDeviceEntity>}
     */
    public List<GGBLEDeviceEntity> getAllConnectedDevice(){
    }

    /**
     * 判断蓝牙开始测量，是否已经结束
     * @param deviceType 蓝牙设备类型
     * @return Boolean对象
     */
    public Boolean isMeasuring(String deviceType){
    }

    /**
     * 设置蓝牙测量状态 是否是开始点击已测量
     * @param deviceType  蓝牙设备类型
     * @param isMeasuring true 是不在测量页面，但是测量完成，false是没有开始测量，或者测量完成已看过
     */
    public void setIsMeasuring(String deviceType, Boolean isMeasuring){
    }

    /**
     * 获取蓝牙信息详情
     * @param deviceType 设备类型
     * @return {@link BluetoothInfo}
     */
    public BluetoothInfo getBluetoothInfo(String deviceType){
    }

    /**
     * 删除蓝牙信息
     * @param context Activity.this
     * @param entity {@link GGBLEDeviceEntity}
     */
    public void deleteBLE(Context context, GGBLEDeviceEntity entity){
    }
}
```

* 对数据进行存储类 DatabaseBLEManager 提供的方法：
单例模式：DatabaseBLEManager.getInstance().xxx().

```java
public class DatabaseBLEManager {
    /*-数据存储-----------*/
    /*蓝牙设备存储*/

    /**
     * 保存蓝牙设备（单个）
     *
     * @param entity  蓝牙单个设备
     */
    public void saveGGBLEDeviceEntity(GGBLEDeviceEntity entity) {
    }

    /**
     * 保存 扫描到的所有设备
     *
     * @param list    设备列表
     */
    public void saveGGBLEDeviceList(List<GGBLEDeviceEntity> list) {
    }

    /**
     * 获取所有的蓝牙设备
     *
     * @return List<GGBLEDeviceEntity>
     */
    public List<GGBLEDeviceEntity> getAllGGBLEDevice() {
    }

    /**
     * 删除单个蓝牙设备
     */
    public void deleteBLEDeviceEntity(GGBLEDeviceEntity entity) {
    }

    /**
     * 删除所有蓝牙设备
     *
     */
    public void deleteAllBLEDevice() {
    }

    /*测量结果存储*/

    /**
     * 保存测量结果
     *
     * @param completeModel 测量数据的结果
     */
    public void saveComplete(CompleteModel completeModel) {
    }

    /**
     * 更新测量数据为已上传
     *
     * @param completeModelList 上传成功的测量数据
     */
    public void updateCompleteToIsUpdate( List<CompleteModel> completeModelList) {
    }

    /**
     * 获取说有测量数据
     *
     * @return List<CompleteModel>
     */
    public List<CompleteModel> getAllComplete() {
    }

    /**
     * 获取所有没有上传的测量数据
     *
     * @return List<CompleteModel>
     */
    public List<CompleteModel> getAllCompleteOfNotUpdate() {
    }

    public List<CompleteModel> getAllNotUpdatedForDeviceType(String deviceType) {
    }

    public List<CompleteModel> getAllCompleteUpdated() {
    }

    public List<CompleteModel> getAllCompleteUpdatedForDeviceType(String deviceType) {
    }

    /**
     * 根据设备类型获取相应的测量结果数据
     *
     * @param deviceType 蓝牙设备类型
     * @return List<CompleteModel>
     */
    public List<CompleteModel> getAllCompleteOfDeviceType(String deviceType) {
    }

    /**
     * 删除一类型的测量结果数据
     *
     * @param deviceType 蓝牙设备类型
     */
    public void deleteCompleteOfDeviceType(String deviceType) {
    }

    /**
     * 删除所有的测量数据
     *
     */
    public void deleteAllComplete() {
    }

}
```
