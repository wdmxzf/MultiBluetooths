package com.zhg.bluetooth;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.zhg.bluetoothL.entity.ZHGBLEDeviceEntity;
import com.zhg.bluetoothL.util.ZHGBluetoothProfile;

import java.util.List;



public class BLEAdapter extends BaseAdapter {

    private Context mContext;
    private List<ZHGBLEDeviceEntity> mEntityList;
    private LayoutInflater mLayoutInflater;
    public BLEAdapter(Context context, List<ZHGBLEDeviceEntity> entityList){
        this.mContext = context;
        this.mEntityList = entityList;
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mEntityList.size();
    }

    @Override
    public Object getItem(int position) {
        return mEntityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (null == convertView){
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.item_ble,parent,false);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tv_bleName);
            viewHolder.tvStatus = (TextView) convertView.findViewById(R.id.status);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ZHGBLEDeviceEntity entity= mEntityList.get(position);
        viewHolder.tvName.setText(entity.getDeviceName());

        switch (entity.getConnectStatus()){
            case ZHGBluetoothProfile.STATE_DISCONNECTED:
                viewHolder.tvStatus.setText("未连接");
                viewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.colorGray));
                viewHolder.tvStatus.setTextColor(ContextCompat.getColor(mContext,R.color.colorGray));
                break;
            case ZHGBluetoothProfile.STATE_CONNECTING:
                viewHolder.tvStatus.setText("连接中");
                viewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                viewHolder.tvStatus.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                break;
            case ZHGBluetoothProfile.STATE_CONNECTED:
                viewHolder.tvStatus.setText("已连接");
                viewHolder.tvName.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                viewHolder.tvStatus.setTextColor(ContextCompat.getColor(mContext,R.color.colorAccent));
                break;
            default:
                break;
        }
        return convertView;
    }


    private static class ViewHolder{
        TextView tvName;
        TextView tvStatus;
    }

}
