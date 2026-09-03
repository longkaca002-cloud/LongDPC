package com.longkaca.autoswipe;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.Random;

public class SwipeAccessibilityService extends AccessibilityService {
    public static final String TIKTOK_LITE = "com.ss.android.ugc.tiktok.lite";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private boolean wasInTikTok;
    private long lastTikTokEventAt;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!isRunning()) {
                wasInTikTok = false;
                schedule(1000);
                return;
            }
            AccessibilityNodeInfo root = getRootInActiveWindow();
            String activePackage = root == null || root.getPackageName() == null
                    ? "" : root.getPackageName().toString();
            boolean inTikTok = isTikTokLite(activePackage)
                    || System.currentTimeMillis() - lastTikTokEventAt < 5_000L;
            if (!inTikTok) {
                wasInTikTok = false;
                schedule(1000);
                return;
            }
            if (!wasInTikTok) {
                wasInTikTok = true;
                schedule(nextVideoDelay());
                return;
            }
            swipeUp();
            schedule(nextVideoDelay());
        }
    };

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        handler.removeCallbacks(tick);
        schedule(1000);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event != null && event.getPackageName() != null
                && isTikTokLite(event.getPackageName().toString())) {
            lastTikTokEventAt = System.currentTimeMillis();
        }
    }

    @Override public void onInterrupt() {
        handler.removeCallbacks(tick);
        wasInTikTok = false;
    }

    @Override public void onDestroy() {
        handler.removeCallbacks(tick);
        super.onDestroy();
    }

    private boolean isRunning() {
        return getSharedPreferences("swipe", MODE_PRIVATE).getBoolean("running", false);
    }

    private long nextVideoDelay() { return 10_000L + random.nextInt(5_001); }

    private boolean isTikTokLite(String packageName) {
        return TIKTOK_LITE.equals(packageName)
                || "com.zhiliaoapp.musically.go".equals(packageName);
    }

    private void schedule(long delayMillis) { handler.postDelayed(tick, delayMillis); }

    private void swipeUp() {
        float width = getResources().getDisplayMetrics().widthPixels;
        float height = getResources().getDisplayMetrics().heightPixels;
        Path path = new Path();
        path.moveTo(width * 0.50f, height * 0.78f);
        path.lineTo(width * 0.50f, height * 0.25f);
        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 450))
                .build();
        dispatchGesture(gesture, new GestureResultCallback() {
            @Override public void onCompleted(GestureDescription gestureDescription) {
                getSharedPreferences("swipe", MODE_PRIVATE).edit()
                        .putString("last_result", "Đã vuốt thành công lúc "
                                + android.text.format.DateFormat.format("HH:mm:ss", System.currentTimeMillis()))
                        .apply();
            }

            @Override public void onCancelled(GestureDescription gestureDescription) {
                getSharedPreferences("swipe", MODE_PRIVATE).edit()
                        .putString("last_result", "Cử chỉ bị Android hủy")
                        .apply();
            }
        }, handler);
    }
}
