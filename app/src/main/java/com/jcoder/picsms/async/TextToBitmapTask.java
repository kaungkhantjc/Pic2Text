package com.jcoder.picsms.async;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

public class TextToBitmapTask implements Callable<byte[]> {

    private final String text;

    public TextToBitmapTask(String text) {
        this.text = text;
    }

    @Override
    public byte[] call() {
        byte[] bytes = Base64.decode(text.getBytes(), Base64.DEFAULT);

        if (isCompressed(bytes)) {
            try {
                GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
                ByteArrayOutputStream obj = new ByteArrayOutputStream();

                int res = 0;
                byte[] buf = new byte[1024];
                while (res >= 0) {
                    res = gzip.read(buf, 0, buf.length);
                    if (res > 0) {
                        obj.write(buf, 0, res);
                    }
                }
                return obj.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return null;

    }

    public static boolean isCompressed(final byte[] compressed) {
        return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }
}
