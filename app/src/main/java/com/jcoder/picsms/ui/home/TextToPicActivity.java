package com.jcoder.picsms.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.EncodingAdapter;
import com.jcoder.picsms.adapters.TextToPicAdapter;
import com.jcoder.picsms.async.JoinCodesTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.async.Text2CodeListTask;
import com.jcoder.picsms.databinding.ActivityTextToPicBinding;
import com.jcoder.picsms.decorations.SpacingItemDecoration;
import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.models.CodePart;
import com.jcoder.picsms.models.Encoding;
import com.jcoder.picsms.ui.BaseActivity;
import com.jcoder.picsms.ui.picksms.PickSmsActivity;
import com.jcoder.picsms.ui.picture.PictureActivity;
import com.jcoder.picsms.ui.settings.SettingsActivity;
import com.jcoder.picsms.utils.ClipboardUtils;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.TextLayoutErrorRemover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class TextToPicActivity extends BaseActivity {

    private ActivityTextToPicBinding binding;
    private final ArrayList<CodePart> codePartList = new ArrayList<>();
    private TextToPicAdapter adapter;
    private final TaskRunner taskRunner = new TaskRunner();

    private EncodingAdapter encodingAdapter;
    private final ArrayList<Encoding> encodings = new ArrayList<>();

    public static final int REQ_CODE_PICK_SMS = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextToPicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.btn_text_to_picture);
        }

        SpacingItemDecoration decoration = new SpacingItemDecoration(1, SpacingItemDecoration.toPx(this, 10), true);
        binding.recycler.addItemDecoration(decoration);
        adapter = new TextToPicAdapter(codePartList);
        binding.recycler.setAdapter(adapter);

        binding.edt.addTextChangedListener(new TextLayoutErrorRemover(binding.edtLayout));
        binding.edtLayout.setEndIconOnClickListener(v -> pasteFromClipboard());
        binding.fabAddToList.setOnClickListener(v -> {
            String text = Objects.requireNonNull(binding.edt.getText()).toString();
            taskRunner.execute(new Text2CodeListTask(text), result -> {
                if (result.size() > 0) for (CodePart codePart : result) addToList(codePart);
                else binding.edtLayout.setError(getString(R.string.invalid_code_part));
            });
        });

        adapter.setOnDeleteClickedListener(this::showDeleteConfirmationDialog);
        adapter.setOnItemLongClickedListener(this::showCodeDialog);

        encodings.add(new Encoding(EncodingType.AUTO_DETECT, getString(R.string.encoding_auto_detect), getString(R.string.encoding_recommended)));
        encodings.add(new Encoding(EncodingType.OPTIMIZED_BASE91_V1_1_0, getString(R.string.encoding_optimized_base91_v1_1_0), getString(R.string.encoding_optimized_base91_version)));
        encodings.add(new Encoding(EncodingType.BASE64, getString(R.string.encoding_base64_v1_0_0), getString(R.string.encoding_base64_v1_0_0_message)));

        encodingAdapter = new EncodingAdapter(encodings);
        binding.actvEncoding.setAdapter(encodingAdapter);
        binding.actvEncoding.setText(encodings.get(0).getEncodingName(), false);

        binding.fabTextToPic.setOnClickListener(v -> launchResultActivity());
        binding.btnAddFromMessages.setOnClickListener(v -> addFromMessages());
    }

    private void addFromMessages() {
        startActivityForResult(new Intent(this, PickSmsActivity.class), REQ_CODE_PICK_SMS);
    }

    private void launchResultActivity() {
        new TaskRunner().execute(new JoinCodesTask(codePartList), result -> {
            Intent intent = new Intent(this, PictureActivity.class);
            intent.putExtra(PictureActivity.EXTRA_ENCODING_TYPE, encodingAdapter.getEncodingType().toString());
            startActivity(intent);
        });
    }

    private void showDeleteConfirmationDialog(int position, String orderName) {
        new MaterialAlertDialogBuilder(TextToPicActivity.this)
                .setMessage(String.format(getString(R.string.code_delete_confirmation), orderName))
                .setNegativeButton(R.string.btn_no, null)
                .setPositiveButton(R.string.btn_yes, ((dialog, which) -> {
                    codePartList.remove(position);
                    adapter.notifyItemRemoved(position);

                    if (codePartList.size() == 0) binding.tvEmpty.setVisibility(View.VISIBLE);
                }))
                .show();
    }

    private void showCodeDialog(String text) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(text)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_copy, ((dialog, which) -> {
                    ClipboardUtils.copy(this, text);
                    Snackbar.make(binding.getRoot(), R.string.text_copied, 1000).show();
                }))
                .show();
    }

    private void convertText2CodeList(String text) {
        taskRunner.execute(new Text2CodeListTask(text), result -> {
            for (CodePart codePart : result) addToList(codePart);
        });
    }

    private void addToList(CodePart model) {
        final ArrayList<CodePart> tmpCodeList = new ArrayList<>(codePartList);
        tmpCodeList.add(model);
        Collections.sort(tmpCodeList, (o1, o2) -> Long.compare(o1.getCodeOrder(), o2.getCodeOrder()));

        int index = tmpCodeList.indexOf(model);
        codePartList.add(index, model);
        adapter.notifyItemInserted(index);

        if (binding.edt.getText() != null) binding.edt.getText().clear();
        binding.tvEmpty.setVisibility(View.GONE);
    }

    private void pasteFromClipboard() {
        binding.edt.setText(ClipboardUtils.getTextFromClipboard(this));
    }

    private void showDeleteAllConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_all_confirmation)
                .setMessage(R.string.code_delete_all_message)
                .setNegativeButton(R.string.btn_no, null)
                .setPositiveButton(R.string.btn_yes, ((dialog, which) -> {
                    int itemCount = codePartList.size();
                    codePartList.clear();
                    adapter.notifyItemRangeRemoved(0, itemCount);
                    binding.tvEmpty.setVisibility(View.VISIBLE);
                }))
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_to_picture_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) finish();
        else if (id == R.id.text_to_picture_menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.text_to_picture_menu_delete_all)
            showDeleteAllConfirmationDialog();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_PICK_SMS && resultCode == RESULT_OK) {
            for (String code : Constants.list) {
                convertText2CodeList(code);
            }
            Constants.list = null;
        }
    }
}