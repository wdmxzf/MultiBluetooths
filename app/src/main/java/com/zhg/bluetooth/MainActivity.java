package com.zhg.bluetooth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void MainOnClick(View view){
        switch (view.getId()){
            case R.id.btn_bluetoothThree:
                startActivity(new Intent(MainActivity.this, MyBleActivity.class));
                break;
            case R.id.btn_bluetoothList:
                startActivity(new Intent(MainActivity.this, BluetoothListActivity.class));
                break;

            default:
//                Toast.makeText(MainActivity.this,"该功能暂为开发",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
