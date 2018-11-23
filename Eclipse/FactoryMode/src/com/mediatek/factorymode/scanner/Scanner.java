package com.mediatek.factorymode.scanner;

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
import android.hardware.SerialManager;
import android.hardware.SerialPort;
import java.nio.ByteBuffer;


import com.yjzn.Em3096Native;

public class Scanner extends  Activity
		implements Runnable,View.OnClickListener
{


	SharedPreferences mSp;
	private Button mBtFailed;
	private Button mBtOk;
	private Button mscanner;
	private TextView mAccuracyText;
	private TextView mTagTip;
	private SerialPort mSerialPort;
	private SerialManager mSerialManager;
	private ByteBuffer mInputBuffer;
  private ByteBuffer mOutputBuffer;
	private static final int MESSAGE_TEXT = 1;
	private Em3096Native   mEm3096Native;
	boolean mIsScanFinish = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scanner);
		mSerialManager = (SerialManager)getSystemService(Context.SERIAL_SERVICE);
		mSp = getSharedPreferences("FactoryMode", 0);
		mEm3096Native = new Em3096Native();
		mAccuracyText = (TextView) findViewById(R.id.scanner_accuracy);
		mscanner = (Button)findViewById(R.id.scanner_take);
		mscanner.setOnClickListener(this);
		mBtOk = (Button)findViewById(R.id.bt_ok);
		mBtOk.setOnClickListener(this);
		mBtFailed = (Button)findViewById(R.id.bt_failed);
		mBtFailed.setOnClickListener(this);
		mInputBuffer = ByteBuffer.allocate(16);
		mOutputBuffer = ByteBuffer.allocate(6);
	}



	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}



	@Override
	protected void onResume() {
		String[] ports = mSerialManager.getSerialPorts();
		if (ports != null && ports.length > 0) {
            try {
                mSerialPort = mSerialManager.openSerialPort(ports[2], 9600);
                if (mSerialPort != null) {
                    new Thread(this).start();
                }
            } catch (IOException e) {
            }
    }
    mEm3096Native.powerOnOff(1);
    //mEm3096Native.start_scan();
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		//ReadThread.stop();
		if (mSerialPort != null) {
        try {
            mSerialPort.close();
        } catch (IOException e) {
        }
        mSerialPort = null;
    }
    //mEm3096Native.stop_scan();
    mEm3096Native.powerOnOff(0);
		super.onDestroy();
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	    if(arg0.getId() == mBtOk.getId()){
	        Utils.SetPreferences(this, mSp, R.string.scanner_name, "success");
	        finish();
	    } else if(arg0.getId() == mscanner.getId()){
	    	if (mIsScanFinish) {
						mEm3096Native.stopScan();
				}
	    	mEm3096Native.start_scan();
	    	mIsScanFinish =false;
	    }
	    else{
	        Utils.SetPreferences(this, mSp, R.string.scanner_name, "failed");
	        finish();
	    }
	}


		@Override
		public void run() {

			int size = 0;
			Message m;
			
			while (size >= 0) {
				try {
					byte[] buffer = new byte[20];
					mInputBuffer.clear();
					size = mSerialPort.read(mInputBuffer);
					mInputBuffer.get(buffer, 0, size);
					if (size > 0 && buffer[0] > 0) {
						mIsScanFinish=true;
						m = Message.obtain(mHandler, MESSAGE_TEXT);
                //m.obj = byteArrayToString(buffer,0,buffer.length);
                m.obj = new String(buffer, 0, size);
                mHandler.sendMessage(m);
      
					}
					//Thread.sleep(1);
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
