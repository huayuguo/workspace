package com.mediatek.settings.sim;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.android.settings.R;

import com.mediatek.provider.MtkSettingsExt;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.ISimManagementExt;
import com.mediatek.telephony.MtkTelephonyManagerEx;

/**
 * Radio power manager to control radio state.
 */
public class RadioPowerController {

    private static final String TAG = "RadioPowerController";
    private Context mContext;
    private static final int MODE_PHONE1_ONLY = 1;
    private ISimManagementExt mExt;
    private static RadioPowerController sInstance = null;
    private static final boolean ENG_LOAD = SystemProperties.get("ro.build.type").equals("eng") ?
            true : false || Log.isLoggable(TAG, Log.DEBUG);

    /// M: CC: Check ECC state according to TeleService state
    private MtkTelephonyManagerEx mTelEx;

   /**
    * Constructor.
    * @param context Context
    */
    private RadioPowerController(Context context) {
        mContext = context;
        mExt = UtilsExt.getSimManagementExt(mContext);
        /// M: CC: Check ECC state according to TeleService state
        mTelEx = MtkTelephonyManagerEx.getDefault();
    }

    private static synchronized void createInstance(Context context) {
        if (sInstance == null) {
            sInstance = new RadioPowerController(context);
        }
    }

    public static RadioPowerController getInstance(Context context) {
        if (sInstance == null) {
            createInstance(context);
        }
        return sInstance;
    }

    public boolean setRadionOn(int subId, boolean turnOn) {
        logInEng("setRadioOn, turnOn: " + turnOn + ", subId = " + subId);
        boolean isSuccessful = false;
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return isSuccessful;
        }

        /// M: CC: Check ECC state according to TeleService state @{
        // Don't allow opetate radio off during ECC to avoid ATD after EFUN
        boolean isInEcc = mTelEx.isEccInProgress();

        if (!turnOn && isInEcc) {
            Log.d(TAG, "Not allow to operate radio power during emergency call");
            Toast.makeText(mContext.getApplicationContext(),
                    R.string.radio_off_during_emergency_call, Toast.LENGTH_LONG).show();
            return false;
        }
        /// @}

        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(
                Context.TELEPHONY_SERVICE));
        try {
            if (telephony != null &&
                    telephony.isRadioOnForSubscriber(subId, mContext.getPackageName()) != turnOn) {
                isSuccessful = telephony.setRadioForSubscriber(subId, turnOn);
                if (isSuccessful) {
                    updateRadioMsimDb(subId, turnOn);
                    /// M: for plug-in
                    mExt.setRadioPowerState(subId, turnOn);
                }
            } else {
                logInEng("telephony is null");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        logInEng("setRadionOn, isSuccessful: " + isSuccessful);
        return isSuccessful;
    }

    private void updateRadioMsimDb(int subId, boolean turnOn) {
        int priviousSimMode = Settings.Global.getInt(mContext.getContentResolver(),
                MtkSettingsExt.Global.MSIM_MODE_SETTING, -1);
        logInEng("updateRadioMsimDb, The current dual sim mode is " + priviousSimMode
                + ", with subId = " + subId);
        int currentSimMode;
        boolean isPriviousRadioOn = false;
        int slot = SubscriptionManager.getSlotIndex(subId);
        int modeSlot = MODE_PHONE1_ONLY << slot;
        if ((priviousSimMode & modeSlot) > 0) {
            currentSimMode = priviousSimMode & (~modeSlot);
            isPriviousRadioOn = true;
        } else {
            currentSimMode = priviousSimMode | modeSlot;
            isPriviousRadioOn = false;
        }

        logInEng("currentSimMode=" + currentSimMode + " isPriviousRadioOn =" + isPriviousRadioOn
                + ", turnOn: " + turnOn);
        if (turnOn != isPriviousRadioOn) {
            Settings.Global.putInt(mContext.getContentResolver(),
                    MtkSettingsExt.Global.MSIM_MODE_SETTING, currentSimMode);
        } else {
            logInEng("quickly click don't allow.");
        }
    }

    /**
     * whether radio switch finish on subId, according to the radio state.
     */
    public boolean isRadioSwitchComplete(final int subId) {
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            return false;
        }
        int slotId = SubscriptionManager.getSlotIndex(subId);

        boolean isRadioOn = TelephonyUtils.isRadioOn(subId, mContext);
        logInEng("isRadioSwitchComplete: slot: " + slotId + ", isRadioOn: " + isRadioOn);
        if (!isRadioOn || (isExpectedRadioStateOn(slotId) && isRadioOn)) {
            logInEng("isRadioSwitchComplete : true");
            return true;
        }
        return false;
    }

    public boolean isExpectedRadioStateOn(int slot) {
        int currentSimMode = Settings.Global.getInt(mContext.getContentResolver(),
                MtkSettingsExt.Global.MSIM_MODE_SETTING, -1);
        boolean expectedRadioOn = (currentSimMode & (MODE_PHONE1_ONLY << slot)) != 0;
        logInEng("isExpectedRadioStateOn: slot: " + slot +
                ", expectedRadioOn: " + expectedRadioOn);
        return expectedRadioOn;
    }

    private void logInEng(String s) {
        if (ENG_LOAD) {
            Log.d(TAG, s);
        }
    }
}