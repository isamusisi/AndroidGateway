package com.example.wasswa.testtest5.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.wasswa.testtest5.R;
import com.example.wasswa.testtest5.model.SendListEvent;
import com.example.wasswa.testtest5.adapter.BleScanAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.relayr.android.RelayrSdk;
import io.relayr.android.ble.BleDevice;
import io.relayr.android.ble.RelayrBleSdk;
import io.relayr.java.ble.BleDeviceType;
import rx.functions.Action1;

public class SensorActivity extends AppCompatActivity {

    ListView sensorListView;
    ListAdapter sensorListAdap;
    BleScanAdapter mScanAdapter;
    BluetoothAdapter bluetoothAdapter;
    TextView text;
    Button ok;
    RelayrSdk mSdk;
    RelayrBleSdk mBleSdk;
    SwipeRefreshLayout swipeRefreshLayout;
    boolean scanning;
    RelayrSdk.Builder builder;

    private final static int REQUEST_ENABLE_BT = 1;
    private final static int PERMISSION_REQUEST_CODE = 1;
    private final static int SCAN_PERIOD = 2000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        ArrayList<String> blubb = new ArrayList<>();
        for (int i = 0; i< 10; i++){
            blubb.add("schubidu");
        }

        text = (TextView) findViewById(R.id.textView);
        ok = (Button) findViewById(R.id.button);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new SendListEvent(mScanAdapter.connectDeviceList));
                finish();
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScanAdapter.clear();
                mScanAdapter.notifyDataSetChanged();
                sensorScan(System.currentTimeMillis());

            }
        });
        sensorListView = (ListView) findViewById(R.id.sensorListViewId);
        sensorListAdap = new ArrayAdapter<>(getApplicationContext(), R.layout.listviewitems,R.id.textView, blubb);
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        //sensorListView.setAdapter(sensorListAdap);


        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        mScanAdapter = new BleScanAdapter(this);
        sensorListView.setAdapter(mScanAdapter);
        sensorScan(System.currentTimeMillis());
    }

    public void sensorScan(final long ts){
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }
        builder = new RelayrSdk.Builder(this).cacheModels(true);
        builder.build();
       // final List<BleDeviceType> sensorValues = Arrays.asList(BleDeviceType.values());

        swipeRefreshLayout.setRefreshing(true);

        Thread blu = new Thread(new Runnable() {
            Runnable bli = new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                }
            };
            @Override
            public void run() {
                scanning=true;
                while (scanning){
                    if(System.currentTimeMillis()>ts+SCAN_PERIOD){

                        RelayrSdk.getRelayrBleSdk().stop();
                        runOnUiThread(bli);
                        scanning=false;
                    }
                }
            }
        });
       blu.start();

                   RelayrSdk.getRelayrBleSdk()
                           .scan(Arrays.asList(BleDeviceType.values()))
                           .forEach(new Action1<List<BleDevice>>() {

                        @Override
                        public void call(List<BleDevice> bleDevices) {

                            for (BleDevice b : bleDevices) {
                                if(!MainActivity.getSensorList().contains(b)){
                                    mScanAdapter.addDevice(b);
                                    mScanAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    });


    }
}
