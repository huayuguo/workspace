package com.mediatek.factorymode.powerMeterReading;

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
//import android.hardware.SerialManager;
//import android.hardware.SerialPort;
import java.nio.ByteBuffer;

import java.io.InputStream;
import java.io.OutputStream;
import com.yjzn.Em3096Native;
import com.yjzn.SerialPort;
import java.io.File;

public class PowerMeterReading extends  Activity
		implements Runnable,View.OnClickListener
{


	SharedPreferences mSp;
	private Button mBtFailed;
	private Button mBtOk;
	private Button mscanner;
	private TextView mAccuracyText;
	private TextView mTagTip;
	private SerialPort mSerialPort;
	private ByteBuffer mInputBuffer;
  private ByteBuffer mOutputBuffer;
	private static final int MESSAGE_TEXT = 1;
	private Em3096Native   mEm3096Native;
	boolean mIsScanFinish = false;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.power_meter_reading);
		mSp = getSharedPreferences("FactoryMode", 0);

		mAccuracyText = (TextView) findViewById(R.id.read_accuracy);
		mscanner = (Button)findViewById(R.id.PointTest_take);
		mscanner.setOnClickListener(this);
		mBtOk = (Button)findViewById(R.id.bt_ok);
		mBtOk.setOnClickListener(this);
		mBtFailed = (Button)findViewById(R.id.bt_failed);
		mBtFailed.setOnClickListener(this);
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}



	@Override
	protected void onResume() {

    try {
        mSerialPort = new SerialPort(new File("/dev/ttyMT1"), 1200, 0);
        mSerialPort.setSerialPortOption(8, 'E', 1200, 1);
        mOutputStream = mSerialPort.getOutputStream();
				mInputStream = mSerialPort.getInputStream();
        if (mSerialPort != null) {
        		
            new Thread(this).start();
        }
    } catch (IOException e) {
    }

		super.onResume();
	}

	@Override
	protected void onDestroy() {

		if (mSerialPort != null) {
        mSerialPort.close();
        mSerialPort = null;
    }


		super.onDestroy();
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	    if(arg0.getId() == mBtOk.getId()){
	        Utils.SetPreferences(this, mSp, R.string.power_meter_reading_name, "success");
	        finish();
	    } else if(arg0.getId() == mscanner.getId()){
				try {
					mOutputStream.write("pass".getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
	    }
	    else{
	        Utils.SetPreferences(this, mSp, R.string.power_meter_reading_name, "failed");
	        finish();
	    }
	}


		@Override
		public void run() {

			int size = 0;
			Message m;
			
			while (true) {
				try {

					byte[] buffer = new byte[64];
					size = mInputStream.read(buffer);
					if (size > 0 && buffer[0] > 0) {

						m = Message.obtain(mHandler, MESSAGE_TEXT);
                //m.obj = byteArrayToString(buffer,0,buffer.length);
                m.obj = new String(buffer, 0, size);
                mHandler.sendMessage(m);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	//}

	private  String byteArrayToString(byte[] btAryHex, int nIndex, int nLen) {
		if (nIndex + nLen > btAryHex.length) {
			nLen = btAryHex.length - nIndex;
		}

		String strResult = String.format("%02X", btAryHex[nIndex]);
		for (int nloop = nIndex + 1; nloop < nIndex + nLen; nloop++ ) {
			String strTemp = String.format(" %02X", btAryHex[nloop]);

			strResult += strTemp;
		}

		return strResult;
	}
	private byte[] stringToByteArray(String strHexValue) {
		String[] strAryHex = strHexValue.split(" ");
		byte[] btAryHex = new byte[strAryHex.length];

		try {
			int nIndex = 0;
			for (String strTemp : strAryHex) {
				btAryHex[nIndex] = (byte) Integer.parseInt(strTemp, 16);
				nIndex++;
			}
		} catch (NumberFormatException e) {

		}

		return btAryHex;
	}
	
   Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TEXT:
                		TextView localTextView = mAccuracyText;
										localTextView.setText((String)msg.obj);
                    break;
             }
        }
    };
}
