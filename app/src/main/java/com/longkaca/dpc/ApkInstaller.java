package com.longkaca.dpc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
        File apk = File.createTempFile("longdpc_", ".apk", context.getCacheDir());
        try {
            downloadToFileResumable(url, apk);
            installSingleApk(context, apk, label, expectedPackage);
        } finally {
            //noinspection ResultOfMethodCallIgnored
            apk.delete();
        }
    }

    private static void installSingleApk(
            Context context, File apk, String label, String expectedPackage) throws Exception {
        PackageInstaller.Session session = null;
        boolean committed = false;
        try {
            PackageInstaller installer = context.getPackageManager().getPackageInstaller();
            int sessionId = installer.createSession(newParams(expectedPackage));
            session = installer.openSession(sessionId);

            long length = apk.length();
            try (InputStream in = new BufferedInputStream(new FileInputStream(apk));
                 OutputStream out = session.openWrite("base.apk", 0, length)) {
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
        }
    }

    private static void downloadAndInstallSplitBundle(
            Context context, URL url, String label, String expectedPackage) throws Exception {
        File bundle = File.createTempFile("longdpc_", ".apks", context.getCacheDir());
        try {
            downloadToFileResumable(url, bundle);
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

    /** Download large GitHub assets with HTTP Range resume after interrupted streams. */
    private static void downloadToFileResumable(URL url, File target) throws Exception {
        Exception last = null;
        for (int attempt = 1; attempt <= 8; attempt++) {
            HttpURLConnection conn = null;
            try {
                long offset = target.length();
                conn = open(url, offset);
                int http = conn.getResponseCode();
                if (http == 416) {
                    try (FileOutputStream ignored = new FileOutputStream(target, false)) { /* restart */ }
                    continue;
                }
                boolean append = offset > 0 && http == HttpURLConnection.HTTP_PARTIAL;
                if (!append && offset > 0) {
                    try (FileOutputStream ignored = new FileOutputStream(target, false)) { /* server ignored Range */ }
                    offset = 0;
                }
                long expectedTotal = expectedTotal(conn, offset);
                try (InputStream in = new BufferedInputStream(conn.getInputStream());
                     OutputStream out = new BufferedOutputStream(new FileOutputStream(target, append))) {
                    copy(in, out);
                }
                if (expectedTotal > 0 && target.length() < expectedTotal) {
                    throw new IOException("Tải chưa đủ: " + target.length() + "/" + expectedTotal);
                }
                if (target.length() <= 0) throw new IOException("File tải về trống");
                return;
            } catch (Exception e) {
                last = e;
                if (attempt < 8) {
                    try { Thread.sleep(1500L * attempt); }
                    catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw interrupted;
                    }
                }
            } finally {
                if (conn != null) conn.disconnect();
            }
        }
        throw new IOException("Tải bị ngắt sau 8 lần nối lại", last);
    }

    private static long expectedTotal(HttpURLConnection conn, long offset) {
        String range = conn.getHeaderField("Content-Range");
        if (range != null) {
            int slash = range.lastIndexOf('/');
            if (slash >= 0) {
                try { return Long.parseLong(range.substring(slash + 1).trim()); }
                catch (NumberFormatException ignored) {}
            }
        }
        long length = conn.getContentLengthLong();
        return length > 0 ? offset + length : -1;
    }

    private static HttpURLConnection open(URL url, long offset) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(20000);
        conn.setReadTimeout(180000);
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-Agent", "LongDPC/2.7");
        if (offset > 0) conn.setRequestProperty("Range", "bytes=" + offset + "-");
        conn.connect();
        int http = conn.getResponseCode();
        if ((http < 200 || http >= 300) && http != 416) {
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
