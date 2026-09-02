package com.longkaca.dpc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ApkInstaller {
    private ApkInstaller() {}

    public static void downloadAndInstall(
            Context context, String urlString, String label, String expectedPackage) throws Exception {
        URL url = new URL(urlString);
        if (!"https".equalsIgnoreCase(url.getProtocol())) {
            throw new IllegalArgumentException("Chỉ chấp nhận URL HTTPS");
        }

        String path = url.getPath() == null ? "" : url.getPath().toLowerCase(Locale.ROOT);
        if (path.endsWith(".apks")) {
            downloadAndInstallSplitBundle(context, url, label, expectedPackage);
        } else {
            downloadAndInstallSingleApk(context, url, label, expectedPackage);
        }
    }

    private static void downloadAndInstallSingleApk(
            Context context, URL url, String label, String expectedPackage) throws Exception {
        HttpURLConnection conn = open(url);
        PackageInstaller.Session session = null;
        boolean committed = false;
        try {
            PackageInstaller installer = context.getPackageManager().getPackageInstaller();
            int sessionId = installer.createSession(newParams(expectedPackage));
            session = installer.openSession(sessionId);

            long length = conn.getContentLengthLong();
            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 OutputStream out = session.openWrite("base.apk", 0, length > 0 ? length : -1)) {
                copy(in, out);
                session.fsync(out);
            }

            commit(context, session, sessionId, label, expectedPackage);
            committed = true;
        } finally {
            if (session != null) {
                if (!committed) {
                    try { session.abandon(); } catch (Exception ignored) {}
                }
                session.close();
            }
            conn.disconnect();
        }
    }

    private static void downloadAndInstallSplitBundle(
            Context context, URL url, String label, String expectedPackage) throws Exception {
        File bundle = File.createTempFile("longdpc_", ".apks", context.getCacheDir());
        try {
            HttpURLConnection conn = open(url);
            try {
                try (InputStream in = new BufferedInputStream(conn.getInputStream());
                     OutputStream out = new BufferedOutputStream(new FileOutputStream(bundle))) {
                    copy(in, out);
                }
            } finally {
                conn.disconnect();
            }

            installSplitBundle(context, bundle, label, expectedPackage);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            bundle.delete();
        }
    }

    private static void installSplitBundle(
            Context context, File bundle, String label, String expectedPackage) throws Exception {
        PackageInstaller installer = context.getPackageManager().getPackageInstaller();
        PackageInstaller.Session session = null;
        boolean committed = false;

        try (ZipFile zip = new ZipFile(bundle)) {
            List<ZipEntry> apkEntries = new ArrayList<>();
            Enumeration<? extends ZipEntry> all = zip.entries();
            while (all.hasMoreElements()) {
                ZipEntry entry = all.nextElement();
                if (entry.isDirectory()) continue;
                String name = entry.getName().toLowerCase(Locale.ROOT);
                if (name.endsWith(".apk")) apkEntries.add(entry);
            }

            if (apkEntries.isEmpty()) {
                throw new IllegalStateException("File .apks không chứa APK nào");
            }

            // base.apk trước cho dễ kiểm tra/log; PackageInstaller vẫn đọc manifest của từng split.
            Collections.sort(apkEntries, new Comparator<ZipEntry>() {
                @Override public int compare(ZipEntry a, ZipEntry b) {
                    boolean aBase = baseName(a.getName()).equalsIgnoreCase("base.apk");
                    boolean bBase = baseName(b.getName()).equalsIgnoreCase("base.apk");
                    if (aBase != bBase) return aBase ? -1 : 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                }
            });

            boolean hasBase = false;
            for (ZipEntry e : apkEntries) {
                if (baseName(e.getName()).equalsIgnoreCase("base.apk")) {
                    hasBase = true;
                    break;
                }
            }
            if (!hasBase) {
                throw new IllegalStateException("File .apks thiếu base.apk");
            }

            int sessionId = installer.createSession(newParams(expectedPackage));
            session = installer.openSession(sessionId);

            int index = 0;
            Set<String> usedNames = new HashSet<>();
            for (ZipEntry entry : apkEntries) {
                String original = baseName(entry.getName());
                String safeName = safeApkName(original);
                if (!usedNames.add(safeName)) {
                    safeName = String.format(Locale.ROOT, "%03d_%s", index, safeName);
                    usedNames.add(safeName);
                }
                index++;
                long size = entry.getSize();
                try (InputStream in = new BufferedInputStream(zip.getInputStream(entry));
                     OutputStream out = session.openWrite(safeName, 0, size >= 0 ? size : -1)) {
                    copy(in, out);
                    session.fsync(out);
                }
            }

            commit(context, session, sessionId, label, expectedPackage);
            committed = true;
        } finally {
            if (session != null) {
                if (!committed) {
                    try { session.abandon(); } catch (Exception ignored) {}
                }
                session.close();
            }
        }
    }

    private static PackageInstaller.SessionParams newParams(String expectedPackage) {
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.setAppPackageName(expectedPackage);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);
        }
        return params;
    }

    private static void commit(Context context, PackageInstaller.Session session, int sessionId,
                               String label, String expectedPackage) throws Exception {
        Intent result = new Intent(context, InstallResultReceiver.class);
        result.putExtra("label", label);
        result.putExtra("expected_package", expectedPackage);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
        PendingIntent pi = PendingIntent.getBroadcast(context, sessionId, result, flags);
        session.commit(pi.getIntentSender());
    }

    private static HttpURLConnection open(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(180000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", "LongDPC/2.0");
        conn.connect();
        int http = conn.getResponseCode();
        if (http < 200 || http >= 300) {
            conn.disconnect();
            throw new IllegalStateException("HTTP " + http);
        }
        return conn;
    }

    private static void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buf = new byte[64 * 1024];
        int n;
        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
    }

    private static String baseName(String path) {
        int slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String safeApkName(String original) {
        String cleaned = original.replaceAll("[^A-Za-z0-9._-]", "_");
        if (!cleaned.toLowerCase(Locale.ROOT).endsWith(".apk")) cleaned += ".apk";
        return cleaned;
    }
}
