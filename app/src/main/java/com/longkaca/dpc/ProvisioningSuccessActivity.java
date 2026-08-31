package com.longkaca.dpc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Runs when managed-device provisioning has completed successfully (Android 8+).
 * The app minSdk is 26, so this is the normal post-provisioning entry point.
 */
public class ProvisioningSuccessActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigStore.markProvisioned(this);
        Intent launch = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(launch);
        finish();
    }
}
