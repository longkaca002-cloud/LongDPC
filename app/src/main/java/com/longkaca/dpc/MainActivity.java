package com.longkaca.dpc;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.*;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout root;

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

    private void persist(EditText e, String key) {
        e.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ConfigStore.put(MainActivity.this, key, s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void showMotherMode() {
        root.addView(title("MÁY MẸ — LongDPC v2.2 — tạo QR provisioning"));
        TextView note = new TextView(this);
        note.setText("v2.2: TikTok Lite dùng APKS; Long OCR chọn email theo dòng; APN đã sửa.");
        root.addView(note);

        EditText apkUrl = field("HTTPS URL của LongDPC.apk",
                ConfigStore.get(this,"mother_dpc_url", AppCatalog.DEFAULT_DPC_URL));
        persist(apkUrl, "mother_dpc_url");

        String ownChecksum = "";
        try { ownChecksum = ChecksumUtil.installedSigningCertSha256Base64Url(this); }
        catch (Exception ignored) {}
        EditText checksum = field("SHA-256 chữ ký APK (URL-safe Base64)", ownChecksum);
        TextView checksumNote = new TextView(this);
        checksumNote.setText("APK tại URL phải là đúng build được ký cùng chứng thư với LongDPC đang cài trên máy mẹ.");
        root.addView(checksumNote);

        EditText ssid = field("Wi-Fi SSID",
                ConfigStore.get(this,"mother_wifi_ssid", AppCatalog.DEFAULT_WIFI_SSID));
        EditText pass = field("Wi-Fi password",
                ConfigStore.get(this,"mother_wifi_password", AppCatalog.DEFAULT_WIFI_PASSWORD));
        pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        persist(ssid, "mother_wifi_ssid");
        persist(pass, "mother_wifi_password");

        List<EditText> appUrls = new ArrayList<>();
        for (int i=0;i<AppCatalog.NAMES.length;i++) {
            EditText e = field("URL HTTPS — " + AppCatalog.NAMES[i] + " (" + AppCatalog.PACKAGES[i] + ")",
                    ConfigStore.get(this,"mother_apk_"+i+"_url", AppCatalog.DEFAULT_URLS[i]));
            persist(e, "mother_apk_"+i+"_url");
            appUrls.add(e);
        }

        TextView splitNote = new TextView(this);
        splitNote.setText("TikTok / TikTok Lite / LINE nên dùng .apks đầy đủ split. Auto Scroll dùng .apk đơn.");
        root.addView(splitNote);

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
                j.put("android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED", true);
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
                x.put("apn_profile", ApnAdmin.PROFILE_AUTO);
                for (int i=0;i<AppCatalog.NAMES.length;i++) {
                    String u = appUrls.get(i).getText().toString().trim();
                    if (!u.isEmpty() && !u.startsWith("https://")) {
                        throw new IllegalArgumentException(AppCatalog.NAMES[i] + " phải là URL HTTPS");
                    }
                    x.put("apk_"+i+"_url", u);
                }
                j.put("android.app.extra.PROVISIONING_ADMIN_EXTRAS_BUNDLE", x);
                Bitmap bmp = QrUtil.make(j.toString(), 1000); qr.setImageBitmap(bmp);
                Toast.makeText(this,"Đã tạo QR — các ô đã được lưu",Toast.LENGTH_SHORT).show();
            } catch(Exception ex) {
                Toast.makeText(this, ex.getMessage() == null ? ex.toString() : ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showManagedMode() {
        root.addView(title("MÁY CON — LongDPC v2.2 — Device Owner"));
        EditText ssid = field("Wi-Fi mới", ConfigStore.get(this,"wifi_ssid",AppCatalog.DEFAULT_WIFI_SSID));
        EditText pass = field("Mật khẩu Wi-Fi mới", ConfigStore.get(this,"wifi_password",AppCatalog.DEFAULT_WIFI_PASSWORD));
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

        root.addView(title("APN SIM"));
        TextView apnInfo = new TextView(this);
        apnInfo.setText("Cả hai SIM đều chạy hạ tầng SoftBank. Tự động chỉ áp dụng khi đọc được chính xác jconnect hoặc LINE; nếu máy chỉ hiện SoftBank, hãy bấm đúng loại SIM.");
        root.addView(apnInfo);
        Button apnAuto = button("TỰ NHẬN DIỆN SIM VÀ ÁP APN");
        apnAuto.setOnClickListener(v -> applyApn(ApnAdmin.PROFILE_AUTO));
        Button apnJconnect = button("ÁP APN — Tên: jconnect");
        apnJconnect.setOnClickListener(v -> applyApn(ApnAdmin.PROFILE_JCONNECT));
        Button apnLine = button("ÁP APN — Tên: LINEモバイル");
        apnLine.setOnClickListener(v -> applyApn(ApnAdmin.PROFILE_LINE_SOFTBANK));

        root.addView(title("Cài APK / APKS"));
        List<EditText> urls = new ArrayList<>();
        for(int i=0;i<AppCatalog.NAMES.length;i++) {
            final int idx = i;
            EditText e = field("URL HTTPS — "+AppCatalog.NAMES[i], ConfigStore.get(this,"apk_"+i+"_url",""));
            urls.add(e);
            Button one = button("CÀI RIÊNG — " + AppCatalog.NAMES[i]);
            one.setOnClickListener(v -> installSingleFromField(e, idx));
        }
        Button install = button("CÀI TẤT CẢ URL KHÔNG TRỐNG");
        install.setOnClickListener(v -> installFromFields(urls));

        Button clear = button("XÓA URL APP ĐÃ LƯU");
        clear.setOnClickListener(v -> {
            for (int i=0;i<AppCatalog.NAMES.length;i++) {
                ConfigStore.put(this,"apk_"+i+"_url","");
                urls.get(i).setText("");
            }
            ConfigStore.clearAutoInstallState(this);
            Toast.makeText(this,"Đã xóa URL app đã lưu",Toast.LENGTH_LONG).show();
        });
    }

    private void applyApn(String profile) {
        try {
            String result = ApnAdmin.apply(this, profile);
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Không áp được APN: " + (e.getMessage() == null ? e : e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void maybeAutoInstall() {
        if (!ConfigStore.isAutoInstallRequested(this)) return;
        if (ConfigStore.isAutoInstallStarted(this)) return;
        AutoInstallScheduler.schedule(this);
    }

    private void installSingleFromField(EditText urlField, int i) {
        String u = urlField.getText().toString().trim();
        if (u.isEmpty()) {
            Toast.makeText(this,"Chưa có URL cho " + AppCatalog.NAMES[i],Toast.LENGTH_LONG).show();
            return;
        }
        if (!u.startsWith("https://")) {
            Toast.makeText(this,"URL phải bắt đầu bằng https://",Toast.LENGTH_LONG).show();
            return;
        }
        ConfigStore.put(this,"apk_"+i+"_url",u);
        new Thread(() -> {
            try {
                ApkInstaller.downloadAndInstall(this,u,AppCatalog.NAMES[i],AppCatalog.PACKAGES[i]);
            } catch(Exception e) {
                final String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                runOnUiThread(() -> Toast.makeText(this,"Lỗi "+AppCatalog.NAMES[i]+": "+msg,Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void installFromFields(List<EditText> urls) {
        for(int i=0;i<AppCatalog.NAMES.length;i++) {
            ConfigStore.put(this,"apk_"+i+"_url",urls.get(i).getText().toString().trim());
        }
        ConfigStore.clearAutoInstallState(this);
        // Manual install: run immediately rather than waiting for a JobScheduler window.
        new Thread(() -> installSavedUrls(true)).start();
    }

    private void installSavedUrls(boolean reportEmpty) {
        boolean didAny = false;
        for(int i=0;i<AppCatalog.NAMES.length;i++) {
            final int idx=i;
            String u=ConfigStore.get(this,"apk_"+i+"_url","").trim();
            if(u.isEmpty()) continue;
            didAny = true;
            try {
                ApkInstaller.downloadAndInstall(this,u,AppCatalog.NAMES[i],AppCatalog.PACKAGES[i]);
            } catch(Exception e) {
                final String msg = e.getMessage() == null ? e.toString() : e.getMessage();
                runOnUiThread(() -> Toast.makeText(this,"Lỗi "+AppCatalog.NAMES[idx]+": "+msg,Toast.LENGTH_LONG).show());
            }
        }
        if (!didAny && reportEmpty) {
            runOnUiThread(() -> Toast.makeText(this,"Chưa có URL APK nào",Toast.LENGTH_LONG).show());
        }
    }

    private int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density+0.5f); }
}
