package com.longkaca.dpc;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;

import java.util.List;
import java.util.Locale;

/** Device-owner override APN manager (Android 9/API 28+). */
public final class ApnAdmin {
    public static final String PROFILE_AUTO = "auto";
    public static final String PROFILE_JCONNECT = "jconnect";
    public static final String PROFILE_LINE_SOFTBANK = "line_softbank";

    private ApnAdmin() {}

    public static String apply(Context context, String requested) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            throw new IllegalStateException("Máy cần Android 9 trở lên");
        }
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (dpm == null || !dpm.isDeviceOwnerApp(context.getPackageName())) {
            throw new SecurityException("LongDPC chưa là Device Owner");
        }

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm == null ? "" : safe(tm.getSimOperator());
        String carrier = tm == null ? "" : (safe(tm.getSimOperatorName()) + " " + safe(tm.getNetworkOperatorName())).toLowerCase(Locale.ROOT);
        String profile = requested;
        if (PROFILE_AUTO.equals(profile)) {
            // jconnect và LINE Mobile đều có thể hiện hạ tầng SoftBank.
            // Không được dùng riêng chữ "softbank" để phân biệt hai cấu hình APN.
            if (carrier.contains("line") || carrier.contains("lineモバイル")) profile = PROFILE_LINE_SOFTBANK;
            else if (carrier.contains("jconnect")) profile = PROFILE_JCONNECT;
            else throw new IllegalStateException("SIM chạy mạng SoftBank nhưng không phân biệt được jconnect/LINE Mobile; hãy bấm đúng loại SIM");
        }

        ApnSetting.Builder b = new ApnSetting.Builder()
                .setApnTypeBitmask(ApnSetting.TYPE_DEFAULT | ApnSetting.TYPE_SUPL)
                .setProtocol(ApnSetting.PROTOCOL_IPV4V6)
                .setRoamingProtocol(ApnSetting.PROTOCOL_IPV4V6);
        if (!operator.isEmpty()) b.setOperatorNumeric(operator);

        String shown;
        if (PROFILE_JCONNECT.equals(profile)) {
            shown = "jconnect";
            b.setEntryName("jconnect").setApnName("plus.4g")
                    .setUser("plus").setPassword("4g")
                    .setAuthType(ApnSetting.AUTH_TYPE_CHAP);
        } else if (PROFILE_LINE_SOFTBANK.equals(profile)) {
            shown = "LINE Mobile (SoftBank)";
            b.setEntryName("LINEモバイル").setApnName("line.me")
                    .setUser("line@line").setPassword("line")
                    .setAuthType(ApnSetting.AUTH_TYPE_PAP_OR_CHAP);
        } else {
            throw new IllegalArgumentException("Hồ sơ APN không hợp lệ");
        }

        ComponentName admin = new ComponentName(context, LongDeviceAdminReceiver.class);
        List<ApnSetting> old = dpm.getOverrideApns(admin);
        if (old != null) for (ApnSetting apn : old) dpm.removeOverrideApn(admin, apn.getId());
        int id = dpm.addOverrideApn(admin, b.build());
        if (id < 0) throw new IllegalStateException("Android từ chối thêm APN");
        dpm.setOverrideApnsEnabled(admin, true);
        ConfigStore.put(context, "last_apn_profile", profile);
        return "Đã bật APN " + shown;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
