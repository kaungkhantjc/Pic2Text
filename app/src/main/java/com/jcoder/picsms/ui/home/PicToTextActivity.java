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
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.EncodingAdapter;
import com.jcoder.picsms.async.BytesToTextTask;
import com.jcoder.picsms.async.OptimizePictureTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.databinding.ActivityPicToTextBinding;
import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.listeners.SeekBarListener;
import com.jcoder.picsms.models.Encoding;
import com.jcoder.picsms.ui.BaseActivity;
import com.jcoder.picsms.ui.sendsms.SendSmsActivity;
import com.jcoder.picsms.ui.settings.SettingsActivity;
import com.jcoder.picsms.utils.PermissionUtil;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class PicToTextActivity extends BaseActivity {

    private ActivityPicToTextBinding binding;
    private static final int REQ_CODE_PICK_IMAGE = 100;
    private static final int REQ_CODE_READ_EXTERNAL_STORAGE = 123;

    private final TaskRunner taskRunner = new TaskRunner();
    private Uri pickedPictureUri;
    private byte[] optimizedPictureBytes;

    private EncodingAdapter adapter;
    private final ArrayList<Encoding> encodings = new ArrayList<>();

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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.btn_picture_to_text);
        }

        encodings.add(new Encoding(EncodingType.OPTIMIZED_BASE91_V1_1_0, getString(R.string.encoding_optimized_base91_v1_1_0), getString(R.string.encoding_optimized_base91_v1_1_0_message)));
        encodings.add(new Encoding(EncodingType.BASE64, getString(R.string.encoding_base64_v1_0_0), getString(R.string.encoding_base64_v1_0_0_message)));

        binding.seekPicResolution.setOnSeekBarChangeListener(new SeekBarListener(new SeekBarListener.Callback() {
            @Override
            public void onProgressChanged(int progress) {
                binding.tvProgressResolution.setText(String.format(Locale.getDefault(), "%d%%", progress));
            }

            @Override
            public void onStopTrackingTouch() {
                startOptimizedPictureTask();
            }
        }));

        binding.seekPicQuality.setOnSeekBarChangeListener(new SeekBarListener(new SeekBarListener.Callback() {
            @Override
            public void onProgressChanged(int progress) {
                binding.tvProgressQuality.setText(String.format(Locale.getDefault(), "%d%%", progress));
            }

            @Override
            public void onStopTrackingTouch() {
                startOptimizedPictureTask();
            }
        }));

        binding.btnPickPicture.setOnClickListener(v -> checkStoragePermission());
        binding.switchOptimizeResolution.setOnCheckedChangeListener((buttonView, isChecked) -> startOptimizedPictureTask());
        binding.fabPic2Text.setOnClickListener(v -> startBytesToCodesTask());

        adapter = new EncodingAdapter(encodings);
        binding.actvEncoding.setAdapter(adapter);
        binding.actvEncoding.setText(encodings.get(0).getEncodingName(), false);
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
                .setTitle(R.string.permission_need_storage_title)
                .setMessage(R.string.permission_need_storage_message1)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_ok, ((dialog, which) -> permissionUtil.requestPermissions()))
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picture_to_text_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) finish();
        if (id == R.id.picture_to_text_menu_settings)
            startActivity(new Intent(this, SettingsActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            pickedPictureUri = data.getData();
            startOptimizedPictureTask();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startOptimizedPictureTask() {
        if (pickedPictureUri != null) {
            taskRunner.execute(new OptimizePictureTask(
                    getBitmapFromUri(pickedPictureUri),
                    binding.seekPicResolution.getProgress(),
                    binding.seekPicQuality.getProgress(),
                    binding.switchOptimizeResolution.isChecked()
            ), result -> {
                this.optimizedPictureBytes = result;
                Glide.with(this)
                        .load(result)
                        .transform(new RoundedCorners(20))
                        .into(binding.ivPreview);
            });
        }
    }

    private void startBytesToCodesTask() {
        EncodingType encodingType = adapter.getEncodingType();

        if (optimizedPictureBytes == null) {
            Snackbar.make(binding.getRoot(), R.string.pick_a_picture_first, 2000).show();
        } else {
            taskRunner.execute(new BytesToTextTask(adapter.getEncodingType(), optimizedPictureBytes), result -> {
                if (result != null) {
                    new MaterialAlertDialogBuilder(PicToTextActivity.this)
                            .setMessage(result)
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                } else {
                    Intent intent = new Intent(this, SendSmsActivity.class);
                    intent.putExtra(SendSmsActivity.EXTRA_ENCODING_TYPE, encodingType.toString());
                    startActivity(intent);
                }
            });
        }
    }

}