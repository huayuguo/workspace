package com.hcn.huangchao.testsensor.fragment;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.hcn.huangchao.testsensor.R;

import java.util.List;


public class GyroscopeFragment extends Fragment{
    private String TAG = "GyroscopeFragment";
    private Context mContext;
    private View mView;
    private TextView xTv;
    private TextView yTv;
    private TextView zTv;
    private SensorManager mSensorManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_three_axis_value, container, false);
        initViews();
        initValues();
        initListeners();
        return mView;
    }

    private void initViews() {
        xTv = mView.findViewById(R.id.xTv);
        yTv = mView.findViewById(R.id.yTv);
        zTv = mView.findViewById(R.id.zTv);
    }

    private void initValues() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    private void initListeners() {
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onResume() {
        super.onResume();
        initListeners();
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(mSensorListener);
    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x,y,z;
            Log.e(TAG, "onSensorChanged " + event.sensor.getType());
            if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                Log.d(TAG,"x = " + x + ", y = " + y + ", z = " + z);
                xTv.setText("" + x);
                yTv.setText("" + y);
                zTv.setText("" + z);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.e(TAG, "onAccuracyChanged");
        }
    };
}
