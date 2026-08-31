package com.longkaca.dpc;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import java.util.ArrayList;

/** Android 12+ admin-integrated provisioning entry point. */
public class GetProvisioningModeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PersistableBundle extras = getProvisioningExtras(getIntent());
        if (extras != null) {
            ConfigStore.saveProvisioningExtras(this, extras);
        }

        int requested = DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE;
        ArrayList<Integer> allowed = getIntent().getIntegerArrayListExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES);

        // This DPC intentionally supports only fully-managed / device-owner mode.
        if (allowed != null && !allowed.contains(requested)) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        Intent result = new Intent();
        result.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_MODE, requested);
        result.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_EDUCATION_SCREENS, true);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @SuppressWarnings("deprecation")
    private static PersistableBundle getProvisioningExtras(Intent intent) {
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            return intent.getParcelableExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE,
                    PersistableBundle.class);
        }
        return intent.getParcelableExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
    }
}
