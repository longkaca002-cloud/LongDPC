# LongDPC v1.4 JAPAN — AQUOS / arrows

## Mục tiêu
Máy mẹ tạo QR Android Enterprise; máy con factory reset -> chạm 6 lần -> quét QR -> Wi‑Fi -> tải DPC -> Fully Managed / Device Owner -> cài APK từ URL HTTPS.

Wi‑Fi mặc định: `Longkaca` / `15082020`.

Apps khóa package cho bản Nhật:
- TikTok Nhật: `com.ss.android.ugc.trill`
- TikTok Lite Nhật: `com.ss.android.ugc.tiktok.lite`
- LINE: `jp.naver.line.android`
- Auto Clicker: `com.truedevelopersstudio.automatictap.autoclicker`

## Cải tiến v1.4
- Đổi TikTok/TikTok Lite sang package đang dùng ở Nhật.
- Thay Auto Scroll cũ bằng Auto Clicker package `com.truedevelopersstudio.automatictap.autoclicker`.
- Nâng `versionCode` lên 5, `versionName` thành `1.4-japan`.
- Giữ nguyên provisioning Android 12+ (`GET_PROVISIONING_MODE`, `ADMIN_POLICY_COMPLIANCE`, `PROVISIONING_SUCCESSFUL`).
- Máy mẹ tự tính SHA-256 URL-safe Base64 của APK LongDPC đang cài và điền sẵn checksum.
- Device Owner dùng `PackageInstaller` để cài APK đơn/universal từ URL HTTPS trực tiếp.
- Đổi Wi‑Fi sau provisioning: Android 12+ dùng `addNetworkPrivileged()`, Android 8–11 dùng `addNetwork()`.

## Build
- compileSdk 34
- targetSdk 34
- minSdk 26
- Android Gradle Plugin 8.2.2
- Gradle 8.2+
- JDK 17

Build thuận tiện nhất trên điện thoại bằng GitHub Actions trong `.github/workflows/build-apk.yml`.
APK sau build nằm trong artifact `LongDPC-debug-apk`, file bên trong là `app-debug.apk`.

## Lưu ý
- Không bypass FRP/activation lock.
- Không tự đăng nhập Gmail cá nhân.
- Firmware docomo/au/SoftBank/Sharp/FCNT có thể có bước Setup Wizard riêng.
- URL của 4 app phải là HTTPS tải trực tiếp **APK đơn/universal** và bạn phải có quyền sử dụng/phân phối nguồn APK đó. Link trang Google Play không phải URL APK trực tiếp.
- Không hỗ trợ XAPK/APKM/split APK nhiều file ở bản này.
