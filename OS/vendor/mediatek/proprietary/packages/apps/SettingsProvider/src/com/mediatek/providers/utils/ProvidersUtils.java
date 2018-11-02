package com.mediatek.providers.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.os.SystemProperties;
import android.provider.Settings;

import com.android.internal.telephony.TelephonyProperties;
import com.android.providers.settings.R;

import com.mediatek.providers.settings.ext.IDatabaseHelperExt;
import com.mediatek.providers.settings.ext.OpSettingsProviderCustomizationFactoryBase;
import com.mediatek.provider.MtkSettingsExt;

public class ProvidersUtils {
    private static final String TAG = "ProvidersUtils";
    private IDatabaseHelperExt mDatebaseHelpExt;
    private Context mContext;
    private Resources mRes;

    public ProvidersUtils(Context context) {
        mContext = context;
        mRes = mContext.getResources();
        initDatabaseHelperPlgin(mContext);
    }

    private void initDatabaseHelperPlgin(Context context) {
        mDatebaseHelpExt = OpSettingsProviderCustomizationFactoryBase.getOpFactory(context)
                .makeDatabaseHelp(context);
    }

    public void loadCustomSystemSettings(SQLiteStatement stmt) {
        Log.d(TAG, "loadCustomSystemSettings");

        loadIntegerSetting(stmt, MtkSettingsExt.System.BG_POWER_SAVING_ENABLE,
                R.integer.def_bg_power_saving);
        // M: Add for Voice-wake-up
        boolean isSupport = SystemProperties.getBoolean("ro.mtk_voice_unlock_support", false);
        int defResId = isSupport ? R.integer.def_voice_unlock_mode
                       : R.integer.def_voice_wakeup_mode;
        loadIntegerSetting(stmt, MtkSettingsExt.System.VOICE_WAKEUP_MODE, defResId);
    }

    public void loadCustomGlobalSettings(SQLiteStatement stmt) {
        Log.d(TAG, "loadCustomGlobalSettings");
        loadSetting(
                stmt,
                MtkSettingsExt.Global.TELEPHONY_MISC_FEATURE_CONFIG,
                getIntegerValue(MtkSettingsExt.Global.TELEPHONY_MISC_FEATURE_CONFIG,
                        R.integer.def_telephony_misc_feature_config));
        loadBooleanSetting(stmt, MtkSettingsExt.Global.AUTO_TIME_GPS, R.bool.def_auto_time_gps);

        loadSetting(stmt, Settings.Global.INSTALL_NON_MARKET_APPS, getBooleanValue(
                Settings.Global.INSTALL_NON_MARKET_APPS, R.bool.def_install_non_market_apps));

        // Add for SIM-Mode @{
        String mSimConfig = SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG);
        int defResId = R.integer.def_single_sim_mode;
        if (mSimConfig.equals("dsds") || mSimConfig.equals("dsda")) {
            defResId = R.integer.def_dual_sim_mode;
        } else if (mSimConfig.equals("tsts")) {
            defResId = R.integer.def_triple_sim_mode;
        } else if (mSimConfig.equals("fsfs")) {
            defResId = R.integer.def_four_sim_mode;
        }
        loadIntegerSetting(stmt, MtkSettingsExt.Global.MSIM_MODE_SETTING, defResId);
        // @}

        loadBooleanSetting(stmt, MtkSettingsExt.Global.DATA_SERVICE_ENABLED,
                R.bool.def_data_service_enabled);
    }

    private void loadSetting(SQLiteStatement stmt, String key, Object value) {
        stmt.bindString(1, key);
        stmt.bindString(2, value.toString());
        stmt.execute();
    }

    private void loadStringSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key, mRes.getString(resid));
    }

    private void loadBooleanSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key, mRes.getBoolean(resid) ? "1" : "0");
    }

    private void loadIntegerSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key, Integer.toString(mRes.getInteger(resid)));
    }

    private void loadFractionSetting(SQLiteStatement stmt, String key, int resid, int base) {
        loadSetting(stmt, key, Float.toString(mRes.getFraction(resid, base, base)));
    }

    public String getBooleanValue(String name, int resId) {
        String defaultValue = mRes.getBoolean(resId) ? "1" : "0";
        return mDatebaseHelpExt.getResBoolean(mContext, name, defaultValue);
    }

    public String getStringValue(String name, int resId) {
        return mDatebaseHelpExt.getResStr(mContext, name, mRes.getString(resId));
    }

    public String getIntegerValue(String name, int resId) {
        String defaultValue = Integer.toString(mRes.getInteger(resId));
        return mDatebaseHelpExt.getResInteger(mContext, name, defaultValue);
    }

    public String getValue(String name, int defaultValue) {
        return mDatebaseHelpExt.getResInteger(mContext, name, Integer.toString(defaultValue));
    }

    /**
     * Load new operator settings value when operator config changed.
     *
     * NOTE: If settings value changed depends on operator, please put
     *       new values here.
     */
    public void loadNewOperatorSettings() {
        Log.v("OperatorConfigChangedReceiver", "loadNewOperatorSettings");
        ContentResolver contentResolver = mContext.getContentResolver();
        // Location settings
        Settings.Secure.putString(contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED,
                getStringValue(Settings.Secure.LOCATION_PROVIDERS_ALLOWED,
                        R.string.def_location_providers_allowed));
        // Haptic feedback settings
        Settings.System
                .putString(contentResolver, Settings.System.HAPTIC_FEEDBACK_ENABLED,
                        getBooleanValue(Settings.System.HAPTIC_FEEDBACK_ENABLED,
                                R.bool.def_haptic_feedback));
        // Auto time settings
        Settings.Global.putString(contentResolver, Settings.Global.AUTO_TIME, getBooleanValue(
                Settings.Global.AUTO_TIME, R.bool.def_auto_time));
        Settings.Global.putString(contentResolver, Settings.Global.AUTO_TIME_ZONE, getBooleanValue(
                Settings.Global.AUTO_TIME_ZONE, R.bool.def_auto_time_zone));
        // Non-market apps settings
        Settings.Global.putString(contentResolver, Settings.Global.INSTALL_NON_MARKET_APPS,
                getBooleanValue(Settings.Global.INSTALL_NON_MARKET_APPS,
                        R.bool.def_install_non_market_apps));
        // Telephony settings
        Settings.Global.putString(contentResolver,
                MtkSettingsExt.Global.TELEPHONY_MISC_FEATURE_CONFIG, getIntegerValue(
                        MtkSettingsExt.Global.TELEPHONY_MISC_FEATURE_CONFIG,
                        R.integer.def_telephony_misc_feature_config));
    }
}
