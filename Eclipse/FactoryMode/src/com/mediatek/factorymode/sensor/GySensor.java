package com.mediatek.factorymode.sensor;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;


public class GySensor extends Activity implements SensorEventListener
{
  public View.OnClickListener cl;
  TextView mAccuracyView = null;
  Button mBtFailed;
  Button mBtOk;
  Sensor mGySensor = null;
  SensorManager mSensorManager = null;
  SharedPreferences mSp;
  TextView mValueX = null;
  

  public void onAccuracyChanged(Sensor paramSensor, int paramInt)
  {
    /*if (paramSensor.getType() != Sensor.TYPE_GYROSCOPE)
      return;
    TextView localTextView = this.mAccuracyView;
    StringBuilder localStringBuilder = new StringBuilder().append(getString(R.string.gyroscope_tip)).append(paramInt);
    localTextView.setText(localStringBuilder.toString());
    */
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.gysensor);
    this.mSp = getSharedPreferences("FactoryMode", 0);
    this.mSensorManager = (SensorManager)getSystemService("sensor");
    this.mGySensor = this.mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    this.mAccuracyView = (TextView)findViewById(R.id.gyroscope_tip);
    this.mValueX = (TextView)findViewById(R.id.gy_proximity);
    this.mValueX.setText("X: 0\nY: 0\nZ: 0");
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(new View.OnClickListener(){
 
        public void onClick(View v) {
            Context localContext = GySensor.this.getApplicationContext();
            SharedPreferences localSharedPreferences = GySensor.this.mSp;
            Utils.SetPreferences(localContext, localSharedPreferences, R.string.gyroscope, "success");
            GySensor.this.finish();
        }
    });
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(new View.OnClickListener(){
        public void onClick(View v) {
            Context localContext = GySensor.this.getApplicationContext();
            SharedPreferences localSharedPreferences = GySensor.this.mSp;
            Utils.SetPreferences(localContext, localSharedPreferences,  R.string.gyroscope, "failed");
            GySensor.this.finish();
        }
    });
  }

  protected void onPause()
  {
    SensorManager localSensorManager = this.mSensorManager;
    Sensor localSensor = this.mGySensor;
    localSensorManager.unregisterListener(this, localSensor);
    super.onPause();
  }

  protected void onResume()
  {
    SensorManager localSensorManager = this.mSensorManager;
    Sensor localSensor = this.mGySensor;
    if(! localSensorManager.registerListener(this, localSensor, SensorManager.SENSOR_DELAY_NORMAL))
    		Log.d("GySensor","registerListener failed " + localSensor.getName() );
    super.onResume();
  }

  public void onSensorChanged(SensorEvent event)
  {
    if (event.sensor.getType() != Sensor.TYPE_GYROSCOPE)
      return;
    float[] arrayOfFloat = event.values;
    TextView localTextView = this.mValueX;
    StringBuilder localStringBuilder1 = new StringBuilder().append("X: ").append(arrayOfFloat[0]).append("\nY: ").append(arrayOfFloat[1]).append("\nZ: ").append(arrayOfFloat[2]);
    localTextView.setText(localStringBuilder1.toString());

  }
}
  
