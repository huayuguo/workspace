/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.yjzn.lightdimcontroller;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;

public class LightDimController extends Service {
	public static final String TAG = "LightDimController";
	private static final Uri LIGHTDIM_ONOFF_URI;
	private static final Uri LIGHTDIM_SETTING_URI;
	private ContentResolver mContentResolver;
	private WindowManager wmManager;
	private int lightdim_enable = 0;
	private int lightdim_delay = 0;
	
    static {
    	LIGHTDIM_ONOFF_URI = System.getUriFor("lightdim_onoff");
        LIGHTDIM_SETTING_URI = System.getUriFor("lightdim_setting");
    }
    
    public LightDimController() {
		super();
		//mContentResolver = getContentResolver();
	}
    
    private ContentObserver mLightDimObserver =
        new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
            	Log.d(TAG, "onChange");
                lightdim_enable = Settings.System.getInt(mContentResolver, "lightdim_onoff", 0);
                lightdim_delay = Settings.System.getInt(mContentResolver, "lightdim_setting", 0);
                if(lightdim_enable == 0) {
                	wmManager.setLightDim(0);
                } else if(lightdim_enable == 1) {
                	wmManager.setLightDim(lightdim_delay);
                }
            }
        };
	
    @Override  
    public void onCreate() {  
        super.onCreate();
        Log.d(TAG, "onCreate");
        mContentResolver = getContentResolver();
        wmManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE); 
        
        String name = "my_package_channel";
        String id = "my_package_channel_1";
        String description = "my_package_first_channel";
        
        lightdim_enable = Settings.System.getInt(mContentResolver, "lightdim_onoff", 0);
        lightdim_delay = Settings.System.getInt(mContentResolver, "lightdim_setting", 0);
        if(lightdim_enable == 0) {
        	wmManager.setLightDim(0);
        } else if(lightdim_enable == 1) {
        	wmManager.setLightDim(lightdim_delay);
        }

        NotificationManager notificationManager =  (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        
        NotificationCompat.Builder notificationBuilder;
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(mChannel);
        }
        notificationBuilder = new NotificationCompat.Builder(this);

        notificationBuilder.
                setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setContentTitle("123")
                .setContentText("1234......")
                .setChannelId(id)
                .setAutoCancel(true);
        
        startForeground(1, notificationBuilder.build());
    }  
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        Log.d(TAG, "onStartCommand");  
        mContentResolver.registerContentObserver(LIGHTDIM_ONOFF_URI, false, mLightDimObserver);
        mContentResolver.registerContentObserver(LIGHTDIM_SETTING_URI, false, mLightDimObserver);
        return super.onStartCommand(intent, flags, startId);  
    }  
      
    @Override  
    public void onDestroy() {  
        super.onDestroy();
        mContentResolver.unregisterContentObserver(mLightDimObserver);
        Log.d(TAG, "onDestroy");
    }  
  
    @Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }
}
