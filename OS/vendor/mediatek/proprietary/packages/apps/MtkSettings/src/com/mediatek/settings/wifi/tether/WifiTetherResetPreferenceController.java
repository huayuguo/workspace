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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.wifi.tether.WifiTetherBasePreferenceController;
import com.android.settings.R;

public class WifiTetherResetPreferenceController extends WifiTetherBasePreferenceController {

    private static final String PREF_KEY = "wifi_tether_network_reset";

    private final FragmentManager mFragmentManager;
    private Preference mResetNetworkPref;

    public WifiTetherResetPreferenceController(Context context,
            OnTetherConfigUpdateListener listener, FragmentManager fragmentManager) {
        super(context, listener);
        mFragmentManager = fragmentManager;
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
        mResetNetworkPref = screen.findPreference(PREF_KEY);
        if (mResetNetworkPref == null) {
            return;
        }
        mResetNetworkPref.setOnPreferenceClickListener((arg) -> {
                    ResetNetworkFragment resetNetworkFragment = new ResetNetworkFragment(mListener);
                    resetNetworkFragment.show(mFragmentManager, PREF_KEY);
                    return true;
                }
        );
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

    /**
     * Fragment for Dialog to show reset network progress.
     */
    public static class ResetNetworkFragment extends InstrumentedDialogFragment {

        private static OnTetherConfigUpdateListener sListener; 

        // Public default constructor is required for rotation.
        public ResetNetworkFragment() {
            super();
        }

        public ResetNetworkFragment(OnTetherConfigUpdateListener listener) {
            super();
            sListener = listener;
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.DIALOG_WPS_SETUP;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DialogInterface.OnClickListener onConfirm = (dialog, which) -> {
                sListener.onNetworkReset();
            };
                
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                            .setPositiveButton(R.string.wifi_ap_reset_OOB,
                                    onConfirm)
                            .setNegativeButton(android.R.string.cancel, null)
                            .create();
            dialog.setTitle(R.string.wifi_ap_reset_OOB);
            dialog.setMessage(getActivity().getString(R.string.wifi_ap_reset_OOB_title));
            return dialog;
        }
    }
}
