# Hướng dẫn LongDPC v2.9 với Cloudflare R2

## Điều Cloudflare sửa được và không sửa được

- R2/Worker thay GitHub cho file lớn, tạo URL HTTPS trực tiếp và hỗ trợ HTTP Range để nối tải.
- Cloudflare không thể làm Wi-Fi đúng khi SSID/mật khẩu/router sai; Wi-Fi phải kết nối trước khi Android tải DPC.
- Cloudflare không tự biến APK cũ thành phiên bản mới. Phải xuất bản `.apks` mới và dùng tên file có version mới.
- Device Owner có thể yêu cầu cài không cần thao tác, nhưng Android luôn yêu cầu người dùng bật dịch vụ Trợ năng của Long Auto Swipe ít nhất một lần.
- Gmail/đăng nhập Google trên máy cũ vẫn có thể yêu cầu cập nhật Google Play Services.

## A. Chuẩn bị Wi-Fi provisioning ổn định

1. Dùng Wi-Fi 2.4 GHz cho máy cũ; có thể giữ tên `Longkaca5G` nhưng mạng đó phải phát cả 2.4 GHz.
2. Dùng WPA2-PSK/AES hoặc chế độ WPA2/WPA3 hỗn hợp; không để WPA3-only.
3. SSID phải phát công khai, không captive portal, DHCP và DNS hoạt động.
4. Nếu dùng 5 GHz, ưu tiên kênh 36/40/44/48; tránh DFS khi thử máy Nhật cũ.
5. Trước khi reset máy con, dùng một máy khác quên mạng rồi nhập lại đúng SSID/mật khẩu để thử.

## B. Tạo R2 và Worker miễn phí

1. Đăng nhập `https://dash.cloudflare.com`.
2. Mở **R2 Object Storage** và kích hoạt R2 nếu được hỏi.
3. Chọn **Create bucket**.
4. Tên bucket: `longdpc-files`.
5. Storage class: **Standard**. Nếu có Location Hint, chọn **Asia Pacific**.
6. Trong Codespaces, giải nén bộ nguồn v2.9, rồi chạy:

```bash
cd cloudflare-worker
npx wrangler login
npx wrangler deploy
```

7. Trình duyệt mở Cloudflare: chọn **Allow**.
8. Sau deploy, ghi lại URL dạng `https://longdpc-files.<tai-khoan>.workers.dev`.
9. Nếu báo bucket không tồn tại, chạy rồi deploy lại:

```bash
npx wrangler r2 bucket create longdpc-files
npx wrangler deploy
```

Worker chỉ chấp nhận GET/HEAD cho `.apk`, `.apks`, `.json`; không cho người ngoài PUT/DELETE. Worker chuyển tiếp Range đến R2.

## C. Dùng chữ ký cố định cho ba app tự tạo

Chỉ làm một lần và giữ khóa suốt đời của dự án. Không commit file `.jks` lên repository.

```bash
keytool -genkeypair -v -keystore long-release.jks -alias longkey -keyalg RSA -keysize 2048 -validity 10000
```

Ghi lại mật khẩu và tải `long-release.jks` về nơi sao lưu an toàn. Sau đó lưu bốn GitHub Actions secrets:

- `ANDROID_KEYSTORE_BASE64`: đặt bằng lệnh dưới đây để không hiện nội dung khóa:

```bash
base64 -w0 long-release.jks | gh secret set ANDROID_KEYSTORE_BASE64
```

- `ANDROID_KEYSTORE_PASSWORD`: mật khẩu keystore.
- `ANDROID_KEY_ALIAS`: `longkey`.
- `ANDROID_KEY_PASSWORD`: mật khẩu key.

Ba secret mật khẩu/alias có thể tạo tại **Repository → Settings → Secrets and variables → Actions → New repository secret**.

Vào **Actions → Build signed LongDPC release APKs → Run workflow**. Sau khi xanh, tải ba Artifact:

- `LongDPC-signed-release` → `app-release.apk`
- `LongOCR-signed-release` → `longocr-release.apk`
- `LongAutoSwipe-signed-release` → `longswipe-release.apk`

Từ lần sau luôn build bằng workflow signed này và tuyệt đối không tạo keystore mới.

## D. Chuẩn bị ứng dụng đúng phiên bản

1. Trên máy mẫu đúng nhóm Android/CPU, cập nhật TikTok, TikTok Lite và LINE từ Play Store.
2. Mở từng app xác nhận chạy được.
3. Xuất full bundle `.apks`, không lấy riêng `base.apk`.
4. Tên object luôn chứa version, ví dụ:

