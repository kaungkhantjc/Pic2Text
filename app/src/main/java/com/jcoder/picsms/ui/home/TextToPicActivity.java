package com.jcoder.picsms.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.TextToPicAdapter;
import com.jcoder.picsms.async.JoinCodesTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.databinding.ActivityTextToPicBinding;
import com.jcoder.picsms.decorations.SpacingItemDecoration;
import com.jcoder.picsms.models.TextToPicModel;
import com.jcoder.picsms.ui.PickSmsActivity;
import com.jcoder.picsms.ui.PictureActivity;
import com.jcoder.picsms.utils.ClipboardUtils;
import com.jcoder.picsms.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;

public class TextToPicActivity extends AppCompatActivity {

    private ActivityTextToPicBinding binding;
    private final ArrayList<TextToPicModel> codeList = new ArrayList<>();
    private TextToPicAdapter adapter;

    public static final int REQ_CODE_PICK_SMS = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTextToPicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SpacingItemDecoration decoration = new SpacingItemDecoration(1, SpacingItemDecoration.toPx(this, 10), true);
        binding.recycler.addItemDecoration(decoration);
        adapter = new TextToPicAdapter(codeList);
        binding.recycler.setAdapter(adapter);

        binding.edtLayout.setEndIconOnClickListener(v -> pasteFromClipboard());
        binding.fabAddToList.setOnClickListener(v -> checkCode(binding.edt.getText().toString()));

        adapter.setOnDeleteClickedListener(this::showDeleteConfirmationDialog);
        adapter.setOnItemLongClickedListener(this::showCodeDialog);

        binding.fabTextToPic.setOnClickListener(v -> launchResultActivity());
        binding.btnAddFromMessages.setOnClickListener(v -> addFromMessages());
    }

    private void addFromMessages() {
        startActivityForResult(new Intent(this, PickSmsActivity.class), REQ_CODE_PICK_SMS);
    }

    private void launchResultActivity() {
        new TaskRunner().execute(new JoinCodesTask(codeList), result -> startActivity(new Intent(this, PictureActivity.class)));
    }

    private void showDeleteConfirmationDialog(int position, String orderName) {
        new MaterialAlertDialogBuilder(TextToPicActivity.this)
                .setMessage(String.format(getString(R.string.code_delete_confirmation), orderName))
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.yes, ((dialog, which) -> {
                    codeList.remove(position);
                    adapter.notifyItemRemoved(position);

                    if (codeList.size() == 0) binding.tvEmpty.setVisibility(View.VISIBLE);
                }))
                .show();
    }

    private void showCodeDialog(String text) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(text)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.copy, ((dialog, which) -> {
                    ClipboardUtils.copy(this, text);
                    Snackbar.make(binding.getRoot(), R.string.text_copied, 1000).show();
                }))
                .show();
    }

    private void checkCode(String code) {
        String[] codeSplit = code.split("-");
        if (codeSplit.length > 2) {

            ArrayList<Integer> orderList = new ArrayList<>();
            ArrayList<String> cList = new ArrayList<>();

            int firstIndex = Integer.parseInt(codeSplit[0].replace("(", ""));
            orderList.add(firstIndex);

            for (int i = 1; i < codeSplit.length; i++) {
                String c = codeSplit[i];

                if (i == codeSplit.length - 1) {
                    // last loop
                    cList.add(c);
                } else {
                    String[] innerSplit = c.split("\\(");
                    cList.add(innerSplit[0]);
                    orderList.add(Integer.parseInt(innerSplit[1]));
                }
            }

            for (int j = 0; j < orderList.size(); j++) {
                addToList(new TextToPicModel(orderList.get(j), cList.get(j)));
            }

        } else if (codeSplit.length == 2)
            addToList(new TextToPicModel(Integer.parseInt(codeSplit[0].replace("(", "")), codeSplit[1]));
        else addToList(new TextToPicModel(System.currentTimeMillis(), code));
    }

    private void addToList(TextToPicModel model) {

        final ArrayList<TextToPicModel> tmpCodeList = new ArrayList<>(codeList);
        tmpCodeList.add(model);
        Collections.sort(tmpCodeList, (o1, o2) -> Long.compare(o1.getOrder(), o2.getOrder()));

        int index = tmpCodeList.indexOf(model);
        codeList.add(index, model);
        adapter.notifyItemInserted(index);

        if (binding.edt.getText() != null) binding.edt.getText().clear();
        binding.tvEmpty.setVisibility(View.GONE);
    }

    private void pasteFromClipboard() {
        binding.edt.setText(ClipboardUtils.getTextFromClipboard(this).replace("\n", ""));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_PICK_SMS && resultCode == RESULT_OK) {
            for (String code : Constants.list) {
                checkCode(code);
            }
            Constants.list = null;
        }
    }
}