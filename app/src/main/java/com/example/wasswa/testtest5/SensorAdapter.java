package com.example.wasswa.testtest5;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import io.relayr.android.ble.BleDevice;
import io.relayr.android.ble.service.BaseService;
import io.relayr.android.ble.service.DirectConnectionService;
import io.relayr.java.model.action.Reading;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * Created by Vince on 15.11.16.
 */

public class SensorAdapter extends ArrayAdapter<BleDevice> {


    private ArrayList<BleDevice> aList;
    Context context;
    int resource;

    public SensorAdapter(Context context, int resource, ArrayList<BleDevice> objects) {
        super(context, resource, objects);
        this.context = context;
        aList = objects;
        this.resource = resource;
    }

    public void subscribeForUpdates(final BleDevice device, final TextView result) {

        device.connect()
                .flatMap(new Func1<BaseService, Observable<Reading>>() {
                    @Override
                    public Observable<Reading> call(BaseService baseService) {
                        return ((DirectConnectionService) baseService).getReadings();
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        device.disconnect();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Reading>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        result.setText(R.string.sensor_reading_error);
                    }

                    @Override
                    public void onNext(Reading reading) {

                        result.setText("" + reading.value);
                        SensorAdapter.this.notifyDataSetChanged();
                    }
                });
    }

    private class ViewHolder {
        TextView sensorValue;
        TextView sensorName;
        ImageButton sensorDisconnect;
    }


    public ArrayList<BleDevice> getaList(){
        return aList;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        BleDevice rowItem = aList.get(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(resource, null);
            holder = new ViewHolder();
            holder.sensorName = (TextView) convertView.findViewById(R.id.device_name);
            holder.sensorValue = (TextView) convertView.findViewById(R.id.device_value);
            holder.sensorDisconnect = (ImageButton) convertView.findViewById(R.id.button2);
            holder.sensorDisconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BleDevice temp = aList.remove(position);
                    SensorAdapter.this.notifyDataSetChanged();
                    temp.disconnect();

                }
            });
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.sensorName.setText(rowItem.getName());
       subscribeForUpdates(rowItem,holder.sensorValue);

        return convertView;
    }



}

