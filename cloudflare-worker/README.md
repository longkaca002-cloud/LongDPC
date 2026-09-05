# Cloudflare R2 cho LongDPC

Worker chỉ cho phép GET/HEAD đối với `.apk`, `.apks`, `.json`, truyền trực tiếp body từ R2 và chuyển tiếp HTTP Range để LongDPC nối lại phần tải bị ngắt.

Tên bucket mặc định: `longdpc-files`. Worker binding bắt buộc: `FILES`.

Dùng tên object có số phiên bản, ví dụ:

- `dpc/longdpc-2.9.apk`
- `apps/tiktok-jp-46.9.0.apks`
- `apps/tiktok-lite-46.9.0.apks`
- `apps/line-26.14.0.apks`
- `apps/long-auto-swipe-1.2.apk`
- `apps/long-ocr-1.4.apk`

Không ghi đè file cũ cùng URL. Tải file mới bằng tên phiên bản mới rồi đổi URL trong máy mẹ; cách này tránh cache cũ.
