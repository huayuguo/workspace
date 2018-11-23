package com.mediatek.factorymode.infraredThermometer;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
//import android.os.SystemProperties;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

import java.io.IOException;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;


public class InfraredThermometer extends  Activity
		implements View.OnClickListener
{


	SharedPreferences mSp;
	private Button mBtFailed;
	private Button mBtOk;
	private Button mscanner;
	private TextView mObjectText;
	private TextView mAmbientText;
	private  final int OBJECT_TEXT = 1;
	private  final int AMBIENT_TEXT = 2;
  private  final String NODE_OBJECT_PATH = "/sys/class/infrared_temperature/MLX90614/object_temp";
  private  final String NODE_AMBIENT_PATH = "/sys/class/infrared_temperature/MLX90614/ambient_temp";
  private  final String NODE_OPEN_PATH = "/sys/class/infrared_temperature/MLX90614/led_open";
  private  final String NODE_CLOSE_PATH = "/sys/class/infrared_temperature/MLX90614/led_close";

  BufferedReader mDeviceReader = null;
  BufferedReader mCloseReader = null;
  BufferedReader mOpenReader = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.infrared_thermometer);
		mSp = getSharedPreferences("FactoryMode", 0);
		mObjectText = (TextView) findViewById(R.id.object_temp);
		mAmbientText = (TextView) findViewById(R.id.ambient_temp);
		
		mBtOk = (Button)findViewById(R.id.bt_ok);
		mBtOk.setOnClickListener(this);
		mBtFailed = (Button)findViewById(R.id.bt_failed);
		mBtFailed.setOnClickListener(this);
		new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    float ret = getTemperature(NODE_OBJECT_PATH);
                    if (ret != 0xff) {
                       Message m = Message.obtain(mHandler, OBJECT_TEXT);
                       StringBuilder localStringBuilder = new StringBuilder().append(getString(R.string.object_temp_text)).append(ret);
                       m.obj = localStringBuilder.toString();
                       mHandler.sendMessage(m);
                        
                    }

                    ret = getTemperature(NODE_AMBIENT_PATH);
                    if (ret != 0xff) {
                       Message m1 = Message.obtain(mHandler, AMBIENT_TEXT);
                       StringBuilder localStringBuilder1 = new StringBuilder().append(getString(R.string.ambient_temp_text)).append(ret);
                       m1.obj = localStringBuilder1.toString();
                       mHandler.sendMessage(m1);
                    }

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		try {
		String mCloseStr;
		 mCloseReader = new BufferedReader(new FileReader(NODE_CLOSE_PATH));
     mCloseStr = mCloseReader.readLine();
     mCloseReader.close();
     } catch (IOException e) {
            e.printStackTrace();
     }
		super.onPause();
	}



	@Override
	protected void onResume() {
		try {
		String mOpenStr;
		 mOpenReader = new BufferedReader(new FileReader(NODE_OPEN_PATH));
     mOpenStr = mOpenReader.readLine();
         mOpenReader.close();
     } catch (IOException e) {
            e.printStackTrace();
     }

		super.onResume();
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	    if(arg0.getId() == mBtOk.getId()){
	        Utils.SetPreferences(this, mSp, R.string.infrared_thermometer, "success");
	        finish();
	    }
	    else{
	        Utils.SetPreferences(this, mSp, R.string.infrared_thermometer, "failed");
	        finish();
	    }
	}





	
   Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	TextView localTextView;
            switch (msg.what) {
                case OBJECT_TEXT:
                		localTextView = mObjectText;
										localTextView.setText((String)msg.obj);
                    break;
                case AMBIENT_TEXT:
                		localTextView = mAmbientText;
										localTextView.setText((String)msg.obj);
                    break;
             }
        }
    };
    float getTemperature(String path) {
        String strTemp;
        float temp = 0xff;

        try {
            mDeviceReader = new BufferedReader(new FileReader(path));
            strTemp = mDeviceReader.readLine();
            Log.d("InfraredThermometer", "get current temperature:" + strTemp);
            if (strTemp != null && !strTemp.equals("null")) {
                temp = Float.parseFloat(strTemp);
                BigDecimal b  = new BigDecimal(temp);
                temp = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            }
            mDeviceReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }
}
