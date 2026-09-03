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
                || !ConfigStore.isAutoInstallRequested(this)) {
            return false;
        }

        ConfigStore.markAutoInstallStarted(this);
        new Thread(() -> {
            int round = ConfigStore.nextAutoInstallRound(this);
            String apnProfile = ConfigStore.get(this, "apn_profile", "").trim();
            if (!apnProfile.isEmpty()) {
                applyApnWithRetry(apnProfile);
            }
            for (int i = 0; i < AppCatalog.NAMES.length; i++) {
                String u = ConfigStore.get(this, "apk_" + i + "_url", "").trim();
                if (u.isEmpty()) continue;
                if (isPackageInstalled(AppCatalog.PACKAGES[i])) continue;
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
            // Android may stop the first provisioning job or finish PackageInstaller callbacks later.
            // Retry only missing packages; installed packages are skipped above.
            jobFinished(params, round < 4);
        }).start();
        return true;
    }

    @Override public boolean onStopJob(JobParameters params) {
        // MainActivity vẫn là fallback thủ công nếu firmware dừng job giữa chừng.
        return false;
    }

    private void applyApnWithRetry(String profile) {
        String last = "";
        for (int attempt = 1; attempt <= 6; attempt++) {
            try {
                String result = ApnAdmin.apply(this, profile);
                ConfigStore.put(this, "last_apn_status", result);
                return;
            } catch (Exception e) {
                last = e.getMessage() == null ? e.toString() : e.getMessage();
                ConfigStore.put(this, "last_apn_status", "Lần " + attempt + ": " + last);
                if (attempt < 6) {
                    try { Thread.sleep(5000); }
                    catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); break; }
                }
            }
        }
        final String msg = last;
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, "APN chưa tự áp dụng: " + msg, Toast.LENGTH_LONG).show());
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                getPackageManager().getPackageInfo(packageName,
                        android.content.pm.PackageManager.PackageInfoFlags.of(0));
            } else {
                //noinspection deprecation
                getPackageManager().getPackageInfo(packageName, 0);
            }
            return true;
        } catch (android.content.pm.PackageManager.NameNotFoundException missing) {
            return false;
        }
    }
}
