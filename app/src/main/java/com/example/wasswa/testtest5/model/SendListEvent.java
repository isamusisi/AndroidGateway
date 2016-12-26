package com.example.wasswa.testtest5.model;

import java.util.ArrayList;

import io.relayr.android.ble.BleDevice;

/**
 * Created by wasswa on 24.11.2016.
 */

public class SendListEvent {
    ArrayList<BleDevice> list;

    public SendListEvent(ArrayList<BleDevice> list){
        this.list = list;
    }

    public ArrayList<BleDevice> getList(){
        return list;
    }
}
