# LongDPC v2.8 + Long OCR 1.4 + Long Auto Swipe 1.0 — kết quả kiểm tra

- Máy mẹ mặc định Wi-Fi `Longkaca5G`, password `15082020`.
- Máy mẹ chọn APN cụ thể trước khi tạo QR; mặc định `jconnect`.
- Gmail mặc định không tải; ứng dụng hệ thống được giữ lại.
- Long OCR dùng asset riêng `long-ocr-v14.apk`, ghép email `.us` bị xuống dòng và bỏ phần sau email.
- Auto Scroll bên ngoài được thay bằng `long-auto-swipe.apk`; app chỉ nhận TikTok Lite và chờ ngẫu nhiên 10–15 giây.

- Long OCR 1.2 dùng ảnh JPEG chất lượng cao thay cho OCR liên tục.
- Có chạm để lấy nét, nút `CHỤP VÀ QUÉT`, danh sách email và trạng thái `ĐÃ COPY`.

Ngày: 2026-09-02

## PASS trong môi trường hiện tại
- JSON QR và toàn bộ XML parse hợp lệ.
- Package Device Owner thống nhất `com.longkaca.dpc`; Long OCR là `com.longkaca.ocr`.
- QR giữ system apps, tự nhập Wi-Fi và chuyển đủ 6 URL app qua admin extras.
- Long Auto Swipe không có quyền Internet; manifest giới hạn dịch vụ vào TikTok Lite và cho phép cử chỉ vuốt.
- Bộ cài nhận APK đơn và `.apks` nhiều split trong một PackageInstaller session.
- Trường Tên APN đúng: `jconnect` và `LINEモバイル`; cả hai dùng IPv4/IPv6.
- Không coi riêng chữ `SoftBank` là LINE Mobile vì cả hai SIM đều dùng hạ tầng SoftBank.
- Long OCR có quyền Camera, CameraX preview/analyzer, ML Kit OCR, ô sửa và Clipboard; LongDPC tự cấp quyền Camera sau khi cài thành công.
- Bộ tách email Java đã biên dịch và chạy thật bằng compiler JDK: kiểm tra địa chỉ thường, khoảng trắng OCR, `.co.jp`, dữ liệu sai và loại trùng trong danh sách.
- ZIP được kiểm tra toàn vẹn sau khi đóng gói.
- Kotlin BOM `1.8.22` khóa đồng bộ stdlib/jdk7/jdk8 để tránh `checkDebugDuplicateClasses`.

## Không thể xác nhận tại đây
- Không có Android SDK/Gradle và không có thiết bị/emulator, nên chưa thể sinh APK hoặc mở camera thật trong môi trường này.
- Chưa thể kiểm chứng Setup Wizard riêng của từng firmware AQUOS/arrows hay dữ liệu di động thực tế.

## Cổng kiểm tra trước khi dùng hàng loạt
1. Chạy workflow GitHub Actions đi kèm để build `app-debug.apk`, `longocr-debug.apk` và `longswipe-debug.apk`.
2. Cài Long OCR lên một điện thoại, cho phép Camera và thử giấy chứa email thật.
3. Upload Long OCR với tên `long-ocr-v14.apk` và app vuốt với tên `long-auto-swipe.apk`; Gmail hệ thống không cần asset mặc định.
4. Upload LongDPC, tạo QR bằng chính APK cùng chữ ký và provision một máy đã factory reset.
5. Xác nhận đủ 6 app; thử từng SIM với nút APN đúng trước khi nhân rộng.
