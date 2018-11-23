
package com.zte.engineer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;
import java.util.List;
import android.widget.RelativeLayout;
import com.mediatek.factorymode.R;
import android.content.SharedPreferences;
import com.mediatek.factorymode.Utils;

public class GSensorTest extends ZteActivity {

    LinearLayout g_sensor_layout, gyroscope_layout, magnetic_layout, hall_test_layout,
            light_layout, proximity_layout;
    private float x, y, z;
    private SensorManager sensorMgr;
    private Sensor mGSensor;
    private SensorEventListener lsn;
    TextView GsensorX, GsensorY, GsensorZ;
    TextView MagneticX, MagneticY, MagneticZ;
    ImageView image;
    TextView GyroscopeX, GyroscopeY, GyroscopeZ;
    boolean[] isTestXYZ = {
            false, false, false, false
    };
    int temp = 0;
    int temp1 = 2;
	int count = 0;
	int number = 0;
    private static final int TIMER_EVENT_TICK = 9;
    boolean[] temp2 = {
            false, false, false, false
    };
    int[] images = {
            R.drawable.jiantou1, R.drawable.jiantou4, R.drawable.jiantou3, R.drawable.jiantou2, R.drawable.jiantou10
	};
	
	int[] images_gsensor = {
            R.drawable.gsensor_y, R.drawable.gsensor_x_2, R.drawable.gsensor_z, R.drawable.gsensor_x
	};
    
    TextView LightView;
    TextView ProximityView;
	TextView gsensor_description;

