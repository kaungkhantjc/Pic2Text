package com.jcoder.picsms.async;

import android.util.Base64;

import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.encoding.OptimizedBase91;
import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;

public class BytesToTextTask implements Callable<String> {

    private final EncodingType encodingType;
    private final byte[] optimizedPictureBytes;

    public BytesToTextTask(EncodingType encodingType, byte[] optimizedPictureBytes) {
        this.encodingType = encodingType;
        this.optimizedPictureBytes = optimizedPictureBytes;
    }

    @Override
    public String call() {
        try {
            ByteArrayOutputStream obj = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(obj);
            gzip.write(optimizedPictureBytes);
            gzip.flush();
            gzip.close();
            byte[] compressedBytes = obj.toByteArray();

            switch (encodingType) {
                case OPTIMIZED_BASE91_V1_1_0:
                    Constants.text = OptimizedBase91.encode(compressedBytes);
                    break;

                case BASE64:
                    Constants.text = Base64.encodeToString(compressedBytes, Base64.DEFAULT);
                    break;

                default:
                    throw new IllegalStateException("Unexpected value: " + encodingType);
            }


            int maxSortNumber = (int) (Math.ceil(Objects.requireNonNull(Constants.text).length() / (double) Constants.MAX_CHARACTERS_PER_SMS));
            // +2 is for '(' and '-'
            int prefixLength = String.valueOf(maxSortNumber).length() + 2;
            Constants.list = ChunkUtils.split(Constants.text, Constants.MAX_CHARACTERS_PER_SMS - prefixLength);
        } catch (IOException e) {
            return e.toString();
        }

        return null;
    }

}
