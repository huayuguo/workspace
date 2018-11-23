package com.mediatek.factorymode.sensor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;  //zqf
import android.view.Surface; //zqf
import android.widget.ImageView;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
import android.util.Log;

import android.os.SystemProperties; //zqf

public class GSensor extends Activity implements View.OnClickListener
{
	private static final int OFFSET = 2;
	private ImageView ivimg;
	private SensorManager mManager = null;
	private Sensor mSensor = null;
	private SensorEventListener mListener = null;
	SharedPreferences mSp;
	private Button mBtFailed;
	private Button mBtOk;
	
	//zqf 2015-09-04 mofidy
    float[] getEventValues(SensorEvent event) {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        //Log.d("info", "rotation=" + rotation);
        float[] values = new float[event.values.length];

        if (rotation == Surface.ROTATION_0) {
                values = event.values;
        } else if (rotation == Surface.ROTATION_180) {
                // newX = -x
                values[0] = -event.values[0];
                // newY = -y
                values[1] = -event.values[1];
                // newZ = z
                values[2] = event.values[2];

        } else if (rotation == Surface.ROTATION_270) {
                // newX = y
                values[0] = event.values[1];
                // newY = -x
                values[1] = -event.values[0];
                // newZ = z
                values[2] = event.values[2];

        } else if (rotation == Surface.ROTATION_90) {
                // newX = -y
                values[0] = -event.values[1];
                // newY = x
                values[1] = event.values[0];
                // newZ = z
                values[2] = event.values[2];

        }
        return values;
}
//zqf 2015-09-04	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gsensor);
	    mManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	    mSensor = mManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	    mListener = new SensorEventListener(){

			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}

			public void onSensorChanged(SensorEvent event) {
				// TODO Auto-generated method stub
				float x = event.values[SensorManager.DATA_X];
				float y = event.values[SensorManager.DATA_Y];
				float z = event.values[SensorManager.DATA_Z];

			/*zqf 2015-09-04 add begin*/
			if(SystemProperties.get("ro.yjzn.GSensor.rotation").equals("yes"))
			{
				float values[] = getEventValues(event);
				
				 x = values[0];
				 y = values[1];
				 z = values[2];
			}				
			/*zqf  2015-09-04 add end*/
				
				//Log.i("Gsensor", "x = " + x + "y = " + y + "z = " + z);

				if(x > -4 && x < 4 && y < 4 && z > 7){
            				GSensor.this.ivimg.setBackgroundResource(R.drawable.gsensor_z);
        			}
        			else if(x > -4 && x < 4 && y > 4 && z < 7){
            				GSensor.this.ivimg.setBackgroundResource(R.drawable.gsensor_y);
				}
				else if (x < -4 && y > -1 && y < 4 && z < 7){
				    GSensor.this.ivimg.setBackgroundResource(R.drawable.gsensor_x_2);
				}
				else if(x > 4 && y > -1 && y < 4 && z < 7){
				    GSensor.this.ivimg.setBackgroundResource(R.drawable.gsensor_x);
				}
			}
	    };
	    
	    mSp = getSharedPreferences("FactoryMode", 0);
	    ivimg = (ImageView)findViewById(R.id.gsensor_iv_img);
	    mBtOk = (Button)findViewById(R.id.bt_ok);
	    mBtOk.setOnClickListener(this);
	    mBtFailed = (Button)findViewById(R.id.bt_failed);
	    mBtFailed.setOnClickListener(this);
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		mManager.unregisterListener(mListener);
		super.onPause();
	}



	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		mManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_GAME);
		super.onResume();
	}



	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	    if(arg0.getId() == mBtOk.getId()){
	        Utils.SetPreferences(this, mSp, R.string.gsensor_name, "success");
	        finish();
	    }
	    else{
	        Utils.SetPreferences(this, mSp, R.string.gsensor_name, "failed");
	        finish();
	    }
	}
	
	
}
