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

import static android.net.ConnectivityManager.TYPE_ETHERNET;
import static android.net.ConnectivityManager.TYPE_WIFI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.BidiFormatter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.R;
import com.android.settings.SummaryPreference;
import com.android.settings.Utils;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.NetworkPolicyEditor;
import com.android.settingslib.net.DataUsageController;

import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.provider.MtkSettingsExt;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.IDataUsageSummaryExt;
import com.mediatek.settings.sim.TelephonyUtils;
import com.mediatek.telephony.MtkTelephonyManagerEx;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Settings preference fragment that displays data usage summary.
 *
 * This class in deprecated use {@link DataPlanUsageSummary}.
 */
@Deprecated
public class DataUsageSummary extends DataUsageBase implements Indexable, DataUsageEditController {

    static final boolean LOGD = false;
    public static final boolean TEST_RADIOS = false;
    public static final String TEST_RADIOS_PROP = "test.radios";

    public static final String KEY_RESTRICT_BACKGROUND = "restrict_background";

    private static final String KEY_STATUS_HEADER = "status_header";
    private static final String KEY_LIMIT_SUMMARY = "limit_summary";

    // Mobile data keys
    public static final String KEY_MOBILE_USAGE_TITLE = "mobile_category";
    public static final String KEY_MOBILE_DATA_USAGE_TOGGLE = "data_usage_enable";
    public static final String KEY_MOBILE_DATA_USAGE = "cellular_data_usage";
    public static final String KEY_MOBILE_BILLING_CYCLE = "billing_preference";

    // Wifi keys
    public static final String KEY_WIFI_USAGE_TITLE = "wifi_category";
    public static final String KEY_WIFI_DATA_USAGE = "wifi_data_usage";
    public static final String KEY_NETWORK_RESTRICTIONS = "network_restrictions";


    private DataUsageController mDataUsageController;
    private DataUsageInfoController mDataInfoController;
    private SummaryPreference mSummaryPreference;
    private Preference mLimitPreference;
    private NetworkTemplate mDefaultTemplate;
    private int mDataUsageTemplate;
    private NetworkRestrictionsPreference mNetworkRestrictionPreference;
    private WifiManager mWifiManager;
    private NetworkPolicyEditor mPolicyEditor;

    /// M: for phonestate listener, when calling ,can not edit mEnableDataService.
    private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
    private PhoneStateListener mPhoneStateListener;
    int mTempPhoneid = 0;
    private IDataUsageSummaryExt mDataUsageSummaryExt;

