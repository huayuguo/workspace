package com.hcn.huangchao.broadcasttest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {
	public MyReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d("MyReceiver", "onReceive Action: " + intent.getAction());
        Log.d("MyReceiver", "onReceive data: " + intent.getIntExtra("keycode", 0));
    }
}
