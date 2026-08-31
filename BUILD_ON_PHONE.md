# Chạy thử LongDPC hoàn toàn từ điện thoại

## Cách A — AndroidIDE (máy mẹ)
1. Cài AndroidIDE đã ngừng bảo trì; ưu tiên Cách B — GitHub Actions. Chỉ dùng AndroidIDE nếu toolchain trên máy đã hoạt động.
2. Mở Terminal trong AndroidIDE, chạy `idesetup -c -j 17` và xác nhận cài build tools.
3. Giải nén project này ra bộ nhớ trong.
4. Trong AndroidIDE: Open existing project -> chọn thư mục `LongDPC_v1_4_JAPAN`.
5. Chờ Gradle sync xong. Bấm Quick Run / Run để chạy `assembleDebug`.
6. APK dự kiến: `app/build/outputs/apk/debug/app-debug.apk`.
7. Cho phép AndroidIDE cài ứng dụng từ nguồn này và cài APK lên máy mẹ.

Bản v1.4 JAPAN dùng compileSdk/targetSdk 34, AGP 8.2.2, JDK 17 để dễ tương thích AndroidIDE hơn.

## Cách B — GitHub Actions (vẫn thao tác hoàn toàn trên điện thoại)
Project có sẵn `.github/workflows/build-apk.yml`.
1. Tạo repo GitHub và upload toàn bộ nội dung project vào root repo.
2. Vào tab Actions -> `Build LongDPC APK` -> Run workflow.
3. Khi build xanh, tải artifact `LongDPC-debug-apk`.
4. Giải nén artifact để lấy `app-debug.apk`.

## Test lần 1 — chỉ test provisioning DPC, chưa cài 4 app
1. Đưa chính `app-debug.apk` lên một HTTPS URL tải trực tiếp.
2. Cài đúng file `app-debug.apk` đó lên máy mẹ.
3. Mở Long DPC trên máy mẹ. App tự tính checksum của APK đang cài.
4. Điền HTTPS URL của DPC; giữ Wi-Fi Longkaca / 15082020.
5. Để trống 4 URL app ở lần test đầu. Bấm TẠO QR PROVISIONING.
6. Factory reset một AQUOS/arrows không bị FRP khóa.
7. Ở Welcome/ようこそ, chạm cùng một vị trí 6 lần -> quét QR.
8. Kết quả mong đợi: nối Wi-Fi -> tải DPC -> provisioning Device Owner -> mở Long DPC ở chế độ MÁY CON.

## Sau khi provisioning thành công
Mới thêm URL HTTPS trực tiếp của 4 APK và thử auto-install. Test từng app một trước khi bật cả 4.
