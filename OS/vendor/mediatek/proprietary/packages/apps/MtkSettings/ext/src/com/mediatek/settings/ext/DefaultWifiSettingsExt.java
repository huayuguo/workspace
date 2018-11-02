package com.mediatek.settings.ext;

import com.android.settingslib.wifi.AccessPoint;

import android.content.ContentResolver;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceCategory;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

/* Dummy implmentation , do nothing */
public class DefaultWifiSettingsExt implements IWifiSettingsExt {
    private static final String TAG = "DefaultWifiSettingsExt";

    public void registerPriorityObserver(ContentResolver contentResolver) {
    }

    public void unregisterPriorityObserver(ContentResolver contentResolver) {
    }

    public void setLastConnectedConfig(WifiConfiguration config) {
    }

    public void updatePriority() {
    }

    public void updateContextMenu(ContextMenu menu, int menuId,
            DetailedState state) {
    }

    public void emptyCategory(PreferenceScreen screen) {
    }

    public void emptyScreen(PreferenceScreen screen) {
    }

    public void refreshCategory(PreferenceScreen screen) {
    }

    public void recordPriority(WifiConfiguration selectPriority) {
    }

    public void setNewPriority(WifiConfiguration config) {
    }

    public void updatePriorityAfterSubmit(WifiConfiguration config) {
    }

    public boolean disconnect(MenuItem item, WifiConfiguration wifiConfig) {
        return false;
    }

    public void addPreference(PreferenceScreen screen,
            PreferenceCategory preferenceCategory, Preference preference,
            boolean isConfiged) {
        if (preferenceCategory != null) {
            preferenceCategory.addPreference(preference);
        }
    }

    public void init(PreferenceScreen screen) {
    }

    public boolean removeConnectedAccessPointPreference() {
        return false;
    }

    public void emptyConneCategory(PreferenceScreen screen) {
    }

    @Override
    public void submit(WifiConfiguration config,
            AccessPoint selectedAccessPoint, DetailedState detailedState) {
    }

    @Override
    public void addRefreshPreference(PreferenceScreen screen,
            Object wifiTracker,
            boolean isUiRestricted) {
    }

    @Override
    public boolean customRefreshButtonClick(Preference preference) {
        return false;
    }

    @Override
    public void customRefreshButtonStatus(boolean checkStatus) {
    }
}
