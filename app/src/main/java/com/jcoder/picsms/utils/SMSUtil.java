package com.jcoder.picsms.utils;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;

import com.jcoder.picsms.BuildConfig;

public class SMSUtil {
    public static final int REQ_CODE_DEFAULT_SMS_APP = 130;
    public static final Uri URI_SENT = Uri.parse("content://sms/sent");

    public static void insertSentText(Context context, String address, String body) {
        ContentValues values = new ContentValues();
        values.put("address", address);
        values.put("body", body);
        values.put("read", 1);
        values.put("type", 2); // CallLog.Calls.OUTGOING_TYPE
        context.getContentResolver().insert(URI_SENT, values);
    }

    public static boolean isDefaultApp(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = context.getSystemService(RoleManager.class);
            boolean isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_SMS);
            if (isRoleAvailable) {
                // check if app is already holding default SMS app role
                return roleManager.isRoleHeld(RoleManager.ROLE_SMS);
            } else return false;
        } else {
            try {
                // If the device doesn't support Telephony.Sms (i.e. tablet) getDefaultSmsPackage will be null.
                final String smsPackage = Telephony.Sms.getDefaultSmsPackage(context);
                return smsPackage == null || smsPackage.equals(BuildConfig.APPLICATION_ID);
            } catch (SecurityException e) {
                // some Samsung devices/tablets want permission GET_TASKS o.O
                return true;
            }
        }
    }

    public static void requestDefaultSmsApp(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = activity.getSystemService(RoleManager.class);
            // check if the app is having permission to be as default SMS
            boolean isRoleAvailable = roleManager.isRoleAvailable(RoleManager.ROLE_SMS);
            if (isRoleAvailable && !roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                Intent roleRequestIntent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS);
                activity.startActivityForResult(roleRequestIntent, REQ_CODE_DEFAULT_SMS_APP);
            }
        } else {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, BuildConfig.APPLICATION_ID);
            activity.startActivityForResult(intent, REQ_CODE_DEFAULT_SMS_APP);
        }
    }

    public static void launchSystemMessagingApp(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
