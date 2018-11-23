package com.mediatek.factorymode.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
//import dalvik.annotation.Signature;
import java.util.BitSet;
import java.util.List;
import com.mediatek.factorymode.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.ImageView;
import android.widget.TextView;
 
public class WifiStateReceiver extends BroadcastReceiver {
 
	//ImageView wifiStateImage;
	TextView mTextView;
	 private String mStrengthName = "";
	Context context;
	public WifiStateReceiver(Context context,TextView textView) {
		// TODO Auto-generated constructor stub
	//	this.wifiStateImage=imageView;
		this.context=context;
		this.mTextView=textView;
		int strength=getStrength(context);
		StringBuilder StringBuilder1 = new StringBuilder()
		.append(context.getString(R.string.WiFi_strength)).append(String.valueOf(strength))
		.append("\n");	
		mStrengthName=StringBuilder1.toString();
	 	mTextView.setText(mStrengthName);
	//	wifiStateImage.setImageLevel(strength);
 
	}
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
	//	System.out.println(intent.getAction());
		if(intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION))
		{
			int strength=getStrength(context);
			StringBuilder StringBuilder1 = new StringBuilder()
			.append(context.getString(R.string.WiFi_strength)).append(String.valueOf(strength))
			.append("\n");				
			mStrengthName=StringBuilder1.toString();
		 	mTextView.setText(mStrengthName);
	
			
		}

 
	}
	public int getStrength(Context context)
	{
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		if (info.getBSSID() != null) {
			int strength = WifiManager.calculateSignalLevel(info.getRssi(), 5);

			return strength;
 
		}
		return 0;
	}
 
}



