package com.longkaca.dpc;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

/** Android 10+ provisioning finalization hook. */
public class PolicyComplianceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PersistableBundle extras = getProvisioningExtras(getIntent());
        if (extras != null) ConfigStore.saveProvisioningExtras(this, extras);
        ConfigStore.markProvisioned(this);

        // Android's admin-integrated provisioning contract requires RESULT_OK + Intent.
        // Do not launch another Activity or background service before returning to Setup Wizard.
        Intent result = new Intent();
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
