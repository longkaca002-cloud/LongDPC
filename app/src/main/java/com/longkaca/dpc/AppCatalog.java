package com.longkaca.dpc;

public final class AppCatalog {
    private AppCatalog() {}

    public static final String DEFAULT_DPC_URL =
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/v2.2-test/app-debug.apk";

    public static final String DEFAULT_WIFI_SSID = "Longkaca";
    public static final String DEFAULT_WIFI_PASSWORD = "15082020";

    public static final String[] NAMES = {
            "TikTok Nhật", "TikTok Lite Nhật", "LINE", "Auto Scroll", "Gmail", "Long OCR"
    };

    public static final String[] PACKAGES = {
            "com.ss.android.ugc.trill",
            "com.ss.android.ugc.tiktok.lite",
            "jp.naver.line.android",
            "com.tafayor.autoscrolling",
            "com.google.android.gm",
            "com.longkaca.ocr"
    };

    // Toàn bộ ứng dụng phụ dùng chung một release apps-v2 để tránh URL 404.
    public static final String[] DEFAULT_URLS = {
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/tiktok.apks",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/tiktok-lite.apks",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/line.apks",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/autoscroll.apk",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/gmail.apks",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/long-ocr.apk"
    };
}
