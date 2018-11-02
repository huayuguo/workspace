package com.mediatek.settings.network;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.util.Log;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;

import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.IRCSSettings;
import com.mediatek.settings.ext.IWWOPJoynSettingsExt;

import java.util.List;

public class RcsePreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, LifecycleObserver {
    private static final String TAG = "RcsePrefContr";

    private static final String RCSE_SETTINGS_INTENT = "com.mediatek.rcse.RCSE_SETTINGS";
    private static final String KEY_RCSE_SETTINGS = "rcse_settings";

    IWWOPJoynSettingsExt mJoynExt;

    public RcsePreferenceController(Context context) {
        super(context);
        mJoynExt = UtilsExt.getWWOPJoynSettingsExt(context);
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!mJoynExt.isJoynSettingsEnabled()
            && KEY_RCSE_SETTINGS.equals(preference.getKey())) {
            Intent intent = new Intent(RCSE_SETTINGS_INTENT);
            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.w(TAG, "handlePreferenceTreeClick: startActivity failed" + e);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        if (mJoynExt.isJoynSettingsEnabled()) {
            Log.d(TAG, RCSE_SETTINGS_INTENT + " is enabled");
            return true;
        } else {
            Log.d(TAG, RCSE_SETTINGS_INTENT + " is not enabled");
            return false;
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY_RCSE_SETTINGS;
    }

    @Override
    public void updateNonIndexableKeys(List<String> keys) {
        if (!isAvailable()) {
            keys.add(getPreferenceKey());
        }
    }
}
