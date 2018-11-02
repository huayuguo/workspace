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

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;

import com.android.settings.widget.ValidatedEditTextPreference;
import com.android.settings.wifi.WifiUtils;

import java.util.UUID;

public class WifiTetherPasswordPreferenceController extends WifiTetherBasePreferenceController
        implements ValidatedEditTextPreference.Validator {

    private static final String PREF_KEY = "wifi_tether_network_password";

    private String mPassword;

    public WifiTetherPasswordPreferenceController(Context context,
            OnTetherConfigUpdateListener listener) {
        super(context, listener);
        final WifiConfiguration config = mWifiManager.getWifiApConfiguration();
        if (config != null) {
            mPassword = config.preSharedKey;
        }
    }

    @Override
    public String getPreferenceKey() {
        return PREF_KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        ((ValidatedEditTextPreference) mPreference).setValidator(this);
        updatePasswordDisplay((EditTextPreference) mPreference);

        /// M: Hotspot settings, update password base on security type @{
        final WifiConfiguration config = mWifiManager.getWifiApConfiguration();
        if (config == null || config.allowedKeyManagement == null
                || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA2_PSK)) {
            mPreference.setEnabled(true);
        } else {
            mPreference.setEnabled(false);
        }
        /// @}
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        mPassword = (String) newValue;
        updatePasswordDisplay((EditTextPreference) mPreference);
        mListener.onTetherConfigUpdated();
        return true;
    }

    public String getPassword() {
        return mPassword;
    }

    @Override
    public boolean isTextValid(String value) {
        return WifiUtils.isPasswordValid(value);
    }

    private void updatePasswordDisplay(EditTextPreference preference) {
        preference.setText(mPassword);
        preference.setSummary(mPassword);
    }

    /// M: Hotspot settings reset network will reset password @{
    public void setPassword(String password) {
        mPassword = password;
        updatePasswordDisplay((EditTextPreference) mPreference);
    }

    public void setEnabled(boolean status) {
        mPreference.setEnabled(status);
        if (status && TextUtils.isEmpty(mPassword)) {
            String randomUUID = UUID.randomUUID().toString();
            mPassword = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
            updatePasswordDisplay((EditTextPreference) mPreference);
        }
    }
    /// @}
}