	private TextView g_calibration_title,gyroscope_calibration_title, gsensor_description_2;
	private Spinner calibration1;
	private ArrayAdapter adapter1;
	private Spinner calibration2;
	private ArrayAdapter adapter2;
	private Toast mToast;
	private ImageView imageGsensor;
	private int image_id = images_gsensor[2];
    SharedPreferences mSp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.gsensortest);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSp = getSharedPreferences("FactoryMode", 0);

        initUi();

        initSensorListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initUi() {
        ((Button) findViewById(R.id.btnPass)).setOnClickListener(this);
        ((Button) findViewById(R.id.btnFail)).setOnClickListener(this);
		((Button) findViewById(R.id.btnPass)).setEnabled(false);
		((Button) findViewById(R.id.gyrosensor)).setOnClickListener(this);

		image = (ImageView) findViewById(R.id.jiantou);		
        image.setImageResource(R.drawable.jiantou);
		
		imageGsensor = (ImageView) findViewById(R.id.gsensor_image);	
		gsensor_description = (TextView)findViewById(R.id.gsensor_description);
		gsensor_description_2 = (TextView)findViewById(R.id.gsensor_description_2);
    }
	
    private void showToast(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

	@Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.btnPass:
                finishSelf(RESULT_PASS);
                Utils.SetPreferences(this, mSp, R.string.gsensor_name, "success");
                break;
            case R.id.btnFail:
                finishSelf(RESULT_FAIL);
                Utils.SetPreferences(this, mSp, R.string.gsensor_name, "failed");
                break;
			case R.id.gyrosensor:
				 if (count > 2) count = 0;
				 if(gsensor_description != null) gsensor_description.setText(R.string.gsensor_description_ing);
				 mHandler.sendEmptyMessageDelayed(TIMER_EVENT_TICK, 100);
				 mHandler1.sendEmptyMessageDelayed(count, 1000);
				 count++;
				break;
            default:
                finishSelf(RESULT_PASS);
                Utils.SetPreferences(this, mSp, R.string.gsensor_name, "success");
                break;
        }
    }
	
	// TIMER_EVENT_TICK handler
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIMER_EVENT_TICK:
				    if (number > 3) number = 0;
					image.setImageResource(images[number]);
					sendEmptyMessageDelayed(TIMER_EVENT_TICK, 100);
					number++;
                    break;
            }
        }
    };

	class SpinnerXMLSelectedListener1 implements OnItemSelectedListener{
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
			if(arg2 == 0)
				return;
            mHandler1.sendEmptyMessage(arg2 - 1);			
        }
 
        public void onNothingSelected(AdapterView<?> arg0) {
             
        }  
    }

	private Handler mHandler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int result = 0;
            if (msg.what == 0) {
                result = Util.doGsensorCalibration(Util.TOLERANCE_20);
            } else if (msg.what == 1) {
                result = Util.doGsensorCalibration(Util.TOLERANCE_30);
            } else if (msg.what == 2) {
                result = Util.doGsensorCalibration(Util.TOLERANCE_40);
            }else if (msg.what == 3) {
                result = Util.clearGsensorCalibration();
            }
			
            if (result == Util.RET_SUCCESS) {
                showToast(getResources().getString(R.string.calibrate_success));
				image.setImageResource(R.drawable.jiantou10);
				if(gsensor_description != null) gsensor_description.setText(R.string.gsensor_calibrate_complete);
				imageGsensor.setVisibility(View.VISIBLE);
				image.setVisibility(View.INVISIBLE);
				if(gsensor_description != null) gsensor_description.setText(R.string.gsensor_description1);
				gsensor_description_2.setVisibility(View.VISIBLE);
				((Button) findViewById(R.id.gyrosensor)).setVisibility(View.GONE);
            }else{
				image.setImageResource(R.drawable.jiantou);
                showToast(getResources().getString(R.string.calibrate_fail));
				if(gsensor_description != null) gsensor_description.setText(R.string.gsensor_description);
            }
			mHandler.removeMessages(TIMER_EVENT_TICK);
        }
    };

    private void initSensorListener() {
        //List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL);
        mGSensor =  sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        lsn = new SensorEventListener() {
            public void onSensorChanged(SensorEvent e) {
					x = e.values[SensorManager.DATA_X];
					y = e.values[SensorManager.DATA_Y];
					z = e.values[SensorManager.DATA_Z];
					 Log.i("zp","x = " + x + " y = " + y + " z = " + z);
					double g = Math.sqrt(x*x + y*y + z*z);
					double x_cos = x/g;
					double y_cos = y/g;
					double z_cos = z/g;
					if(x_cos > 1)x_cos = 1;
					if(x_cos < -1)x_cos = -1;
					if(y_cos > 1)y_cos = 1;
					if(y_cos < -1)y_cos = -1;
					if(z_cos > 1)z_cos = 1;
					if(z_cos < -1)z_cos = -1;				
					Log.i("zp","x_cos = " + x_cos + "  y_cos = " + y_cos + "  z_cos = " + z_cos);
					if((x_cos < -0.5) && (z_cos < 0.5) && (x_cos > -1) && (z_cos > 0)){
						imageGsensor.setImageResource(images_gsensor[1]);//left
						temp2[3] = true;
					}
					if((x_cos > 0.5) && (z_cos < 0.5) && (x_cos < 1) && (z_cos > 0)){
						imageGsensor.setImageResource(images_gsensor[3]);//right
						temp2[1] = true;
					}
					if((y_cos > 0.5) && (z_cos < 0.5) && (y_cos < 1) && (z_cos > 0)){
						imageGsensor.setImageResource(images_gsensor[0]);//up
						temp2[2] = true;
					}
					if((y_cos < -0.5) && (z_cos < 0.5) && (y_cos > -1) && (z_cos > 0)){
						imageGsensor.setImageResource(images_gsensor[2]);//down
						temp2[0] = true;
					}
					if (temp2[0] && temp2[1] && temp2[2] && temp2[3]) {
						((Button) findViewById(R.id.btnPass)).setEnabled(true);
					}
       /*
       if((x_cos < 1.0) && (z_cos < 0.5) && (x_cos > 0.8) && (z_cos > 0.1)){  
						imageGsensor.setImageResource(images_gsensor[3]);//left  ( 0.9, 0 , 0.1 )
						temp2[3] = true;
					}
					if((x_cos > -0.99) && (z_cos < 0.5) && (x_cos < -0.60) && (z_cos > 0.1)){
						imageGsensor.setImageResource(images_gsensor[1]);//right    ( -0.9, 0 , 0.1  )
						temp2[1] = true;
					}
					if((y_cos > 0.6) && (z_cos < 0.5) && (y_cos < 1.0) && (z_cos > 0.1)){
						imageGsensor.setImageResource(images_gsensor[2]);//up   ( 0 , 0.9 , 0.1 )
						temp2[2] = true;
					}
					if((y_cos < -0.6) && (z_cos < 0.5) && (y_cos > -0.99) && (z_cos > 0.1)){
						imageGsensor.setImageResource(images_gsensor[0]);//down  ( 0 , -0.9 , 0.1 )
						temp2[0] = true;
					}*/
					if (temp2[0] && temp2[1] && temp2[2] && temp2[3]) {
						((Button) findViewById(R.id.btnPass)).setEnabled(true);
					}
       
        }

            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

        };

      //  for (Sensor s : sensors) {
      //      sensorMgr.registerListener(lsn, s, SensorManager.SENSOR_DELAY_NORMAL);
      //  }
            sensorMgr.registerListener(lsn, mGSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void finishSelf(int result) {

        sensorMgr.unregisterListener(lsn);

        super.finishSelf(result);
    }

      @Override
    protected void onDestroy() {
        super.onDestroy();
       if(lsn != null){
       	sensorMgr.unregisterListener(lsn);
       	lsn = null;
       }
    }
    // Debug use another way.
}
