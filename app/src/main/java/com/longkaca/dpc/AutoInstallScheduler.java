package com.longkaca.dpc;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public final class AutoInstallScheduler {
    private static final int JOB_ID = 18001;
    private AutoInstallScheduler() {}

    public static void schedule(Context context) {
        if (!ConfigStore.isAutoInstallRequested(context)) return;
        JobScheduler js = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js == null) return;

        JobInfo job = new JobInfo.Builder(
                JOB_ID, new ComponentName(context, AutoInstallJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(8000)
                .build();
        js.schedule(job);
    }
}
