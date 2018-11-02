/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 */

package com.mediatek.settings;

import android.app.Activity;
import android.bluetooth.BluetoothPan;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.System;
import android.support.v14.preference.PreferenceFragment;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceGroup;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.Utils;
import com.mediatek.settings.ext.IApnSettingsExt;

import java.util.concurrent.atomic.AtomicReference;

public class TetherSettingsExt implements OnPreferenceChangeListener,
    OnPreferenceClickListener {
    private static final String TAG = "TetherSettingsExt";

    public static final String KEY_WIFI_TETHER = "wifi_tether_settings";
    private static final String ACTION_WIFI_HOTSPOT = "mediatek.intent.action.WIFI_TETHER";
    private Preference mWifiTether;

    private static final String USB_DATA_STATE = "mediatek.intent.action.USB_DATA_STATE";

    private ConnectivityManager mConnectService;
    private Context mContext;
    private Resources mResources;
    private PreferenceScreen mPrfscreen;

    private String[] mBluetoothRegexs;

    public TetherSettingsExt(Context context) {
        Log.d(TAG, "TetherSettingsExt");
        mContext = context;
        initServices();
    }

    public void onCreate(PreferenceScreen screen) {
        Log.d(TAG, "onCreate");
        mPrfscreen = screen;
        initPreference(screen);
    }

    public void onStart(Activity activity, BroadcastReceiver receiver) {
        // add the receiver intent filter
        IntentFilter filter = getIntentFilter();
        activity.registerReceiver(receiver, filter);
    }

    public void updateWifiTether(Preference enableWifiAp,
            Preference wifiApSettings, boolean wifiAvailable) {
        // Fistly , always remove Google default
        mPrfscreen.removePreference(enableWifiAp);
        mPrfscreen.removePreference(wifiApSettings);
        // Init MTK WifiAPEnabler
        if (!wifiAvailable || Utils.isMonkeyRunning() || Utils.isWifiOnly(mContext)) {
            mPrfscreen.removePreference(mWifiTether);
        }
    }

    private void initPreference(PreferenceScreen screen) {
        // create wifi hotspot preference
        mWifiTether = new Preference(screen.getPreferenceManager().getContext());
        mWifiTether.setKey(KEY_WIFI_TETHER);
        mWifiTether.setTitle( R.string.wifi_tethering_title);
        mWifiTether.setPersistent(false);
        screen.addPreference(mWifiTether);
        mWifiTether.setOnPreferenceClickListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object value) {
        String key = preference.getKey();
        Log.d(TAG, "onPreferenceChange key=" + key);
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mWifiTether) {
            try {
                Intent intent = new Intent(ACTION_WIFI_HOTSPOT);
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(mContext, R.string.launch_error,
                        Toast.LENGTH_SHORT).show();
            }
        }
        return true;
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(USB_DATA_STATE);
        filter.addAction(BluetoothPan.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
        return filter;
    }

    private synchronized void initServices() {
        // get connectivity service
        mConnectService = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // get Resource
        mResources = mContext.getResources();
        mBluetoothRegexs = mConnectService.getTetherableBluetoothRegexs();
    }

    public boolean isUMSEnabled() {
        return false;
    }

    public void updateBTPrfSummary(Preference pref, String originSummary) {
        pref.setSummary(originSummary);
    }
}
