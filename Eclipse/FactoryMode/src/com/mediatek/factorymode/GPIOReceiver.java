package com.mediatek.factorymode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class GPIOReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.e("Keven","com.android.gpio.control");
		SharedPreferences localSharedPreferences = context.getSharedPreferences("FactoryMode", 0);
		boolean flag = intent.getBooleanExtra("isOk", false);
		if(flag){
			 Utils.SetPreferences(context, localSharedPreferences, R.string.GPIO, "success");
		}else{
			Utils.SetPreferences(context, localSharedPreferences, R.string.GPIO, "failed");
		}
	}

}
