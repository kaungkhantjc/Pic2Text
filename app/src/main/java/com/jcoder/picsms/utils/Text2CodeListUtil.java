package com.jcoder.picsms.utils;

import android.util.Log;

import com.jcoder.picsms.models.CodePart;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class Text2CodeListUtil {
    private static final Pattern codeValidatorPattern = Pattern.compile("[a-zA-Z0-9/+=](.*?)[a-zA-Z0-9/+=]");

    public static ArrayList<CodePart> text2CodeParts(String sms, boolean onlyCodeOrderSms) {
        sms = sms.trim().replace("\n", "");
        final ArrayList<CodePart> codeParts = new ArrayList<>();

        // check SMS is Combined Code List or not
        StringTokenizer codeListTokenizer = new StringTokenizer(sms, "(");
        while (codeListTokenizer.hasMoreTokens()) {
            // separate code order number & code
            StringTokenizer codeTokenizer = new StringTokenizer(codeListTokenizer.nextToken(), "-");

            int tokens = codeTokenizer.countTokens();
            if (tokens == 2) {
                String codeOrderStr = codeTokenizer.nextToken();
                String code = codeTokenizer.nextToken();
                try {
                    long codeOrder = Long.parseLong(codeOrderStr);
                    if (validateCode(code)) {
                        Log.e("CODE", "validated");
                        codeParts.add(new CodePart(codeOrder, code));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (!onlyCodeOrderSms && codeParts.size() == 0) {
            // SMS is not Combined Code List now, check SMS contains base64 characters only
            if (validateCode(sms)) {
                codeParts.add(new CodePart(System.currentTimeMillis(), sms));
            }
        }
        return codeParts;
    }

    private static boolean validateCode(String sms) {
        return codeValidatorPattern.matcher(sms).matches();
    }
}
