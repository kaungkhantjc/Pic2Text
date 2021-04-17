package com.jcoder.picsms.ui.library;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.PictureLibraryAdapter;
import com.jcoder.picsms.async.DeleteAllInPictureLibraryTask;
import com.jcoder.picsms.async.ReadPictureLibraryTask;
import com.jcoder.picsms.async.SplitTextTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.databinding.ActivityPictureLibraryBinding;
import com.jcoder.picsms.decorations.SpacingItemDecoration;
import com.jcoder.picsms.models.PictureLibraryItem;
import com.jcoder.picsms.ui.BaseActivity;
import com.jcoder.picsms.ui.sendsms.SendSmsActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class PictureLibraryActivity extends BaseActivity {

    private static final int REQ_CODE_UPDATE_ITEMS = 100;
    private ActivityPictureLibraryBinding binding;
    private final ArrayList<PictureLibraryItem> pictureLibraryItems = new ArrayList<>();
    private PictureLibraryAdapter adapter;

    private int spanCount;
    private static final int SPAN_PORTRAIT = 2;
    private static final int SPAN_LANDSCAPE = 3;
    private int firstVisibleItemPosition;

    private final TaskRunner taskRunner = new TaskRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPictureLibraryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.title_activity_picture_library);

        spanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                SPAN_PORTRAIT : SPAN_LANDSCAPE;

        addItemDecoration();
        setLayoutManager();

        adapter = new PictureLibraryAdapter(this, pictureLibraryItems);
        binding.recycler.setAdapter(adapter);
        binding.swipe.setOnRefreshListener(() -> {
            this.firstVisibleItemPosition = 0;
            loadItemsFromCodeLibrary();
        });
        adapter.setOnItemClickListener(this::openFileNow);

        registerForContextMenu(binding.recycler);
        loadItemsFromCodeLibrary();
    }

    private void setLayoutManager() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        binding.recycler.setLayoutManager(gridLayoutManager);
    }

    private void loadItemsFromCodeLibrary() {
        binding.swipe.setRefreshing(true);
        taskRunner.execute(new ReadPictureLibraryTask(getExternalFilesDir(null)), result -> {
            binding.swipe.setRefreshing(false);
            this.pictureLibraryItems.clear();
            this.pictureLibraryItems.addAll(result);
            adapter.notifyDataSetChanged();

            binding.recycler.scrollToPosition(firstVisibleItemPosition);
        });
    }

    private SpacingItemDecoration getDecoration() {
        return new SpacingItemDecoration(spanCount, SpacingItemDecoration.toPx(this, 10), true);
    }

    private void addItemDecoration() {
        binding.recycler.addItemDecoration(getDecoration());
    }

    private void removeItemDecoration() {
        while (binding.recycler.getItemDecorationCount() > 1) {
            if (binding.recycler.getItemDecorationAt(1) instanceof SpacingItemDecoration) {
                binding.recycler.removeItemDecorationAt(1);
            }
        }
    }

    private void showDeleteAllConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_all_confirmation)
                .setMessage(R.string.picture_library_delete_all_message)
                .setNegativeButton(R.string.btn_no, null)
                .setPositiveButton(R.string.btn_yes, ((dialog, which) -> taskRunner.execute(new DeleteAllInPictureLibraryTask(pictureLibraryItems), result -> {
                    int count = pictureLibraryItems.size();
                    pictureLibraryItems.clear();
                    adapter.notifyItemRangeRemoved(0, count);
                    Snackbar.make(binding.getRoot(), R.string.all_files_deleted, 2000).show();
                })))
                .show();
    }

    private void openFileNow(int position) {
        firstVisibleItemPosition = ((GridLayoutManager) binding.recycler.getLayoutManager()).findFirstVisibleItemPosition();
        PictureLibraryItem item = pictureLibraryItems.get(position);
        taskRunner.execute(new SplitTextTask(item.getText()), result -> {
            Intent intent = new Intent(this, SendSmsActivity.class);
            intent.putExtra(SendSmsActivity.EXTRA_OPEN_FROM_LIBRARY, true);
            intent.putExtra(SendSmsActivity.EXTRA_ENCODING_TYPE, item.getEncodingType().toString());
            intent.putExtra(SendSmsActivity.EXTRA_FILE_PATH, item.getFilePath());
            intent.putExtra(SendSmsActivity.EXTRA_SENDING_SMS_POSITION, item.getSendingSmsPosition());
            startActivityForResult(intent, REQ_CODE_UPDATE_ITEMS);
        });
    }

    private void showDeleteFileConfirmationDialog(int position) {
        PictureLibraryItem item = pictureLibraryItems.get(position);
        new MaterialAlertDialogBuilder(this)
                .setMessage(String.format(getString(R.string.picture_library_delete_file_message), item.getFileName()))
                .setNegativeButton(R.string.btn_no, null)
                .setPositiveButton(R.string.btn_yes, ((dialog, which) -> {
                    boolean deleted = new File(item.getFilePath()).delete();
                    if (deleted) {
                        pictureLibraryItems.remove(position);
                        adapter.notifyItemRemoved(position);
                        Snackbar.make(binding.getRoot(), String.format(getString(R.string.file_deleted), item.getFileName()), 2000).show();
                    } else
                        Snackbar.make(binding.getRoot(), R.string.cannot_delete_file, 2000).show();
                }))
                .show();
    }

    private void showPathDialog(int position) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(pictureLibraryItems.get(position).getFilePath())
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picture_library_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) finish();
        else if (id == R.id.menu_code_library_delete_all) showDeleteAllConfirmationDialog();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case PictureLibraryAdapter.MENU_OPEN:
                openFileNow(adapter.getPosition());
                break;

            case PictureLibraryAdapter.MENU_DELETE:
                showDeleteFileConfirmationDialog(adapter.getPosition());
                break;

            case PictureLibraryAdapter.MENU_PATH:
                showPathDialog(adapter.getPosition());
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        spanCount = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT ? SPAN_PORTRAIT : SPAN_LANDSCAPE;
        removeItemDecoration();
        addItemDecoration();
        setLayoutManager();

        binding.recycler.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_UPDATE_ITEMS && resultCode == RESULT_OK) {
            loadItemsFromCodeLibrary();
        }
    }
}