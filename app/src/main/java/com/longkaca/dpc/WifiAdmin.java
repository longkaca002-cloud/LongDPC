package com.longkaca.dpc;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

@SuppressWarnings("deprecation")
public final class WifiAdmin {
    private WifiAdmin() {}

    /** Add and switch to a WPA/WPA2-PSK network as Device Owner. */
    public static int replaceWpaNetwork(Context context, String ssid, String password) {
        if (ssid == null || ssid.trim().isEmpty()) return -1;
        if (password == null || password.length() < 8) return -1;

        WifiManager wm = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wm == null) return -1;

        WifiConfiguration cfg = new WifiConfiguration();
        cfg.SSID = quote(ssid);
        cfg.preSharedKey = quote(password);
        cfg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        int netId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? Api31Impl.addNetworkPrivileged(wm, cfg)
                : wm.addNetwork(cfg);

        if (netId >= 0) {
            wm.enableNetwork(netId, true);
            wm.reconnect();
        }
        return netId;
    }

    private static String quote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    /** Keeps API-31-only types out of the main code path on Android 8–11. */
    private static final class Api31Impl {
        static int addNetworkPrivileged(WifiManager wm, WifiConfiguration cfg) {
            WifiManager.AddNetworkResult r = wm.addNetworkPrivileged(cfg);
            return r == null ? -1 : r.networkId;
        }
    }
}
