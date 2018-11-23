package com.mediatek.factorymode.TempHumidityTester;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediatek.factorymode.R;
import com.mediatek.factorymode.Utils;

public class TempHumidityTester extends Activity implements OnClickListener{
    private static final String TAG = "TempHumidityTester";
    private TextView mViewTemp;
    private TextView mViewHumi;
    private static final String TEMP_FILE = "/sys/bus/i2c/devices/i2c-1/1-0070/temp1_input";
    private static final String HUMI_FILE = "/sys/bus/i2c/devices/i2c-1/1-0070/humidity1_input";
    private static int REFRESH_PERIOD = 2*1000;
	private SharedPreferences mSp;

    Handler mHandler;
    Runnable mTempService = null;
    BufferedReader mDeviceReader = null;
    float mCurrTemp;
    float mCurrHumi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temphumidity);
		this.mSp = getSharedPreferences("FactoryMode", 0);

        mViewTemp = (TextView) findViewById(R.id.content_temp);
        mViewHumi = (TextView) findViewById(R.id.content_humi);
		findViewById(R.id.bt_ok).setOnClickListener(this);
		findViewById(R.id.bt_failed).setOnClickListener(this);
		
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                mViewTemp.setText(Float.toString(mCurrTemp)+getString(R.string.degree_celsius));
                mViewHumi.setText(Float.toString(mCurrHumi)+getString(R.string.percentage));
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    float ret = getFileData(TEMP_FILE);
                    if (ret != 0xff) {
                        mCurrTemp = ret;
                    }

                    ret = getFileData(HUMI_FILE);
                    if (ret != 0xff) {
                        mCurrHumi = ret;
                    }

                    mHandler.sendEmptyMessage(0);
                    try {
                        Thread.sleep(REFRESH_PERIOD);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    float getFileData(String path) {
        String strData;
        float temp = 0xff;

        try {
            mDeviceReader = new BufferedReader(new FileReader(path));
            strData = mDeviceReader.readLine();
            Log.d(TAG, "get current data:" + strData);
            if (strData != null) {
                temp = Float.parseFloat(strData);
                BigDecimal b  = new BigDecimal(temp);
                temp = b.setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
            }
            mDeviceReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bt_ok:
			Utils.SetPreferences(this, mSp, R.string.temphumiditytester, "success");
			finish();

			break;
		case R.id.bt_failed:
			Utils.SetPreferences(this, mSp, R.string.temphumiditytester, "failed");
			finish();
			break;

		default:
			break;
		}
	}
	
}

