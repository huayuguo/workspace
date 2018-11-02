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
package com.mediatek.settings.display;

import android.content.Context;
import android.content.res.Resources;
import android.provider.Settings;
import android.support.v7.preference.Preference;

import com.android.settings.R;
import com.mediatek.settings.accessibility.CustomToggleFontSizePreferenceFragment;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.mediatek.settings.ext.IDisplaySettingsExt;
import com.mediatek.settings.UtilsExt;

public class CustomFontSizePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_CUSTOM_FONT_SIZE = "custom_font_size";
    private IDisplaySettingsExt customFontSizePref;
    private Context mContext;

    public CustomFontSizePreferenceController(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public boolean isAvailable() {
        /* ToDo: Only available for OP18 */
        customFontSizePref = UtilsExt.getDisplaySettingsExt(mContext);
        if(customFontSizePref.isCustomPrefPresent()) {
            return true;
        }
        return false;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_CUSTOM_FONT_SIZE;
    }

    @Override
    public void updateState(Preference preference) {
        final float currentScale = Settings.System.getFloat(mContext.getContentResolver(),
                Settings.System.FONT_SCALE, 1.0f);
        final Resources res = mContext.getResources();
        final String[] entries = res.getStringArray(R.array.custom_entries_font_size);
        final String[] strEntryValues = res.getStringArray(R.array.custom_entryvalues_font_size);
        final int index = CustomToggleFontSizePreferenceFragment.fontSizeValueToIndex(currentScale,
                strEntryValues);
        preference.setSummary(entries[index]);
    }
}
