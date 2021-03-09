package com.jcoder.picsms.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MIUIUtils {
    public static boolean isMIUI() {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
    }

    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return line;
    }

    public static boolean sendSMSPermissionGranted(Context context) {
        return permissionGranted(context, "android:send_sms");
    }

    public static boolean readSMSPermissionGranted(Context context) {
        return permissionGranted(context, "android:read_sms");
    }

    public static boolean readPhoneStatePermissionGranted(Context context) {
        return permissionGranted(context, "android:read_phone_state");
    }

    private static boolean permissionGranted(Context context, String op) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            return appOps.checkOpNoThrow(op, android.os.Process.myUid(), context.getPackageName()) == AppOpsManager.MODE_ALLOWED;
        }
        return true;
    }
}
