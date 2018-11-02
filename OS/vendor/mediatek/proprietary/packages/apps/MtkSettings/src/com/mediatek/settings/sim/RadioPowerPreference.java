package com.mediatek.settings.sim;

import android.content.Context;
import android.os.SystemProperties;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.util.Log;

import com.android.settings.R;
import com.mediatek.settings.FeatureOption;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.ext.ISimManagementExt;

/**
 * A preference for radio switch function.
 */
public class RadioPowerPreference extends Preference {

    private static final String TAG = "RadioPowerPreference";
    private boolean mPowerState;
    private boolean mPowerEnabled = true;
    private int mSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    private Switch mRadioSwith = null;
    private RadioPowerController mController;
    private static final boolean ENG_LOAD = SystemProperties.get("ro.build.type").equals("eng") ?
            true : false || Log.isLoggable(TAG, Log.DEBUG);
    private ISimManagementExt mExt;

    /**
     * Construct of RadioPowerPreference.
     * @param context Context.
     */
    public RadioPowerPreference(Context context) {
        super(context);
        mExt = UtilsExt.getSimManagementExt(context);
        mController = RadioPowerController.getInstance(context);
        setWidgetLayoutResource(R.layout.radio_power_switch);
    }

    /**
     * Set the radio switch state.
     * @param state On/off.
     */
    public void setRadioOn(boolean state) {
        logInEng("setRadioOn " + state + " subId = " + mSubId);
        mPowerState = state;
        if (mRadioSwith != null) {
            mRadioSwith.setChecked(state);
        }
    }

    /**
     * Set the radio switch enable state.
     * @param enable Enable.
     */
    public void setRadioEnabled(boolean enable) {
        logInEng("setRadioEnabled : " + enable);
        mPowerEnabled = enable;
        if (mRadioSwith != null) {
            mRadioSwith.setEnabled(enable);
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        mRadioSwith = (Switch) view.findViewById(R.id.radio_state);
        if (mRadioSwith != null) {
            if (FeatureOption.MTK_A1_FEATURE) {
                mRadioSwith.setVisibility(View.GONE);
            }
            mRadioSwith.setEnabled(mPowerEnabled);
            mRadioSwith.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    logInEng("onCheckedChanged, mPowerState = " + mPowerState
                            + ", isChecked = " + isChecked + ", subId = " + mSubId);
                    if (mPowerState != isChecked) {
                        if (mController.setRadionOn(mSubId, isChecked)) {
                            // disable radio switch to prevent continuous click
                            logInEng("onCheckedChanged mPowerState = " + isChecked);
                            mPowerState = isChecked;
                            setRadioEnabled(false);
                            ///M: for opeator require, when radio off, maybe need update main phone. @{
                            mExt.customizeMainCapabily(mPowerState, mSubId);
                            /// @}
                        } else {
                            // if set radio fail, revert button status.
                            logInEng("set radio power FAIL!");
                            setRadioOn(!isChecked);
                        }
                    }
                }
            });
            // ensure setOnCheckedChangeListener before setChecked state, or the
            // expired OnCheckedChangeListener will be called, due to the view is RecyclerView
            Log.d(TAG, "onBindViewHolder mPowerState = " + mPowerState + " subid = " + mSubId);
            mRadioSwith.setChecked(mPowerState);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        mPowerEnabled = enabled;
        super.setEnabled(enabled);
    }

    /**
     * Bind the preference with corresponding property.
     * @param subId sub id for this preference
     * @param normal radio is not switching and not in airplane mode
     */
    public void bindRadioPowerState(final int subId, boolean normal) {
        mSubId = subId;
        if (normal) {
            setRadioOn(TelephonyUtils.isRadioOn(subId, getContext()));
            boolean isValidSub = SubscriptionManager.isValidSubscriptionId(subId);
            logInEng("setRadioEnabled : " + isValidSub);
            setRadioEnabled(isValidSub);
        } else {
            logInEng("setRadioEnabled : false");
            setRadioEnabled(false);
            setRadioOn(mController.isExpectedRadioStateOn(SubscriptionManager.getSlotIndex(subId)));
        }
    }

    private void logInEng(String s) {
        if (ENG_LOAD) {
            Log.d(TAG, s);
        }
    }
}
