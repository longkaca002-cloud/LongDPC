# LongDPC v1.5 AUTOSCROLL — kiểm tra source

## PASS
- Project/package: `com.longkaca.dpc`.
- AndroidManifest.xml và resource XML parse hợp lệ.
- QR provisioning JSON template parse hợp lệ.
- Có DeviceAdminReceiver với `BIND_DEVICE_ADMIN`.
- Có `GET_PROVISIONING_MODE`, `ADMIN_POLICY_COMPLIANCE`, `PROVISIONING_SUCCESSFUL`.
- Wi‑Fi mặc định: `Longkaca` / `15082020`.
- Package cài APK đã đổi đúng cho bản Nhật:
  - TikTok: `com.ss.android.ugc.trill`
  - TikTok Lite: `com.ss.android.ugc.tiktok.lite`
  - LINE: `jp.naver.line.android`
  - Auto Scroll: `com.tafayor.autoscrolling`
- Android 12+ dùng `WifiManager.addNetworkPrivileged()`; Android 8–11 dùng `addNetwork()`.
- PackageInstaller khóa package mong đợi của từng APK.
- Máy mẹ tự tính SHA-256 URL-safe Base64 của APK LongDPC đang cài để điền checksum QR.

## Cần xác nhận bằng build/test thật
- Chạy GitHub Actions `:app:assembleDebug`.
- Cài `app-debug.apk` lên máy mẹ và xác nhận giao diện hiển thị 4 package mới.
- Factory reset một AQUOS/arrows và test QR provisioning.
- Test URL APK từng app một trước khi bật cả 4.

## Hạn chế
- Chỉ hỗ trợ một APK đơn/universal cho mỗi app.
- Không hỗ trợ XAPK/APKM/split APK nhiều file.
- URL app phải là HTTPS tải trực tiếp.
