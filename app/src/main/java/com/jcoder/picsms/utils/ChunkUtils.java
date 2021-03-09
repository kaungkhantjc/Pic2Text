package com.jcoder.picsms.utils;

import android.os.Build;
import android.telephony.SmsManager;

import java.util.Locale;

public class ChunkUtils {
    public static String[] split(String input, int chunkSize) {
        int arrayLength = (int) Math.ceil(((input.length() / (double) chunkSize)));
        String[] result = new String[arrayLength];

        int j = 0;
        int lastIndex = result.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            result[i] = input.substring(j, j + chunkSize);
            j += chunkSize;
        } //Add the last bit
        result[lastIndex] = input.substring(j);
        return result;
    }

    public static int getChunkCount(String text) {
        SmsManager smsManager;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            int subscriptionId = SmsManager.getDefaultSmsSubscriptionId();
            smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
        } else
            smsManager = SmsManager.getDefault();
        return smsManager.divideMessage(text).size();
    }

    public static String getSortedText(int position, String text) {
        return String.format(Locale.ENGLISH, "(%d-%s", position, text);
    }


}
