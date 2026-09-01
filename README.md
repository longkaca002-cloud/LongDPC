# LongDPC v1.6 provisioning-fix — AQUOS / arrows

Bản này tập trung sửa luồng QR Device Owner sau khi TestDPC chính thức đã provision được trên máy thử.

Thay đổi chính:
- Tự trả `PROVISIONING_MODE_FULLY_MANAGED_DEVICE`; không hiển thị màn chọn mode.
- Trả `EXTRA_PROVISIONING_SKIP_EDUCATION_SCREENS=true`.
- Chuyển tiếp `PROVISIONING_ADMIN_EXTRAS_BUNDLE` từ GET_PROVISIONING_MODE sang compliance.
- `ADMIN_POLICY_COMPLIANCE` trả đúng `setResult(RESULT_OK, Intent)` rồi `finish()`.
- Không tự mở Activity khác trong luồng compliance Android 10+.
- QR dùng `PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM` giống TestDPC thay vì hash toàn file APK.
- Wi-Fi chỉ được nhúng QR khi SSID không trống.
- App thứ 4: Auto Scroll `com.tafayor.autoscrolling`.

Build: compileSdk 34 / targetSdk 34 / minSdk 26 / AGP 8.2.2 / JDK 17.
