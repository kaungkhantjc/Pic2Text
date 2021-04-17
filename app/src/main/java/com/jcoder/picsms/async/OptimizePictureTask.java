package com.jcoder.picsms.async;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;

public class OptimizePictureTask implements Callable<byte[]> {

    private final Bitmap pictureBitmap;
    private final int resolution, quality;
    private final boolean optimizeResolution;

    public OptimizePictureTask(Bitmap pictureBitmap, int resolution, int quality, boolean optimizeResolution) {
        this.pictureBitmap = pictureBitmap;
        this.resolution = resolution;
        this.quality = quality;
        this.optimizeResolution = optimizeResolution;
    }

    @Override
    public byte[] call() {
        Bitmap resizedBitmap = resize(pictureBitmap);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality == 0 ? 1 : quality, stream);
        return stream.toByteArray();
    }

    private Bitmap resize(Bitmap bitmap) {
        final int maxHeight = 920;
        final int maxWidth = 700;

        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();

        if (optimizeResolution) {
            if (srcHeight > maxHeight) {
                double percent = maxHeight / (double) srcHeight;
                srcHeight = maxHeight;
                srcWidth = (int) (srcWidth * percent);
            }

            if (srcWidth > maxWidth) {
                double percent = maxWidth / (double) srcWidth;
                srcWidth = maxWidth;
                srcHeight = (int) (srcHeight * percent);
            }
        }

        double userPercent = resolution == 0 ? 0.005f : resolution / 100f;

        int dstWidth = (int) (srcWidth * userPercent);
        int dstHeight = (int) (srcHeight * userPercent);
        return Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
    }
}
