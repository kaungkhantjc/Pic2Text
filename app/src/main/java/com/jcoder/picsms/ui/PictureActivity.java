package com.jcoder.picsms.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.R;
import com.jcoder.picsms.async.SavePictureTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.async.TextToBitmapTask;
import com.jcoder.picsms.databinding.ActivityPictureBinding;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.PermissionUtil;

import java.util.Objects;

public class PictureActivity extends AppCompatActivity {

    public static final String EXTRA_TITLE = "EXTRA_TITLE";
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

        new TaskRunner().execute(new TextToBitmapTask(Constants.text), result -> {
            if (result == null)
                new MaterialAlertDialogBuilder(this)
                        .setMessage("Cannot decode text into bytes.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            else Glide.with(this)
                    .load(result)
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.zoomView);
        });

    }

    private void showPermissionAlert() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Need Storage Access")
                .setMessage("App needs Storage Access to save picture.")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> permissionUtil.requestPermissions()))
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
        taskRunner.execute(new TextToBitmapTask(Constants.text), bytes ->
                taskRunner.execute(new SavePictureTask(this, bytes), result -> {
                    if (result != null) showErrorDialog(result);
                    else Snackbar.make(binding.getRoot(), R.string.saved_picture, 2000).show();
                }));
    }

    private void showErrorDialog(String result) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(result)
                .setPositiveButton(android.R.string.ok, null)
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
        else if (id == R.id.picture_menu_all_text) startActivity(new Intent(this, WebActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}