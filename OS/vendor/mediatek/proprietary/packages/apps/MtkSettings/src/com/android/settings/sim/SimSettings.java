/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.sim;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import android.text.TextUtils;
import android.util.Log;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.TelephonyProperties;
import com.android.settings.R;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.MtkTelephonyIntents;
import com.mediatek.settings.cdma.CdmaUtils;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.ext.ISettingsMiscExt;
import com.mediatek.settings.ext.ISimManagementExt;
import com.mediatek.settings.sim.RadioPowerController;
import com.mediatek.settings.sim.RadioPowerPreference;
import com.mediatek.settings.sim.SimHotSwapHandler;
import com.mediatek.settings.sim.TelephonyUtils;
import com.mediatek.settings.sim.SimHotSwapHandler.OnSimHotSwapListener;
import com.mediatek.settings.UtilsExt;

import java.util.ArrayList;
import java.util.List;

public class SimSettings extends RestrictedSettingsFragment implements Indexable {
    private static final String TAG = "SimSettings";
    private static final boolean DBG = true;

    private static final String DISALLOW_CONFIG_SIM = "no_config_sim";
    private static final String SIM_CARD_CATEGORY = "sim_cards";
    private static final String KEY_CELLULAR_DATA = "sim_cellular_data";
    private static final String KEY_CALLS = "sim_calls";
    private static final String KEY_SMS = "sim_sms";
    public static final String EXTRA_SLOT_ID = "slot_id";

    /**
     * By UX design we use only one Subscription Information(SubInfo) record per SIM slot.
     * mAvalableSubInfos is the list of SubInfos we present to the user.
     * mSubInfoList is the list of all SubInfos.
     * mSelectableSubInfos is the list of SubInfos that a user can select for data, calls, and SMS.
     */
    private List<SubscriptionInfo> mAvailableSubInfos = null;
    private List<SubscriptionInfo> mSubInfoList = null;
    private List<SubscriptionInfo> mSelectableSubInfos = null;
    private PreferenceScreen mSimCards = null;
    private SubscriptionManager mSubscriptionManager;
    private int mNumSlots;
    private Context mContext;

    private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
    private int[] mCallState = new int[mPhoneCount];
    private PhoneStateListener[] mPhoneStateListener = new PhoneStateListener[mPhoneCount];

    public SimSettings() {
        super(DISALLOW_CONFIG_SIM);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SIM;
    }

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        mContext = getActivity();

        mSubscriptionManager = SubscriptionManager.from(getActivity());
        final TelephonyManager tm =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        addPreferencesFromResource(R.xml.sim_settings);

        mNumSlots = tm.getSimCount();
        mSimCards = (PreferenceScreen)findPreference(SIM_CARD_CATEGORY);
        mAvailableSubInfos = new ArrayList<SubscriptionInfo>(mNumSlots);
        mSelectableSubInfos = new ArrayList<SubscriptionInfo>();
        SimSelectNotification.cancelNotification(getActivity());
        /// M: for [SIM Hot Swap], [SIM Radio On/Off] etc.
        initForSimStateChange();
        /// M: for Plug-in @{
        mSimManagementExt = UtilsExt.getSimManagementExt(getActivity());
        mSimManagementExt.onCreate();
        mMiscExt = UtilsExt.getMiscPlugin(getActivity());

        /// M: for radio switch control
        mRadioController = RadioPowerController.getInstance(getContext());

