package com.longkaca.dpc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class ApkInstaller {
    private ApkInstaller() {}

    public static void downloadAndInstall(
            Context context, String urlString, String label, String expectedPackage) throws Exception {
        URL url = new URL(urlString);
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new IllegalArgumentException("Chỉ chấp nhận URL HTTPS");
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(120000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", "LongDPC/1.2");
        conn.connect();

        int http = conn.getResponseCode();
        if (http < 200 || http >= 300) {
            conn.disconnect();
            throw new IllegalStateException("HTTP " + http);
        }

        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(expectedPackage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        }

        int sessionId = installer.createSession(params);
        PackageInstaller.Session session = installer.openSession(sessionId);
        boolean committed = false;
        try {
            long length = conn.getContentLengthLong();
            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 OutputStream out = session.openWrite("base.apk", 0, length > 0 ? length : -1)) {
                byte[] buf = new byte[64 * 1024];
                int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                session.fsync(out);
            }

            Intent result = new Intent(context, InstallResultReceiver.class);
            result.putExtra("label", label);
            result.putExtra("expected_package", expectedPackage);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }
            PendingIntent pi = PendingIntent.getBroadcast(context, sessionId, result, flags);
            session.commit(pi.getIntentSender());
            committed = true;
        } finally {
            if (!committed) {
                try { session.abandon(); } catch (Exception ignored) {}
            }
            session.close();
            conn.disconnect();
        }
    }
}
