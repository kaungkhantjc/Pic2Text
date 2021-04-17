package com.jcoder.picsms.encoding;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class OptimizedBase91OutputStream extends FilterOutputStream {

    private int ebq = 0;
    private int en = 0;

    public OptimizedBase91OutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) throws IOException {
        ebq |= (b & 255) << en;
        en += 8;
        if (en > 13) {
            int ev = ebq & 8191;

            if (ev > 88) {
                ebq >>= 13;
                en -= 13;
            } else {
                ev = ebq & 16383;
                ebq >>= 14;
                en -= 14;
            }
            out.write(OptimizedBase91.ENCODING_TABLE[ev % OptimizedBase91.BASE]);
            out.write(OptimizedBase91.ENCODING_TABLE[ev / OptimizedBase91.BASE]);
        }
    }

    @Override
    public void write(byte[] data, int offset, int length) throws IOException {
        for (int i = offset; i < length; ++i) {
            write(data[i]);
        }
    }

    @Override
    public void flush() throws IOException {
        if (en > 0) {
            out.write(OptimizedBase91.ENCODING_TABLE[ebq % OptimizedBase91.BASE]);
            if (en > 7 || ebq > 90) {
                out.write(OptimizedBase91.ENCODING_TABLE[ebq / OptimizedBase91.BASE]);
            }
        }
        super.flush();
    }
}