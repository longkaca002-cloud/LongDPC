package com.longkaca.autoswipe;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String[] TIKTOK_LITE_PACKAGES = {
            SwipeAccessibilityService.TIKTOK_LITE,
            "com.zhiliaoapp.musically.go"
    };
    private TextView status;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(20));

        TextView title = new TextView(this);
        title.setText("LONG AUTO SWIPE\nTikTok Lite · 10–15 giây");
        title.setTextSize(23);
        title.setTextColor(Color.BLACK);
        root.addView(title);

        TextView note = new TextView(this);
        note.setText("App chỉ vuốt lên khi TikTok Lite đang ở trước màn hình. Không tự thích, theo dõi hay bình luận.\n\nLần đầu: mở Trợ năng và bật Long Auto Swipe.");
        note.setTextSize(16);
        note.setPadding(0, dp(18), 0, dp(12));
        root.addView(note);

        status = new TextView(this);
        status.setTextSize(18);
        status.setPadding(0, dp(8), 0, dp(16));
        root.addView(status);

        root.addView(button("1. MỞ TRỢ NĂNG", v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))));
        root.addView(button("2. BẮT ĐẦU VÀ MỞ TIKTOK LITE", v -> startWatching()));
        root.addView(button("DỪNG TỰ VUỐT", v -> setRunning(false)));
        setContentView(root);
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    private void startWatching() {
        setRunning(true);
        Intent launch = null;
        for (String packageName : TIKTOK_LITE_PACKAGES) {
            launch = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launch != null) break;
        }
        if (launch == null) {
            Toast.makeText(this, "Không tìm thấy biểu tượng mở TikTok Lite. Hãy mở TikTok Lite thủ công; tự vuốt vẫn đang bật.", Toast.LENGTH_LONG).show();
        } else {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(launch);
        }
    }

    private void setRunning(boolean running) {
        getSharedPreferences("swipe", MODE_PRIVATE).edit().putBoolean("running", running).apply();
        refresh();
        Toast.makeText(this, running ? "Đã bật: mở TikTok Lite và chờ 10–15 giây" : "Đã dừng", Toast.LENGTH_LONG).show();
    }

    private void refresh() {
        boolean running = getSharedPreferences("swipe", MODE_PRIVATE).getBoolean("running", false);
        String last = getSharedPreferences("swipe", MODE_PRIVATE)
                .getString("last_result", "Chưa ghi nhận lần vuốt nào");
        status.setText((running ? "Trạng thái: ĐANG CHẠY" : "Trạng thái: ĐÃ DỪNG")
                + "\n" + last);
        status.setTextColor(running ? Color.rgb(0, 120, 40) : Color.rgb(180, 30, 30));
    }

    private Button button(String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setOnClickListener(listener);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(58));
        p.setMargins(0, dp(6), 0, dp(6));
        b.setLayoutParams(p);
        return b;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
