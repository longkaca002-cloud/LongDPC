# LongDPC v1.2 — kiểm tra ngày 2026-08-31

## PASS
- Project structure / package consistency: `com.longkaca.dpc`.
- AndroidManifest.xml và resource XML parse hợp lệ.
- QR provisioning JSON template parse hợp lệ.
- Có DeviceAdminReceiver với `BIND_DEVICE_ADMIN`.
- Có `GET_PROVISIONING_MODE` và `ADMIN_POLICY_COMPLIANCE` cho Android 12+.
- Có `PROVISIONING_SUCCESSFUL` activity theo mô hình TestDPC.
- Wi‑Fi mặc định: `Longkaca` / `15082020`.
- Android 12+ dùng `WifiManager.addNetworkPrivileged()`; Android 8–11 dùng `addNetwork()`.
- PackageInstaller khóa package mong đợi của 4 APK.
- Máy mẹ tự tính SHA‑256 URL-safe Base64 của APK LongDPC đang cài để điền checksum QR.
- Có helper `tools/apk_checksum.py` để tính checksum từ một file APK cụ thể.

## Không thể chạy trong môi trường hiện tại
- Không có Android SDK / Gradle được cài sẵn.
- Runtime không truy cập được Internet để tải Gradle/Android SDK (DNS bị chặn), nên chưa chạy được `assembleDebug` trong container này.
- Không có Android emulator hoặc thiết bị AQUOS/arrows kết nối để factory-reset và scan QR.

## Điểm phải test trên máy thật
1. Factory reset AQUOS/arrows.
2. Chạm màn hình Welcome 6 lần và quét QR.
3. Xác nhận máy tải LongDPC từ HTTPS URL và trở thành Device Owner.
4. Xác nhận Setup Wizard của docomo/au/SoftBank không chặn finalization.
5. Xác nhận đổi Wi‑Fi sau provisioning.
6. Xác nhận cài APK đơn/universal cho TikTok, TikTok Lite, LINE, Auto Scroll.

## Hạn chế cài app
- Downloader hiện hỗ trợ một APK đơn/universal cho mỗi ứng dụng.
- Không hỗ trợ XAPK/APKM hoặc bộ split APK nhiều file.
- URL phải là HTTPS tải trực tiếp và là nguồn APK hợp lệ mà người triển khai có quyền sử dụng/phân phối.
