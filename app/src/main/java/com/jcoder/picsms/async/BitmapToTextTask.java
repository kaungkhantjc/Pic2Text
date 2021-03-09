package com.jcoder.picsms.async;

import android.graphics.Bitmap;
import android.util.Base64;

import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

public class BitmapToTextTask implements Callable<String> {


    private final Bitmap bitmap;
    private final int resolution, quality;
    private final boolean optimizeResolution;

    public BitmapToTextTask(Bitmap bitmap, int resolution, int quality, boolean doNotOptimizeResolution) {
        this.bitmap = bitmap;
        this.resolution = resolution;
        this.quality = quality;
        this.optimizeResolution = doNotOptimizeResolution;
    }

    @Override
    public String call() {
        Bitmap resizedBitmap = resize(bitmap);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality == 0 ? 1 : quality, stream);
        byte[] imageBytes = stream.toByteArray();

        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzip = new GZIPOutputStream(obj);
            gzip.write(imageBytes);
            gzip.flush();
            gzip.close();
            byte[] compressedBytes = obj.toByteArray();
            Constants.text = new String(Base64.encode(compressedBytes, Base64.DEFAULT));
            Constants.list = ChunkUtils.split(Constants.text, Constants.MAX_CHARACTERS_PER_SMS);

        } catch (IOException e) {
            return e.toString();
        }

        return null;
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

        double userPercent = resolution == 0 ? 0.01f : resolution / 100f;

        int dstWidth = (int) (srcWidth * userPercent);
        int dstHeight = (int) (srcHeight * userPercent);
        return Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
    }

}
