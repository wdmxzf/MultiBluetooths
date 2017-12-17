package com.zhg.bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zhg.bluetoothL.db.DatabaseBLEManager;
import com.zhg.bluetoothL.entity.CompleteModel;
import com.zhg.bluetoothL.entity.ErrorModel;
import com.zhg.bluetoothL.ifc.BluetoothErrorListener;
import com.zhg.bluetoothL.ifc.BluetoothInfo;
import com.zhg.bluetoothL.ifc.MeasuredCompleteListener;
import com.zhg.bluetoothL.service.ZHGBleServiceFactory;
import com.zhg.bluetoothL.util.BluetoothFactory;

import java.util.List;


public class MyBleActivity extends AppCompatActivity implements BluetoothErrorListener, MeasuredCompleteListener ,View.OnClickListener{


    private final String deviceType = "ble";
    Button btnConnect;
    Button btnMeasure;
    Button btnStop;
    Button btnDisConnect;
    TextView tvDeviceName;
    TextView tvConncetStatus;
    TextView tvResult;

    private ZHGBleServiceFactory mFactory;
    private List<BluetoothInfo> mInfoList;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeListener();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        init();
    }

    private void init() {
        btnConnect = findViewById(R.id.btn_connect);
        btnMeasure =findViewById(R.id.btn_measure);
        btnStop = findViewById(R.id.btn_stop);
        btnDisConnect =findViewById(R.id.btn_disConnect);
        tvDeviceName = findViewById(R.id.tv_deviceName);
        tvConncetStatus = findViewById(R.id.tv_conncetStatus);
        tvResult = findViewById(R.id.tv_result);
        btnConnect.setOnClickListener(this);
        initService();
        initData();
        initListener();
    }

    private void initService() {
        mFactory = ZHGBleServiceFactory.getInstance();
        mFactory.setBLEErrorListener(this);
    }

    private void initListener() {
        mInfoList = BluetoothFactory.getInstance().createBluetoothInfo(this);
        for (BluetoothInfo info : mInfoList) {
            info.addCompleteListener(this);
        }
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        List<CompleteModel> completeModels = DatabaseBLEManager.getInstance().getAllCompleteOfDeviceType(deviceType);
        if (null != completeModels && completeModels.size() > 0) {
            CompleteModel completeModel = completeModels.get(completeModels.size() - 1);
            tvResult.setText("this data is "
                    + completeModel.getMeasuredData()[0]
                    + "、" + completeModel.getMeasuredData()[1]
                    + "、" + completeModel.getMeasuredData()[2]
//                    + "\n time is " + MyBleAdapter.stampToDate(completeModel.getMeasuredTime())
            );
        }
    }

    private void removeListener() {
        for (BluetoothInfo info : mInfoList) {
            info.removeCompleteListener(this);
        }
    }

    @Override
    public void onMeasureError(ErrorModel errorModel) {

    }

    @Override
    public void onMeasureComplete(final CompleteModel completeModel) {
        runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                tvResult.setText("this data is "
                        + completeModel.getMeasuredData()[0]
                        + "、" + completeModel.getMeasuredData()[1]
                        + "、" + completeModel.getMeasuredData()[2]
                );
            }
        });
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_connect:
                SharedPreferences sp = getSharedPreferences("bleMacTable", Context.MODE_PRIVATE);
                String address = sp.getString(deviceType,"");
                if (!"".equals(address)) {
                    mFactory.setPinCode("166909");
                    mFactory.connectBluetooth(this, deviceType, address);
                }else {
                    Toast.makeText(this,"请到列表页面进行连接",Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