    @Override
    protected int getHelpResource() {
        return R.string.help_url_data_usage;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Context context = getContext();
        mDataUsageSummaryExt = UtilsExt.getDataUsageSummaryExt(getContext()
                .getApplicationContext());
        NetworkPolicyManager policyManager = NetworkPolicyManager.from(context);
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mPolicyEditor = new NetworkPolicyEditor(policyManager);

        boolean hasMobileData = DataUsageUtils.hasMobileData(context);
        mDataUsageController = new DataUsageController(context);
        mDataInfoController = new DataUsageInfoController();
        addPreferencesFromResource(R.xml.data_usage);

        int defaultSubId = DataUsageUtils.getDefaultSubscriptionId(context);
        if (defaultSubId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            Log.d("DataUsageSummary", "onCreate INVALID_SUBSCRIPTION_ID Mobile data false");
            hasMobileData = false;
        }
        mDefaultTemplate = DataUsageUtils.getDefaultTemplate(context, defaultSubId);
        mSummaryPreference = (SummaryPreference) findPreference(KEY_STATUS_HEADER);

        if (!hasMobileData || !isAdmin()) {
            removePreference(KEY_RESTRICT_BACKGROUND);
        }
        if (hasMobileData) {
            mLimitPreference = findPreference(KEY_LIMIT_SUMMARY);
            List<SubscriptionInfo> subscriptions =
                    services.mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptions == null || subscriptions.size() == 0) {
                addMobileSection(defaultSubId);
            }
            for (int i = 0; subscriptions != null && i < subscriptions.size(); i++) {
                SubscriptionInfo subInfo = subscriptions.get(i);
                if (subscriptions.size() > 1) {
                    addMobileSection(subInfo.getSubscriptionId(), subInfo);
                } else {
                    addMobileSection(subInfo.getSubscriptionId());
                }
            }
            mSummaryPreference.setSelectable(true);
            addDataServiceSection(subscriptions);
        } else {
            removePreference(KEY_LIMIT_SUMMARY);
            mSummaryPreference.setSelectable(false);
        }
        boolean hasWifiRadio = DataUsageUtils.hasWifiRadio(context);
        if (hasWifiRadio) {
            addWifiSection();
        }
        if (hasEthernet(context)) {
            addEthernetSection();
        }
        mDataUsageTemplate = hasMobileData ? R.string.cell_data_template
                : hasWifiRadio ? R.string.wifi_data_template
                : R.string.ethernet_data_template;

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (UserManager.get(getContext()).isAdminUser()) {
            inflater.inflate(R.menu.data_usage, menu);
        }
        /// M: Remove Cellular networks menu item on wifi-only device @{
        if (Utils.isWifiOnly(getActivity())) {
            menu.removeItem(R.id.data_usage_menu_cellular_networks);
        }
        /// @}
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.data_usage_menu_cellular_networks: {
                Log.d(TAG, "select CELLULAR_NETWORKDATA");
                final Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.phone",
                        "com.android.phone.MobileNetworkSettings"));
                startActivity(intent);
                return true;
            }
            /// M: for [CTA2016 requirement] @{
            // start a cellular data control page
            case R.id.data_usage_menu_cellular_data_control: {
                Log.d(TAG, "select CELLULAR_DATA");
                Intent intent = new Intent("com.mediatek.security.CELLULAR_DATA");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "cellular data control activity not found!!!");
                }
                return true;
            }
            /// @}
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == findPreference(KEY_STATUS_HEADER)) {
            BillingCycleSettings.BytesEditorFragment.show(this, false);
            return false;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void addMobileSection(int subId) {
        addMobileSection(subId, null);
    }

    private void addMobileSection(int subId, SubscriptionInfo subInfo) {
        TemplatePreferenceCategory category = (TemplatePreferenceCategory)
                inflatePreferences(R.xml.data_usage_cellular);
        Log.d(TAG, "addMobileSection with subID: " + subId);
        category.setTemplate(getNetworkTemplate(subId), subId, services);
        category.pushTemplates(services);
        if (subInfo != null && !TextUtils.isEmpty(subInfo.getDisplayName())) {
            Preference title  = category.findPreference(KEY_MOBILE_USAGE_TITLE);
            title.setTitle(subInfo.getDisplayName());
        }
    }

    private void addWifiSection() {
        TemplatePreferenceCategory category = (TemplatePreferenceCategory)
                inflatePreferences(R.xml.data_usage_wifi);
        category.setTemplate(NetworkTemplate.buildTemplateWifiWildcard(), 0, services);
        mNetworkRestrictionPreference =
            (NetworkRestrictionsPreference) category.findPreference(KEY_NETWORK_RESTRICTIONS);
    }

    private void addEthernetSection() {
        TemplatePreferenceCategory category = (TemplatePreferenceCategory)
                inflatePreferences(R.xml.data_usage_ethernet);
        category.setTemplate(NetworkTemplate.buildTemplateEthernet(), 0, services);
    }

    private Preference inflatePreferences(int resId) {
        PreferenceScreen rootPreferences = getPreferenceManager().inflateFromResource(
                getPrefContext(), resId, null);
        Preference pref = rootPreferences.getPreference(0);
        rootPreferences.removeAll();

        PreferenceScreen screen = getPreferenceScreen();
        pref.setOrder(screen.getPreferenceCount());
        screen.addPreference(pref);

        return pref;
    }

    private NetworkTemplate getNetworkTemplate(int subscriptionId) {
        NetworkTemplate mobileAll = NetworkTemplate.buildTemplateMobileAll(
                services.mTelephonyManager.getSubscriberId(subscriptionId));
        Log.d(TAG, "getNetworkTemplate with subID: " + subscriptionId);
        return NetworkTemplate.normalize(mobileAll,
                services.mTelephonyManager.getMergedSubscriberIds());
    }

    @Override
    public void onResume() {
        super.onResume();
        int mainPhoneid = TelephonyUtils.getMainCapabilityPhoneId();
        if (mainPhoneid == 0) {
            mTempPhoneid = 1;
        } else {
            mTempPhoneid = 0;
        }
        updateScreenEnabled();
        updateState();
    }

    private static CharSequence formatTitle(Context context, String template, long usageLevel) {
        final float LARGER_SIZE = 1.25f * 1.25f;  // (1/0.8)^2
        final float SMALLER_SIZE = 1.0f / LARGER_SIZE;  // 0.8^2
        final int FLAGS = Spannable.SPAN_INCLUSIVE_INCLUSIVE;

        final Formatter.BytesResult usedResult = Formatter.formatBytes(context.getResources(),
                usageLevel, Formatter.FLAG_SHORTER);
        final SpannableString enlargedValue = new SpannableString(usedResult.value);
        enlargedValue.setSpan(new RelativeSizeSpan(LARGER_SIZE), 0, enlargedValue.length(), FLAGS);

        final SpannableString amountTemplate = new SpannableString(
                context.getString(com.android.internal.R.string.fileSizeSuffix)
                .replace("%1$s", "^1").replace("%2$s", "^2"));
        final CharSequence formattedUsage = TextUtils.expandTemplate(amountTemplate,
                enlargedValue, usedResult.units);

        final SpannableString fullTemplate = new SpannableString(template);
        fullTemplate.setSpan(new RelativeSizeSpan(SMALLER_SIZE), 0, fullTemplate.length(), FLAGS);
        return TextUtils.expandTemplate(fullTemplate,
                BidiFormatter.getInstance().unicodeWrap(formattedUsage));
    }

    private void updateState() {
        DataUsageController.DataUsageInfo info = mDataUsageController.getDataUsageInfo(
                mDefaultTemplate);
        Context context = getContext();
        mDataInfoController.updateDataLimit(info,
                services.mPolicyEditor.getPolicy(mDefaultTemplate));

        if (mSummaryPreference != null) {
            mSummaryPreference.setTitle(
                    formatTitle(context, getString(mDataUsageTemplate), info.usageLevel));
            long limit = mDataInfoController.getSummaryLimit(info);
            mSummaryPreference.setSummary(info.period);

            if (limit <= 0) {
                mSummaryPreference.setChartEnabled(false);
            } else {
                mSummaryPreference.setChartEnabled(true);
                mSummaryPreference.setLabels(Formatter.formatFileSize(context, 0),
                        Formatter.formatFileSize(context, limit));
                mSummaryPreference.setRatios(info.usageLevel / (float) limit, 0,
                        (limit - info.usageLevel) / (float) limit);
            }
        }
        if (mLimitPreference != null && (info.warningLevel > 0 || info.limitLevel > 0)) {
            String warning = Formatter.formatFileSize(context, info.warningLevel);
            String limit = Formatter.formatFileSize(context, info.limitLevel);
            mLimitPreference.setSummary(getString(info.limitLevel <= 0 ? R.string.cell_warning_only
                    : R.string.cell_warning_and_limit, warning, limit));
        } else if (mLimitPreference != null) {
            mLimitPreference.setSummary(null);
        }

        updateNetworkRestrictionSummary(mNetworkRestrictionPreference);

        PreferenceScreen screen = getPreferenceScreen();
        for (int i = 1; i < screen.getPreferenceCount(); i++) {
            if (((PreferenceCategory) screen.getPreference(i))
                    .getKey().equals(KEY_SERVICE_CATEGORY)) {
                continue;
            }
            ((TemplatePreferenceCategory) screen.getPreference(i)).pushTemplates(services);
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.DATA_USAGE_SUMMARY;
    }

    @Override
    public NetworkPolicyEditor getNetworkPolicyEditor() {
        return services.mPolicyEditor;
    }

    @Override
    public NetworkTemplate getNetworkTemplate() {
        Log.d(TAG, "getNetworkTemplate without subID: DefaultTemplate");
        return mDefaultTemplate;
    }

    @Override
    public void updateDataUsage() {
        updateState();
    }

    /**
     * Test if device has an ethernet network connection.
     */
    public boolean hasEthernet(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("ethernet");
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

    public static boolean hasMobileData(Context context) {
        return ConnectivityManager.from(context).isNetworkSupported(
                ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Test if device has a Wi-Fi data radio.
     */
    public static boolean hasWifiRadio(Context context) {
        if (TEST_RADIOS) {
            return SystemProperties.get(TEST_RADIOS_PROP).contains("wifi");
        }

        final ConnectivityManager conn = ConnectivityManager.from(context);
        return conn.isNetworkSupported(TYPE_WIFI);
    }

    public static int getDefaultSubscriptionId(Context context) {
        SubscriptionManager subManager = SubscriptionManager.from(context);
        if (subManager == null) {
            return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
        }
        SubscriptionInfo subscriptionInfo = subManager.getDefaultDataSubscriptionInfo();
        if (subscriptionInfo == null) {
            List<SubscriptionInfo> list = subManager.getAllSubscriptionInfoList();
            if (list.size() == 0) {
                return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
            }
            subscriptionInfo = list.get(0);
        }
        Log.d(TAG, "getDefaultSubscriptionId = " + subscriptionInfo);
        return subscriptionInfo.getSubscriptionId();
    }

    public static NetworkTemplate getDefaultTemplate(Context context, int defaultSubId) {
        if (hasMobileData(context) && defaultSubId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            TelephonyManager telephonyManager = TelephonyManager.from(context);
            NetworkTemplate mobileAll = NetworkTemplate.buildTemplateMobileAll(
                    telephonyManager.getSubscriberId(defaultSubId));
            return NetworkTemplate.normalize(mobileAll,
                    telephonyManager.getMergedSubscriberIds());
        } else if (hasWifiRadio(context)) {
            return NetworkTemplate.buildTemplateWifiWildcard();
        } else {
            return NetworkTemplate.buildTemplateEthernet();
        }
    }

    @VisibleForTesting
    void updateNetworkRestrictionSummary(NetworkRestrictionsPreference preference) {
        if (preference == null) {
            return;
        }
        mPolicyEditor.read();
        int count = 0;
        for (WifiConfiguration config : mWifiManager.getConfiguredNetworks()) {
            if (WifiConfiguration.isMetered(config, null)) {
                count++;
            }
        }
        preference.setSummary(getResources().getQuantityString(
            R.plurals.network_restrictions_summary, count, count));
    }

    private static class SummaryProvider
            implements SummaryLoader.SummaryProvider {

        private final Activity mActivity;
        private final SummaryLoader mSummaryLoader;
        private final DataUsageController mDataController;

        public SummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            mActivity = activity;
            mSummaryLoader = summaryLoader;
            mDataController = new DataUsageController(activity);
        }

        @Override
        public void setListening(boolean listening) {
            if (listening) {
                DataUsageController.DataUsageInfo info = mDataController.getDataUsageInfo();
                String used;
                if (info == null) {
                    used = Formatter.formatFileSize(mActivity, 0);
                } else if (info.limitLevel <= 0) {
                    used = Formatter.formatFileSize(mActivity, info.usageLevel);
                } else {
                    used = Utils.formatPercentage(info.usageLevel, info.limitLevel);
                }
                mSummaryLoader.setSummary(this,
                        mActivity.getString(R.string.data_usage_summary_format, used));
            }
        }
    }

    public static final SummaryLoader.SummaryProviderFactory SUMMARY_PROVIDER_FACTORY
        = SummaryProvider::new;

    /**
     * For search
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
        new BaseSearchIndexProvider() {

            @Override
            public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                    boolean enabled) {
                List<SearchIndexableResource> resources = new ArrayList<>();
                SearchIndexableResource resource = new SearchIndexableResource(context);
                resource.xmlResId = R.xml.data_usage;
                resources.add(resource);

                resource = new SearchIndexableResource(context);
                resource.xmlResId = R.xml.data_usage_cellular;
                resources.add(resource);

                resource = new SearchIndexableResource(context);
                resource.xmlResId = R.xml.data_usage_wifi;
                resources.add(resource);

                return resources;
            }

            @Override
            public List<String> getNonIndexableKeys(Context context) {
                List<String> keys = super.getNonIndexableKeys(context);

                if (!DataUsageUtils.hasMobileData(context)) {
                    keys.add(KEY_MOBILE_USAGE_TITLE);
                    keys.add(KEY_MOBILE_DATA_USAGE_TOGGLE);
                    keys.add(KEY_MOBILE_DATA_USAGE);
                    keys.add(KEY_MOBILE_BILLING_CYCLE);
                }

                if (!DataUsageUtils.hasWifiRadio(context)) {
                    keys.add(KEY_WIFI_DATA_USAGE);
                    keys.add(KEY_NETWORK_RESTRICTIONS);
                }

                // This title is named Wifi, and will confuse users.
                keys.add(KEY_WIFI_USAGE_TITLE);

                return keys;
            }
        };

    ///------------------------------------MTK------------------------------------------------
    // Data service keys
    public static final String KEY_DATA_SERVICE_ENABLE = "data_service_enable";
    public static final String KEY_SERVICE_CATEGORY = "service_category";
    private final static String ONE = "1";
    private static final String DATA_SERVICE_ENABLED = MtkSettingsExt.Global.DATA_SERVICE_ENABLED;

    private boolean mIsAirplaneModeOn;
    private SwitchPreference mEnableDataService;

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        /// M: for [CTA2016 requirement] @{
        MenuItem cellularDataControl = menu.findItem(R.id.data_usage_menu_cellular_data_control);
        try {
            Class<?> ctaClass = Class.forName("com.mediatek.cta.CtaUtils", false,
                    ClassLoader.getSystemClassLoader());
            Class paraClass[] = {};
            Method isCtaSupported = ctaClass.getDeclaredMethod("isCtaSupported", paraClass);
            isCtaSupported.setAccessible(true);
            Class objClass[] = {};
            Object value = isCtaSupported.invoke(ctaClass, objClass);
            if (cellularDataControl != null) {
                cellularDataControl.setVisible(Boolean.valueOf(value.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        /// @}
    }

    /// M: [CMCC VOLTE] @{
    private void addDataServiceSection(List<SubscriptionInfo> subscriptions) {
        if (!isDataServiceSupport()) {
            return;
        }

        Log.d(TAG, "addDataServiceSection");
        if ((subscriptions == null) || (subscriptions.size() != 2)) {
            Log.d(TAG, "subscriptions size != 2");
            return;
        }

        PreferenceCategory category = (PreferenceCategory)
                inflatePreferences(R.xml.data_service_cellular);
        mEnableDataService = (SwitchPreference) findPreference(KEY_DATA_SERVICE_ENABLE);
        mEnableDataService.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Log.d(TAG, "onPreferenceChange preference = " + preference.getTitle());
                if (preference == mEnableDataService) {
                    if (!mEnableDataService.isChecked()) {
                        showDataServiceDialog();
                        return true;
                    }
                    setDataService(0);
                }
                return true;
            }
        });

        mIsAirplaneModeOn = TelephonyUtils.isAirplaneModeOn(getContext());
        int mainPhoneid = TelephonyUtils.getMainCapabilityPhoneId();
        if (mainPhoneid == 0) {
            mTempPhoneid = 1;
        } else {
            mTempPhoneid = 0;
        }
        updateScreenEnabled();
        boolean dataServiceMode = getDataService();
        mEnableDataService.setChecked(dataServiceMode);
        getContentResolver().registerContentObserver(
                Settings.Global.getUriFor(DATA_SERVICE_ENABLED), true, mContentObserver);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        intentFilter.addAction(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        // For radio on/off
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE);
        intentFilter.addAction(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED);
        mDataUsageSummaryExt.customReceiver(intentFilter);
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        int subid = SubscriptionManager.getSubId(mTempPhoneid)[0];
        tm.listen(getPhoneStateListener(mTempPhoneid, subid), PhoneStateListener.LISTEN_CALL_STATE);

        getContext().registerReceiver(mReceiver, intentFilter);
    }

    private PhoneStateListener getPhoneStateListener(int phoneId, int subId) {
        mPhoneStateListener = new PhoneStateListener(subId) {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                Log.d(TAG, "onCallStateChanged state = " + state);
                updateScreenEnabled();
            }
        };
        return mPhoneStateListener;
    }

    private void showDataServiceDialog() {
        Log.d(TAG, "showDataServiceDialog");
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String title = this.getString(R.string.data_service_prompt);
        Dialog dialog = builder.setMessage(title).setNegativeButton(android.R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Dialog is cancelled");
                        mEnableDataService.setChecked(false);
                        setDataService(0);
                    }
                }).setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d(TAG, "Dialog is ok");
                        mEnableDataService.setChecked(true);
                        setDataService(1);
                    }
                }).create();

        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_BACK == keyCode) {
                    if (null != dialog) {
                        Log.d(TAG, "onKey keycode = back");
                        mEnableDataService.setChecked(false);
                        setDataService(0);
                        dialog.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
        //mDialog = dialog;
        dialog.show();
    }

    private void updateScreenEnabled() {
        boolean isSwitching = TelephonyUtils.isCapabilitySwitching();
        Log.d(TAG, "updateScreenEnabled, mIsAirplaneModeOn = " + mIsAirplaneModeOn
                + ", isSwitching = " + isSwitching
                + ", mTempPhoneid = " + mTempPhoneid);
        if (mEnableDataService != null) {
            mEnableDataService.setEnabled(!mIsAirplaneModeOn && !isSwitching
                && !mDataUsageSummaryExt.customTempdata(mTempPhoneid));
            mDataUsageSummaryExt.customTempdataHide(mEnableDataService);
        } else {
            Log.d(TAG, "mEnableDataService == null");
        }
    }

    private boolean getDataService() {
        int dataServie = 0;
        Context context = getContext();
        if (context != null) {
            dataServie = Settings.Global.getInt(context.getContentResolver(),
                    DATA_SERVICE_ENABLED, 0);
        }
        Log.d(TAG, "getDataService =" + dataServie);
        return dataServie == 0 ? false : true;
    }

    private void setDataService(int value) {
        Log.d(TAG, "setDataService =" + value);
        Settings.Global.putInt(getContext().getContentResolver(),
                DATA_SERVICE_ENABLED, value);
    }

    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (mEnableDataService == null) {
                Log.d(TAG, "onChange mEnableDataService == null");
                return;
            }

            boolean dataService = getDataService();
            Log.d(TAG, "onChange dataService = " + dataService
                    + ", isChecked = " + mEnableDataService.isChecked());
            if (dataService != mEnableDataService.isChecked()) {
                mEnableDataService.setChecked(dataService);
            }
        }
    };

    // Receiver to handle different actions
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mReceiver action = " + action);
            if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                mIsAirplaneModeOn = intent.getBooleanExtra("state", false);
                updateScreenEnabled();
            } else if (action.equals(TelephonyIntents.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)
                    || mDataUsageSummaryExt.customDualReceiver(action)) {
                updateScreenEnabled();
            } else if (action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_DONE)
                    || action.equals(TelephonyIntents.ACTION_SET_RADIO_CAPABILITY_FAILED)) {
                updateScreenEnabled();
            }
        }
    };

    private static boolean isDataServiceSupport() {
        boolean isSupport = ONE.equals(
                SystemProperties.get("persist.radio.smart.data.switch")) ? true : false;
        return isSupport;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (!isDataServiceSupport()) {
            return;
        }

        if (mEnableDataService != null) {
            getContentResolver().unregisterContentObserver(mContentObserver);
            getContext().unregisterReceiver(mReceiver);
            mEnableDataService = null;
        }
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (null != mPhoneStateListener) {
            tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
    /// @}

}
