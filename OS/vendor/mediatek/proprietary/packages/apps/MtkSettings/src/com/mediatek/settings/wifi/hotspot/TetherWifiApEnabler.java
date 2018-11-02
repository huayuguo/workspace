/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.mediatek.settings.wifi.hotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.datausage.DataSaverBackend;
import com.android.settings.widget.SwitchBar;

import java.util.ArrayList;

public class TetherWifiApEnabler {

    private static final String TAG = "TetherWifiApEnabler";

    private final Context mContext;
    private final SwitchBar mSwitch;
    private final DataSaverBackend mDataSaverBackend;
    private final IntentFilter mIntentFilter;
    private ConnectivityManager mCm;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE,
                        WifiManager.WIFI_AP_STATE_FAILED);
                handleWifiApStateChanged(state);
            } else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED.equals(action)) {
                ArrayList<String> available = intent
                        .getStringArrayListExtra(ConnectivityManager.EXTRA_AVAILABLE_TETHER);
                ArrayList<String> active = intent
                        .getStringArrayListExtra(ConnectivityManager.EXTRA_ACTIVE_TETHER);
                ArrayList<String> errored = intent
                        .getStringArrayListExtra(ConnectivityManager.EXTRA_ERRORED_TETHER);
                // No preference summary need update, so add log
                Log.d(TAG, "onReceive available = " + available + " active = " + active
                        + " errored = " + errored);
            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
                enableWifiSwitch();
            }
        }
    };

    public TetherWifiApEnabler(SwitchBar switchBar, Context context,
            DataSaverBackend dataSaverBackend) {
        mContext = context;
        mSwitch = switchBar;
        mDataSaverBackend = dataSaverBackend;

        mCm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(ConnectivityManager.ACTION_TETHER_STATE_CHANGED);
        mIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    }

    public void resume() {
        mContext.registerReceiver(mReceiver, mIntentFilter);
        enableWifiSwitch();
    }

    public void pause() {
        mContext.unregisterReceiver(mReceiver);
    }

    private void enableWifiSwitch() {
        boolean isAirplaneMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (!isAirplaneMode) {
            mSwitch.setEnabled(!mDataSaverBackend.isDataSaverEnabled());
        } else {
            mSwitch.setEnabled(false);
        }
    }

    private void handleWifiApStateChanged(int state) {
        Log.d(TAG, "handleWifiApStateChanged state = " + state);
        switch (state) {
        case WifiManager.WIFI_AP_STATE_ENABLING:
            mSwitch.setEnabled(false);
            break;
        case WifiManager.WIFI_AP_STATE_ENABLED:
            mSwitch.setChecked(true);
            /* Doesnt need the airplane check */
            mSwitch.setEnabled(!mDataSaverBackend.isDataSaverEnabled());
            break;
        case WifiManager.WIFI_AP_STATE_DISABLING:
            mSwitch.setChecked(false);
            mSwitch.setEnabled(false);
            break;
        case WifiManager.WIFI_AP_STATE_DISABLED:
            mSwitch.setChecked(false);
            enableWifiSwitch();
            break;
        default:
            mSwitch.setChecked(false);
            enableWifiSwitch();
        }
    }
}
