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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.dashboard.RestrictedDashboardFragment;

import mediatek.net.wifi.HotspotClient;
import mediatek.net.wifi.WifiHotspotManager;

import java.util.ArrayList;
import java.util.List;

public class WifiTetherUserListSettings extends RestrictedDashboardFragment {

    private static final String TAG = "WifiTetherUserListSettings";
    private static final String EXTRA_USERMODE = "usermode";
    private static final IntentFilter WIFI_TETHER_USER_CHANGED_FILTER;

    static {
        WIFI_TETHER_USER_CHANGED_FILTER = new IntentFilter(
                "android.net.wifi.WIFI_HOTSPOT_CLIENTS_IP_READY");
        WIFI_TETHER_USER_CHANGED_FILTER
                .addAction(WifiHotspotManager.WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION);
    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWifiApClients();
        }
    };
    private WifiHotspotManager mHotspotManager;
    private int mUserMode = 0; // block 1, connect 0

    public WifiTetherUserListSettings() {
        super(UserManager.DISALLOW_CONFIG_TETHERING);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.WIFI_TETHER_SETTINGS;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mHotspotManager = wifiManager.getWifiHotspotManager();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mUserMode = args != null ? args.getInt(EXTRA_USERMODE) : 0;
        if (mUserMode == 0) {
            getPreferenceScreen().setTitle(R.string.wifi_ap_connected_title);
        } else if (mUserMode == 1) {
            getPreferenceScreen().setTitle(R.string.wifi_ap_blocked_title);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWifiApClients();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Context context = getContext();
        if (context != null) {
            context.registerReceiver(mReceiver, WIFI_TETHER_USER_CHANGED_FILTER);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        final Context context = getContext();
        if (context != null) {
            context.unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_tether_user_settings;
    }

    private void updateWifiApClients() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            return;
        }
        screen.removeAll();
        Context prefContext = screen.getPreferenceManager().getContext();
        List<HotspotClient> clientList = mHotspotManager.getHotspotClients();
        if (clientList != null) {
            Log.d(TAG, "client number is " + clientList.size());
            for (HotspotClient client : clientList) {
                if ((mUserMode == 0 && !client.isBlocked) || (mUserMode == 1 && client.isBlocked)) {
                       String deviceName = mHotspotManager.getClientDeviceName(client.deviceAddress);
                        Preference preference = new Preference(prefContext);
                        preference.setTitle(deviceName);
                        preference.setOnPreferenceClickListener((arg) -> {
                            WifiTetherClientFragment clientFragment =
                                new WifiTetherClientFragment(client, mHotspotManager);
                            clientFragment.show(getFragmentManager(), "WifiTetherClientFragment");
                            return true;
                        });
                        screen.addPreference(preference);
                    }
                }
            }
        }

    /**
     * Fragment for Dialog to show WPS progress.
     */
    public static class WifiTetherClientFragment extends InstrumentedDialogFragment {
        private static HotspotClient sClient;
        private static WifiHotspotManager sHotspotManager;

        // Public default constructor is required for rotation.
        public WifiTetherClientFragment() {
            super();
        }

        public WifiTetherClientFragment(HotspotClient client, WifiHotspotManager hotspotManager) {
            super();
            sClient = client;
            sHotspotManager = hotspotManager;
        }

        @Override
        public int getMetricsCategory() {
            return MetricsEvent.DIALOG_WPS_SETUP;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DialogInterface.OnClickListener onConfirm = (dialog, which) -> {
                if (sClient.isBlocked) {
                    Log.d(TAG, "onClick,client is blocked, unblock now");
                    sHotspotManager.unblockClient(sClient);
                } else {
                    Log.d(TAG, "onClick,client isn't blocked, block now");
                    sHotspotManager.blockClient(sClient);
                }
            };

            AlertDialog dialog = new AlertDialog.Builder(getActivity())
            .setPositiveButton(sClient.isBlocked ? R.string.wifi_ap_client_unblock_title
                            : R.string.wifi_ap_client_block_title, onConfirm)
            .setNegativeButton(android.R.string.cancel, null)
            .create();
            View detailView = getActivity().getLayoutInflater().inflate(
                    R.layout.wifi_ap_client_dialog, null);
            ((TextView) detailView.findViewById(R.id.mac_address)).setText(sClient.deviceAddress);
            if (sClient.isBlocked) {
                detailView.findViewById(R.id.ip_filed).setVisibility(View.GONE);
            } else {
                detailView.findViewById(R.id.ip_filed).setVisibility(View.VISIBLE);
                ((TextView) detailView.findViewById(R.id.ip_address))
                     .setText(sHotspotManager.getClientIp(sClient.deviceAddress));
            }
            dialog.setTitle(sHotspotManager.getClientDeviceName(sClient.deviceAddress));
            dialog.setView(detailView);
            return dialog;
        }
    }

    @Override
    protected String getLogTag() {
        return "WifiTetherUserListSettings";
    }

    @Override
    protected List<AbstractPreferenceController> getPreferenceControllers(Context context) {
        return null;
    }
}
