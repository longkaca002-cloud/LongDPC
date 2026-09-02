package com.longkaca.ocr;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends ComponentActivity {
    private static final int CAMERA_REQUEST = 2001;
    private PreviewView preview;
    private EditText result;
    private TextView status;
    private ExecutorService cameraExecutor;
    private TextRecognizer recognizer;
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private volatile boolean frozen;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        buildUi();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }
    }

    private void buildUi() {
        FrameLayout frame = new FrameLayout(this);
        preview = new PreviewView(this);
        preview.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        frame.addView(preview, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14), dp(10), dp(14), dp(14));
        panel.setBackgroundColor(0xEFFFFFFF);
        status = new TextView(this);
        status.setText("Đưa địa chỉ email vào khung camera");
        status.setTextColor(Color.BLACK);
        status.setTextSize(16);
        panel.addView(status);

        result = new EditText(this);
        result.setHint("Email nhận dạng được sẽ hiện tại đây");
        result.setTextSize(18);
        result.setSingleLine(true);
        panel.addView(result, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        Button freeze = new Button(this);
        freeze.setText("GIỮ / QUÉT LẠI");
        freeze.setOnClickListener(v -> {
            frozen = !frozen;
            status.setText(frozen ? "Đã giữ kết quả — kiểm tra rồi sao chép" : "Đang quét lại…");
        });
        actions.addView(freeze, new LinearLayout.LayoutParams(0, -2, 1));
        Button copy = new Button(this);
        copy.setText("SAO CHÉP EMAIL");
        copy.setOnClickListener(v -> copyResult());
        actions.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        panel.addView(actions);

        FrameLayout.LayoutParams panelParams = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        frame.addView(panel, panelParams);
        setContentView(frame);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();
                Preview cameraPreview = new Preview.Builder().build();
                cameraPreview.setSurfaceProvider(preview.getSurfaceProvider());
                ImageAnalysis analysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(cameraExecutor, this::analyze);
                provider.unbindAll();
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, cameraPreview, analysis);
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("Không mở được camera: " + e.getMessage()));
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyze(ImageProxy proxy) {
        if (frozen || !processing.compareAndSet(false, true)) { proxy.close(); return; }
        Image media = proxy.getImage();
        if (media == null) { processing.set(false); proxy.close(); return; }
        InputImage image = InputImage.fromMediaImage(media, proxy.getImageInfo().getRotationDegrees());
        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    String email = EmailExtractor.firstEmail(text.getText());
                    if (!email.isEmpty()) runOnUiThread(() -> {
                        if (!email.equals(result.getText().toString())) result.setText(email);
                        status.setText("Đã thấy email — kiểm tra kỹ ký tự rồi sao chép");
                    });
                })
                .addOnFailureListener(e -> runOnUiThread(() -> status.setText("OCR lỗi: " + e.getMessage())))
                .addOnCompleteListener(task -> { processing.set(false); proxy.close(); });
    }

    private void copyResult() {
        String value = result.getText().toString().trim();
        if (EmailExtractor.firstEmail(value).isEmpty()) {
            Toast.makeText(this, "Kết quả chưa giống một địa chỉ email hợp lệ", Toast.LENGTH_LONG).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("email", value));
        frozen = true;
        Toast.makeText(this, "Đã sao chép email — chuyển sang chỗ đăng nhập và dán", Toast.LENGTH_LONG).show();
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grants) {
        super.onRequestPermissionsResult(requestCode, permissions, grants);
        if (requestCode == CAMERA_REQUEST && grants.length > 0 && grants[0] == PackageManager.PERMISSION_GRANTED) startCamera();
        else status.setText("Cần cho phép Camera để quét chữ");
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        if (recognizer != null) recognizer.close();
        if (cameraExecutor != null) cameraExecutor.shutdown();
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
}
