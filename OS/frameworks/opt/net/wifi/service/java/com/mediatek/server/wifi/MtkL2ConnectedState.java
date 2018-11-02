/*
 * Copyright (C) 2014 MediaTek Inc.
 * Modification based on code covered by the mentioned copyright
 * and/or permission notice(s).
*/

package com.mediatek.server.wifi;

import static android.net.wifi.WifiManager.WIFI_STATE_DISABLING;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;

import com.android.internal.util.Protocol;
import com.android.internal.util.State;
import com.android.server.wifi.WifiStateMachine;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MtkL2ConnectedState extends State {

    private static final String TAG = "WifiStateMachine";

    /* WFC status:
     * 0: Indicate Fwk no need to defer disable wifi process
     * 1: Indicate Fwk to defer disable wifi process if needed
     * 2: Indicate Fwk good to go
    */
    private static final int NO_NEED_DEFER = 0;
    private static final int NEED_DEFER = 1;
    private static final int WFC_NOTIFY_GO = 2;

    static final int MTK_BASE = Protocol.BASE_WIFI + 400;

    /* used to indicate that the notification from WFC */
    private static final int CMD_WFC_NOTIFY_GO            = MTK_BASE + 1;
    private static final int CMD_WFC_NOTIFY_TIMEOUT       = MTK_BASE + 2;

    private static final int CMD_STOP_SUPPLICANT          = Protocol.BASE_WIFI + 12;
    /* Set operational mode. CONNECT, SCAN ONLY, SCAN_ONLY with Wi-Fi off mode */
    private static final int CMD_SET_OPERATIONAL_MODE     = Protocol.BASE_WIFI + 72;

    /* used to indicate that whether WFC need to defer disable wifi */
    private boolean mShouldDeferDisableWifi = false;

    /* used to record the defer message */
    private Message mDeferredMsg = null;

    private enum Status {
        MONITORING,
        WAITING
    }
    private Status mState = Status.MONITORING;

    private Context mContext;
    private WifiStateMachine mWSM;
    private State mL2ConnectedState;

    public MtkL2ConnectedState(WifiStateMachine wsm, Context context, State stt) {
        Log.d(TAG, "Initialize MtkL2ConnectedState");
        mWSM = wsm;
        mContext = context;
        mL2ConnectedState = stt;

        mContext.registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        int wfcStatus = intent.getIntExtra("wfc_status", NO_NEED_DEFER);
                        Log.d(TAG, "Received WFC_STATUS_CHANGED, current status: "+ wfcStatus);
                        switch(wfcStatus) {
                            case NO_NEED_DEFER:
                                mShouldDeferDisableWifi = false;
                                break;
                            case NEED_DEFER:
                                mShouldDeferDisableWifi = true;
                                break;
                            case WFC_NOTIFY_GO:
                                mWSM.sendMessage(CMD_WFC_NOTIFY_GO);
                                break;
                            default:
                                break;
                        }
                    }
                },
                new IntentFilter("com.mediatek.intent.action.WFC_STATUS_CHANGED"));
    }

    @Override
    public void enter() {
        mL2ConnectedState.enter();
    }

    @Override
    public void exit() {
        mL2ConnectedState.exit();
    }

    @Override
    public boolean processMessage(Message message) {
        Log.d(TAG, " " + this.getClass().getSimpleName() + " " + message.toString() + " "
                + message.arg1 + " " +  message.arg2 + ", mShouldDeferDisableWifi = "
                + mShouldDeferDisableWifi + ", mState = " + mState.toString());
        // We should guarantee that this state keep receiving message even mShouldDeferDisableWifi
        // equal to false until status goes to monitoring. There is a scenario could happen:
        // State is under waiting but WFC set WFC_NOTIFY_GO and NO_NEED_DEFER consecutively, and
        // we will miss to handle CMD_WFC_NOTIFY_GO.
        if (!mShouldDeferDisableWifi && mState != Status.WAITING) {
            return mL2ConnectedState.processMessage(message);
        }

        if (mState == Status.MONITORING) {
            switch(message.what) {
                case CMD_SET_OPERATIONAL_MODE:
                    // This logic should be aligned with the behavior in L2ConnectedState
                    if (message.arg1 == WifiStateMachine.CONNECT_MODE) {
                        break;
                    }
                case CMD_STOP_SUPPLICANT:
                    // Broadcast disabling to prevent multi wifi off
                    try {
                        Method method =
                                mWSM.getClass().getDeclaredMethod("setWifiState", int.class);
                        method.setAccessible(true);
                        method.invoke(mWSM, WIFI_STATE_DISABLING);
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                    // Create a copy message for defer
                    mDeferredMsg = mWSM.obtainMessage();
                    mDeferredMsg.copyFrom(message);
                    mState = Status.WAITING;
                    mWSM.sendMessageDelayed(CMD_WFC_NOTIFY_TIMEOUT, 3000);
                    break;
                default:
                    return mL2ConnectedState.processMessage(message);
            }
        } else if (mState == Status.WAITING) {
            switch(message.what) {
                case CMD_SET_OPERATIONAL_MODE:
                    if (message.arg1 == WifiStateMachine.CONNECT_MODE) {
                        mWSM.deferMessage(message);
                        break;
                    }
                case CMD_STOP_SUPPLICANT:
                    mState = Status.MONITORING;
                    return mL2ConnectedState.processMessage(message);
                case CMD_WFC_NOTIFY_TIMEOUT:
                    Log.e(TAG, "WFC callback timeout!!!!!");
                case CMD_WFC_NOTIFY_GO:
                    goToL2ConnectedState();
                    break;
                default:
                    // -4: MESSAGE_HANDLING_STATUS_DEFERRED in WifiStateMachine
                    try {
                        Field field = mWSM.getClass().getDeclaredField("messageHandlingStatus");
                        field.setAccessible(true);
                        field.set(mWSM, -4);
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                    }
                    mWSM.deferMessage(message);
                    break;
            }
        }
        return true; /*HANDLED*/
    }

    private void goToL2ConnectedState() {
        if (mDeferredMsg != null) {
            mWSM.sendMessage(mDeferredMsg);
            mDeferredMsg = null;
        }
    }
}

