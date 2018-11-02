package com.mediatek.settings.sim;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;
/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class SimUtilService extends JobService {
    private static final String TAG = "SimUtilService";

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            Intent service = new Intent(getApplicationContext(), SimSelectService.class);
            getApplicationContext().startService(service);
        } catch (Exception e) {
            Log.e(TAG, "SimSelectService Not running");
        } finally {
            SimUtilJob.scheduleJob(getApplicationContext()); // reschedule the job
        }
        /* Intent service = new Intent(getApplicationContext(), SimSelectService.class);
        getApplicationContext().startService(service);
        SimUtilJob.scheduleJob(getApplicationContext()); // reschedule the job */
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}