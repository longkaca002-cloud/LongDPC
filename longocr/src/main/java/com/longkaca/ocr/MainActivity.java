package com.longkaca.ocr;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Camera;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
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
import com.google.mlkit.vision.text.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends ComponentActivity {
    private static final int CAMERA_REQUEST = 2001;
    private PreviewView preview;
    private TextView status;
    private LinearLayout lineList;
    private ExecutorService cameraExecutor;
    private TextRecognizer recognizer;
    private ImageCapture imageCapture;
    private Button scanButton;
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private int rowCount = 0;

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
        status.setText("Chụp toàn bộ văn bản — sau đó chọn từng hàng để sao chép");
        status.setTextColor(Color.BLACK);
        status.setTextSize(16);
        panel.addView(status);

        lineList = new LinearLayout(this);
        lineList.setOrientation(LinearLayout.VERTICAL);
        ScrollView lineScroll = new ScrollView(this);
        lineScroll.addView(lineList);
        panel.addView(lineScroll, new LinearLayout.LayoutParams(-1, dp(320)));

        scanButton = new Button(this);
        scanButton.setText("CHỤP VÀ QUÉT");
        scanButton.setTextSize(18);
        scanButton.setOnClickListener(v -> captureAndRecognize());
        panel.addView(scanButton);

        Button clear = new Button(this);
        clear.setText("XÓA DANH SÁCH — QUÉT TỜ KHÁC");
        clear.setOnClickListener(v -> {
            rowCount = 0;
            lineList.removeAllViews();
            status.setText("Đã xóa — đưa bảng mới vào khung rồi bấm CHỤP VÀ QUÉT");
        });
        panel.addView(clear);

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
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                        .setJpegQuality(100)
                        .build();
                provider.unbindAll();
                Camera camera = provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, cameraPreview, imageCapture);
                preview.setOnTouchListener((view, event) -> {
                    if (event.getAction() != android.view.MotionEvent.ACTION_UP) return true;
                    FocusMeteringAction focus = new FocusMeteringAction.Builder(
                            preview.getMeteringPointFactory().createPoint(event.getX(), event.getY()))
                            .setAutoCancelDuration(3, TimeUnit.SECONDS).build();
                    camera.getCameraControl().startFocusAndMetering(focus);
                    status.setText("Đang lấy nét — khi chữ rõ hãy bấm CHỤP VÀ QUÉT");
                    return true;
                });
                status.setText("Camera sẵn sàng — chạm vào bảng để lấy nét rồi bấm CHỤP VÀ QUÉT");
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("Không mở được camera: " + e.getMessage()));
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureAndRecognize() {
        if (imageCapture == null || !processing.compareAndSet(false, true)) return;
        scanButton.setEnabled(false);
        status.setText("Đang chụp ảnh rõ và nhận dạng…");
        File photo = new File(getCacheDir(), "long_ocr_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(photo).build();
        imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                try {
                    InputImage image = InputImage.fromFilePath(MainActivity.this, Uri.fromFile(photo));
                    recognizer.process(image)
                            .addOnSuccessListener(text -> {
                                List<RecognizedLine> lines = collectLines(text);
                                List<String> rawLines = new ArrayList<>();
                                for (RecognizedLine line : lines) rawLines.add(line.value);
                                List<String> emails = EmailExtractor.usEmailsFromLines(rawLines);
                                runOnUiThread(() -> {
                                    if (emails.isEmpty()) status.setText("Chưa thấy email kết thúc bằng .us — đưa máy gần hơn rồi chụp lại");
                                    else showEmails(emails);
                                });
                            })
                            .addOnFailureListener(e -> runOnUiThread(() -> status.setText("OCR lỗi: " + e.getMessage())))
                            .addOnCompleteListener(task -> finishScan(photo));
                } catch (IOException e) {
                    runOnUiThread(() -> status.setText("Không đọc được ảnh vừa chụp: " + e.getMessage()));
                    finishScan(photo);
                }
            }

            @Override public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> status.setText("Không chụp được ảnh: " + exception.getMessage()));
                finishScan(photo);
            }
        });
    }

    private void finishScan(File photo) {
        if (photo.exists()) photo.delete();
        processing.set(false);
        runOnUiThread(() -> scanButton.setEnabled(true));
    }

    private List<RecognizedLine> collectLines(Text result) {
        List<RecognizedLine> lines = new ArrayList<>();
        for (Text.TextBlock block : result.getTextBlocks()) {
            for (Text.Line line : block.getLines()) {
                String value = line.getText() == null ? "" : line.getText().trim();
                Rect box = line.getBoundingBox();
                if (!value.isEmpty()) lines.add(new RecognizedLine(value,
                        box == null ? Integer.MAX_VALUE : box.top,
                        box == null ? Integer.MAX_VALUE : box.left));
            }
        }
        lines.sort(Comparator.comparingInt((RecognizedLine line) -> line.top)
                .thenComparingInt(line -> line.left));
        return lines;
    }

    private void showEmails(List<String> emails) {
        rowCount = 0;
        lineList.removeAllViews();
        for (String email : emails) {
            if (rowCount >= 100) break;
            rowCount++;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            TextView number = new TextView(this);
            number.setText(rowCount + ".");
            number.setGravity(Gravity.CENTER);
            number.setTextSize(16);
            row.addView(number, new LinearLayout.LayoutParams(dp(42), -2));
            EditText value = new EditText(this);
            value.setText(email);
            value.setSingleLine(true);
            value.setTextSize(16);
            row.addView(value, new LinearLayout.LayoutParams(0, -2, 1));
            Button copy = new Button(this);
            copy.setText("SAO CHÉP");
            copy.setOnClickListener(v -> copyEmail(value, copy));
            row.addView(copy, new LinearLayout.LayoutParams(dp(128), -2));
            lineList.addView(row);
        }
        status.setText("Đã ghép và nhận " + rowCount + " email .us từ trên xuống — chọn email để sao chép");
    }

    private void copyEmail(EditText field, Button button) {
        String value = field.getText().toString().trim();
        if (value.isEmpty()) {
            Toast.makeText(this, "Hàng này đang trống", Toast.LENGTH_LONG).show();
            return;
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("email", value));
        button.setText("ĐÃ COPY ✓");
        button.setEnabled(false);
        Toast.makeText(this, "Đã sao chép: " + value, Toast.LENGTH_SHORT).show();
    }

    private static final class RecognizedLine {
        final String value;
        final int top;
        final int left;
        RecognizedLine(String value, int top, int left) {
            this.value = value;
            this.top = top;
            this.left = left;
        }
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
