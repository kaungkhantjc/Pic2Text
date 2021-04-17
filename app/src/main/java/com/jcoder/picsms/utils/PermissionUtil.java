package com.jcoder.picsms.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

public class PermissionUtil {

    private final Activity activity;
    private final int REQUEST_CODE;
    private final PermissionListener listener;
    private final String[] permissions;

    public final int REQ_CODE_FOR_MIUI = 200;

    public PermissionUtil(Activity activity, int REQUEST_CODE, PermissionListener listener, String[] permissions) {
        this.activity = activity;
        this.REQUEST_CODE = REQUEST_CODE;
        this.listener = listener;
        this.permissions = permissions;
    }

    public static PermissionUtil getInstance(Activity activity, int REQUEST_CODE, PermissionListener permissionListener, String... permissions) {
        return new PermissionUtil(activity, REQUEST_CODE, permissionListener, permissions);
    }

    public boolean permissionsGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public boolean shouldShowAlert() {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                return true;
        }
        return false;
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
    }

    public void openSettings() {
        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity.getPackageName()));
        settingIntent.addCategory(Intent.CATEGORY_DEFAULT);
        settingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(settingIntent);
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (listener != null && requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listener.onPermissionGranted();
            } else {
                if (shouldShowAlert()) {
                    listener.onShouldShowAlert();
                } else {
                    listener.onDoNotAskAgain();
                }
            }
        }
    }

    public void openPermissionSettings() {
        try {
            // MiUi 8+
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", activity.getPackageName());
            localIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            localIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivityForResult(localIntent, REQ_CODE_FOR_MIUI);
        } catch (Exception ignored1) {
            openSettings();
        }
    }

    public interface PermissionListener {
        void onPermissionGranted();

        void onShouldShowAlert();

        void onDoNotAskAgain();
    }
}
