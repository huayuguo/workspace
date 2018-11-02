package com.mediatek.settings.sim;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.RadioAccessFamily;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.ProxyController;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.MtkProxyController;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.settings.FeatureOption;

import java.util.Iterator;

public class TelephonyUtils {
    private static boolean DBG = SystemProperties.get("ro.build.type").equals("eng") ? true : false;
    private static final String TAG = "TelephonyUtils";

    /**
     * Get whether airplane mode is in on.
     * @param context Context.
     * @return True for on.
     */
    public static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    /**
     * Calling API to get subId is in on.
     * @param subId Subscribers ID.
     * @return {@code true} if radio on
     */
    public static boolean isRadioOn(int subId, Context context) {
        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isOn = false;
        try {
            if (phone != null) {
                isOn = subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID ? false :
                    phone.isRadioOnForSubscriber(subId, context.getPackageName());
            } else {
                Log.e(TAG, "ITelephony is null !!!");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        log("isRadioOn = " + isOn + ", subId: " + subId);
        return isOn;
    }

    /**
     * capability switch.
     * @return true : switching.
     */

    public static boolean isCapabilitySwitching() {
        MtkProxyController proxyCtrl = (MtkProxyController) ProxyController.getInstance();
        boolean isSwitching = false;
            if (proxyCtrl != null) {
                isSwitching = proxyCtrl.isCapabilitySwitching();
            } else {
                Log.d(TAG, "proxyCtrl is null, returen false");
            }
        log("isSwitching: " + isSwitching);
        return isSwitching;
    }

    private static void log(String msg){
        if (DBG) {
            Log.d(TAG, msg);
        }
    }

    /**
     * Get the phone id with main capability.
     */
    public static int getMainCapabilityPhoneId() {
        int phoneId = SubscriptionManager.INVALID_PHONE_INDEX;
        IMtkTelephonyEx iTelEx = IMtkTelephonyEx.Stub.asInterface(
                                                ServiceManager.getService("phoneEx"));
        if (iTelEx != null) {
            try {
                phoneId = iTelEx.getMainCapabilityPhoneId();
            } catch (RemoteException e) {
                log("getMainCapabilityPhoneId: remote exception");
            }
        } else {
            log("IMtkTelephonyEx service not ready!");
            phoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        }
        return phoneId;
    }
}