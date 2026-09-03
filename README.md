# LongDPC v2.8 + Long OCR 1.4 + Long Auto Swipe 1.0

## Mặc định máy mẹ

- Wi-Fi: `Longkaca5G`; password: `15082020`.
- Chọn APN `jconnect` hoặc `LINEモバイル` trước khi tạo QR.
- Gmail để trống vì giữ Gmail hệ thống có sẵn.

## Release assets

Tag `apps-v2`:

- `tiktok.apks`
- `tiktok-lite.apks`
- `line.apks`
- `long-auto-swipe.apk`
- `long-ocr-v14.apk`

Tag `v2.8-test`:

- `app-debug.apk`

## Long OCR 1.4

Ứng dụng chụp ảnh chất lượng cao, lấy nét khi chạm, sắp xếp dòng OCR theo tọa độ từ trên xuống, ghép tối đa ba dòng và trích mọi email có domain kết thúc bằng `.us`. Kết quả dừng ngay sau `.us`, không lấy phần `|mật khẩu`. Mỗi email có nút sao chép và trạng thái `ĐÃ COPY`.

## Kiểm tra

```bash
python3 tools_static_check.py
```

GitHub Actions build `LongDPC-debug-apk`, `LongOCR-debug-apk` và `LongAutoSwipe-debug-apk`.

LongDPC tự cài lại tối đa bốn vòng và bỏ qua package đã cài, giúp phục hồi khi mạng hoặc firmware dừng vòng đầu.
Trình tải APK/APKS lưu phần đã nhận và dùng HTTP Range để nối tiếp tối đa tám lần khi đường truyền bị ngắt.

## Long Auto Swipe 1.0

App riêng không quảng cáo và không xin quyền Internet. Sau khi người dùng bật dịch vụ Trợ năng một lần, app chỉ vuốt lên trong TikTok Lite `com.ss.android.ugc.tiktok.lite`, với khoảng chờ ngẫu nhiên 10–15 giây. Android không cho Device Owner tự bật dịch vụ Trợ năng trên mọi ROM.
