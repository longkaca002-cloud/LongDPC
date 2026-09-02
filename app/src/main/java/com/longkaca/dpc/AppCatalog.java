package com.longkaca.dpc;

public final class AppCatalog {
    private AppCatalog() {}

    public static final String DEFAULT_DPC_URL =
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/v2.0-test/app-debug.apk";

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

    // Bốn app cũ giữ nguyên apps-v2; Gmail/Lens dùng tag apps-v3.
    public static final String[] DEFAULT_URLS = {
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/tiktok.apks",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/tiktok-lite.apks",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/line.apks",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v2/autoscroll.apk",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v3/gmail.apks",
            "https://github.com/longkaca002-cloud/LongDPC/releases/download/apps-v3/long-ocr.apk"
    };
}
