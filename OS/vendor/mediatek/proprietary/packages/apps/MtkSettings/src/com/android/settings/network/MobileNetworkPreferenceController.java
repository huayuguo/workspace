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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.mediatek.ims.internal.MtkImsManager;
import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.settings.sim.TelephonyUtils;

import java.util.List;
import static android.os.UserHandle.myUserId;
import static android.os.UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS;

public class MobileNetworkPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin, LifecycleObserver, OnResume, OnPause {

    private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";

    private final UserManager mUserManager;
    private final boolean mIsSecondaryUser;
    private final TelephonyManager mTelephonyManager;
    private Preference mPreference;
    private int mSubId;
    private static final String TAG = "MobileNetworkPreferenceController";
    private static final String SUB_ID = "sub_id";
    private PhoneStateListener mPhoneStateListener;

    @VisibleForTesting

    public MobileNetworkPreferenceController(Context context) {
        super(context);
        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        mIsSecondaryUser = !mUserManager.isAdminUser();

        List<SubscriptionInfo> si = SubscriptionManager.from(
                context).getActiveSubscriptionInfoList();
        mSubId = MtkSubscriptionManager.getSubIdUsingPhoneId(TelephonyUtils
                                            .getMainCapabilityPhoneId());
        /*if (MtkImsManager.isSupportMims()) {
             mSubId = ((Activity) context).getIntent().getIntExtra(SUB_ID,
                           SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        }*/
    }

    @Override
    public boolean isAvailable() {
        return !isUserRestricted() && !Utils.isWifiOnly(mContext);
    }

    public boolean isUserRestricted() {
        final RestrictedLockUtilsWrapper wrapper = new RestrictedLockUtilsWrapper();
        return mIsSecondaryUser ||
                wrapper.hasBaseUserRestriction(
                        mContext,
                        DISALLOW_CONFIG_MOBILE_NETWORKS,
                        myUserId());
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mPreference = screen.findPreference(getPreferenceKey());
        }
    }

    @Override
    public String getPreferenceKey() {
        return KEY_MOBILE_NETWORK_SETTINGS;
    }

    @Override
    public void onResume() {
        IntentFilter intentFilter =
            new IntentFilter(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
        mContext.registerReceiver(mReceiver, intentFilter);
        if (isAvailable()) {
            if (Looper.myLooper() == null) {
                Log.d(TAG, "onResume Looper is null.");
                return;
            }
            if (mPhoneStateListener == null) {
                mPhoneStateListener = new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        super.onCallStateChanged(state, incomingNumber);
                        Log.d(TAG, "PhoneStateListener, new state=" + state);
                        if (state == TelephonyManager.CALL_STATE_IDLE) {
                            updateMobileNetworkEnabled();
                        }
                    }

                    @Override
                    public void onServiceStateChanged(ServiceState serviceState) {
                        /*if (mPreference != null) {
                            int simNum = SubscriptionManager.from(mContext).
                                    getActiveSubscriptionInfoCount();
                            if (simNum == 1) {
                                mPreference.setSummary(
                                mTelephonyManager.getNetworkOperatorName());
                            }
                        }*/
                    }
                };
            }
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_SERVICE_STATE |
                    PhoneStateListener.LISTEN_CALL_STATE);
        }
    }

    @Override
    public void onPause() {
        if (mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        mContext.unregisterReceiver(mReceiver);
    }

    @Override
    public void updateState(Preference preference) {
        if (preference instanceof RestrictedPreference) {
            RestrictedPreference rp = (RestrictedPreference) preference;
            if (rp.isDisabledByAdmin()) {
                Log.d(TAG, "updateState,Mobile Network preference disabled by Admin");
                return;
            }
        }
        List<SubscriptionInfo> si = SubscriptionManager.from(mContext).
                getActiveSubscriptionInfoList();
        try {
            if (si == null) {
                preference.setEnabled(false);
            } else {
                TelephonyManager telephonyManager =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                int callState = telephonyManager.getCallState();
                if (callState == TelephonyManager.CALL_STATE_IDLE) {
                    preference.setEnabled(true);
                } else {
                    preference.setEnabled(true);
                }
            }
        //Plugin need setSummary when two sims?
        } catch (IndexOutOfBoundsException ex) {
            android.util.Log.e("MobileNetworkPreferenceController", "IndexOutOfBoundsException");
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED.equals(action)) {
                Log.d("MobileNetworkPreferenceController", "ACTION_SIM_INFO_UPDATE received");
                updateMobileNetworkEnabled();
            // when received Carrier config changes, update WFC buttons
            }
        }
    };

    /// M: update MOBILE_NETWORK_SETTINGS enabled state by multiple conditions
    private void updateMobileNetworkEnabled() {
        if (mPreference == null) {
            return;
        }
        if (mPreference instanceof RestrictedPreference) {
            RestrictedPreference rp = (RestrictedPreference) mPreference;
            if (rp.isDisabledByAdmin()) {
                Log.d(TAG, "updateMobileNetworkEnabled,Mobile Network disabled by Admin");
                return;
            }
        }
        int simNum = SubscriptionManager.from(mContext).getActiveSubscriptionInfoCount();
            TelephonyManager telephonyManager =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            int callState = telephonyManager.getCallState();
            Log.d("MobileNetworkPreferenceController", "callstate = " + callState +
                    " simNum = " + simNum);
            if (simNum > 0 && callState == TelephonyManager.CALL_STATE_IDLE) {
                mPreference.setEnabled(true);
            } else {
                /// M: for plug-in
                mPreference.setEnabled(false);
            }
    }
}
