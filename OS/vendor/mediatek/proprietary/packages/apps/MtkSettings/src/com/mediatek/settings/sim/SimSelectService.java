package com.mediatek.settings.sim;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;


import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.settings.R;
import com.android.settings.Settings.SimSettingsActivity;
import com.android.settings.Utils;
import com.android.settings.sim.SimDialogActivity;

import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.settings.UtilsExt;
import com.mediatek.settings.cdma.CdmaSimDialogActivity;
import com.mediatek.settings.cdma.CdmaUtils;
import com.mediatek.settings.cdma.OmhEventHandler;
import com.mediatek.settings.ext.ISettingsMiscExt;

import java.util.List;

/**
 * Service to monitor Sub Info Record event.
 */
public class SimSelectService extends Service {
    private static final String TAG = "SimSelectService";
    private static final int NOTIFICATION_ID = 1;

    private static boolean sBootCompleted = false;
    private static boolean sNotifyPended = false;
    private static int sPendingNotifyCdmaType = -1;

    /*@Override
    protected void onHandleIntent(Intent intent) {
        IntentFilter mIntentFilter;
        mIntentFilter = new IntentFilter(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
        getApplicationContext().registerReceiver(mReceiver, mIntentFilter);
        return ;
    }*/

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        sBootCompleted = SystemProperties.get("sys.boot_completed").equals("1");
        IntentFilter mIntentFilter;
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(TelephonyIntents.ACTION_SUBINFO_RECORD_UPDATED);
        mIntentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
        ///M: for opeator dual with broadcast @{
        UtilsExt.getSimManagementExt(this).customRegisteBroadcast(mIntentFilter);
        /// @}
        getApplicationContext().registerReceiver(mReceiver, mIntentFilter);
        Log.d(TAG, "onCreate, bootCompleted=" + sBootCompleted);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        getApplicationContext().unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action=" + action + ", bootCompleted=" + sBootCompleted);
            /// M: for opeator dual with broadcast @{
            UtilsExt.getSimManagementExt(context).customBroadcast(intent);
            /// @}
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                sBootCompleted = true;
            }

            /// M: add for auto sanity @{
            if (UtilsExt.shouldDisableForAutoSanity()) {
                Log.d(TAG, "disable for auto sanity, return");
                return;
            }
            /// @}
            /// M: for 02811539, airplane mode on not allow SIM switch, so ignore the event @{
            if (TelephonyUtils.isAirplaneModeOn(context)) {
                Log.d(TAG, "airplane mode is on, ignore!");
                return;
            }
            /// @}

            /// M: for [C2K 2 SIM Warning] @{
            // can not listen to SIM_STATE_CHANGED because it happened even when SIM switch
            // (switch default data), in which case should not show the C2K 2 SIM warning
            // SIM_STATE_CHANGED is not exact, maybe ICC is ready or absent,
            // but SubscriptionController is not ready.
            List<SubscriptionInfo> subs = SubscriptionManager.from(context)
                    .getActiveSubscriptionInfoList();

            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.d(TAG, "notifyPended=" + sNotifyPended
                        + ", pendingNotifyCdmaType=" + sPendingNotifyCdmaType);
                if (sNotifyPended) {
                    if (sPendingNotifyCdmaType == CdmaSimDialogActivity.TWO_CDMA_CARD) {
                        if (subs != null && subs.size() > 1) {
                            CdmaUtils.checkCdmaSimStatus(context, subs.size());
                        }
                        sPendingNotifyCdmaType = -1;
                    }
                    sNotifyPended = false;
                }
                return;
            }

            final int detectedType = intent.getIntExtra(
                    MtkSubscriptionManager.INTENT_KEY_DETECT_STATUS, 0);
            Log.d(TAG, "sub info update, type = " + detectedType + ", subs = " + subs);
            if (detectedType == MtkSubscriptionManager.EXTRA_VALUE_NOCHANGE) {
                Log.d(TAG, "extra value no change, return");
                return;
            }

            /// M: Ignore this broadcast when the SIM count is changed again. @{
            final int numSims = intent.getIntExtra(
                    MtkSubscriptionManager.INTENT_KEY_SIM_COUNT, (subs == null ? 0 : subs.size()));
            if (subs != null && numSims != subs.size()) {
                Log.d(TAG, "SIM count is changed again, extraSimCount=" + numSims
                        + ", currentSimCount=" + subs.size());
                return;
            }
            /// @}

            if (subs != null && subs.size() > 1) {
                if (sBootCompleted) {
                    CdmaUtils.checkCdmaSimStatus(context, subs.size());
                } else {
                    sNotifyPended = true;
                    sPendingNotifyCdmaType = CdmaSimDialogActivity.TWO_CDMA_CARD;
                    Log.d(TAG, "Boot is not completed, keep CDMA notify type="
                            + sPendingNotifyCdmaType);
                }
            }
            /// @}

            final TelephonyManager telephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            final SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            final int numSlots = telephonyManager.getSimCount();
            final boolean isInProvisioning = Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.DEVICE_PROVISIONED, 0) == 0;
            Log.d(TAG, "numSlots = " + numSlots + "isInProvisioning = " + isInProvisioning);
            // Do not create notifications on single SIM devices or when provisioning
            if (numSlots < 2 || isInProvisioning) {
                return;
            }

            // Cancel any previous notifications
            cancelNotification(context);

            List<SubscriptionInfo> sil = subscriptionManager.getActiveSubscriptionInfoList();
            if (sil == null || sil.size() < 1) {
                Log.d(TAG, "Subscription list is empty");
                return;
            }

            // Clear defaults for any subscriptions which no longer exist
            subscriptionManager.clearDefaultsForInactiveSubIds();

            boolean dataSelected = SubscriptionManager.isUsableSubIdValue(
                    SubscriptionManager.getDefaultDataSubscriptionId());
            boolean smsSelected = SubscriptionManager.isUsableSubIdValue(
                    SubscriptionManager.getDefaultSmsSubscriptionId());
            Log.d(TAG, "dataSelected = " + dataSelected + " smsSelected = " + dataSelected);

            // If data and sms defaults are selected, dont show notification
            //(Calls default is optional)
            if (dataSelected && smsSelected
                    /// M: For Op01 open market. @{
                    && !SystemProperties.get("ro.cmcc_light_cust_support").equals("1")
                /// @}
            ) {
                Log.d(TAG, "Data & SMS default sims are selected. No notification");
                return;
            }

            // Create a notification to tell the user that some defaults are missing
            createNotification(context);
            /// M: for China opeator sepc, remove dialog
            if (!UtilsExt.getSimManagementExt(context).isSimDialogNeeded()) {
                Log.d(TAG, "sim dialog not needed, RETURN!");
                return;
            }
            if (sil.size() == 1) {
                /// M: For Op01 open market,show no notification. @{
                if (SystemProperties.get("ro.cmcc_light_cust_support").equals("1")) {
                    Log.d(TAG, "size == 1,show no notification");
                    return;
                }
                /// @}
                // If there is only one subscription, ask if user wants to use if for everything
                Log.d(TAG, "sim size == 1, SimDialogActivity shown");
                Intent newIntent = new Intent(context, SimDialogActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                newIntent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY,
                        SimDialogActivity.PREFERRED_PICK);
                newIntent.putExtra(SimDialogActivity.PREFERRED_SIM, sil.get(0).getSimSlotIndex());
                context.startActivity(newIntent);
               /// M: for [C2K OMH Warning]
                OmhEventHandler.getInstance(context).sendEmptyMessage(OmhEventHandler.SET_BUSY);
            } else if (!dataSelected ||
                /// M: Op01 open market request. @{
                    SystemProperties.get("ro.cmcc_light_cust_support").equals("1")) {
                /// @}
                // If there are mulitple, ensure they pick default data
                Log.d(TAG, "SimDialogActivity shown for multiple sims");
                Intent newIntent = new Intent(context, SimDialogActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                newIntent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, SimDialogActivity.DATA_PICK);
                context.startActivity(newIntent);
                /// M: for [C2K OMH Warning]
                OmhEventHandler.getInstance(context).sendEmptyMessage(OmhEventHandler.SET_BUSY);
            }
        }
    };

    private void createNotification(Context context) {
        final Resources resources = context.getResources();
        // The id of the channel.
        String id = "sim_select_channel_01";
        Notification.Builder builder =
                new Notification.Builder(context, id)
                .setSmallIcon(R.drawable.ic_sim_card_alert_white_48dp)
                .setColor(context.getColor(R.color.sim_noitification))
                .setContentTitle(resources.getString(R.string.sim_notification_title))
                .setContentText(resources.getString(R.string.sim_notification_summary));
        /// M: for plug-in
        customizeSimDisplay(context, builder);
        Intent resultIntent = new Intent(context, SimSettingsActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The user-visible name of the channel.
        CharSequence name = resources.getString(R.string.sim_notification_title);
        // The user-visible description of the channel.
        String description = resources.getString(R.string.sim_notification_summary);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(false);
        mChannel.enableVibration(false);
        notificationManager.createNotificationChannel(mChannel);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Remove notification for sim card UI.
     * @param context UI conetxt.
     */
    public static void cancelNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    ///---------------------------------------MTK-------------------------------------------------

    /**
     * only for plug-in, change "SIM" to "UIM/SIM".
     * @param context the context.
     * @param builder the notification builder.
     */
    private void customizeSimDisplay(
                    Context context,
                    Notification.Builder builder) {
        Resources resources = context.getResources();
        String title = resources.getString(R.string.sim_notification_title);
        String text = resources.getString(R.string.sim_notification_summary);

        ISettingsMiscExt miscExt = UtilsExt.getMiscPlugin(context);
        title = miscExt.customizeSimDisplayString(
                            title,
                            SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        text = miscExt.customizeSimDisplayString(
                            text,
                            SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        builder.setContentTitle(title);
        builder.setContentText(text);
    }
}
