package com.longkaca.dpc;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

/**
 * Android 12+ provisioning finalization hook.
 * There is no custom compliance UI in this local DPC, so persist config and return OK.
 */
public class PolicyComplianceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PersistableBundle extras = getProvisioningExtras(getIntent());
        if (extras != null) {
            ConfigStore.saveProvisioningExtras(this, extras);
        }

        setResult(Activity.RESULT_OK);
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
