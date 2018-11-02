/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.mediatek.settings.wifi.tether;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;

import mediatek.net.wifi.HotspotClient;
import mediatek.net.wifi.WifiHotspotManager;

public class WifiTetherUserPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {

    private static final String TAG = "WifiTetherClientPreferenceController";
    private static final IntentFilter WIFI_TETHER_USER_CHANGED_FILTER;
    private final WifiHotspotManager mHotspotManager;

    private Preference mConnectedPrefer;
    private Preference mBlockedPrefer;

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleWifiApClientsChanged();
        }
    };

    static {
        WIFI_TETHER_USER_CHANGED_FILTER = new IntentFilter(
                "android.net.wifi.WIFI_HOTSPOT_CLIENTS_IP_READY");
        WIFI_TETHER_USER_CHANGED_FILTER
                .addAction(WifiHotspotManager.WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION);
    }

    public WifiTetherUserPreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mHotspotManager = wifiManager.getWifiHotspotManager();
        lifecycle.addObserver(this);
    }

    @Override
    public boolean isAvailable() {
        // Always show preference.
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return null;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mConnectedPrefer = screen.findPreference("wifi_tether_connected_user");
        mBlockedPrefer = screen.findPreference("wifi_tether_blocked_user");
    }

    @Override
    public void onResume() {
        mContext.registerReceiver(mReceiver, WIFI_TETHER_USER_CHANGED_FILTER);
        handleWifiApClientsChanged();
    }

    @Override
    public void onPause() {
        mContext.unregisterReceiver(mReceiver);
    }

    private void handleWifiApClientsChanged() {
        int blockNum = 0;
        int connectNum = 0;
        List<HotspotClient> clientList = mHotspotManager.getHotspotClients();
        if (clientList != null) {
            for (HotspotClient client : clientList) {
                if (client.isBlocked) {
                    blockNum++;
                } else {
                    connectNum++;
                }
            }
        }
        if (mBlockedPrefer != null) {
            mBlockedPrefer.setSummary(String.valueOf(blockNum));
            if (blockNum == 0) {
                mBlockedPrefer.setEnabled(false);
            } else {
                mBlockedPrefer.setEnabled(true);
            }
        }
        if (mConnectedPrefer != null) {
            mConnectedPrefer.setSummary(String.valueOf(connectNum));
            if (connectNum == 0) {
                mConnectedPrefer.setEnabled(false);
            } else {
                mConnectedPrefer.setEnabled(true);
            }
        }
    }
}
