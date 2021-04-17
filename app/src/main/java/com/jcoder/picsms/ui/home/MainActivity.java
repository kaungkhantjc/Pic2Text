package com.jcoder.picsms.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.WhatIsNewAdapter;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.async.WhatIsNewReaderTask;
import com.jcoder.picsms.databinding.ActivityMainBinding;
import com.jcoder.picsms.databinding.DialogWhatIsNewBinding;
import com.jcoder.picsms.preferences.PreferenceUtils;
import com.jcoder.picsms.ui.BaseActivity;
import com.jcoder.picsms.ui.library.PictureLibraryActivity;
import com.jcoder.picsms.ui.settings.SettingsActivity;
import com.jcoder.picsms.utils.SMSUtil;

public class MainActivity extends BaseActivity {

    private AlertDialog defaultSmsAppNoticeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnPic2Text.setOnClickListener(v -> launchActivity(PicToTextActivity.class));
        binding.btnText2Pic.setOnClickListener(v -> launchActivity(TextToPicActivity.class));
        binding.btnPictureLibrary.setOnClickListener(v -> launchActivity(PictureLibraryActivity.class));

        if (PreferenceUtils.shouldShowWhatIsNew(this)) showWhatIsNewDialog();
    }

    private void showWhatIsNewDialog() {
        new TaskRunner().execute(new WhatIsNewReaderTask(getAssets()), result -> {
            DialogWhatIsNewBinding binding = DialogWhatIsNewBinding.inflate(getLayoutInflater());
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.what_is_new_title)
                    .setView(binding.getRoot())
                    .setPositiveButton(R.string.btn_ok, ((dialog, which) -> PreferenceUtils.disableShouldShowWhatIsNew(this)))
                    .show();

            binding.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
            binding.recycler.setAdapter(new WhatIsNewAdapter(result));
        });
    }

    private void launchActivity(Class<?> activity) {
        startActivity(new Intent(this, activity));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_menu_github) openGithub();
        else if (id == R.id.main_menu_settings)
            startActivity(new Intent(this, SettingsActivity.class));
        return super.onOptionsItemSelected(item);
    }

    private void openGithub() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://github.com/kaungkhantjc/Pic2Text/"));
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SMSUtil.isDefaultApp(this)) {
            defaultSmsAppNoticeDialog = new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.default_sms_app_notice_home)
                    .setPositiveButton(R.string.btn_open_system_messaging_app, ((dialog, which) -> SMSUtil.launchSystemMessagingApp(this)))
                    .setNeutralButton(R.string.btn_ok, null)
                    .show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (defaultSmsAppNoticeDialog != null && defaultSmsAppNoticeDialog.isShowing())
            defaultSmsAppNoticeDialog.dismiss();
    }
}