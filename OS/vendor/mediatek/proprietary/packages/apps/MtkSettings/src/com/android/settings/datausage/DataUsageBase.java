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

package com.android.settings.datausage;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.NetworkPolicyEditor;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener;
import static android.net.ConnectivityManager.TYPE_ETHERNET;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.NetworkPolicyEditor;

public abstract class DataUsageBase extends SettingsPreferenceFragment {
    public static final String TAG = "DataUsageBase";
    private static final String ETHERNET = "ethernet";

    protected final TemplatePreference.NetworkServices services =
            new TemplatePreference.NetworkServices();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Context context = getActivity();

        services.mNetworkService = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        services.mStatsService = INetworkStatsService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        services.mPolicyManager = NetworkPolicyManager.from(context);

        services.mPolicyEditor = new NetworkPolicyEditor(services.mPolicyManager);

        services.mTelephonyManager = TelephonyManager.from(context);
        services.mSubscriptionManager = SubscriptionManager.from(context);
        services.mUserManager = UserManager.get(context);
        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getActivity().getApplicationContext());
        mSimHotSwapHandler.registerOnSimHotSwap(new OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                if (getActivity() != null) {
                    Log.d(TAG, "onSimHotSwap, finish Activity~~");
                    getActivity().finish();
                }
            }
        });
        /// @}
    }

    @Override
    public void onResume() {
        super.onResume();
        services.mPolicyEditor.read();
    }

    protected boolean isAdmin() {
        return services.mUserManager.isAdminUser();
    }

    protected boolean isMobileDataAvailable(int subId) {
        if (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            Log.w(TAG, "isMobileDataAvailable INVALID_SUBSCRIPTION_ID");
            return false;
        }
        return services.mSubscriptionManager.getActiveSubscriptionInfo(subId) != null;
    }

    protected boolean isNetworkPolicyModifiable(NetworkPolicy policy, int subId) {
        return policy != null && isBandwidthControlEnabled() && services.mUserManager.isAdminUser()
                && isDataEnabled(subId);
    }

    private boolean isDataEnabled(int subId) {
        if (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            Log.w(TAG, "isDataEnabled INVALID_SUBSCRIPTION_ID");
            return false;
        }
        return services.mTelephonyManager.getDataEnabled(subId);
    }

    protected boolean isBandwidthControlEnabled() {
        try {
            return services.mNetworkService.isBandwidthControlEnabled();
        } catch (RemoteException e) {
            Log.w(TAG, "problem talking with INetworkManagementService: ", e);
            return false;
        }
    }

    /**
     * Test if device has an ethernet network connection.
     */
    public boolean hasEthernet(Context context) {
        if (DataUsageUtils.TEST_RADIOS) {
            return SystemProperties.get(DataUsageUtils.TEST_RADIOS_PROP).contains(ETHERNET);
        }

        final ConnectivityManager conn = ConnectivityManager.from(context);
        final boolean hasEthernet = conn.isNetworkSupported(TYPE_ETHERNET);

        final long ethernetBytes;
        try {
            INetworkStatsSession statsSession = services.mStatsService.openSession();
            if (statsSession != null) {
                ethernetBytes = statsSession.getSummaryForNetwork(
                        NetworkTemplate.buildTemplateEthernet(), Long.MIN_VALUE, Long.MAX_VALUE)
                        .getTotalBytes();
                TrafficStats.closeQuietly(statsSession);
            } else {
                ethernetBytes = 0;
            }
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        // only show ethernet when both hardware present and traffic has occurred
        return hasEthernet && ethernetBytes > 0;
    }

    ///------------------------------------MTK------------------------------------------------

    /// M: for [SIM Hot Swap]
    private SimHotSwapHandler mSimHotSwapHandler;

    @Override
    public void onDestroy() {
        super.onDestroy();
        /// M: for [Sim Hot Swap]
        mSimHotSwapHandler.unregisterOnSimHotSwap();
    }
}