        /// M: for [Smart Call Forwarding] @{
        mSimManagementExt.initPlugin(this);
        /// @}
    }

    private final SubscriptionManager.OnSubscriptionsChangedListener mOnSubscriptionsChangeListener
            = new SubscriptionManager.OnSubscriptionsChangedListener() {
        @Override
        public void onSubscriptionsChanged() {
            if (DBG) log("onSubscriptionsChanged:");
            updateSubscriptions();
        }
    };

    private void updateSubscriptions() {
        mSubInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
        for (int i = 0; i < mNumSlots; ++i) {
            Preference pref = mSimCards.findPreference("sim" + i);
            if (pref instanceof SimPreference) {
                mSimCards.removePreference(pref);
            }
        }
        mAvailableSubInfos.clear();
        mSelectableSubInfos.clear();

        for (int i = 0; i < mNumSlots; ++i) {
            final SubscriptionInfo sir = mSubscriptionManager
                    .getActiveSubscriptionInfoForSimSlotIndex(i);
            SimPreference simPreference = new SimPreference(getPrefContext(), sir, i);
            simPreference.setOrder(i-mNumSlots);
            /// M: for [SIM Radio On/Off]

            if (sir != null) {
                int subId = sir.getSubscriptionId();
                simPreference.bindRadioPowerState(subId,
                        !mIsAirplaneModeOn && mRadioController.isRadioSwitchComplete(subId));
            } else {
                simPreference.bindRadioPowerState(SubscriptionManager.INVALID_SUBSCRIPTION_ID,
                        !mIsAirplaneModeOn && mRadioController.isRadioSwitchComplete(
                                SubscriptionManager.INVALID_SUBSCRIPTION_ID));
            }

            logInEng("addPreference slot " + i);
            mSimCards.addPreference(simPreference);
            mAvailableSubInfos.add(sir);
            if (sir != null) {
                mSelectableSubInfos.add(sir);
            }
        }
        updateAllOptions();
    }

    private void updateAllOptions() {
        updateSimSlotValues();
        updateActivitesCategory();
    }

    private void updateSimSlotValues() {
        final int prefSize = mSimCards.getPreferenceCount();
        for (int i = 0; i < prefSize; ++i) {
            Preference pref = mSimCards.getPreference(i);
            if (pref instanceof SimPreference) {
                ((SimPreference)pref).update();
            }
        }
    }

    private void updateActivitesCategory() {
        updateCellularDataValues();
        updateCallValues();
        updateSmsValues();
    }

    private void updateSmsValues() {
        final Preference simPref = findPreference(KEY_SMS);
        if (simPref != null) {
            SubscriptionInfo sir = mSubscriptionManager.getDefaultSmsSubscriptionInfo();
            simPref.setTitle(R.string.sms_messages_title);
            if (DBG) {
                log("[updateSmsValues] mSubInfoList=" + mSubInfoList);
            }

            /// M: for plug-in
            sir = mSimManagementExt.setDefaultSubId(getActivity(), sir, KEY_SMS);

            if (sir != null) {
                simPref.setSummary(sir.getDisplayName());
                /// M: set enable state below to join more conditions
                // simPref.setEnabled(mSelectableSubInfos.size() > 1);
            } else if (sir == null) {
                /// M: for [Always Ask]
                // simPref.setSummary(R.string.sim_selection_required_pref);
                simPref.setSummary(R.string.sim_calls_ask_first_prefs_title);
                /// M: set enable state below to join more conditions
                // simPref.setEnabled(mSelectableSubInfos.size() >= 1);
                /// M: for plug-in
                mSimManagementExt.updateDefaultSmsSummary(simPref);
            }

            boolean enabled = sir == null ? mSelectableSubInfos.size() >= 1
                    : mSelectableSubInfos.size() > 1;
            simPref.setEnabled(enabled);
            /// M: for plug-in
            mSimManagementExt.configSimPreferenceScreen(simPref, KEY_SMS,
                       mSelectableSubInfos.size());
            mSimManagementExt.setPrefSummary(simPref, KEY_SMS);
        }
        /// M: CNOP remove dialog, (CTA required)
        /// but call and sms can select in only one card insert.
        mSimManagementExt.configSimPreferenceScreen(simPref, KEY_SMS,
                   mSelectableSubInfos.size());
    }

    private void updateCellularDataValues() {
        final Preference simPref = findPreference(KEY_CELLULAR_DATA);
        if (simPref != null) {
            SubscriptionInfo sir = mSubscriptionManager.getDefaultDataSubscriptionInfo();
            simPref.setTitle(R.string.cellular_data_title);
            if (DBG) {
                Log.d(TAG, "default subID = " + sir);
                log("[updateCellularDataValues] mSubInfoList=" + mSubInfoList);
            }

            /// M: for plug-in
            sir = mSimManagementExt.setDefaultSubId(getActivity(), sir, KEY_CELLULAR_DATA);
            if (DBG) {
                Log.d(TAG, "default subID after plugin update = " + sir);
            }

            /// M: set enable state below to join more conditions @{
            /*
            boolean callStateIdle = isCallStateIdle();
            final boolean ecbMode = SystemProperties.getBoolean(
                    TelephonyProperties.PROPERTY_INECM_MODE, false);
            */
            /// @}

            if (sir != null) {
                simPref.setSummary(sir.getDisplayName());
                // Enable data preference in msim mode and call state idle
             /// M: set enable state below to join more conditions
             // simPref.setEnabled((mSelectableSubInfos.size() > 1) && callStateIdle && !ecbMode);
            } else if (sir == null) {
                simPref.setSummary(R.string.sim_selection_required_pref);
                // Enable data preference in msim mode and call state idle
            /// M: set enable state below to join more conditions
            // simPref.setEnabled((mSelectableSubInfos.size() >= 1) && callStateIdle && !ecbMode);
            }
            /// M: check should enable data preference by multiple conditions @{
            boolean defaultState = sir == null ? mSelectableSubInfos.size() >= 1
                    : mSelectableSubInfos.size() > 1;
            simPref.setEnabled(shouldEnableSimPref(defaultState));

            mSimManagementExt.configSimPreferenceScreen(simPref, KEY_CELLULAR_DATA, -1);
            /// @}
        }
        /// M: CNOP remove dialog, (CTA required)
        /// but call and sms can select in only one card insert.
        mSimManagementExt.configSimPreferenceScreen(simPref, KEY_CELLULAR_DATA, -1);

    }

    private void updateCallValues() {
        final Preference simPref = findPreference(KEY_CALLS);
        if (simPref != null) {
            final TelecomManager telecomManager = TelecomManager.from(mContext);
            PhoneAccountHandle phoneAccount =
                telecomManager.getUserSelectedOutgoingPhoneAccount();
            final List<PhoneAccountHandle> allPhoneAccounts =
                telecomManager.getCallCapablePhoneAccounts();

            phoneAccount = mSimManagementExt.setDefaultCallValue(phoneAccount);
            log("updateCallValues allPhoneAccounts size = " + allPhoneAccounts.size()
                    + " phoneAccount =" + phoneAccount);

            simPref.setTitle(R.string.calls_title);
            /// M: for ALPS02320747 @{
            // phoneaccount may got unregistered, need to check null here
            /*
            simPref.setSummary(phoneAccount == null
                    ? mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title)
                    : (String)telecomManager.getPhoneAccount(phoneAccount).getLabel());
             */
            PhoneAccount defaultAccount = phoneAccount == null ? null : telecomManager
                    .getPhoneAccount(phoneAccount);
            simPref.setSummary(defaultAccount == null
                    ? mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title)
                    : (String) defaultAccount.getLabel());
            /// @}
            simPref.setEnabled(allPhoneAccounts.size() > 1);
            mSimManagementExt.configSimPreferenceScreen(simPref, KEY_CALLS,
                    allPhoneAccounts.size());

            /// M: For Op01 open market. @{
            if (SystemProperties.get("ro.cmcc_light_cust_support").equals("1")) {
                if (DBG) {
                    log("Op01 open market:set call enable if size >= 1");
                }
                boolean hasActiveSubscription
                    = SubscriptionManager.from(mContext).getActiveSubscriptionInfoCount() >= 1;
                simPref.setEnabled(allPhoneAccounts.size() >= 1 && hasActiveSubscription);
            }
            /// M: For Op01 open market. @}
            mSimManagementExt.setPrefSummary(simPref, KEY_CALLS);
        }
        /// M: For Op01 open market. @}
    }

    @Override
    public void onResume() {
        super.onResume();
        mSubscriptionManager.addOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        updateSubscriptions();
        /// M: fix Google bug: only listen to default sub, listen to Phone state change instead @{
        /*
        final TelephonyManager tm =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        if (mSelectableSubInfos.size() > 1) {
            Log.d(TAG, "Register for call state change");
            for (int i = 0; i < mPhoneCount; i++) {
                int subId = mSelectableSubInfos.get(i).getSubscriptionId();
                tm.listen(getPhoneStateListener(i, subId),
                        PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
        */
        /// @}

        /// M: for [Tablet]
        removeItemsForTablet();
        /// M: for Plug-in @{
        customizeSimDisplay();
        mSimManagementExt.onResume(getActivity());
        /// @}
    }

    @Override
    public void onPause() {
        super.onPause();
        mSubscriptionManager.removeOnSubscriptionsChangedListener(mOnSubscriptionsChangeListener);
        /// M: Google bug: only listen to default sub, listen to Phone state change instead @{
        /*
        final TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        for (int i = 0; i < mPhoneCount; i++) {
            if (mPhoneStateListener[i] != null) {
                tm.listen(mPhoneStateListener[i], PhoneStateListener.LISTEN_NONE);
                mPhoneStateListener[i] = null;
            }
        }
        */
        ///@}

        /// M: for Plug-in
        mSimManagementExt.onPause();
    }

    private PhoneStateListener getPhoneStateListener(int phoneId, int subId) {
        // Disable Sim selection for Data when voice call is going on as changing the default data
        // sim causes a modem reset currently and call gets disconnected
        // ToDo : Add subtext on disabled preference to let user know that default data sim cannot
        // be changed while call is going on
        final int i = phoneId;
        mPhoneStateListener[phoneId]  = new PhoneStateListener(subId) {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (DBG) log("PhoneStateListener.onCallStateChanged: state=" + state);
                mCallState[i] = state;
                updateCellularDataValues();
            }
        };
        return mPhoneStateListener[phoneId];
    }

    @Override
    public boolean onPreferenceTreeClick(final Preference preference) {
        final Context context = mContext;
        Intent intent = new Intent(context, SimDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (preference instanceof SimPreference) {
            Intent newIntent = new Intent(context, SimPreferenceDialog.class);
            newIntent.putExtra(EXTRA_SLOT_ID, ((SimPreference)preference).getSlotId());
            startActivity(newIntent);
        } else if (findPreference(KEY_CELLULAR_DATA) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.DATA_PICK);
            context.startActivity(intent);
        } else if (findPreference(KEY_CALLS) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.CALLS_PICK);
            context.startActivity(intent);
        } else if (findPreference(KEY_SMS) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.SMS_PICK);
            context.startActivity(intent);
        } else {
            mSimManagementExt.handleEvent(this, context, preference);
        }
        /// @}

        return true;
    }
    /// M: for [SIM Radio On/Off]
    /**
     * Class to update Sim Radio preference for Sim radio.
     */
    private class SimPreference extends RadioPowerPreference {
        private SubscriptionInfo mSubInfoRecord;
        private int mSlotId;
        Context mContext;

        public SimPreference(Context context, SubscriptionInfo subInfoRecord, int slotId) {
            super(context);

            mContext = context;
            mSubInfoRecord = subInfoRecord;
            mSlotId = slotId;
            setKey("sim" + mSlotId);
            update();
        }

        public void update() {
            final Resources res = mContext.getResources();

            setTitle(String.format(mContext.getResources()
                    .getString(R.string.sim_editor_title), (mSlotId + 1)));
            /// M: for Plug-in
            customizePreferenceTitle();
            if (mSubInfoRecord != null) {
                /// M: ALPS02871084, only get phone number once
                String phoneNum = getPhoneNumber(mSubInfoRecord);
                logInEng("phoneNum = " + phoneNum);
                //if (TextUtils.isEmpty(getPhoneNumber(mSubInfoRecord))) {
                if (TextUtils.isEmpty(phoneNum)) {
                    setSummary(mSubInfoRecord.getDisplayName());
                } else {
                    setSummary(mSubInfoRecord.getDisplayName() + " - " +
                            PhoneNumberUtils.createTtsSpannable(phoneNum));
                    setEnabled(true);
                }
                setIcon(new BitmapDrawable(res, (mSubInfoRecord.createIconBitmap(mContext))));
                /// M: add for radio on/off @{
                int subId = mSubInfoRecord.getSubscriptionId();
                setRadioEnabled(!mIsAirplaneModeOn
                        && mRadioController.isRadioSwitchComplete(subId));
                if (mRadioController.isRadioSwitchComplete(subId)) {
                    setRadioOn(TelephonyUtils.isRadioOn(subId, getContext()));
                }

                /// @}
            } else {
                setSummary(R.string.sim_slot_empty);
                setFragment(null);
                setEnabled(false);
            }
        }

        private int getSlotId() {
            return mSlotId;
        }
        /**
         * only for plug-in, change "SIM" to "UIM/SIM".
         */
        private void customizePreferenceTitle() {
            int subId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
            if (mSubInfoRecord != null) {
                subId = mSubInfoRecord.getSubscriptionId();
            }
            setTitle(String.format(mMiscExt.customizeSimDisplayString(mContext.getResources()
                    .getString(R.string.sim_editor_title), subId), (mSlotId + 1)));
        }
    }

    // Returns the line1Number. Line1number should always be read from TelephonyManager since it can
    // be overridden for display purposes.
    private String getPhoneNumber(SubscriptionInfo info) {
        final TelephonyManager tm =
            (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number(info.getSubscriptionId());
    }

    private void log(String s) {
        Log.d(TAG, s);
    }

    /**
     * For search
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    if (Utils.showSimCardTile(context)) {
                        SearchIndexableResource sir = new SearchIndexableResource(context);
                        sir.xmlResId = R.xml.sim_settings;
                        result.add(sir);
                    }

                    return result;
                }
            };

    private boolean isCallStateIdle() {
        boolean callStateIdle = true;
        for (int i = 0; i < mCallState.length; i++) {
            if (TelephonyManager.CALL_STATE_IDLE != mCallState[i]) {
                callStateIdle = false;
            }
        }
        Log.d(TAG, "isCallStateIdle " + callStateIdle);
        return callStateIdle;
    }

     ///----------------------------------------MTK-----------------------------------------------

    private static final String KEY_SIM_ACTIVITIES = "sim_activities";
    private static final boolean ENG_LOAD = SystemProperties.get("ro.build.type").equals("eng") ?
            true : false || Log.isLoggable(TAG, Log.DEBUG);

    // / M: for Plug in @{
    private ISettingsMiscExt mMiscExt;
    private ISimManagementExt mSimManagementExt;
    // / @}

    private IMtkTelephonyEx mTelephonyEx;
    private SimHotSwapHandler mSimHotSwapHandler;
    private boolean mIsAirplaneModeOn = false;

    private RadioPowerController mRadioController;

    // Receiver to handle different actions
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mReceiver action = " + action);
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                handleAirplaneModeChange(intent);
            } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)) {
                updateCellularDataValues();
            } else if (action.equals(TelecomManager.ACTION_PHONE_ACCOUNT_REGISTERED)
                    || action.equals(TelecomManager.ACTION_PHONE_ACCOUNT_UNREGISTERED)) {
                updateCallValues();
            } else if (action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE)
                    || action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED)) {
                updateActivitesCategory();
            } else if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                updateActivitesCategory();
            // listen to radio state change
            } else if (action.equals(MtkTelephonyIntents.ACTION_RADIO_STATE_CHANGED)) {
                int subId = intent.getIntExtra("subId", -1);
                if (mRadioController.isRadioSwitchComplete(subId)) {
                    handleRadioPowerSwitchComplete();
                }
            } //Migration change
        }
    };

    /**
     * init for sim state change, including SIM hot swap, airplane mode, etc.
     */
    private void initForSimStateChange() {
        mTelephonyEx = IMtkTelephonyEx.Stub.asInterface(
                ServiceManager.getService("phoneEx"));
        /// M: for [SIM Hot Swap] @{
        mSimHotSwapHandler = new SimHotSwapHandler(getActivity().getApplicationContext());
        mSimHotSwapHandler.registerOnSimHotSwap(new OnSimHotSwapListener() {
            @Override
            public void onSimHotSwap() {
                if (getActivity() != null) {
                    log("onSimHotSwap, finish Activity~~");
                    getActivity().finish();
                }
            }
        });
        /// @}

        mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getActivity().getApplicationContext());
        logInEng("init()... airplane mode is: " + mIsAirplaneModeOn);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);

        // For radio on/off
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
        // listen to radio state
        intentFilter.addAction(MtkTelephonyIntents.ACTION_RADIO_STATE_CHANGED);
        // listen to PHONE_STATE_CHANGE
        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        //listen to Telecom Manager event
        intentFilter.addAction(TelecomManager.ACTION_PHONE_ACCOUNT_REGISTERED);
        intentFilter.addAction(TelecomManager.ACTION_PHONE_ACCOUNT_UNREGISTERED);
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    /**
     * update SIM values after radio switch
     */
    private void handleRadioPowerSwitchComplete() {
        if (isResumed()) {
            updateSimSlotValues();
        }
        logInEng("handleRadioPowerSwitchComplete isResumed =  " + isResumed());
        updateActivitesCategory();
    }

    /**
     * When airplane mode is on, some parts need to be disabled for prevent some telephony issues
     * when airplane on.
     * Default data is not able to switch as may cause modem switch
     * SIM radio power switch need to disable, also this action need operate modem
     * @param airplaneOn airplane mode state true on, false off
     */
    private void handleAirplaneModeChange(Intent intent) {
        mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
        Log.d(TAG, "airplane mode is = " + mIsAirplaneModeOn);
        updateSimSlotValues();
        updateActivitesCategory();
        removeItemsForTablet();
        /// M: for [Smart Call Forwarding] @{
        mSimManagementExt.updatePrefState();
        /// @}
    }

    /**
     * remove unnecessary items for tablet
     */
    private void removeItemsForTablet() {
        // remove some item when in 4gds wifi-only
        if (FeatureOption.MTK_PRODUCT_IS_TABLET) {
            Preference sim_call_Pref = findPreference(KEY_CALLS);
            Preference sim_sms_Pref = findPreference(KEY_SMS);
            Preference sim_data_Pref = findPreference(KEY_CELLULAR_DATA);
            PreferenceCategory mPreferenceCategoryActivities =
                (PreferenceCategory) findPreference(KEY_SIM_ACTIVITIES);
            TelephonyManager tm = TelephonyManager.from(getActivity());
            if (!tm.isSmsCapable() && sim_sms_Pref != null) {
                mPreferenceCategoryActivities.removePreference(sim_sms_Pref);
            }
            if (!tm.isMultiSimEnabled() && sim_data_Pref != null && sim_sms_Pref != null) {
                mPreferenceCategoryActivities.removePreference(sim_data_Pref);
                mPreferenceCategoryActivities.removePreference(sim_sms_Pref);
            }
            if (!tm.isVoiceCapable() && sim_call_Pref != null) {
                mPreferenceCategoryActivities.removePreference(sim_call_Pref);
            }
        }
    }

    @Override
    public void onDestroy() {
        logInEng("onDestroy()");
        getActivity().unregisterReceiver(mReceiver);
        mSimHotSwapHandler.unregisterOnSimHotSwap();
        /// M: for Plug-in
        mSimManagementExt.onDestroy();
        super.onDestroy();
    }

    /**
     * only for plug-in, change "SIM" to "UIM/SIM".
     */
    private void customizeSimDisplay() {
        if (mSimCards != null) {
            mSimCards.setTitle(mMiscExt.customizeSimDisplayString(
                    getString(R.string.sim_settings_title),
                    SubscriptionManager.INVALID_SUBSCRIPTION_ID));
        }
        getActivity().setTitle(
                mMiscExt.customizeSimDisplayString(getString(R.string.sim_settings_title),
                        SubscriptionManager.INVALID_SUBSCRIPTION_ID));
    }


    private boolean shouldEnableSimPref(boolean defaultState) {
        String ecbMode = SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE, "false");
        boolean isInEcbMode = false;
        if (ecbMode != null && ecbMode.contains("true")) {
            isInEcbMode = true;
        }
        boolean capSwitching = TelephonyUtils.isCapabilitySwitching();
        boolean inCall = TelecomManager.from(mContext).isInCall();

        log("defaultState :" + defaultState + ", capSwitching :"
                + capSwitching + ", airplaneModeOn :" + mIsAirplaneModeOn + ", inCall :"
                + inCall + ", ecbMode: " + ecbMode);
        return defaultState && !capSwitching && !mIsAirplaneModeOn && !inCall && !isInEcbMode;
    }

    private void logInEng(String s) {
        if (ENG_LOAD) {
            Log.d(TAG, s);
        }
    }
}
