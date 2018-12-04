package com.hcn.huangchao.testpressure;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.huangchao.dashboardview.DashboardView;
import com.huangchao.dashboardview.HighlightCR;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String TAG = "TestPressure";
    private SensorManager mSensorManager;
    private DashboardView dashboardViewPress;
    private DashboardView dashboardViewTemp;
    private DashboardView dashboardViewHmdy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY), SensorManager.SENSOR_DELAY_NORMAL);

        dashboardViewPress = (DashboardView) findViewById(R.id.dashboard_view_press);
        dashboardViewTemp = (DashboardView) findViewById(R.id.dashboard_view_temp);
        dashboardViewHmdy = (DashboardView) findViewById(R.id.dashboard_view_hmdy);

        List<HighlightCR> highlight1 = new ArrayList<>();
        highlight1.add(new HighlightCR(135, 180, Color.parseColor("#03A9F4")));
        highlight1.add(new HighlightCR(315, 90, Color.parseColor("#FFA000")));
        dashboardViewPress.setStripeHighlightColorAndRange(highlight1);
    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.e(TAG, "onSensorChanged " + event.sensor.getType());
            if(event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                float pressure = event.values[0];
                dashboardViewPress.setRealTimeValue(pressure,true);
            } else if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                float temp = event.values[0];
                dashboardViewTemp.setRealTimeValue(temp, true);
            } else if(event.sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
                float humidity = event.values[0];
                dashboardViewHmdy.setRealTimeValue(humidity, true);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.e(TAG, "onAccuracyChanged");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(mSensorListener);
    }
}
