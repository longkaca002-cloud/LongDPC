# LongDPC v2.4 + Long OCR 1.3 — chọn từng hàng văn bản

## Luồng máy con
1. Factory reset, chạm 6 lần ở màn hình Welcome và quét QR do máy mẹ tạo.
2. QR tự nhập Wi-Fi đã chọn (mặc định `Longkaca` / `15082020`) và tải LongDPC.
3. LongDPC trở thành Device Owner, yêu cầu Android bỏ qua màn hình hướng dẫn có thể bỏ qua.
4. Khi có mạng, JobScheduler cài lần lượt TikTok Nhật, TikTok Lite Nhật, LINE, Auto Scroll, Gmail và Long OCR.
5. LongDPC thử nhận diện APN. Nếu không chắc chắn, mở LongDPC và bấm `JCONNECT` hoặc `LINE MOBILE (SOFTBANK)`.

## APN có sẵn
- Tên `jconnect`: APN `plus.4g`, user `plus`, password `4g`, CHAP, IPv4/IPv6.
- Tên `LINEモバイル`: APN `line.me`, user `line@line`, password `line`, PAP hoặc CHAP, IPv4/IPv6.

Cả hai loại SIM đều chạy trên hạ tầng SoftBank. Vì vậy chữ `SoftBank` không được dùng để tự phân biệt; nếu máy không trả về đúng tên MVNO thì người dùng chọn một trong hai nút.

Override APN cần Android 9+ và LongDPC phải là Device Owner. Chế độ tự động không đoán bừa: nếu tên nhà mạng không nhận diện chắc chắn, app yêu cầu bấm đúng nút.

## File app cần host bằng HTTPS trực tiếp
- `apps-v2/tiktok.apks`
- `apps-v2/tiktok-lite.apks`
- `apps-v2/line.apks`
- `apps-v2/autoscroll.apk`
- Gmail để trống: giữ Gmail hệ thống có sẵn, không tải thêm.
- `apps-v2/long-ocr-v13.apk`

Long OCR tự cài từ asset `long-ocr-v13.apk`. Ứng dụng chụp toàn bộ văn bản, sắp xếp các dòng theo tọa độ từ trên xuống và cho sửa/sao chép từng hàng. Gmail hệ thống được giữ nhờ provisioning không tắt ứng dụng hệ thống; ô Gmail mặc định để trống để tránh lỗi phiên bản trên Android 12. Máy mẹ chọn `jconnect` hoặc `LINEモバイル` trước khi tạo QR.

## Dùng Long OCR
1. Mở Long OCR. Khi được cài bởi LongDPC Device Owner, quyền Camera được tự cấp; nếu cài riêng thì cho phép Camera lần đầu.
2. Đưa bảng email vào khung; app thêm mỗi email vào một dòng cố định, không nhảy vị trí.
3. Giữ máy thẳng, đủ sáng rồi bấm `CHỤP VÀ QUÉT`; kiểm tra/sửa đúng dòng và bấm `SAO CHÉP`.
4. Dòng đã dùng đổi thành `ĐÃ COPY ✓`; bấm `XÓA DANH SÁCH` khi chuyển sang bảng khác.

## Giới hạn Android/OEM
LongDPC yêu cầu Android bỏ qua education screens và tự trả về `RESULT_OK` ở compliance flow. Màn hình pháp lý, kích hoạt SIM hoặc màn hình bắt buộc riêng của firmware AQUOS/arrows vẫn có thể xuất hiện; DPC không được phép vượt màn hình bắt buộc.

## Build
Build bằng AndroidIDE/JDK 17 + SDK 34 hoặc GitHub Actions. APK tại URL provisioning phải được ký bằng đúng chứng thư mà máy mẹ dùng để tạo checksum.
