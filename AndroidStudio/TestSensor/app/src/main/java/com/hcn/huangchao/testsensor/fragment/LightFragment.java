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
import android.widget.TextView;

import com.hcn.huangchao.testsensor.R;


public class LightFragment extends Fragment{
    private String TAG = "LightFragment";
    private Context mContext;
    private View mView;
    private TextView xTv;
    private SensorManager mSensorManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_one_value, container, false);
        initViews();
        initValues();
        initListeners();
        return mView;
    }

    private void initViews() {
        xTv = mView.findViewById(R.id.xTv);
    }

    private void initValues() {
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    }

    private void initListeners() {
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
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
            if(event.sensor.getType() == Sensor.TYPE_LIGHT) {
                x = event.values[0];
                Log.d(TAG,"x = " + x);
                xTv.setText("" + x);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.e(TAG, "onAccuracyChanged");
        }
    };

}
