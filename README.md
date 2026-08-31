# LongDPC v1.3 phone-test — AQUOS / arrows

## Mục tiêu
Máy mẹ tạo QR Android Enterprise; máy con factory reset -> chạm 6 lần -> quét QR -> Wi‑Fi -> tải DPC -> Fully Managed / Device Owner -> cài APK từ URL HTTPS.

Wi‑Fi mặc định: `Longkaca` / `15082020`.

Apps:
- TikTok: `com.zhiliaoapp.musically`
- TikTok Lite: `com.zhiliaoapp.musically.go`
- LINE: `jp.naver.line.android`
- Auto Scroll: `com.tafayor.autoscrolling`

## Cải tiến v1.2
- Package hợp lệ: `com.longkaca.dpc`.
- Android 12+ có `GET_PROVISIONING_MODE` và `ADMIN_POLICY_COMPLIANCE`.
- Có `PROVISIONING_SUCCESSFUL` activity theo mẫu TestDPC.
- Máy mẹ tự tính SHA‑256 URL-safe Base64 của APK LongDPC đang cài và điền sẵn vào ô checksum.
- URL provisioning phải phục vụ **đúng cùng file APK** với bản đang cài trên máy mẹ, nếu không checksum sẽ sai.
- Device Owner dùng `PackageInstaller` để cài custom/private APK; hỗ trợ APK đơn/universal.
- Đổi Wi‑Fi sau provisioning: Android 12+ dùng `addNetworkPrivileged()`, Android 8–11 dùng `addNetwork()`.

## Build
- compileSdk 34
- targetSdk 34
- minSdk 26
- Android Gradle Plugin 8.2.2
- Gradle 8.2+
- JDK 17

Xem `BUILD_ON_PHONE.md` để build/test hoàn toàn từ điện thoại.

Mở thư mục project trong AndroidIDE/Android Studio rồi build `assembleDebug` hoặc signed APK.

## Lưu ý
- Không bypass FRP/activation lock.
- Không tự đăng nhập Gmail cá nhân.
- Firmware docomo/au/SoftBank/Sharp/FCNT có thể có bước riêng.
- URL TikTok/LINE/Auto Scroll cần là nguồn APK hợp lệ mà bạn có quyền phân phối; không dùng API không chính thức để kéo APK từ Google Play.
