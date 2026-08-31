package com.longkaca.dpc;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public final class QrUtil {
    private QrUtil() {}
    public static Bitmap make(String text, int size) throws WriterException {
        BitMatrix m = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
        Bitmap b = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int y=0; y<size; y++) for (int x=0; x<size; x++) b.setPixel(x, y, m.get(x,y) ? 0xFF000000 : 0xFFFFFFFF);
        return b;
    }
}
