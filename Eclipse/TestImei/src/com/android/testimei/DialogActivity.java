/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.testimei;

import android.content.Context;
import android.os.Bundle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import android.widget.TextView;
import android.support.v7.app.ActionBar;

public class DialogActivity extends AppCompatActivity {
    String TAG = "DialogActivity";
	List<String> deviceIds;
	TextView tvImei0;
	TextView tvImei1;
	TextView tvMeid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dialog);
		
		/*ActionBar actionbar = getSupportActionBar();
        if(actionbar != null){
            actionbar.hide();
        }*/
		
        tvImei0 = findViewById(R.id.tvImei0);
        tvImei1 = findViewById(R.id.tvImei1);
        tvMeid = findViewById(R.id.tvMeid);
		
		deviceIds = new ArrayList<String>();
		
		final TelephonyManager telephonyManager =
            (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		int phoneCount = telephonyManager.getSimCount();
		Log.e(TAG, "phoneCount = " + phoneCount);
		for(int i = 0; i < phoneCount; i++) {
			deviceIds.addAll(getImeiInformation(i));
		}     	
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
	
	private List<String> getImeiInformation(int phoneId) {
	  List<String> deviceIds = new ArrayList<String>();
      Phone phone = null;
  
      try {
		  PhoneFactory.dump_var();
		  Log.e(TAG, "myPid : " + android.os.Process.myPid());
          phone = PhoneFactory.getPhone(phoneId);
      } catch (IllegalStateException e) {
          Log.e(TAG, "Get phone failed: " + e);
      }
  
      if (phone != null) {
          if (phone.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA) {
			  deviceIds.add(phone.getMeid());
			  tvMeid.setText("MEID: " + phone.getMeid());
			  Log.e(TAG,"MEID: " + phone.getMeid());
              if (phone.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE) {
				  deviceIds.add(phone.getImei());
				  tvImei0.setText("IMEI0: " + phone.getImei());
				  Log.e(TAG,"IMEI0: " + phone.getImei());
              }
          } else {
			  deviceIds.add(phone.getImei());
			  tvImei1.setText("IMEI1: " + phone.getImei());
			  Log.e(TAG,"IMEI1: " + phone.getImei());
          }
      }
	  return deviceIds;
  }
}
