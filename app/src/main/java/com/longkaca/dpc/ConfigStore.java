package com.longkaca.dpc;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PersistableBundle;

public final class ConfigStore {
    private static final String PREF = "long_dpc_config";
    private ConfigStore() {}

    public static void saveProvisioningExtras(Context c, PersistableBundle b) {
        if (b == null) return;
        SharedPreferences.Editor e = prefs(c).edit();
        e.putString("wifi_ssid", safe(b.getString("wifi_ssid")));
        e.putString("wifi_password", safe(b.getString("wifi_password")));
        e.putString("apn_profile", safe(b.getString("apn_profile")));
        boolean anyAppUrl = false;
        for (int i = 0; i < AppCatalog.NAMES.length; i++) {
            String url = safe(b.getString("apk_" + i + "_url")).trim();
            e.putString("apk_" + i + "_url", url);
            if (!url.isEmpty()) anyAppUrl = true;
        }
        // Chỉ auto-install cho bộ URL nhận từ provisioning QR hiện tại.
        e.putBoolean("auto_install_requested", anyAppUrl);
        e.putBoolean("auto_install_started", false);
        e.apply();
    }

    public static void markProvisioned(Context c) {
        prefs(c).edit().putBoolean("provisioned", true).apply();
    }

    public static boolean isProvisioned(Context c) {
        return prefs(c).getBoolean("provisioned", false);
    }


    public static boolean isAutoInstallRequested(Context c) {
        return prefs(c).getBoolean("auto_install_requested", false);
    }

    public static void clearAutoInstallState(Context c) {
        prefs(c).edit()
                .putBoolean("auto_install_requested", false)
                .putBoolean("auto_install_started", false)
                .apply();
    }

    public static boolean isAutoInstallStarted(Context c) {
        return prefs(c).getBoolean("auto_install_started", false);
    }

    public static void markAutoInstallStarted(Context c) {
        prefs(c).edit().putBoolean("auto_install_started", true).apply();
    }

    public static String get(Context c, String key, String def) {
        return prefs(c).getString(key, def);
    }

    public static void put(Context c, String key, String value) {
        prefs(c).edit().putString(key, value).apply();
    }

    /** Apply the requested v2.4 mother defaults once, replacing values cached by older builds. */
    public static void initializeMotherV24Defaults(Context c) {
        if ("24".equals(get(c, "mother_defaults_version", ""))) return;
        SharedPreferences.Editor e = prefs(c).edit();
        e.putString("mother_dpc_url", AppCatalog.DEFAULT_DPC_URL);
        e.putString("mother_wifi_ssid", AppCatalog.DEFAULT_WIFI_SSID);
        e.putString("mother_wifi_password", AppCatalog.DEFAULT_WIFI_PASSWORD);
        e.putString("mother_apn_profile", ApnAdmin.PROFILE_JCONNECT);
        for (int i = 0; i < AppCatalog.DEFAULT_URLS.length; i++) {
            e.putString("mother_apk_" + i + "_url", AppCatalog.DEFAULT_URLS[i]);
        }
        e.putString("mother_defaults_version", "24");
        e.apply();
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
