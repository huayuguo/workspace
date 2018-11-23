package com.mediatek.factorymode.sensor;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.Window;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MSensor extends Activity
    implements SensorEventListener
{
  private View.OnClickListener cl;
  private Button mBtFailed;
  private Button mBtOk;
  private float mDegressQuondam = 0;
  private ImageView mImgCompass = null;
  private RotateAnimation mMyAni = null;
  private TextView mOrientText = null;
  private TextView mOrientValue = null;
  private SensorManager mSm = null;
  private SharedPreferences mSp;
  private String text = "";
  public Sensor mSensor;
  public SensorManager mSensorManager;
  private String   _message   = "";  
  private float    _decDegree = 0; 


 
  private void AniRotateImage(float paramFloat)
  {
    float k = this.mDegressQuondam;


    {
        RotateAnimation localRotateAnimation1 = new RotateAnimation(mDegressQuondam, -paramFloat, 
        		RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        		//(k, paramFloat, 1, 1056964608, 1, 1056964608);
        this.mMyAni = localRotateAnimation1;
        this.mMyAni.setDuration(1000L);
        this.mMyAni.setFillAfter(true);
        ImageView localImageView = this.mImgCompass;
        RotateAnimation localRotateAnimation2 = this.mMyAni;
        localImageView.startAnimation(localRotateAnimation2);
        //this.mDegressQuondam = paramFloat;
        this.mDegressQuondam = -paramFloat;
        
        mOrientText.setText(_message);
        TextView localTextView = this.mOrientValue;
  	    StringBuilder localStringBuilder1 = new StringBuilder(text).append(paramFloat);
  	    localTextView.setText(localStringBuilder1.toString());
    }
  
  }


  public void onAccuracyChanged(Sensor paramSensor, int paramInt)//public void onAccuracyChanged(int paramInt1, int paramInt2)
  {
  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.msensor);
    this.mSp = getSharedPreferences("FactoryMode", 0);
    this.mOrientText = (TextView)findViewById(R.id.OrientText);
    this.mImgCompass = (ImageView)findViewById(R.id.ivCompass);
    this.mOrientValue = (TextView)findViewById(R.id.OrientValue);
    this.mBtOk = (Button)findViewById(R.id.bt_ok);
    this.mBtOk.setOnClickListener(new View.OnClickListener(){
        public void onClick(View v) {
            Context localContext = MSensor.this.getApplicationContext();
            SharedPreferences localSharedPreferences = MSensor.this.mSp;
            Utils.SetPreferences(localContext, localSharedPreferences, R.string.msensor_name, "success");
            MSensor.this.finish();
        }
    });
    this.mBtFailed = (Button)findViewById(R.id.bt_failed);
    this.mBtFailed.setOnClickListener(new View.OnClickListener(){
        public void onClick(View v) {
            Context localContext = MSensor.this.getApplicationContext();
            SharedPreferences localSharedPreferences = MSensor.this.mSp;
            Utils.SetPreferences(localContext, localSharedPreferences,  R.string.msensor_name, "failed");
           MSensor.this.finish();
        }
    });
    getWindow().setFlags(128, 128);
  }

  public void onDestroy()
  {
    //this.mSm.unregisterListener(this);
    super.onDestroy();
  }

  protected void onResume()
  {
   /* super.onResume();
    this.mSm.registerListener(this, 1, 0);
    */
	  super.onResume();
	    SensorManager localSensorManager = this.mSensorManager;
	    Sensor localSensor = this.mSensor;
	    localSensorManager.registerListener(this, localSensor, 3);
  }
  
  public void setDegree(float degree)  
  {  
      if(Math.abs(_decDegree - degree) >= 2 )  
      {  
          _decDegree = degree;  
            
          //int range = 1;  
            
          String degreeStr = String.valueOf(_decDegree);           
          StringBuilder sb = new StringBuilder();

          if(_decDegree ==0||_decDegree ==360)//(_decDegree > 360 - range && _decDegree < 360 + range)  
          {  
        	  _message = getString(R.string.MSensor_North);      	  
          }  
            
          if(_decDegree ==90)//(_decDegree > 90 - range && _decDegree <range)//(_decDegree > 90 - range && _decDegree < 90 + range)  
          {  
		  _message = getString(R.string.MSensor_East);  
          }  
            
          if(_decDegree ==180)//(_decDegree > 180 - range && _decDegree < 180 + range)  
          {  	  
		  _message = getString(R.string.MSensor_South); 
          }  
            
          if(_decDegree ==270)//(_decDegree > 270 - range && _decDegree < 270 + range)  
          {  
           	_message =getString(R.string.MSensor_West);  

          }  
              
          if(_decDegree > 0 && _decDegree < 90)//(_decDegree > 45 - range && _decDegree < 45 + range)  
          {  
         	 _message = sb.append(getString(R.string.MSensor_north_east)).append(degreeStr).append(getString(R.string.MSensor_degree)).toString();    
          }  
            
          if(_decDegree > 90 && _decDegree < 180)//(_decDegree > 135 - range && _decDegree < 135 + range)  
          {  
          	 _message = sb.append(getString(R.string.MSensor_south_east)).append(degreeStr).append(getString(R.string.MSensor_degree)).toString(); ;  
          }  

          if(_decDegree > 180 && _decDegree < 270)//(_decDegree > 225 - range && _decDegree < 225 + range)  
          {  
          	 _message = sb.append(getString(R.string.MSensor_south_west)).append(degreeStr).append(getString(R.string.MSensor_degree)).toString();   
 
          }  
 
          if(_decDegree > 270 && _decDegree < 360)//(_decDegree > 315 - range && _decDegree < 315 + range)  
          {  
           	_message = sb.append(getString(R.string.MSensor_north_west)).append(degreeStr).append(getString(R.string.MSensor_degree)).toString();    
          }  
          AniRotateImage(_decDegree);
      }  

  }  
//public void onSensorChanged(int paramInt, float[] values)
  public void onSensorChanged(SensorEvent paramSensorEvent)
  {    
	  if (paramSensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION)  
		  setDegree(paramSensorEvent.values[SensorManager.DATA_X]);
  }

  public void onStart()
  {
    /*super.onStart();
    this.mSm = (SensorManager)getSystemService("sensor");
    */
	    SensorManager localSensorManager = (SensorManager)getSystemService("sensor");
	    this.mSensorManager = localSensorManager;
	    Sensor localSensor = this.mSensorManager.getDefaultSensor(3);
	    this.mSensor = localSensor;
	    super.onStart();

  }

  protected void onStop()
  {
    /*this.mSm.unregisterListener(this);
    super.onStop();*/
	    this.mSensorManager.unregisterListener(this);
	    super.onStop();
  }

public void onClick(View v) {
    Context localContext = this.getApplicationContext();
    SharedPreferences localSharedPreferences = this.mSp;
    if(v.getId() == this.mBtOk.getId()){
        Utils.SetPreferences(localContext, localSharedPreferences, R.string.msensor_name, "success");
        finish();
    }
    else{
        Utils.SetPreferences(localContext, localSharedPreferences, R.string.msensor_name, "failed");
        finish();
    }
    
}
}
