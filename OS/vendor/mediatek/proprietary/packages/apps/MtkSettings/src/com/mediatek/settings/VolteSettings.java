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
 * limitations under the License.
 */

package com.mediatek.settings;

import android.app.ActionBar;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;

import com.mediatek.ims.internal.MtkImsManager;
import com.mediatek.ims.internal.MtkImsManagerEx;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.MtkPhoneConstants;
import com.mediatek.internal.telephony.MtkSubscriptionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * "Volte settings" screen.  This preference screen lets you
 * enable/disable Volte and change Volte mode.
 */
public class VolteSettings extends SettingsPreferenceFragment
        implements SwitchBar.OnSwitchChangeListener,
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "VolteSettings";

    //String keys for preference lookup
    private static final String BUTTON_VOLTE_MODE = "volte_mode";
    private static final String VOLTE_MODE_PREF = "volte_mode_pref";

    //UI objects
    private SwitchBar mSwitchBar;
    private Switch mSwitch;
    private TextView mEmptyView;

    private boolean mValidListener = false;
    private CheckBoxPreference mVolteMode;
    private SubscriptionManager mSubscriptionManager;
    private List<SubscriptionInfo> mActiveSubInfos;
    private int mDefaultImsPhoneId;
    private int mDefaultImsSubId;
    private Context mContext;

    private Phone mPhone;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final SettingsActivity activity = (SettingsActivity) getActivity();
        mContext = getActivity();
        mSwitchBar = activity.getSwitchBar();
        mSwitch = mSwitchBar.getSwitch();
        mSwitchBar.show();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSwitchBar.hide();
    }

    /**
     * Provide active subinfo list.
     * @return subinfo list.
     */
    public List<SubscriptionInfo> getActiveSubInfoList() {
        List<SubscriptionInfo> subInfoList;
        try {
            subInfoList = SubscriptionManager.
                from(mContext).getActiveSubscriptionInfoList();
        } catch (Exception e) {
            subInfoList = null;
        }
        if (subInfoList == null) {
            return new ArrayList<SubscriptionInfo>();
        }
        return subInfoList;
    }

    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener
        = new SubscriptionManager.OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            Log.d(TAG, "onSubscriptionsChanged:");
            List<SubscriptionInfo> currentSubInfos = getActiveSubInfoList();
            if (mActiveSubInfos != null && !mActiveSubInfos.equals(currentSubInfos)) {
                for (SubscriptionInfo item : mActiveSubInfos) {
                    Log.d(TAG, "prevSubInfos");
                    Log.d(TAG, item.toString());
                }
                for (SubscriptionInfo item : currentSubInfos) {
                    Log.d(TAG, "currentSubInfos");
                    Log.d(TAG, item.toString());
                }
                mActiveSubInfos = currentSubInfos;
                int newSubId = MtkSubscriptionManager
                        .getSubIdUsingPhoneId(getDefaultImsPhoneId(getActivity()));
                if (mDefaultImsSubId != newSubId) {
                    mDefaultImsSubId = newSubId;
                    finish();
                }
            }
            //finish();
        }
    };

    private IntentFilter mIntentFilter;

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "action: " + action);
            /// When receive aiplane mode, we would like to finish the activity
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                getActivity().finish();
            }
        }
    };

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.WIFI_CALLING;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.volte_settings);
        mVolteMode = (CheckBoxPreference) findPreference(BUTTON_VOLTE_MODE);
        mVolteMode.setPersistent(true);
        mVolteMode.setOnPreferenceChangeListener(this);
        //mVolteModePref = (CheckBoxPreference)findViewById(R.id.volte_mode_pref);
        mSubscriptionManager = SubscriptionManager.from(getActivity());
        mActiveSubInfos = mSubscriptionManager.getActiveSubscriptionInfoList();
        mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIntentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        getActivity().registerReceiver(mIntentReceiver, mIntentFilter);
        final Context context = getActivity();
        if (ImsManager.isVolteEnabledByPlatform(context)) {
            mSwitchBar.addOnSwitchChangeListener(this);

            mValidListener = true;
        }
        int isChecked = Settings.Global.getInt(getContentResolver(),
                Settings.Global.ENHANCED_4G_MODE_ENABLED, 0);
        mSwitch.setChecked(isChecked == 1);
        if (isChecked != 1) {
            mVolteMode.setEnabled(false);
        }
        SharedPreferences sharedPref = getActivity().getSharedPreferences(VOLTE_MODE_PREF, 0);
        Boolean isModeChecked = sharedPref.getBoolean("checked", true);
        mVolteMode.setChecked(isModeChecked);
        mDefaultImsPhoneId = getDefaultImsPhoneId(context);
        mDefaultImsSubId = MtkSubscriptionManager.getSubIdUsingPhoneId(mDefaultImsPhoneId);
        try {
            mPhone = PhoneFactory.getPhone(mDefaultImsPhoneId);
        } catch (Exception e) {
            Log.e(TAG, "Phone not available");
            Toast.makeText(getActivity().getApplicationContext(), "Phone not available",
                    Toast.LENGTH_LONG).show();
            finish();
        }
        int settingsNetworkMode = android.provider.Settings.Global.getInt(
                mPhone.getContext().getContentResolver(),
                android.provider.Settings.Global.PREFERRED_NETWORK_MODE + mDefaultImsSubId,
                Phone.PREFERRED_NT_MODE);
        if (settingsNetworkMode == Phone.NT_MODE_LTE_ONLY) {
            mVolteMode.setEnabled(false);
        }
    }

    private int getDefaultImsPhoneId(Context context) {
        return (ImsManager.isVolteEnabledByPlatform(context, 0) ? 0 : 1);
    }

    @Override
    public void onPause() {
        super.onPause();

        final Context context = getActivity();

        if (mValidListener) {
            mValidListener = false;
            mSwitchBar.removeOnSwitchChangeListener(this);
        }

        context.unregisterReceiver(mIntentReceiver);
   }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSubscriptionManager.removeOnSubscriptionsChangedListener(
                mOnSubscriptionsChangeListener);
    }

    /**
     * Listens to the state change of the switch.
     * @param switchView votle settings switch.
     * @param isChecked whether setting is checked or not.
     */
    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Log.d(TAG, "OnSwitchChanged IsChecked = " + mSwitch.isChecked());
        final Context context = getActivity();
        if (canNotSetAdvanced4GMode()) {
            mSwitch.setChecked(!mSwitch.isChecked());
            Log.d(TAG, "onSwitchChanged can't set Enhanced 4G mode.");
            //ShowTips(R.string.can_not_switch_enhanced_4g_lte_mode_tips);
            Toast.makeText(getContext(),
                    R.string.can_not_switch_enhanced_4g_lte_mode_tips, Toast.LENGTH_SHORT).show();
        } else {
            //mSwitch.setChecked(!mSwitch.isChecked());
            ImsManager.setEnhanced4gLteModeSetting(getActivity(), mSwitch.isChecked());
        }
        int isVolteEnabled = Settings.Global.getInt(getContentResolver(),
                Settings.Global.ENHANCED_4G_MODE_ENABLED, 0);
        if (isVolteEnabled == 1) {
            mVolteMode.setEnabled(true);
        } else {
            mVolteMode.setEnabled(false);
        }
    }

    private boolean canNotSetAdvanced4GMode() {
        return isInCall(getContext()) || isInSwitchProcess()
             || isAirplaneModeOn(getContext());
    }

    /**
     * Check if user is in Call.
     * @param context Settings context
     * @return true if call is ongoing.
     */
    public static boolean isInCall(Context context) {
        TelecomManager manager = (TelecomManager) context.getSystemService(
                Context.TELECOM_SERVICE);
        boolean inCall = false;
        if (manager != null) {
            inCall = manager.isInCall();
        }
        Log.d(TAG, "[isInCall] = " + inCall);
        return inCall;
    }

    /**
     * Get airplane Mode settings value.
     * @param context Settings context
     * @return true if airplane mode is on.
     */
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * Get the IMS_STATE_XXX, so can get whether the state is in changing.
     * @return true if the state is in changing, else return false.
     */
    private boolean isInSwitchProcess() {
        int imsState = MtkPhoneConstants.IMS_STATE_DISABLED;
        try {
            final IMtkTelephonyEx telephonyEx = IMtkTelephonyEx.Stub.asInterface(
                ServiceManager.getService("phoneEx"));
            imsState = MtkImsManagerEx.getInstance().getImsState(
                    telephonyEx.getMainCapabilityPhoneId());
        } catch (ImsException e) {
            Log.e(TAG, "[isInSwitchProcess]" + e);
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "[isInSwitchProcess]" + e);
            return false;
        }
        Log.e(TAG, "[canSetAdvanced4GMode] imsState = " + imsState);
        return imsState == MtkPhoneConstants.IMS_STATE_DISABLING
                || imsState == MtkPhoneConstants.IMS_STATE_ENABLING;
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getActivity();
        if (preference == mVolteMode) {
            Log.d(TAG, "onPreferenceChange newValue = " + newValue);
            mVolteMode.setChecked((Boolean) newValue);
            Boolean boolVal = (Boolean) newValue;
            SharedPreferences settings = getActivity().getSharedPreferences(VOLTE_MODE_PREF, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean("checked", boolVal);
            editor.commit();
            int mode = (settings.getBoolean("checked", true) ? 3 : 2);
            Log.d(TAG, "setvoltemode = " + mode);
            MtkImsManager.setVoltePreferSetting(context, mode, getDefaultImsPhoneId(context));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "Move to home");
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
