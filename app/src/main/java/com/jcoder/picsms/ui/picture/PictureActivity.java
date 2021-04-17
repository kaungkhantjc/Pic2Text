package com.jcoder.picsms.ui.picture;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.R;
import com.jcoder.picsms.async.SavePictureTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.async.TextToBytesTask;
import com.jcoder.picsms.databinding.ActivityPictureBinding;
import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.ui.BaseActivity;
import com.jcoder.picsms.ui.web.WebActivity;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.PermissionUtil;

import java.util.Objects;

public class PictureActivity extends BaseActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
    public static final String EXTRA_ENCODING_TYPE = "EXTRA_ENCODING_TYPE";
    private ActivityPictureBinding binding;

    private final PermissionUtil permissionUtil = PermissionUtil.getInstance(
            this,
            125,
            new PermissionUtil.PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    savePicture();
                }

                @Override
                public void onShouldShowAlert() {
                    showPermissionAlert();
                }

                @Override
                public void onDoNotAskAgain() {
                    permissionUtil.openSettings();
                }
            },
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPictureBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (getIntent().hasExtra(EXTRA_TITLE))
            getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_TITLE));

        if (!getIntent().hasExtra(EXTRA_ENCODING_TYPE))
            throw new RuntimeException("EXTRA_ENCODING_TYPE is required to decode text to bytes.");

        EncodingType encodingType = EncodingType.valueOf(getIntent().getStringExtra(EXTRA_ENCODING_TYPE));
        new TaskRunner().execute(new TextToBytesTask(encodingType, Constants.text), result -> {
            if (result == null)
                new MaterialAlertDialogBuilder(this)
                        .setMessage(R.string.cannot_decode_text_to_pic)
                        .setPositiveButton(R.string.btn_ok, null)
                        .show();
            else Glide.with(this)
                    .load(result)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.zoomView);
        });

    }

    private void showPermissionAlert() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.permission_need_storage_title)
                .setMessage(R.string.permission_need_storage_message2)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_ok, ((dialog, which) -> permissionUtil.requestPermissions()))
                .show();
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || permissionUtil.permissionsGranted())
            savePicture();
        else if (permissionUtil.shouldShowAlert()) showPermissionAlert();
        else permissionUtil.requestPermissions();
    }

    private void savePicture() {
        TaskRunner taskRunner = new TaskRunner();
        taskRunner.execute(new TextToBytesTask(EncodingType.AUTO_DETECT, Constants.text), bytes ->
                taskRunner.execute(new SavePictureTask(this, bytes), result -> {
                    if (result != null) showErrorDialog(result);
                    else Snackbar.make(binding.getRoot(), R.string.saved_picture, 2000).show();
                }));
    }

    private void showErrorDialog(String result) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(result)
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picture_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) onBackPressed();
        else if (id == R.id.picture_menu_save) checkStoragePermission();
        else if (id == R.id.picture_menu_all_text)
            startActivity(new Intent(this, WebActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}