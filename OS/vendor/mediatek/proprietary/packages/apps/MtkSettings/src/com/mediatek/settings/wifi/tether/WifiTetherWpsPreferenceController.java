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

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settings.R;

import mediatek.net.wifi.WifiHotspotManager;

public class WifiTetherWpsPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, LifecycleObserver, OnPause, OnResume {

    private static final String PREF_KEY = "wifi_tether_network_wps";
    private static final String TAG = "WifiTetherWpsPreferenceController";

    private final FragmentManager mFragmentManager;
    private final WifiTetherWpsFragment mTetherWpsFragment;

    private static final IntentFilter WIFI_TETHER_WPS_FILTER;

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action: " + action);
            if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE,
                        WifiManager.WIFI_AP_STATE_FAILED);
                handleWifiApStateChanged(state);
            } else if (WifiHotspotManager.WIFI_WPS_CHECK_PIN_FAIL_ACTION.equals(action)) {
                Toast.makeText(context, R.string.wifi_tether_wps_pin_error, Toast.LENGTH_LONG)
                .show();
            } else if (WifiHotspotManager.WIFI_HOTSPOT_OVERLAP_ACTION.equals(action)) {
                Toast.makeText(context, R.string.wifi_wps_failed_overlap, Toast.LENGTH_LONG).show();
            }
        }
    };

    static {
        WIFI_TETHER_WPS_FILTER = new IntentFilter(
                WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        WIFI_TETHER_WPS_FILTER.addAction(WifiHotspotManager.WIFI_HOTSPOT_OVERLAP_ACTION);
        WIFI_TETHER_WPS_FILTER.addAction(WifiHotspotManager.WIFI_WPS_CHECK_PIN_FAIL_ACTION);
    }

    private Preference mWpsConnectPref;

    public WifiTetherWpsPreferenceController(Context context, Lifecycle lifecycle,
             FragmentManager fragmentManager) {
        super(context);
        mFragmentManager = fragmentManager;
        mTetherWpsFragment = new WifiTetherWpsFragment();
        lifecycle.addObserver(this);
    }

    @Override
    public boolean isAvailable() {
        // Always show preference.
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mWpsConnectPref = screen.findPreference(PREF_KEY);
        if (mWpsConnectPref == null) {
            return;
        }
        mWpsConnectPref.setOnPreferenceClickListener((arg) -> {
                    mTetherWpsFragment.show(mFragmentManager, PREF_KEY);
                    return true;
                }
        );
        mWpsConnectPref.setEnabled(false);
    }

    @Override
    public void onResume() {
        mContext.registerReceiver(mReceiver, WIFI_TETHER_WPS_FILTER);
    }

    @Override
    public void onPause() {
        mContext.unregisterReceiver(mReceiver);
    }

    private void handleWifiApStateChanged(int state) {
        switch (state) {
        case WifiManager.WIFI_AP_STATE_ENABLING:
            mWpsConnectPref.setEnabled(false);
            break;
        case WifiManager.WIFI_AP_STATE_ENABLED:
            mWpsConnectPref.setEnabled(true);
            break;
        case WifiManager.WIFI_AP_STATE_DISABLING:
        case WifiManager.WIFI_AP_STATE_DISABLED:
            mWpsConnectPref.setEnabled(false);
            break;
        default:
            break;
        }
    }

    /**
     * Fragment for Dialog to show WPS progress.
     */
    public static class WifiTetherWpsFragment extends InstrumentedDialogFragment {

        // Public default constructor is required for rotation.
        public WifiTetherWpsFragment() {
            super();
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.DIALOG_WPS_SETUP;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new WifiApWpsDialog(getActivity());
        }
    }
}
