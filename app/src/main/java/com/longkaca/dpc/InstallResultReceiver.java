package com.longkaca.dpc;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class InstallResultReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(
                PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
        String label = intent.getStringExtra("label");
        String installedPackage = intent.getStringExtra("expected_package");

        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm;
            if (Build.VERSION.SDK_INT >= 33) {
                confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class);
            } else {
                confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
            }
            if (confirm != null) {
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(confirm);
            }
            return;
        }

        if (status == PackageInstaller.STATUS_SUCCESS
                && "com.longkaca.ocr".equals(installedPackage)) {
            try {
                DevicePolicyManager dpm = (DevicePolicyManager)
                        context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName admin = new ComponentName(context, LongDeviceAdminReceiver.class);
                if (dpm != null && dpm.isDeviceOwnerApp(context.getPackageName())) {
                    dpm.setPermissionGrantState(admin, installedPackage, Manifest.permission.CAMERA,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                }
            } catch (Exception e) {
                Log.w("LongDPC", "Không tự cấp được quyền Camera cho Long OCR", e);
            }
        }

        String text = (status == PackageInstaller.STATUS_SUCCESS ? "Đã cài " : "Lỗi cài ")
                + label + (msg == null ? "" : ": " + msg);
        Log.i("LongDPC", text);
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }
}
