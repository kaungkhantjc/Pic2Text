package com.jcoder.picsms.ui.home;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jcoder.picsms.async.BitmapToTextTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.databinding.ActivityPicToTextBinding;
import com.jcoder.picsms.listeners.SeekBarListener;
import com.jcoder.picsms.ui.TextActivity;
import com.jcoder.picsms.utils.PermissionUtil;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Locale;

public class PicToTextActivity extends AppCompatActivity {

    private ActivityPicToTextBinding binding;
    private static final int REQ_CODE_PICK_IMAGE = 100;
    private static final int REQ_CODE_READ_EXTERNAL_STORAGE = 123;

    PermissionUtil.PermissionListener permissionListener = new PermissionUtil.PermissionListener() {
        @Override
        public void onPermissionGranted() {
            pickImage();
        }

        @Override
        public void onShouldShowAlert() {
            showPermissionAlert();
        }

        @Override
        public void onDoNotAskAgain() {
            permissionUtil.openSettings();
        }
    };

    private final PermissionUtil permissionUtil = PermissionUtil.getInstance(
            this,
            REQ_CODE_READ_EXTERNAL_STORAGE,
            permissionListener,
            Manifest.permission.READ_EXTERNAL_STORAGE
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPicToTextBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.seekPicResolution.setOnSeekBarChangeListener(new SeekBarListener(
                progress -> binding.tvProgressResolution.setText(String.format(Locale.getDefault(), "%d%%", progress))));

        binding.seekPicQuality.setOnSeekBarChangeListener(new SeekBarListener(
                progress -> binding.tvProgressQuality.setText(String.format(Locale.getDefault(), "%d%%", progress))));

        binding.fabPic2Text.setOnClickListener(v -> checkStoragePermission());
    }

    private void pickImage() {
        startActivityForResult(getPickImageIntent(), REQ_CODE_PICK_IMAGE);
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || permissionUtil.permissionsGranted())
            pickImage();
        else if (permissionUtil.shouldShowAlert()) showPermissionAlert();
        else permissionUtil.requestPermissions();
    }

    private void showPermissionAlert() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Need Storage Access")
                .setMessage("App needs Storage Access to pick picture.")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> permissionUtil.requestPermissions()))
                .show();
    }

    private Intent getPickImageIntent() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent chooserIntent = Intent.createChooser(pickIntent, "Select Picture");
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INTENT, pickIntent);

        return chooserIntent;
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            if (imageUri != null) {
                new TaskRunner().execute(
                        new BitmapToTextTask(
                                getBitmapFromUri(imageUri),
                                binding.seekPicResolution.getProgress(),
                                binding.seekPicQuality.getProgress(),
                                binding.switchOptimizeResolution.isChecked()
                        ),
                        result -> {
                            if (result != null)
                                new MaterialAlertDialogBuilder(PicToTextActivity.this)
                                        .setMessage(result)
                                        .setPositiveButton(android.R.string.ok, null)
                                        .show();
                            else showResult();
                        });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showResult() {
        Intent intent = new Intent(this, TextActivity.class);
        startActivity(intent);
    }


}