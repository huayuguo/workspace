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

public class WifiTetherDisablePolicyPreferenceController
    extends WifiTetherBasePreferenceController {

    private static final String PREF_KEY = "wifi_tether_network_disable_policy";
    private int mDisablePolicyValue = MtkSettingsExt.System.WIFI_HOTSPOT_AUTO_DISABLE_FOR_FIVE_MINS;

    public WifiTetherDisablePolicyPreferenceController(Context context) {
        super(context, null);
        mDisablePolicyValue = System.getInt(context.getContentResolver(),
                MtkSettingsExt.System.WIFI_HOTSPOT_AUTO_DISABLE,
                MtkSettingsExt.System.WIFI_HOTSPOT_AUTO_DISABLE_FOR_FIVE_MINS);
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ListPreference preference = (ListPreference) mPreference;
        preference.setSummary(preference.getEntries()[mDisablePolicyValue]);
        preference.setValue(String.valueOf(mDisablePolicyValue));
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ListPreference listPreference = (ListPreference) preference;
        mDisablePolicyValue = Integer.parseInt((String) newValue);
        preference.setSummary(listPreference.getEntries()[mDisablePolicyValue]);
        System.putInt(mContext.getContentResolver(),
                MtkSettingsExt.System.WIFI_HOTSPOT_AUTO_DISABLE, mDisablePolicyValue);
        return true;
    }
}
