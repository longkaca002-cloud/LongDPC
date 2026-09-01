package com.longkaca.dpc;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import java.util.ArrayList;

/** Android 10+ admin-integrated provisioning entry point. */
public class GetProvisioningModeActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final int requested = DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE;
        ArrayList<Integer> allowed = getIntent().getIntegerArrayListExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES);

        // Never show a mode-picker: this DPC only supports fully-managed Device Owner mode.
        if (allowed != null && !allowed.contains(requested)) {
            setResult(Activity.RESULT_CANCELED, new Intent());
            finish();
            return;
        }

        PersistableBundle extras = getProvisioningExtras(getIntent());

        Intent result = new Intent();
        result.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_MODE, requested);
        result.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_EDUCATION_SCREENS, true);
        if (extras != null) {
            // Explicitly hand the bundle back so Android forwards it to ADMIN_POLICY_COMPLIANCE.
            result.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, extras);
        }

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
