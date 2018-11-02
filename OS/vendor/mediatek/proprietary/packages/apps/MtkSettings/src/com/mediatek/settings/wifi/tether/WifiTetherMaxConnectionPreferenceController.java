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
import android.provider.Settings.System;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference;

import com.android.settings.wifi.tether.WifiTetherBasePreferenceController;
import com.mediatek.provider.MtkSettingsExt;

public class WifiTetherMaxConnectionPreferenceController
    extends WifiTetherBasePreferenceController {

    private static final String PREF_KEY = "wifi_tether_network_connections";
    private static final int WIFI_HOTSPOT_DEFAULT_CLIENT_NUM = 6;
    private int mMaxConnectionsValue = WIFI_HOTSPOT_DEFAULT_CLIENT_NUM;

    public WifiTetherMaxConnectionPreferenceController(Context context,
            OnTetherConfigUpdateListener listener) {
        super(context, listener);
        mMaxConnectionsValue = System.getInt(context.getContentResolver(),
                MtkSettingsExt.System.WIFI_HOTSPOT_MAX_CLIENT_NUM,
                WIFI_HOTSPOT_DEFAULT_CLIENT_NUM);
    }


    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ListPreference preference = (ListPreference) mPreference;
        preference.setSummary(preference.getEntries()[mMaxConnectionsValue - 1]);
        preference.setValue(String.valueOf(mMaxConnectionsValue));
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ListPreference listPreference = (ListPreference) preference;
        mMaxConnectionsValue = Integer.parseInt((String) newValue);
        System.putInt(mContext.getContentResolver(),
                MtkSettingsExt.System.WIFI_HOTSPOT_MAX_CLIENT_NUM, mMaxConnectionsValue);
        preference.setSummary(listPreference.getEntries()[mMaxConnectionsValue - 1]);
        mListener.onSecurityChanged();
        return true;
    }
}
