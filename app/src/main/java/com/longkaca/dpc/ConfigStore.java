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
        for (int i = 0; i < 4; i++) {
            e.putString("apk_" + i + "_url", safe(b.getString("apk_" + i + "_url")));
        }
        e.apply();
    }

    public static void markProvisioned(Context c) {
        prefs(c).edit().putBoolean("provisioned", true).apply();
    }

    public static boolean isProvisioned(Context c) {
        return prefs(c).getBoolean("provisioned", false);
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

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
