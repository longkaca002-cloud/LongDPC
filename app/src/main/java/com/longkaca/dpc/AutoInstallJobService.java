package com.longkaca.dpc;

import android.app.admin.DevicePolicyManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class AutoInstallJobService extends JobService {
    @Override public boolean onStartJob(JobParameters params) {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        if (dpm == null || !dpm.isDeviceOwnerApp(getPackageName())
                || !ConfigStore.isAutoInstallRequested(this)
                || ConfigStore.isAutoInstallStarted(this)) {
            return false;
        }

        ConfigStore.markAutoInstallStarted(this);
        new Thread(() -> {
            String apnProfile = ConfigStore.get(this, "apn_profile", "").trim();
            if (!apnProfile.isEmpty()) {
                try { ApnAdmin.apply(this, apnProfile); }
                catch (Exception ignored) { /* Manual APN buttons remain available. */ }
            }
            for (int i = 0; i < AppCatalog.NAMES.length; i++) {
                String u = ConfigStore.get(this, "apk_" + i + "_url", "").trim();
                if (u.isEmpty()) continue;
                try {
                    ApkInstaller.downloadAndInstall(
                            this, u, AppCatalog.NAMES[i], AppCatalog.PACKAGES[i]);
                } catch (Exception e) {
                    final String label = AppCatalog.NAMES[i];
                    final String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(this, "Lỗi " + label + ": " + msg, Toast.LENGTH_LONG).show());
                }
            }
            jobFinished(params, false);
        }).start();
        return true;
    }

    @Override public boolean onStopJob(JobParameters params) {
        // MainActivity vẫn là fallback thủ công nếu firmware dừng job giữa chừng.
        return false;
    }
}
