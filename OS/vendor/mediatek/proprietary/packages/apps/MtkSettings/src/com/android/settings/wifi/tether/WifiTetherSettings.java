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

package com.android.settings.wifi.tether;

import static android.net.ConnectivityManager.ACTION_TETHER_STATE_CHANGED;
import static android.net.wifi.WifiManager.WIFI_AP_STATE_CHANGED_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.dashboard.RestrictedDashboardFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBarController;
import com.android.settingslib.core.AbstractPreferenceController;

import com.mediatek.settings.ext.IWifiTetherSettingsExt;
import com.mediatek.settings.UtilsExt;

import com.mediatek.settings.wifi.tether.WifiTetherDisablePolicyPreferenceController;
import com.mediatek.settings.wifi.tether.WifiTetherMaxConnectionPreferenceController;
import com.mediatek.settings.wifi.tether.WifiTetherResetPreferenceController;
import com.mediatek.settings.wifi.tether.WifiTetherSecurityPreferenceController;
import com.mediatek.settings.wifi.tether.WifiTetherUserPreferenceController;
import com.mediatek.settings.wifi.tether.WifiTetherWpsPreferenceController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class WifiTetherSettings extends RestrictedDashboardFragment
        implements WifiTetherBasePreferenceController.OnTetherConfigUpdateListener {

    public static boolean isTetherSettingPageEnabled() {
        return SystemProperties.getBoolean("settings.ui.wifi.tether.enabled", false);
    }

    private static final IntentFilter TETHER_STATE_CHANGE_FILTER;

    private WifiTetherSwitchBarController mSwitchBarController;
    private WifiTetherSSIDPreferenceController mSSIDPreferenceController;
    private WifiTetherPasswordPreferenceController mPasswordPreferenceController;
    private WifiTetherApBandPreferenceController mApBandPreferenceController;

    private WifiManager mWifiManager;
    private boolean mRestartWifiApAfterConfigChange;

    private IWifiTetherSettingsExt mWifiTetherSettingsExt;

    @VisibleForTesting
    TetherChangeReceiver mTetherChangeReceiver;

    static {
        TETHER_STATE_CHANGE_FILTER = new IntentFilter(ACTION_TETHER_STATE_CHANGED);
        TETHER_STATE_CHANGE_FILTER.addAction(WIFI_AP_STATE_CHANGED_ACTION);
    }

    public WifiTetherSettings() {
        super(UserManager.DISALLOW_CONFIG_TETHERING);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.WIFI_TETHER_SETTINGS;
    }

    @Override
    protected String getLogTag() {
        return "WifiTetherSettings";
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mTetherChangeReceiver = new TetherChangeReceiver();

        // / M: Hotspot manager settings @{
        int tileLimit = 3;
        mProgressiveDisclosureMixin.setTileLimit(tileLimit);
        // / @}
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Assume we are in a SettingsActivity. This is only safe because we currently use
        // SettingsActivity as base for all preference fragments.
        final SettingsActivity activity = (SettingsActivity) getActivity();
        final SwitchBar switchBar = activity.getSwitchBar();
        mSwitchBarController = new WifiTetherSwitchBarController(activity,
                new SwitchBarController(switchBar));
        getLifecycle().addObserver(mSwitchBarController);
        switchBar.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Context context = getContext();
        if (context != null) {
            context.registerReceiver(mTetherChangeReceiver, TETHER_STATE_CHANGE_FILTER);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        final Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(mTetherChangeReceiver);
        }
    }


    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_tether_settings;
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        mWifiTetherSettingsExt = UtilsExt.getWifiTetherSettingsExt(context);
        mSSIDPreferenceController = new WifiTetherSSIDPreferenceController(context, this);
        mPasswordPreferenceController = new WifiTetherPasswordPreferenceController(context, this);
        mApBandPreferenceController = new WifiTetherApBandPreferenceController(context, this);
        /// M: Hotspot manager settings @{
        mSecurityPreferenceController = new WifiTetherSecurityPreferenceController(context, this);
        mDisablePolicyPreferenceController
                                      = new WifiTetherDisablePolicyPreferenceController(context);
        mMaxConnectionPreferenceController
                                      = new WifiTetherMaxConnectionPreferenceController(context, this);
        mWpsPreferenceController = new WifiTetherWpsPreferenceController(context, getLifecycle(), getFragmentManager());
        mResetPreferenceController = new WifiTetherResetPreferenceController(context, this, getFragmentManager());
        mUserPreferenceController = new WifiTetherUserPreferenceController(context, getLifecycle());
        /// @}

        controllers.add(mSSIDPreferenceController);
        controllers.add(mPasswordPreferenceController);
        controllers.add(mApBandPreferenceController);
        /// M: Hotspot manager settings @{
        controllers.add(mSecurityPreferenceController);
        controllers.add(mDisablePolicyPreferenceController);
        controllers.add(mMaxConnectionPreferenceController);
        controllers.add(mWpsPreferenceController);
        controllers.add(mResetPreferenceController);
        controllers.add(mUserPreferenceController);
        /// @}

        /// M: Plugin feature @{
        mWifiTetherSettingsExt.addPreferenceController(context, controllers, this);
        /// @}

        return controllers;
    }

    @Override
    public void onTetherConfigUpdated() {
        final WifiConfiguration config = buildNewConfig();
        /**
         * if soft AP is stopped, bring up
         * else restart with new config
         * TODO: update config on a running access point when framework support is added
         */
        if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
            Log.d("TetheringSettings",
                    "Wifi AP config changed while enabled, stop and restart");
            mRestartWifiApAfterConfigChange = true;
            mSwitchBarController.stopTether();
        }
        mWifiManager.setWifiApConfiguration(config);
    }

    private WifiConfiguration buildNewConfig() {
        final WifiConfiguration config = new WifiConfiguration();

        config.SSID = mSSIDPreferenceController.getSSID();
        config.preSharedKey = mPasswordPreferenceController.getPassword();

        /// M: Hotspot manager settings, set security @{
        switch (mSecurityPreferenceController.getSecurityIndex()) {

        case WifiTetherSecurityPreferenceController.OPEN_INDEX:
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            break;
        case WifiTetherSecurityPreferenceController.WPA2_INDEX:
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA2_PSK);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            break;
        }
        /// @}
        config.apBand = mApBandPreferenceController.getBandIndex();
        return config;
    }

    @VisibleForTesting
    class TetherChangeReceiver extends BroadcastReceiver {
        private static final String TAG = "TetherChangeReceiver";

        @Override
        public void onReceive(Context content, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_TETHER_STATE_CHANGED)) {
                if (mWifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_DISABLED
                        && mRestartWifiApAfterConfigChange) {
                    mRestartWifiApAfterConfigChange = false;
                    Log.d(TAG, "Restarting WifiAp due to prior config change.");
                    mSwitchBarController.startTether();
                }
            } else if (action.equals(WIFI_AP_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_AP_STATE, 0);
                if (state == WifiManager.WIFI_AP_STATE_DISABLED
                        && mRestartWifiApAfterConfigChange) {
                    mRestartWifiApAfterConfigChange = false;
                    Log.d(TAG, "Restarting WifiAp due to prior config change.");
                    mSwitchBarController.startTether();
                }
            }
        }
    }

    /// M: Hotspot manager settings, onSecurityChanged & onNetworkReset @{
    private static final int RAND_SSID_INT_MIN = 1000;
    private static final int RAND_SSID_INT_MAX = 9999;

    private WifiTetherSecurityPreferenceController mSecurityPreferenceController;
    private WifiTetherDisablePolicyPreferenceController mDisablePolicyPreferenceController;
    private WifiTetherMaxConnectionPreferenceController mMaxConnectionPreferenceController;
    private WifiTetherWpsPreferenceController mWpsPreferenceController;
    private WifiTetherResetPreferenceController mResetPreferenceController;
    private WifiTetherUserPreferenceController mUserPreferenceController;

    @Override
    public void onNetworkReset() {
        // Reset ssid as random ssid
        Random random = new Random();
        int randomSsid = random.nextInt((RAND_SSID_INT_MAX - RAND_SSID_INT_MIN) + 1) + RAND_SSID_INT_MIN;
        String ssid = WifiTetherSSIDPreferenceController.DEFAULT_SSID + "_" + randomSsid;
        mSSIDPreferenceController.setSSID(ssid);

        // Reset security to WPA2
        mSecurityPreferenceController.setSecurityIndex(WifiTetherSecurityPreferenceController.WPA2_INDEX);

        // Reset ssid password random password and enabled password preference
        mPasswordPreferenceController.setEnabled(true);
        String randomUUID = UUID.randomUUID().toString();
        String randomPassword = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
        mPasswordPreferenceController.setPassword(randomPassword);
        // Change config
        onTetherConfigUpdated();
    }

    @Override
    public void onSecurityChanged() {
        // Set password preference status depend on security type
        mPasswordPreferenceController
                .setEnabled(mSecurityPreferenceController.getSecurityIndex()
                        == WifiTetherSecurityPreferenceController.WPA2_INDEX);
        // Change config
        onTetherConfigUpdated();
    }
    /// @}
}
