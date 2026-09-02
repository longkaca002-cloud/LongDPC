# LongDPC v2.2 + Long OCR — kết quả kiểm tra

- Long OCR 1.2 dùng ảnh JPEG chất lượng cao thay cho OCR liên tục.
- Có chạm để lấy nét, nút `CHỤP VÀ QUÉT`, danh sách email và trạng thái `ĐÃ COPY`.

Ngày: 2026-09-02

## PASS trong môi trường hiện tại
- JSON QR và toàn bộ XML parse hợp lệ.
- Package Device Owner thống nhất `com.longkaca.dpc`; Long OCR là `com.longkaca.ocr`.
- QR giữ system apps, tự nhập Wi-Fi và chuyển đủ 6 URL app qua admin extras.
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
1. Chạy workflow GitHub Actions đi kèm để build `app-debug.apk` và `longocr-debug.apk`.
2. Cài Long OCR lên một điện thoại, cho phép Camera và thử giấy chứa email thật.
3. Upload Long OCR với tên release asset `long-ocr.apk` và Gmail thành `gmail.apks`.
4. Upload LongDPC, tạo QR bằng chính APK cùng chữ ký và provision một máy đã factory reset.
5. Xác nhận đủ 6 app; thử từng SIM với nút APN đúng trước khi nhân rộng.
