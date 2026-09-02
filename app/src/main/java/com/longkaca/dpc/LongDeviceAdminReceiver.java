package com.longkaca.dpc;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.util.Log;

public class LongDeviceAdminReceiver extends DeviceAdminReceiver {
    private static final String TAG = "LongDPC";

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        super.onProfileProvisioningComplete(context, intent);

        PersistableBundle extras = getExtras(intent);
        if (extras != null) ConfigStore.saveProvisioningExtras(context, extras);
        ConfigStore.markProvisioned(context);

        DevicePolicyManager dpm = (DevicePolicyManager)
                context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        Log.i(TAG, "Provisioning complete; Device Owner=" +
                (dpm != null && dpm.isDeviceOwnerApp(context.getPackageName())));
        AutoInstallScheduler.schedule(context);
        // Do not launch UI from this broadcast. Android 8+ sends ACTION_PROVISIONING_SUCCESSFUL
        // to the new owner; ProvisioningSuccessActivity handles the post-setup launch.
    }

    @SuppressWarnings("deprecation")
    private static PersistableBundle getExtras(Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            return intent.getParcelableExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE,
                    PersistableBundle.class);
        }
        return intent.getParcelableExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
    }
}
