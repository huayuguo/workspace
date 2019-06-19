/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.tv.mysettings.about;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CarrierConfigManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.tv.mysettings.R;
import com.android.tv.mysettings.name.DeviceManager;
//import com.android.settingslib.DeviceInfoUtils;

//import android.support.v7.preference.Preference;
//import android.support.v7.preference.PreferenceScreen;

//import com.android.internal.telephony.TelephonyProperties;
//import com.android.settingslib.DeviceInfoUtils;
//import com.android.tv.mysettings.name.DeviceManager;

public class AboutFragment extends PreferenceFragment {
    private static final String TAG = "AboutFragment";

    private static final String KEY_MANUAL = "manual";
    private static final String KEY_REGULATORY_INFO = "regulatory_info";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";
    private static final String PROPERTY_SELINUX_STATUS = "ro.build.selinux";
    private static final String KEY_KERNEL_VERSION = "kernel_version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_SELINUX_STATUS = "selinux_status";
    private static final String KEY_BASEBAND_VERSION = "baseband_version";
    private static final String KEY_FIRMWARE_VERSION = "firmware_version";
    private static final String KEY_SECURITY_PATCH = "security_patch";
    private static final String KEY_UPDATE_SETTING = "additional_system_update_settings";
    private static final String KEY_EQUIPMENT_ID = "fcc_equipment_id";
    private static final String PROPERTY_EQUIPMENT_ID = "ro.ril.fccid";
    private static final String KEY_DEVICE_FEEDBACK = "device_feedback";
    private static final String KEY_SAFETY_LEGAL = "safetylegal";
    private static final String KEY_DEVICE_NAME = "device_name";
    private static final String KEY_RESTART = "restart";

    static final int TAPS_TO_BE_A_DEVELOPER = 7;

    int mDevHitCountdown;
    Toast mDevHitToast;


    private final BroadcastReceiver mDeviceNameReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshDeviceName();
        }
    };

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView lv = view.findViewById(android.R.id.list);
        if (lv != null) {
            lv.setDivider(new ColorDrawable(Color.TRANSPARENT));
            lv.setSelector(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.device_info_settings);

        final Preference firmwareVersionPref = findPreference(KEY_FIRMWARE_VERSION);
        firmwareVersionPref.setSummary(Build.VERSION.RELEASE);
        firmwareVersionPref.setEnabled(true);

        //findPreference(KEY_DEVICE_MODEL).setSummary(Build.MODEL + DeviceInfoUtils.getMsvSuffix());
    }

    private void removePreference(@Nullable Preference preference) {
        if (preference != null) {
            getPreferenceScreen().removePreference(preference);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshDeviceName();

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mDeviceNameReceiver,
                new IntentFilter(DeviceManager.ACTION_DEVICE_NAME_UPDATE));
    }

    @Override
    public void onResume() {
        super.onResume();
        final boolean developerEnabled = Settings.Global.getInt(getActivity().getContentResolver(),
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                android.os.Build.TYPE.equals("eng") ? 1 : 0) == 1;
        mDevHitCountdown = developerEnabled ? -1 : TAPS_TO_BE_A_DEVELOPER;
        mDevHitToast = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDeviceNameReceiver);
    }

    private void refreshDeviceName() {
        final Preference deviceNamePref = findPreference(KEY_DEVICE_NAME);
        if (deviceNamePref != null) {
            deviceNamePref.setSummary(DeviceManager.getDeviceName(getActivity()));
        }
    }
}
