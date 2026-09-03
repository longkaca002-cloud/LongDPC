# LongDPC v2.5 + Long OCR 1.4

## Mặc định máy mẹ

- Wi-Fi: `Longkaca5G`; password: `15082020`.
- Chọn APN `jconnect` hoặc `LINEモバイル` trước khi tạo QR.
- Gmail để trống vì giữ Gmail hệ thống có sẵn.

## Release assets

Tag `apps-v2`:

- `tiktok.apks`
- `tiktok-lite.apks`
- `line.apks`
- `autoscroll.apk`
- `long-ocr-v14.apk`

Tag `v2.5-test`:

- `app-debug.apk`

## Long OCR 1.4

Ứng dụng chụp ảnh chất lượng cao, lấy nét khi chạm, sắp xếp dòng OCR theo tọa độ từ trên xuống, ghép tối đa ba dòng và trích mọi email có domain kết thúc bằng `.us`. Kết quả dừng ngay sau `.us`, không lấy phần `|mật khẩu`. Mỗi email có nút sao chép và trạng thái `ĐÃ COPY`.

## Kiểm tra

```bash
python3 tools_static_check.py
```

GitHub Actions build cả `LongDPC-debug-apk` và `LongOCR-debug-apk`.
