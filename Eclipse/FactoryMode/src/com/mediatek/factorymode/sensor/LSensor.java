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


public class LSensor extends Activity
  implements SensorEventListener
{
  public View.OnClickListener cl;
  TextView mAccuracyView = null;
  Button mBtFailed;
  Button mBtOk;
  Sensor mLightSensor = null;
  SensorManager mSensorManager = null;
  SharedPreferences mSp;
  TextView mValueX = null;
  

  public void onAccuracyChanged(Sensor paramSensor, int paramInt)
  {
    if (paramSensor.getType() != 5)
      return;
    TextView localTextView = this.mAccuracyView;
    StringBuilder localStringBuilder = new StringBuilder().append(getString(R.string.LSensor_accuracy)).append(paramInt);
    localTextView.setText(localStringBuilder.toString());
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.lsensor);
    this.mSp = getSharedPreferences("FactoryMode", 0);
    this.mSensorManager = (SensorManager)getSystemService("sensor");
    this.mLightSensor = this.mSensorManager.getDefaultSensor(5);
    this.mAccuracyView = (TextView)findViewById(R.id.lsensor_accuracy);
    this.mValueX = (TextView)findViewById(R.id.lsensor_value);
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(new View.OnClickListener(){
        public void onClick(View v) {
            Context localContext = LSensor.this.getApplicationContext();
            SharedPreferences localSharedPreferences = LSensor.this.mSp;
            Utils.SetPreferences(localContext, localSharedPreferences, R.string.lsensor_name, "success");
            LSensor.this.finish();
        }
    });
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(new View.OnClickListener(){
        public void onClick(View v) {
            Context localContext = LSensor.this.getApplicationContext();
            SharedPreferences localSharedPreferences = LSensor.this.mSp;
            Utils.SetPreferences(localContext, localSharedPreferences,  R.string.lsensor_name, "failed");
            LSensor.this.finish();
        }
    });
  }

  protected void onPause()
  {
    super.onPause();
    SensorManager localSensorManager = this.mSensorManager;
    Sensor localSensor = this.mLightSensor;
    localSensorManager.unregisterListener(this, localSensor);
  }

  protected void onResume()
  {
    super.onResume();
    SensorManager localSensorManager = this.mSensorManager;
    Sensor localSensor = this.mLightSensor;
    localSensorManager.registerListener(this, localSensor, 3);
  }

  public void onSensorChanged(SensorEvent paramSensorEvent)
  {
    if (paramSensorEvent.sensor.getType() != 5)
      return;
    float[] arrayOfFloat = paramSensorEvent.values;
    TextView localTextView = this.mValueX;
    StringBuilder localStringBuilder1 = new StringBuilder().append(getString(R.string.LSensor_value)).append(arrayOfFloat[0]);
    localTextView.setText(localStringBuilder1.toString());

  }
}