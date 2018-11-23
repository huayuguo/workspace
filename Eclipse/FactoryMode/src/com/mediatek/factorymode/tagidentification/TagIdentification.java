package com.mediatek.factorymode.tagidentification;

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

public class TagIdentification extends  Activity
		implements Runnable,View.OnClickListener
{


	SharedPreferences mSp;
	private Button mBtFailed;
	private Button mBtOk;
	private TextView mTagText;
	private TextView mTagTip;
	private SerialPort mSerialPort;
	private SerialManager mSerialManager;
	private ByteBuffer mInputBuffer;
  private ByteBuffer mOutputBuffer;
	private static final int MESSAGE_TEXT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tag_module);
		mSerialManager = (SerialManager)getSystemService(Context.SERIAL_SERVICE);
		mSp = getSharedPreferences("FactoryMode", 0);
		mTagText = (TextView) findViewById(R.id.tag_accuracy);
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
                mSerialPort = mSerialManager.openSerialPort(ports[0], 115200);
                if (mSerialPort != null) {
                    new Thread(this).start();
                }
            } catch (IOException e) {
            }
    }
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
		super.onDestroy();
	}

	public void onClick(View arg0) {
		// TODO Auto-generated method stub
	    if(arg0.getId() == mBtOk.getId()){
	        Utils.SetPreferences(this, mSp, R.string.tag_Identification_name, "success");
	        finish();
	    }
	    else{
	        Utils.SetPreferences(this, mSp, R.string.tag_Identification_name, "failed");
	        finish();
	    }
	}


		@Override
		public void run() {

				try {
				mOutputBuffer.clear();
        mOutputBuffer.put(stringToByteArray("A0 04 FF 74 00 E9"));
			mSerialPort.write(mOutputBuffer, 6);
			Thread.sleep(10);
			} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			int size = 0;
			Message m;
			
			while (size >= 0) {
				try {
					byte[] buffer = new byte[20];
					mOutputBuffer.clear();
					mOutputBuffer.put(stringToByteArray("A0 04 01 74 00 E7"));
					mSerialPort.write(mOutputBuffer, 6);
					//size = mInputStream.read(buffer);
					mInputBuffer.clear();
					size = mSerialPort.read(mInputBuffer);
					mInputBuffer.get(buffer, 0, size);
					if (size > 0) {
						if(buffer[3] == (byte) 0x89 && buffer[4] != (byte) 0x00){
                m = Message.obtain(mHandler, MESSAGE_TEXT);
                m.obj = byteArrayToString(buffer,7,6);
                mHandler.sendMessage(m);
            }
						
					}
					
					mOutputBuffer.clear();
        	mOutputBuffer.put(stringToByteArray("A0 04 01 89 01 D1"));
					mSerialPort.write(mOutputBuffer, 6);

					mInputBuffer.clear();
					size = mSerialPort.read(mInputBuffer);
					mInputBuffer.get(buffer, 0, size);
					if (size > 0) {
						if(buffer[3] == (byte) 0x89 && buffer[4] != (byte) 0x00){
						m = Message.obtain(mHandler, MESSAGE_TEXT);
                m.obj = byteArrayToString(buffer,7,6);
                mHandler.sendMessage(m);
            }
					}
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	//}
	private  void mSetText(String result) {
			TextView localTextView = mTagText;
			localTextView.setText(result);
	}
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
                		TextView localTextView = mTagText;
										localTextView.setText((String)msg.obj);
                    break;
             }
        }
    };
}
