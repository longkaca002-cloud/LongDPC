package com.longkaca.dpc;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/** Legacy post-provisioning hook. Modern Android finalizes via ADMIN_POLICY_COMPLIANCE. */
public class ProvisioningSuccessActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigStore.markProvisioned(this);

        // Android 8-9 don't have the admin-integrated compliance flow.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Intent launch = new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(launch);
        }
        finish();
    }
}
