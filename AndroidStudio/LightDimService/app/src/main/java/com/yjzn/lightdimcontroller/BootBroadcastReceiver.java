package com.yjzn.lightdimcontroller;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {
	public static final String TAG = "BootBroadcastReceiver";
	
	@SuppressLint("UnsafeProtectedBroadcastReceiver")
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, LightDimController.class);
		//context.startService(service);
		context.startForegroundService(service);
		Log.v(TAG, "BootBroadcastReceiver.....");
		Log.d(TAG, "onCreate");
	}
}
