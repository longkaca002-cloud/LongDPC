package com.longkaca.dpc;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.widget.*;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout root;
    private final String[] appNames = {"TikTok Nhật", "TikTok Lite Nhật", "LINE", "Auto Scroll"};
    private final String[] packages = {
            "com.ss.android.ugc.trill",
            "com.ss.android.ugc.tiktok.lite",
            "jp.naver.line.android",
            "com.tafayor.autoscrolling"
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16); root.setPadding(p,p,p,p);
        ScrollView sv = new ScrollView(this); sv.addView(root); setContentView(sv);
        DevicePolicyManager dpm = (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);
        if (dpm.isDeviceOwnerApp(getPackageName())) {
            showManagedMode();
            maybeAutoInstall();
        } else {
            showMotherMode();
        }
    }

    private TextView title(String s) { TextView v=new TextView(this); v.setText(s); v.setTextSize(22); v.setPadding(0,0,0,dp(12)); return v; }
    private EditText field(String hint, String value) { EditText e=new EditText(this); e.setHint(hint); e.setText(value); e.setSingleLine(true); root.addView(e); return e; }
    private Button button(String text) { Button b=new Button(this); b.setText(text); root.addView(b); return b; }

    private void showMotherMode() {
        root.addView(title("MÁY MẸ — tạo QR provisioning"));
        TextView note = new TextView(this);
        note.setText("APK DPC phải ở HTTPS URL tải trực tiếp. v1.6 dùng SHA-256 của CHỮ KÝ APK (giống TestDPC) để provisioning ổn định hơn.");
        root.addView(note);

        EditText apkUrl = field("HTTPS URL của LongDPC.apk", "");
        String ownChecksum = "";
        try { ownChecksum = ChecksumUtil.installedSigningCertSha256Base64Url(this); }
        catch (Exception ignored) {}
        EditText checksum = field("SHA-256 chữ ký APK (URL-safe Base64)", ownChecksum);
        TextView checksumNote = new TextView(this);
        checksumNote.setText("Checksum trên là SHA-256 của chứng thư ký APK. APK ở URL phải được ký bằng cùng chứng thư với LongDPC đang cài trên máy mẹ.");
        root.addView(checksumNote);
        EditText ssid = field("Wi-Fi SSID", "Longkaca");
        EditText pass = field("Wi-Fi password", "15082020");
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        List<EditText> appUrls = new ArrayList<>();
        for (int i=0;i<4;i++) appUrls.add(field("URL APK HTTPS — " + appNames[i] + " (" + packages[i] + ")", ""));
        ImageView qr = new ImageView(this); root.addView(qr, new LinearLayout.LayoutParams(-1, dp(420)));
        Button make = button("TẠO QR PROVISIONING");
        make.setOnClickListener(v -> {
            try {
                String dpc = apkUrl.getText().toString().trim();
                String sum = checksum.getText().toString().trim();
                if (!dpc.startsWith("https://") || sum.isEmpty()) {
                    Toast.makeText(this, "Cần URL HTTPS của DPC và checksum", Toast.LENGTH_LONG).show();
                    return;
                }

                JSONObject j = new JSONObject();
                j.put("android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME",
                        "com.longkaca.dpc/com.longkaca.dpc.LongDeviceAdminReceiver");
                j.put("android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION", dpc);
                j.put("android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM", sum);
                String wifiSsid = ssid.getText().toString().trim();
                String wifiPass = pass.getText().toString();
                if (!wifiSsid.isEmpty()) {
                    j.put("android.app.extra.PROVISIONING_WIFI_SSID", wifiSsid);
                    j.put("android.app.extra.PROVISIONING_WIFI_PASSWORD", wifiPass);
                    j.put("android.app.extra.PROVISIONING_WIFI_SECURITY_TYPE", "WPA");
                }

                JSONObject x = new JSONObject();
                x.put("wifi_ssid", wifiSsid);
                x.put("wifi_password", wifiPass);
                for (int i=0;i<4;i++) {
                    String u = appUrls.get(i).getText().toString().trim();
                    if (!u.isEmpty() && !u.startsWith("https://")) {
                        throw new IllegalArgumentException(appNames[i] + " phải là URL HTTPS");
                    }
                    x.put("apk_"+i+"_url", u);
                }
                j.put("android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE", x);
                Bitmap bmp = QrUtil.make(j.toString(), 1000); qr.setImageBitmap(bmp);
            } catch(Exception ex) {
                Toast.makeText(this, ex.getMessage() == null ? ex.toString() : ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showManagedMode() {
        root.addView(title("MÁY CON — Device Owner đang hoạt động"));
        EditText ssid = field("Wi-Fi mới", ConfigStore.get(this,"wifi_ssid","Longkaca"));
        EditText pass = field("Mật khẩu Wi-Fi mới", ConfigStore.get(this,"wifi_password","15082020"));
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        Button wifi = button("ĐỔI / THÊM WI-FI");
        wifi.setOnClickListener(v -> {
            String s=ssid.getText().toString(), p=pass.getText().toString();
            new Thread(() -> {
                int id;
                try { id = WifiAdmin.replaceWpaNetwork(this,s,p); }
                catch (Exception e) { id = -1; }
                if (id >= 0) {
                    ConfigStore.put(this,"wifi_ssid",s);
                    ConfigStore.put(this,"wifi_password",p);
                }
                final int result=id;
                runOnUiThread(() -> Toast.makeText(this,
                        result>=0 ? "Đã thêm và chuyển Wi-Fi" : "Không thêm được Wi-Fi trên firmware này",
                        Toast.LENGTH_LONG).show());
            }).start();
        });

        root.addView(title("Cài APK"));
        List<EditText> urls = new ArrayList<>();
        for(int i=0;i<4;i++) urls.add(field("URL APK HTTPS — "+appNames[i], ConfigStore.get(this,"apk_"+i+"_url","")));
        Button install = button("CÀI CÁC APP TỪ URL");
        install.setOnClickListener(v -> installFromFields(urls));
    }

    private void maybeAutoInstall() {
        if (ConfigStore.isAutoInstallStarted(this)) return;
        boolean any = false;
        for (int i=0;i<4;i++) {
            if (!ConfigStore.get(this,"apk_"+i+"_url","").trim().isEmpty()) { any = true; break; }
        }
        if (!any) return;

        ConfigStore.markAutoInstallStarted(this);
        new Thread(() -> installSavedUrls(false)).start();
    }

    private void installFromFields(List<EditText> urls) {
        for(int i=0;i<4;i++) {
            ConfigStore.put(this,"apk_"+i+"_url",urls.get(i).getText().toString().trim());
        }
        new Thread(() -> installSavedUrls(true)).start();
    }

    private void installSavedUrls(boolean reportEmpty) {
        boolean didAny = false;
        for(int i=0;i<4;i++) {
            final int idx=i;
            String u=ConfigStore.get(this,"apk_"+i+"_url","").trim();
            if(u.isEmpty()) continue;
            didAny = true;
            try {
                ApkInstaller.downloadAndInstall(this,u,appNames[i],packages[i]);
            } catch(Exception e) {
                final String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                runOnUiThread(() -> Toast.makeText(this,"Lỗi "+appNames[idx]+": "+msg,Toast.LENGTH_LONG).show());
            }
        }
        if (!didAny && reportEmpty) {
            runOnUiThread(() -> Toast.makeText(this,"Chưa có URL APK nào",Toast.LENGTH_LONG).show());
        }
    }

    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
}
