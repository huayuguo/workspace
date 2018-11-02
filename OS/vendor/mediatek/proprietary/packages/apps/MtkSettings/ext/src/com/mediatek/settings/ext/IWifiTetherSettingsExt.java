package com.mediatek.settings.ext;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.view.View;

public interface IWifiTetherSettingsExt {

    /**
     * [OP03]
     * Customize list preference.
     * @param prefScreen preferenceScreen
     */
    void customizePreference(Object prefScreen);

    /**
     * [OP18]
     * For RJIL WIFI TETHER feature.
     * add WifiTetherPreferenceController to show WifiAp broadcast channel.
     * @param context context
     * @param controllers OM List controller.
     * @param listener listener
     */
    void addPreferenceController(Context context, Object controllers, Object listener);

    /**
     * [OP18]
     * For RJIL WIFI TETHER feature.
     * Notify perference change (for Wifi AP Band preference).
     * @param pref_key preference key
     * @param value changed value onPreferenceChange
     */
    void onPrefChangeNotify(String pref_key, Object value);

    /**
     * [OP18][OLD]
     * Customize Wifi ap dialog view to add broadcast channel selection option.
     * @param context The parent context
     * @param view parent layout view
     * @param config wificonfiguration object
     */
    void customizeView(Context context, View view, WifiConfiguration config);

    /**
     * [OP18][OLD]
     * Update wifiConfiguration with selected apChannel information.
     * @param config wificonfiguration object
     */
    void updateConfig(WifiConfiguration config);

    /**
     * [OP18][OLD]
     * Set ApChannel spinner when band is changed.
     * @param apBand selected AP band
     * @param needToSet this is to check if different band is selected
     */
    void setApChannel(int apBand, boolean needToSet);


    /**
     * [OP08]
     * Add allowed-device-list preference.
     * @param prefScreen parent preference screen
     */
    void addAllowedDeviceListPreference(Object prefScreen);

    /**
     * [OP08]
     * Launch allowed device list activity [OP08].
     * @param preference allowedDevicePreference
     */
    void launchAllowedDeviceActivity(Object preference);
}
