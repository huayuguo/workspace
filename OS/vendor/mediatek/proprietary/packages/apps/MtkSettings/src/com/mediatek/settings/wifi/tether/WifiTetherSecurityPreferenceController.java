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

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference;
import android.util.Log;

import com.mediatek.settings.ext.IWifiTetherSettingsExt;
import com.mediatek.settings.UtilsExt;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController;

public class WifiTetherSecurityPreferenceController extends WifiTetherBasePreferenceController {

    private static final String PREF_KEY = "wifi_tether_network_security";
    public static final int OPEN_INDEX = 0;
    public static final int WPA2_INDEX = 1;

    private int mSecurityTypeIndex = WPA2_INDEX;
    private IWifiTetherSettingsExt mWifiTetherSettingsExt;
    public WifiTetherSecurityPreferenceController(Context context,
            OnTetherConfigUpdateListener listener) {
        super(context, listener);
        mWifiTetherSettingsExt = UtilsExt.getWifiTetherSettingsExt(context);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final WifiConfiguration config = mWifiManager.getWifiApConfiguration();
        mWifiTetherSettingsExt.customizePreference(screen);
        updateSecurityDisplay((ListPreference) mPreference, getSecurityTypeIndex(config));
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        updateSecurityDisplay((ListPreference) preference, Integer.parseInt((String) newValue));
        mListener.onSecurityChanged();
        return true;
    }

    public int getSecurityIndex() {
        return mSecurityTypeIndex;
    }

    public void setSecurityIndex(int index) {
        updateSecurityDisplay((ListPreference) mPreference, index);
    }

    private void updateSecurityDisplay(ListPreference preference, int index) {
        mSecurityTypeIndex = index;
        preference.setSummary(preference.getEntries()[mSecurityTypeIndex]);
        preference.setValue(String.valueOf(mSecurityTypeIndex));
    }

    private int getSecurityTypeIndex(WifiConfiguration wifiConfig) {
        if (wifiConfig == null || wifiConfig.allowedKeyManagement == null
                || wifiConfig.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA2_PSK)) {
            return WPA2_INDEX;
        }
        return OPEN_INDEX;
    }
}
