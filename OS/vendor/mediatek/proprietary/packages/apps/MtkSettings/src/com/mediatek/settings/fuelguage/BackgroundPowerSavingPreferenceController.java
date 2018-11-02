/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.mediatek.settings.fuelguage;

import android.content.Context;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.util.Log;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.mediatek.settings.FeatureOption;

import static com.mediatek.provider.MtkSettingsExt.System.BG_POWER_SAVING_ENABLE;

public class BackgroundPowerSavingPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, Preference.OnPreferenceChangeListener {
    private static final String TAG = "BackgroundPowerSavingPreferenceContr";
    private static final String KEY_BACKGROUND_POWER_SAVING = "background_power_saving";

    public BackgroundPowerSavingPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return FeatureOption.MTK_BG_POWER_SAVING_SUPPORT
                   && FeatureOption.MTK_BG_POWER_SAVING_UI_SUPPORT;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BACKGROUND_POWER_SAVING;
    }

    @Override
    public void updateState(Preference preference) {
        int backgroundPowerSavingState = Settings.System.getInt(mContext.getContentResolver(),
                BG_POWER_SAVING_ENABLE, 1);
        Log.d(TAG, "update background power saving state: " + backgroundPowerSavingState);
        ((SwitchPreference) preference).setChecked(backgroundPowerSavingState != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int bgState = (Boolean) newValue ? 1 : 0;
        Log.d(TAG, "set background power saving state: " + bgState);
        Settings.System.putInt(mContext.getContentResolver(), BG_POWER_SAVING_ENABLE, bgState);
        return true;
    }
}
