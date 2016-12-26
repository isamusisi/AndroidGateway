package com.example.wasswa.testtest5.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.wasswa.testtest5.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import io.relayr.android.ble.BleDevice;

/**
 * Created by wasswa on 23.11.2016.
 */

public class BleScanAdapter extends BaseAdapter {
    private ArrayList<BleDevice> mLeDevices;
    public ArrayList<BleDevice> connectDeviceList;
    private LayoutInflater mInflator;
    Context context;
    public final static String INTENT_KEY = "deviceList";

    public BleScanAdapter(Context context) {
        super();
        mLeDevices = new ArrayList<BleDevice>();
        connectDeviceList = new ArrayList<BleDevice>();
        this.context = context;
        mInflator = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
    }

    public void addDevice(BleDevice device) {
        if(!mLeDevices.contains(device)) {
            mLeDevices.add(device);
        }
    }

    public int size(){
        return mLeDevices.size();
    }

    public String content(){
        return mLeDevices.toString();
    }

    static class ViewHolder {
        TextView deviceName;
        ToggleButton deviceState;
        TextView deviceAddress;
    }


    public BleDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        final int pos = i;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.listviewitems, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.sensorAddress);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.textView);
            viewHolder.deviceState = (ToggleButton) view.findViewById(R.id.toggleButton);
            viewHolder.deviceState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    BleDevice temp = mLeDevices.get(pos);

                    if(isChecked){
                        if(!connectDeviceList.contains(temp))
                        connectDeviceList.add(temp);
                    }else{
                        connectDeviceList.remove(temp);
                    }


                }
            });
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        BleDevice device = mLeDevices.get(i);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0){
            viewHolder.deviceName.setText(deviceName);

        }else
            viewHolder.deviceName.setText(R.string.unknown_device);

        viewHolder.deviceAddress.setText(device.getAddress());
            if(true){
                viewHolder.deviceState.setChecked(false);


        }

        return view;
    }
}
