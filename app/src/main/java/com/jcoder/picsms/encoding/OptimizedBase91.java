package com.jcoder.picsms.encoding;

import android.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class OptimizedBase91 {

    static final byte[] ENCODING_TABLE;
    private static final byte[] DECODING_TABLE;
    static final int BASE;
    private static final float AVERAGE_ENCODING_RATIO = 1.2297f;
    private static final ArrayList<Pair<String, String>> rule = new ArrayList<>();

    static {

        rule.add(new Pair<>("\u0028", "\u00a3"));
        rule.add(new Pair<>("\u0029", "\u00a5"));
        rule.add(new Pair<>("\u005b", "\u00d8"));
        rule.add(new Pair<>("\u005d", "\u00f8"));
        rule.add(new Pair<>("\u005e", "\u0398"));
        rule.add(new Pair<>("\u0060", "\u03a0"));
        rule.add(new Pair<>("\u007b", "\u03a3"));
        rule.add(new Pair<>("\u007c", "\u03a6"));
        rule.add(new Pair<>("\u007d", "\u03a8"));
        rule.add(new Pair<>("\u007e", "\u03a9"));

        String ts = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!#$%&()*+,./:;<=>?@[]^_`{|}~\"";
        ENCODING_TABLE = ts.getBytes(StandardCharsets.ISO_8859_1);
        BASE = ENCODING_TABLE.length;

        DECODING_TABLE = new byte[256];
        for (int i = 0; i < 256; ++i)
            DECODING_TABLE[i] = -1;

        for (int i = 0; i < BASE; ++i)
            DECODING_TABLE[ENCODING_TABLE[i]] = (byte) i;
    }

    public static String encode(byte[] data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OptimizedBase91OutputStream base91OutputStream = new OptimizedBase91OutputStream(out);
        try {
            base91OutputStream.write(data);
            base91OutputStream.flush();
        } catch (IOException e) {
            return null;
        }

        return replaceWithRule(false, out.toString());
    }

    public static byte[] decode(String encoded) {
        encoded = replaceWithRule(true, encoded);

        if (encoded == null) return null;

        int dbq = 0;
        int dn = 0;
        int dv = -1;

        byte[] data = encoded.getBytes();
        int estimatedSize = Math.round(data.length / AVERAGE_ENCODING_RATIO);
        ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedSize);

        for (byte datum : data) {
            if (dv == -1)
                dv = DECODING_TABLE[datum];
            else {
                dv += DECODING_TABLE[datum] * BASE;
                dbq |= dv << dn;
                dn += (dv & 8191) > 88 ? 13 : 14;
                do {
                    output.write((byte) dbq);
                    dbq >>= 8;
                    dn -= 8;
                } while (dn > 7);
                dv = -1;
            }
        }

        if (dv != -1) {
            output.write((byte) (dbq | dv << dn));
        }

        return output.toByteArray();
    }

    private static String replaceWithRule(boolean reverse, String output) {
        for (Pair<String, String> pair : rule) {
            output = reverse ? output.replace(pair.second, pair.first) : output.replace(pair.first, pair.second);
        }
        return output;
    }

    public static boolean isOptimizedBase91(String str) {
        for (Pair<String, String> pair : rule) {
            if (str.contains(pair.second)) return true;
        }
        return false;
    }

}