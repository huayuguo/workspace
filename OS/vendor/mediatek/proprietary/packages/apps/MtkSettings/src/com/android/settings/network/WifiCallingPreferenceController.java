/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.settings.network;

import android.app.Activity;
import android.content.Context;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.ims.ImsManager;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settings.SettingsActivity;
import com.android.settings.R;
import com.android.settings.WifiCallingSettings;
import com.mediatek.ims.internal.MtkImsManager;
import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.settings.ext.IWfcSettingsExt;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener;
import com.mediatek.settings.sim.TelephonyUtils;
import com.mediatek.settings.UtilsExt;
import java.util.List;
import mediatek.telephony.MtkCarrierConfigManager;

public class WifiCallingPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin {

    private static final String KEY_WFC_SETTINGS = "wifi_calling_settings";
    private static final String TAG = "WifiCallingPreferenceController";
    private static final String SUB_ID = "sub_id";
    private TelephonyManager mTm;
    /// M: Wfc plugin @{
    private IWfcSettingsExt mWfcExt;
    /// @}
    private SimHotSwapHandler mSimHotSwapHandler;

    public WifiCallingPreferenceController(Context context) {
        super(context);
        mTm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        /// M: for plug-in, make wfc setting plugin & add custom preferences @{
        mWfcExt = UtilsExt.getWfcSettingsExt(context);
        /// @}
        mSimHotSwapHandler = new SimHotSwapHandler(context);
        mSimHotSwapHandler.registerOnSimHotSwap(new OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                Log.d(TAG, "onSimHotSwap, finish Activity~~");
                ((SettingsActivity) context).finish();
            }
        });
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
           mWfcExt.customizedWfcPreference(mContext, screen);
        }
    }

    @Override
    public void updateState(Preference preference) {
        /// M: MTK change for WFC summary @{
        List<SubscriptionInfo> si = SubscriptionManager.from(
                mContext).getActiveSubscriptionInfoList();

        try {
            if (si != null && si.size() > 0) {
                if (MtkImsManager.isSupportMims() && si.size() > 1) {
                    preference.setSummary("");
                    Log.d(TAG, "Multi IMS supported and multi SIM inserted, so no wfc summary");
                } else {
                    int subId;
                    int phoneId;

                    if (MtkImsManager.isSupportMims()) {
                        subId = si.get(0).getSubscriptionId();
                        phoneId = SubscriptionManager.getPhoneId(subId);
                        Log.d(TAG, "Multi IMS supported and one SIM inserted,"
                                + ", phoneId=" + phoneId + ", subId=" + subId);
                    } else {
                        phoneId = TelephonyUtils.getMainCapabilityPhoneId();
                        subId = MtkSubscriptionManager.getSubIdUsingPhoneId(phoneId);
                        Log.d(TAG, "Multi IMS not supported,"
                                + ", mainPhoneId=" + phoneId + ", subId=" + subId);
                    }

                    CarrierConfigManager configManager = (CarrierConfigManager)
                            mContext.getSystemService(Context.CARRIER_CONFIG_SERVICE);
                    boolean removeWfcPrefMode = false;

                    if (configManager != null) {
                        PersistableBundle configBundle = configManager.getConfigForSubId(subId);
                        if (configBundle != null) {
                            removeWfcPrefMode =
                                    configBundle.getBoolean(MtkCarrierConfigManager
                                            .MTK_KEY_WFC_REMOVE_PREFERENCE_MODE_BOOL);
                            Log.d(TAG, "removeWfcPrefMode=" + removeWfcPrefMode
                                    + ", phoneId=" + phoneId + ", subId=" + subId);
                        }
                    }

                    if (removeWfcPrefMode) {
                        preference.setSummary("");
                        Log.d(TAG, "remove wfc mode is true, so no wfc summary");
                    } else {
                        int resId = R.string.wifi_calling_disabled;
                        MtkImsManager imsMgr = (MtkImsManager)
                                ImsManager.getInstance(mContext, phoneId);
                        if (imsMgr.isWfcEnabledByPlatformForSlot()
                                && imsMgr.isWfcProvisionedOnDeviceForSlot()
                                && mWfcExt.isWifiCallingProvisioned(mContext, phoneId)) {
                            resId = WifiCallingSettings.getWfcModeSummary(
                                        mContext,
                                        MtkImsManager.getWfcMode(mContext,
                                            mTm.isNetworkRoaming(subId), phoneId),
                                        phoneId);
                        }
                        preference.setSummary(mWfcExt.getWfcSummary(mContext, resId));
                        Log.d(TAG, "set wfc summary for phoneId=" + phoneId
                                + ", subId=" + subId);
                    }
                }
            } else {
                preference.setSummary(mWfcExt.getWfcSummary(
                        mContext, R.string.wifi_calling_disabled));
                Log.d(TAG, "Subscription Info List is null");
            }
        //Plugin need setSummary when two sims?
        } catch (IndexOutOfBoundsException ex) {
            Log.e(TAG, "IndexOutOfBoundsException");
        }
        /// @}
        /* AOSP code
        preference.setSummary(WifiCallingSettings.getWfcModeSummary(
                mContext, ImsManager.getWfcMode(mContext, mTm.isNetworkRoaming())));
        */
    }

    @Override
    public boolean isAvailable() {
        if (MtkImsManager.isSupportMims()) {
            if ((SystemProperties.getInt("persist.mtk_wfc_support", 0) == 1)
                    && (SystemProperties.getInt("ro.boot.opt_lte_support", 0) == 1)) {
                Log.d(TAG, "Multi IMS supported so WifiCallingPreference always available");
                return true;
            } else {
                Log.d(TAG, "Multi IMS supported but WFC not supported");
                return false;
            }
        }
        return ImsManager.isWfcEnabledByPlatform(mContext)
                && ImsManager.isWfcProvisionedOnDevice(mContext)
                        && mWfcExt.isWifiCallingProvisioned(mContext,
                                    TelephonyUtils.getMainCapabilityPhoneId());
    }

    @Override
    public String getPreferenceKey() {
        return KEY_WFC_SETTINGS;
    }
}
