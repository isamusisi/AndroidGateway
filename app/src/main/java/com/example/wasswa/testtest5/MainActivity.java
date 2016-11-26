package com.example.wasswa.testtest5;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import io.relayr.android.RelayrSdk;
import io.relayr.android.ble.BleDevice;
import io.relayr.android.ble.service.BaseService;
import io.relayr.android.ble.service.DirectConnectionService;
import io.relayr.java.ble.BleDeviceType;
import io.relayr.java.model.action.Reading;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    private final static int PERMISSION_REQUEST_CODE = 1;
    private final static String SAVE_INSTANCE_KEY = "saveInstanceKey";
    static BleDevice bd;
    private SeekBar frequency;
    private TextView frequencyText;
    private Button connectButton;
    boolean started = false;
    private Button selectSensorButton;
    ListView listViewMain;
    Intent intent;
    static ArrayList<BleDevice> sensorList = new ArrayList<>();
    SensorAdapter sensorAdapter;
    RelayrSdk.Builder builder;
    boolean scanning;

    public static ArrayList<BleDevice> getSensorList(){
        return sensorList;
    }

    public void scanSpecific(final ArrayList<String> adress){
        builder = new RelayrSdk.Builder(this).cacheModels(true);
        builder.build();
        // final List<BleDeviceType> sensorValues = Arrays.asList(BleDeviceType.values());




        Log.v("adress list", ""+adress);
        final ArrayList<BleDevice> temp = new ArrayList<>();
        final Runnable bli = new Runnable() {
            @Override
            public void run() {
                sensorAdapter.add(bd);
            }
        };
        Thread blu = new Thread(new Runnable() {

            @Override
            public void run() {
                scanning=true;
                while (scanning){
                    if(temp.size()>=adress.size()){

                        sensorList = temp;
                        RelayrSdk.getRelayrBleSdk().stop();
                        scanning=false;
                        return;

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
                            Log.v("found addresses",b.getAddress());
                            if(adress.contains(b.getAddress())){
                                Log.v("is contains true?","oh yeah"+b.getAddress());
                                if(!temp.contains(b)){
                                    temp.add(b);
                                    bd = b;
                                    runOnUiThread(bli);
                                    Log.v("temp after found",""+temp);
                                }
                            }


                        }

                    }
                });


        Log.v("temp list", ""+temp);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
          scanSpecific(savedInstanceState.getStringArrayList(SAVE_INSTANCE_KEY));
            Log.v("neue  new list",""+sensorList);
            if (sensorList != null) {
                sensorAdapter = new SensorAdapter(this,R.layout.listitem_device,sensorList);
            }
        }



        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_CODE);
        }



       initializeObjects();
        frequencyText.setText(frequency.getProgress() + "ms");
        frequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                frequencyText.setText(progress*10 + "ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
           // frequencyText.setText(progress*10 + "ms");
            }
        });



        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if(!started) {
                    connectButton.setText(R.string.stop_button);
                    connectButton.setBackgroundColor(0xa2ff0000);


                    started = true;
                }else{
                    connectButton.setText(R.string.start_button);
                    connectButton.setBackgroundResource(android.R.drawable.btn_default);
                    started = false;

                }


            }
        });

        selectSensorButton.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {
                Intent selSensIntent = new Intent(getApplicationContext(), SensorActivity.class);
                startActivity(selSensIntent);

            }
        });


    }



    @Override
    protected void onResume() {
        super.onResume();
        if(sensorList!=null){
            sensorAdapter = new SensorAdapter(this,R.layout.listitem_device,sensorList);
            listViewMain.setAdapter(sensorAdapter);
        }
        Log.v("new liste",""+sensorList);


    }
    @Subscribe
    public void onSendListEvent(SendListEvent event){
        Log.v("übergebene liste",""+event.getList());
        for(BleDevice b:event.getList()){
            if(sensorList!=null){
                if(!sensorList.contains(b)){
                    sensorList.add(b);
                }
            }else{
                sensorList = event.getList();
            }

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

            super.onSaveInstanceState(outState);
            ArrayList<String> temp = new ArrayList<>();
            for (BleDevice s: sensorList){
                temp.add(s.getAddress());
            }
            outState.putStringArrayList(SAVE_INSTANCE_KEY,temp);

            Log.v("SaveInstance bla", ""+temp);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!EventBus.getDefault().hasSubscriberForEvent(SendListEvent.class)){
            EventBus.getDefault().register(this);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    public void initializeObjects(){

        frequencyText = (TextView) findViewById(R.id.frequencyTextViewId);
        frequency = (SeekBar) findViewById(R.id.frequencyBarId);
        connectButton = (Button) findViewById(R.id.connect_buttonId);
        selectSensorButton = (Button) findViewById(R.id.sel_sensorButtonId);
        listViewMain = (ListView) findViewById(R.id.listViewMain);


    }
}
