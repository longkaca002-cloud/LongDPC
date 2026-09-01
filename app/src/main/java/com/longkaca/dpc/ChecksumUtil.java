package com.longkaca.dpc;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

/** Utilities for Android Enterprise provisioning checksums. */
public final class ChecksumUtil {
    private ChecksumUtil() {}

    /** SHA-256 of the exact installed APK, URL-safe Base64. */
    public static String installedApkSha256Base64Url(Context context) throws Exception {
        String source = context.getApplicationInfo().sourceDir;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream in = new BufferedInputStream(new FileInputStream(source))) {
            byte[] buffer = new byte[64 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) digest.update(buffer, 0, n);
        }
        return Base64.encodeToString(digest.digest(), Base64.URL_SAFE | Base64.NO_WRAP);
    }

    /**
     * SHA-256 of the APK signing certificate, URL-safe Base64.
     * This matches EXTRA_PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM and mirrors TestDPC.
     */
    @SuppressWarnings("deprecation")
    public static String installedSigningCertSha256Base64Url(Context context) throws Exception {
        PackageManager pm = context.getPackageManager();
        PackageInfo info;
        Signature[] signatures;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            if (info.signingInfo == null) throw new IllegalStateException("Không đọc được signingInfo");
            signatures = info.signingInfo.getApkContentsSigners();
        } else {
            info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            signatures = info.signatures;
        }

        if (signatures == null || signatures.length == 0) {
            throw new IllegalStateException("Không đọc được chữ ký APK");
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return Base64.encodeToString(
                digest.digest(signatures[0].toByteArray()),
                Base64.URL_SAFE | Base64.NO_WRAP);
    }
}
