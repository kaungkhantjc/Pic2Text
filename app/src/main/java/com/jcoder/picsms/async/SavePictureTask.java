package com.jcoder.picsms.async;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.jcoder.picsms.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.concurrent.Callable;

public class SavePictureTask implements Callable<String> {

    private final Context context;
    private final byte[] bytes;

    public SavePictureTask(Context context, byte[] bytes) {
        this.context = context;
        this.bytes = bytes;
    }

    @Override
    public String call() {
        try {
            boolean saved = writeFile(bytes);
            return saved ? null : context.getString(R.string.error_saving_picture);
        } catch (IOException e) {
            return e.toString();
        }
    }

    private boolean writeFile(byte[] bytes) throws IOException {

        String name = String.format(Locale.ENGLISH, "%d.jpg", System.currentTimeMillis());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/" + context.getString(R.string.app_name));
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            OutputStream fos = resolver.openOutputStream(imageUri);

            if (fos != null) {
                fos.write(bytes);
                fos.flush();
                fos.close();

                return true;
            } else return false;

        } else {
            //noinspection deprecation
            String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + context.getString(R.string.app_name);

            File file = new File(imagesDir);
            if (!file.exists())
                if (!file.mkdir()) return false;

            File image = new File(imagesDir, name);
            OutputStream fos = new FileOutputStream(image);

            fos.write(bytes);
            fos.flush();
            fos.close();

            MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, new String[]{"image/jpg"}, null);
            return true;
        }


    }

}
