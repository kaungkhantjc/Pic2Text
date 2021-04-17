package com.jcoder.picsms.async;

import android.util.Base64;

import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.encoding.OptimizedBase91;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

public class TextToBytesTask implements Callable<byte[]> {

    private final EncodingType encodingType;
    private final String text;

    public TextToBytesTask(EncodingType encodingType, String text) {
        this.encodingType = encodingType;
        this.text = text;
    }

    @Override
    public byte[] call() {
        byte[] bytes;

        switch (encodingType) {
            case AUTO_DETECT:
                if (OptimizedBase91.isOptimizedBase91(text)) {
                    bytes = OptimizedBase91.decode(text);
                } else {
                    bytes = Base64.decode(text, Base64.DEFAULT);
                }
                break;

            case OPTIMIZED_BASE91_V1_1_0:
                bytes = OptimizedBase91.decode(text);
                break;

            case BASE64:
                bytes = Base64.decode(text, Base64.DEFAULT);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + encodingType);
        }

        if (bytes != null && isCompressed(bytes)) {
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
