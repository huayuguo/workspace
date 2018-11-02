package com.mediatek.settings.sim;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

/**
 * Util class schedule for for monitoring Sim status.
 */
public class SimUtilJob {

    /** Schedule the start of the service every 10 - 30 seconds.
     * @param context App context to run job.
     */
    public static void scheduleJob(Context context) {
        ComponentName serviceComponent = new ComponentName(context, SimUtilService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(1 * 1000); // wait at least
        builder.setOverrideDeadline(3 * 1000); // maximum delay
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());
    }

}