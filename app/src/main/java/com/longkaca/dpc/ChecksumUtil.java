package com.longkaca.dpc;

import android.content.Context;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/** Utilities for Android Enterprise provisioning checksums. */
public final class ChecksumUtil {
    private ChecksumUtil() {}

    /**
     * SHA-256 of the exact APK file currently installed, encoded as URL-safe Base64.
     * The provisioning URL must serve this exact same APK file for the checksum to match.
     */
    public static String installedApkSha256Base64Url(Context context) throws Exception {
        String source = context.getApplicationInfo().sourceDir;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new BufferedInputStream(new FileInputStream(source))) {
            byte[] buffer = new byte[64 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                digest.update(buffer, 0, n);
            }
        }
        return Base64.encodeToString(digest.digest(), Base64.URL_SAFE | Base64.NO_WRAP);
    }
}
