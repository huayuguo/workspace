package com.mediatek.factorymode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.IntentFilter;
import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;
//import dalvik.annotation.Signature;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.lang.Integer;

import com.yjzn.SerialPort;

public class TiBle extends Activity
	implements View.OnClickListener
{
	private static final String TAG = "TIBLE";
    public static final String SERIALPORT = "/dev/ttyMT1";
    public static final int SERIALBURATE = 115200;
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
	private Button mBtFailed = null;
	private Button mBtOk = null;
	WifiHandler mHandler;
	private SharedPreferences mSp = null;
	private TextView mTvState = null;
	private TextView mTvVersion = null;
	private TextView mTvResult = null;

	HandlerThread mWifiThread = null;
	ReadThread readThread = null;
    Map<String, String> mDevMap = new HashMap<String, String>();

    private Runnable wifirunnable = new Runnable(){
		public void run() {
			String buffer = null;
			sendData("AT+START_SCAN=0\r\n");
			try {
				Thread.sleep(120);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			buffer = readData();
			Log.e(TAG,"readData (" + buffer.length() + ") " + buffer);
			sendData("AT+VERION=?\r\n");
			try {
				Thread.sleep(120);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			buffer = readData().substring(7);
			Log.e(TAG,"readData (" + buffer.length() + ") " + buffer);
			if(buffer != null) {
				Message msg = new Message(); 
				Bundle b = new Bundle();
				b.putString("version", buffer);
				msg.setData(b);
				msg.what = 0;
				UiHandler.sendMessage(msg);
			}
			sendData("AT+START_SCAN=1\r\n");
			readThread.start();
		}
	};

	private Handler UiHandler = new Handler(){
        public void handleMessage(Message paramMessage) {
            Bundle b;
            switch (paramMessage.what) {
                case 0:
                    b = paramMessage.getData();
                    String version = b.getString("version");
                    mTvState.setText(R.string.ble_open);
                    mTvVersion.setText(getResources().getString(R.string.fw_version) + version);
                    mTvResult.setText(getResources().getString(R.string.ble_scan) + '\n');
                    break;
                case 1:
                    /*for(String k : mDevMap.keySet()) {
                        mac_addr = k;
                        rssi = (String)mDevMap.get(k);
                        mTvResult.append("MAC"mac_addr + " RSSI:" + rssi);
                    }*/
                    b = paramMessage.getData();
                    String name = b.getString("name");
                    String mac_addr = b.getString("mac_addr");
                    int rssi = b.getInt("rssi");
                    mTvResult.append("MAC: "+ mac_addr + " RSSI: " + rssi + "\n");
                    break;
                default:
                    break;
            }
        }
	};

	public TiBle() {
		this.mWifiThread = new HandlerThread("wifiThread");
	}

	public void onClick(View paramView) {
		SharedPreferences localSharedPreferences = this.mSp;
		int i = R.string.ti_ble;
		if (paramView.getId() == this.mBtOk.getId()) {
			Utils.SetPreferences(this, localSharedPreferences, i, "success");
			finish();
		} else {
			Utils.SetPreferences(this, localSharedPreferences, i, "failed");
			finish();
		}
	}

	public void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.ti_ble);
		this.mSp = getSharedPreferences("FactoryMode", 0);
		this.mTvState = (TextView)findViewById(R.id.ble_state_id);
		this.mTvVersion = (TextView)findViewById(R.id.ble_version_id);
		this.mTvResult = (TextView)findViewById(R.id.ble_result_id);

		this.mBtOk = (Button)findViewById(R.id.bt_ok);
		this.mBtFailed = (Button)findViewById(R.id.bt_failed);
		this.mBtOk.setOnClickListener(this);
		this.mBtFailed.setOnClickListener(this);

		initData();
		readThread = new ReadThread();

		this.mWifiThread.start();
		Looper localLooper = this.mWifiThread.getLooper();
		this.mHandler = new WifiHandler(localLooper);
		this.mHandler.post(this.wifirunnable);		
	}

	public void onDestroy() {
		super.onDestroy();
		this.mHandler.removeCallbacks(wifirunnable);
		sendData("AT+START_SCAN=0\r\n");
		mSerialPort =null;
        try {
            mInputStream.close();
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            readThread.join();
            //mWifiThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            mInputStream.close();
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	class WifiHandler extends Handler {
		public WifiHandler() {
		}

		public WifiHandler(Looper arg2) {
		super(arg2);
		}

		public void handleMessage(Message paramMessage) {
			super.handleMessage(paramMessage);
		}
	}

	void initData() {
        try {
            mSerialPort = new SerialPort(new File(SERIALPORT), SERIALBURATE, 0);
            if (mSerialPort == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TiBle.this);
                builder.setMessage(getResources().getString(R.string.create_serial_port_fail));
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return;
            }
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(TiBle.this);
            builder.setMessage(getResources().getString(R.string.create_serial_port_fail));
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendData(final String string) {
        try {
            Log.e(TAG,"sendData " + string);
            mOutputStream.write(string.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readData() {
		byte[] buffer = new byte[1024];
        int size = 0;
        String string = null;
        if (mInputStream != null) {
            try {
                size = mInputStream.read(buffer, 0, 1023);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (size > 0) {
            	buffer[size] = '\0';
                string = new String(buffer).trim();
            }
        }
        return string;
    }

    private class ReadThread extends Thread {
        String string = null;
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                if (mSerialPort != null && mInputStream != null) {
                    byte[] buffer = new byte[1024];
                    int size = 0;
                    try {
                        Log.e(TAG,"read ++");
                        size = mInputStream.read(buffer, 0, 1023);
                        Log.e(TAG,"read --");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (size > 0) {
                        try {
                            string = new String(buffer, "GB2312").trim();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            string = new String(buffer);
                        }
                        //Log.e(TAG,"ReadThread (" + string.length() + ") " + string);
                        paramData(string);
                    }
                    paramData(string);
                } else {
                    Log.e(TAG, "ReadThread exit");
                    break;
                }
            }
        }
    }

    void paramData(String buffer) {
        String[] lines = buffer.split("\r\n");
        for (int i = 0 ; i < lines.length ; i++ ) {
            String line = lines[i];
            if (line.startsWith("AT+BOARD=")) {
                String mac_addr = line.substring(11, 22);
                int index = line.indexOf(',');
                String rssi = line.substring(23, index);
                int rssi_int = Integer.parseInt(rssi, 16);
                String name = line.substring(index + 1);
                if (mDevMap.containsKey(mac_addr) == false) {
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    b.putString("name", name);
                    b.putString("mac_addr", mac_addr);
                    b.putInt("rssi", rssi_int);
                    msg.setData(b);
                    msg.what = 1;
                    UiHandler.sendMessage(msg);
                }
                mDevMap.put(mac_addr, rssi);
            }
        }

    }
}
