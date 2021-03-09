package com.jcoder.picsms.ui;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.PickSmsChatAdapter;
import com.jcoder.picsms.adapters.PickSmsHomeAdapter;
import com.jcoder.picsms.async.ReadSmsTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.databinding.ActivityPickSmsBinding;
import com.jcoder.picsms.models.Conversation;
import com.jcoder.picsms.models.Message;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.MIUIUtils;
import com.jcoder.picsms.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PickSmsActivity extends AppCompatActivity {

    private ActivityPickSmsBinding binding;

    private ArrayList<Conversation> conversations;
    private final ArrayList<Message> selectedMessages = new ArrayList<>();

    private final PermissionUtil permissionUtil = PermissionUtil.getInstance(
            this,
            126,
            new PermissionUtil.PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    loadConversations();
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
            Manifest.permission.READ_SMS
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPickSmsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        checkReadSmsPermission();
        binding.swipe.setOnRefreshListener(this::checkReadSmsPermission);
        binding.btnHome.setOnClickListener(v -> checkReadSmsPermission());

    }

    private void checkReadSmsPermission() {
        if (MIUIUtils.isMIUI()) {
            if (permissionUtil.permissionsGranted()) loadConversations();
            else showMIUIWarningDialog();
        } else {
            if (permissionUtil.permissionsGranted()) loadConversations();
            else showNormalPermissionAlertDialog();
        }
    }

    private void showNormalPermissionAlertDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.read_sms_permission_title)
                .setMessage(R.string.read_sms_permission_message)
                .setPositiveButton(getString(R.string.open_settings), ((dialog, which) -> permissionUtil.openPermissionSettings()))
                .show();
    }

    private void showMIUIWarningDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.miui_warning_title)
                .setMessage(R.string.miui_warning_read_sms)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> permissionUtil.openPermissionSettings()))
                .show();
    }

    private void loadConversations() {
        binding.ivArrow.setVisibility(View.GONE);
        binding.tvAddress.setVisibility(View.GONE);

        if (conversations == null) {
            binding.btnHome.setEnabled(false);
            binding.swipe.setRefreshing(true);

            new TaskRunner().execute(new ReadSmsTask(getContentResolver()), result -> {
                this.conversations = result;
                binding.btnHome.setEnabled(true);
                binding.swipe.setRefreshing(false);
                setupHomeAdapter();
            });
        } else setupHomeAdapter();
    }

    private void setupHomeAdapter() {
        binding.swipe.setRefreshing(false);
        setActionBarTitle(getString(R.string.pick_sms));
        selectedMessages.clear();

        PickSmsHomeAdapter homeAdapter = new PickSmsHomeAdapter(conversations);
        binding.recycler.setAdapter(homeAdapter);
        binding.recycler.scheduleLayoutAnimation();

        homeAdapter.setOnItemClickedListener(this::showMessages);
    }

    private void showMessages(String address, ArrayList<Message> messages) {
        binding.ivArrow.setVisibility(View.VISIBLE);
        binding.tvAddress.setVisibility(View.VISIBLE);
        binding.tvAddress.setText(address);
        setActionBarTitle(0);

        ArrayList<Boolean> checkedStates = new ArrayList<>();
        for (Message ignored : messages) checkedStates.add(false);

        PickSmsChatAdapter chatAdapter = new PickSmsChatAdapter(this, messages, checkedStates);
        binding.recycler.setAdapter(chatAdapter);
        binding.recycler.scheduleLayoutAnimation();


        chatAdapter.setOnItemCheckChangedListener((pos, message) -> {
            boolean isChecked = selectedMessages.contains(message);

            if (isChecked) selectedMessages.remove(message);
            else selectedMessages.add(message);
            setActionBarTitle(selectedMessages.size());

            checkedStates.set(pos, !isChecked);
            chatAdapter.notifyItemChanged(pos);

        });

    }

    private void setActionBarTitle(int selectedCount) {
        setActionBarTitle(String.format(Locale.ENGLISH, getString(R.string._selected), selectedCount));
    }

    private void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    private void showPermissionAlert() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Need Read SMS Messages Permission")
                .setMessage("App needs Read SMS Messages Permission to pick messages.")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> permissionUtil.requestPermissions()))
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pic_sms_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) finish();
        else if (id == R.id.pic_sms_menu_done) pickSelectedMessages();
        return super.onOptionsItemSelected(item);
    }

    private void pickSelectedMessages() {
        if (selectedMessages.size() > 0) {
            List<String> codes = new ArrayList<>();
            for (Message message : selectedMessages) {
                codes.add(message.getBody());
            }
            Constants.list = codes.toArray(new String[0]);
            setResult(RESULT_OK);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == permissionUtil.REQ_CODE_FOR_MIUI) {
            if (permissionUtil.permissionsGranted()) loadConversations();
            else if (MIUIUtils.isMIUI()) showMIUIWarningDialog();
            else showNormalPermissionAlertDialog();
        }
    }
}