```text
dpc/longdpc-2.9.apk
apps/tiktok-jp-46.9.0.apks
apps/tiktok-lite-46.9.0.apks
apps/line-26.14.0.apks
apps/long-auto-swipe-1.2.apk
apps/long-ocr-1.4.apk
```

Không ghi đè file mới lên cùng tên cũ. Tên version mới tạo URL mới nên không lấy nhầm cache.

## E. Upload file lên R2

1. Cloudflare Dashboard → **R2 → longdpc-files → Upload**.
2. Tạo thư mục `dpc` và `apps` hoặc upload bằng object key tương ứng.
3. Upload năm/sáu file ở mục D.
4. Đặt Content-Type:
   - APK/APKS: `application/octet-stream`
   - JSON: `application/json`

## F. Kiểm tra URL và Range trước khi tạo QR

Thay `BASE` bằng URL workers.dev thật:

```bash
curl -I "BASE/dpc/longdpc-2.9.apk"
curl -I -H "Range: bytes=0-1023" "BASE/apps/tiktok-lite-46.9.0.apks"
```

Lệnh đầu phải trả `HTTP 200`; lệnh thứ hai phải trả `HTTP 206`, `accept-ranges: bytes` và `content-range`. Mở từng URL bằng Chrome cũng phải tải ngay, không 404, không trang xác nhận.

## G. Cập nhật máy mẹ và tạo QR

1. Gỡ LongDPC debug cũ trên máy mẹ nếu Android báo khác chữ ký.
2. Cài đúng `app-release.apk` lấy từ workflow signed.
3. Upload chính file đó thành `dpc/longdpc-2.9.apk` trên R2.
4. Mở LongDPC máy mẹ. Checksum tự lấy từ app đang cài; không sửa tay.
5. DPC URL: `BASE/dpc/longdpc-2.9.apk`.
6. Điền URL TikTok/LINE/OCR/Swipe đúng tên version đã upload.
7. Gmail để trống nếu ROM có Gmail hệ thống.
8. Chọn APN jconnect hoặc LINE, rồi tạo QR mới. Không dùng ảnh QR GitHub cũ.

APK máy mẹ và APK ở DPC URL phải là cùng một file được ký cùng chứng thư. Nếu khác, Setup Wizard từ chối tải ứng dụng quản lý.

## H. Máy con và APN

1. Factory reset, chạm sáu lần và quét QR mới.
2. Sau khi vào máy, chờ LongDPC tự cài app. Không mở link bằng trình duyệt để cài tay.
3. Nếu đang dùng jconnect: giữ APN jconnect đã áp dụng.
4. Nếu thay LINE: mở LongDPC → **ÁP APN — Tên: LINEモバイル**.
5. Nếu thay SIM tự có APN hoặc không cần override: mở LongDPC → **TẮT APN QUẢN LÝ — DÙNG APN CỦA SIM**.
6. Sau khi tắt APN, bật/tắt chế độ máy bay hoặc dữ liệu di động một lần.

Nút tắt sẽ gọi `setOverrideApnsEnabled(false)`, xóa override do LongDPC tạo và trả máy về bảng APN của SIM/nhà mạng.

## I. Long Auto Swipe

- Cài mới qua LongDPC/Device Owner được yêu cầu không cần thao tác người dùng.
- Dịch vụ Trợ năng vẫn phải được người dùng bật một lần: **Long Auto Swipe → Mở Trợ năng → bật Long Auto Swipe**.
- Đây là khóa bảo mật Android, không được bỏ qua bằng QR hay Cloudflare.
- Khi cập nhật bằng APK cùng chữ ký cố định, thường không cần gỡ app và quyền có thể được giữ; nếu Android tắt dịch vụ sau cập nhật, bật lại một lần.

## J. Chuyển đổi và giới hạn

- Máy con cũ đang có LongDPC debug khác chữ ký không thể cập nhật trực tiếp sang LongDPC signed. Muốn có nút tắt APN mới trên máy đó phải dùng APK cùng chữ ký cũ hoặc reset/provision lại bằng bản signed.
- Cloudflare không bảo đảm hết mọi lỗi Wi-Fi; nó chỉ bắt đầu có tác dụng sau khi máy đã kết nối Internet.
- Cloudflare không bảo đảm app luôn mới nhất. Mỗi đợt phải kiểm tra version, xuất `.apks` mới và đổi URL version trên máy mẹ.
