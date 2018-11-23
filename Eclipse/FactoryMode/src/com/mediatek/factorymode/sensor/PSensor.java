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


public class PSensor extends Activity
  implements SensorEventListener
{
  public View.OnClickListener cl;
 // TextView mAccuracyView = null;
  Button mBtFailed;
  Button mBtOk;
  Sensor mPSensor = null;
  SensorManager mSensorManager = null;
  SharedPreferences mSp;
  TextView mValueX = null;
  

  public void onAccuracyChanged(Sensor paramSensor, int paramInt)
  {
   /* if (paramSensor.getType() != 8)
      return;
    TextView localTextView = this.mAccuracyView;
    StringBuilder localStringBuilder = new StringBuilder().append(getString(R.string.LSensor_accuracy)).append(paramInt);
    localTextView.setText(localStringBuilder.toString());
    */
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.psensor);
    this.mSp = getSharedPreferences("FactoryMode", 0);
    this.mSensorManager = (SensorManager)getSystemService("sensor");
    this.mPSensor = this.mSensorManager.getDefaultSensor(8);
    //this.mAccuracyView = (TextView)findViewById(R.id.lsensor_accuracy);
    this.mValueX = (TextView)findViewById(R.id.proximity);
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(new View.OnClickListener(){
 
        public void onClick(View v) {
            Context localContext = PSensor.this.getApplicationContext();
            SharedPreferences localSharedPreferences = PSensor.this.mSp;
            Utils.SetPreferences(localContext, localSharedPreferences, R.string.psensor_name, "success");
            PSensor.this.finish();
        }
    });
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(new View.OnClickListener(){
        public void onClick(View v) {
            Context localContext = PSensor.this.getApplicationContext();
            SharedPreferences localSharedPreferences = PSensor.this.mSp;
            Utils.SetPreferences(localContext, localSharedPreferences,  R.string.psensor_name, "failed");
            PSensor.this.finish();
        }
    });
  }

  protected void onPause()
  {
    super.onPause();
    SensorManager localSensorManager = this.mSensorManager;
    Sensor localSensor = this.mPSensor;
    localSensorManager.unregisterListener(this, localSensor);
  }

  protected void onResume()
  {
    super.onResume();
    SensorManager localSensorManager = this.mSensorManager;
    Sensor localSensor = this.mPSensor;
    localSensorManager.registerListener(this, localSensor, 0);
  }

  public void onSensorChanged(SensorEvent paramSensorEvent)
  {
    if (paramSensorEvent.sensor.getType() != 8)
      return;
    float[] arrayOfFloat = paramSensorEvent.values;
    TextView localTextView = this.mValueX;
    float ranage = this.mPSensor.getMaximumRange();
    StringBuilder localStringBuilder1 = new StringBuilder().append(getString(R.string.proximity)).append(arrayOfFloat[0]).append(" -").append(ranage);
    localTextView.setText(localStringBuilder1.toString());

  }
}
  