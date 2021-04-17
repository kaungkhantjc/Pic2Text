package com.jcoder.picsms.ui.picksms;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.PickSmsChatAdapter;
import com.jcoder.picsms.adapters.PickSmsHomeAdapter;
import com.jcoder.picsms.async.AutoSelectSmsBetweenTask;
import com.jcoder.picsms.async.AutoSelectSmsTask;
import com.jcoder.picsms.async.ReadSmsTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.databinding.ActivityPickSmsBinding;
import com.jcoder.picsms.dialogs.LoadingDialog;
import com.jcoder.picsms.models.Conversation;
import com.jcoder.picsms.models.Message;
import com.jcoder.picsms.preferences.PreferenceUtils;
import com.jcoder.picsms.ui.BaseActivity;
import com.jcoder.picsms.utils.ClipboardUtils;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.MIUIUtils;
import com.jcoder.picsms.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PickSmsActivity extends BaseActivity {

    private ActivityPickSmsBinding binding;
    private MenuItem autoSelectMenu;
    private MenuItem doneMenu;
    private MenuItem autoSelectBetweenMenu;
    private MenuItem selectAllMenu;
    private MenuItem deselectAllMenu;

    private ArrayList<Conversation> conversations;
    private final ArrayList<Message> userMessages = new ArrayList<>();
    private final ArrayList<Boolean> checkedStates = new ArrayList<>();

    private final ArrayList<Message> selectedMessages = new ArrayList<>();
    private PickSmsChatAdapter chatAdapter;
    private DividerItemDecoration dividerItemDecoration;

    private int firstVisibleItemPositionOfConversation;

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
        setActionBarTitle(getString(R.string.title_activity_pick_sms));

        dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        checkReadSmsPermission();
        binding.swipe.setOnRefreshListener(() -> {
            conversations = null;
            firstVisibleItemPositionOfConversation = 0;
            checkReadSmsPermission();
        });
        binding.btnHome.setOnClickListener(v -> checkReadSmsPermission());
    }

    private void addDividerToRecycler() {
        binding.recycler.addItemDecoration(dividerItemDecoration);
    }

    private void removeDividerFromRecycler() {
        binding.recycler.removeItemDecoration(dividerItemDecoration);
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
                .setTitle(R.string.permission_read_sms_title)
                .setMessage(R.string.default_permission_read_sms_message)
                .setPositiveButton(getString(R.string.open_settings), ((dialog, which) -> permissionUtil.openPermissionSettings()))
                .show();
    }

    private void showMIUIWarningDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.miui_warning_title)
                .setMessage(R.string.miui_permission_read_sms)
                .setPositiveButton(R.string.btn_ok, ((dialog, which) -> permissionUtil.openPermissionSettings()))
                .show();
    }

    private void loadConversations() {
        binding.ivArrow.setVisibility(View.GONE);
        binding.tvAddress.setVisibility(View.GONE);

        if (conversations == null) {
            binding.btnHome.setEnabled(false);
            binding.swipe.setRefreshing(true);

            new TaskRunner().execute(new ReadSmsTask(getContentResolver()), result -> {
                conversations = result;
                binding.btnHome.setEnabled(true);
                binding.swipe.setRefreshing(false);
                setupHomeAdapter();
            });
        } else setupHomeAdapter();
    }

    private void setupHomeAdapter() {
        removeDividerFromRecycler();
        addDividerToRecycler();
        disableAllMenus();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recycler.setLayoutManager(layoutManager);

        binding.swipe.setEnabled(true);
        binding.swipe.setRefreshing(false);
        setActionBarTitle(getString(R.string.title_activity_pick_sms));
        selectedMessages.clear();

        PickSmsHomeAdapter homeAdapter = new PickSmsHomeAdapter(this, conversations);
        binding.recycler.setAdapter(homeAdapter);
        binding.recycler.scheduleLayoutAnimation();

        if (firstVisibleItemPositionOfConversation > 0)
            binding.recycler.scrollToPosition(firstVisibleItemPositionOfConversation);

        homeAdapter.setOnItemClickedListener(this::showMessages);
    }

    private void disableAllMenus() {
        if (autoSelectMenu != null)
            autoSelectMenu.setEnabled(false);
        if (doneMenu != null)
            doneMenu.setEnabled(false);
        if (autoSelectBetweenMenu != null)
            autoSelectBetweenMenu.setEnabled(false);
        if (selectAllMenu != null)
            selectAllMenu.setEnabled(false);
        if (deselectAllMenu != null)
            deselectAllMenu.setEnabled(false);
    }

    private void enableAllMenus() {
        autoSelectMenu.setEnabled(true);
        doneMenu.setEnabled(true);
        autoSelectBetweenMenu.setEnabled(true);
        selectAllMenu.setEnabled(true);
        deselectAllMenu.setEnabled(true);
    }

    private void showMessages(String address, ArrayList<Message> messages) {
        this.firstVisibleItemPositionOfConversation = ((LinearLayoutManager) Objects.requireNonNull(binding.recycler.getLayoutManager())).findFirstVisibleItemPosition();

        removeDividerFromRecycler();
        enableAllMenus();
        binding.swipe.setEnabled(false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true);
        binding.recycler.setLayoutManager(layoutManager);

        this.userMessages.clear();
        this.checkedStates.clear();
        this.userMessages.addAll(messages);
        for (Message ignored : userMessages) checkedStates.add(false);

        binding.ivArrow.setVisibility(View.VISIBLE);
        binding.tvAddress.setVisibility(View.VISIBLE);
        binding.tvAddress.setText(address);
        setActionBarTitle(0);

        chatAdapter = new PickSmsChatAdapter(this, userMessages, checkedStates);
        binding.recycler.setAdapter(chatAdapter);
        binding.recycler.scheduleLayoutAnimation();

        chatAdapter.setOnItemCheckChangedListener(new PickSmsChatAdapter.OnItemCheckChangedListener() {
            @Override
            public void onItemChecked(int position, Message message) {
                selectedMessages.add(message);
                setActionBarTitle(selectedMessages.size());
                checkedStates.set(position, true);
                chatAdapter.notifyItemChanged(position);
            }

            @Override
            public void onItemUnchecked(int position, Message message) {
                selectedMessages.remove(message);
                setActionBarTitle(selectedMessages.size());
                checkedStates.set(position, false);
                chatAdapter.notifyItemChanged(position);
            }
        });

        chatAdapter.setOnItemLongClickListener(body -> new MaterialAlertDialogBuilder(this)
                .setMessage(body)
                .setPositiveButton(R.string.btn_ok, null)
                .setNegativeButton(R.string.btn_copy, (dialog, which) -> ClipboardUtils.copy(this, body))
                .show());
    }

    private void setActionBarTitle(int selectedCount) {
        setActionBarTitle(String.format(Locale.ENGLISH, getString(R.string._selected), selectedCount));
    }

    private void setActionBarTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    private void showPermissionAlert() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.permission_read_sms_title)
                .setMessage(R.string.permission_read_sms_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_ok, ((dialog, which) -> permissionUtil.requestPermissions()))
                .show();
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

    private void autoSelectMessages() {
        TaskRunner taskRunner = new TaskRunner();
        LoadingDialog dialog = new LoadingDialog(getString(R.string.auto_selecting_messages));
        dialog.setOnCancelClickListener(taskRunner::shutdown);
        getSupportFragmentManager().beginTransaction().add(dialog, LoadingDialog.class.getName()).commit();

        taskRunner.execute(
                new AutoSelectSmsTask(
                        PreferenceUtils.getAutoSelectType(this),
                        PreferenceUtils.getRangeOfAutoSelect(this),
                        userMessages),
                result -> {
                    handleAutoSelectedResults(result.getAutoSelectedMessages(), result.getSelectedMessagePositions());
                    dialog.dismiss();
                });
    }

    private void handleAutoSelectedResults(ArrayList<Message> autoSelectedMessages, ArrayList<Integer> selectedMessagePositions) {
        if (autoSelectedMessages.size() > 0) {
            for (Integer i : selectedMessagePositions) {
                checkedStates.set(i, true);
            }
            Objects.requireNonNull(chatAdapter).notifyDataSetChanged();
            int lastIndex = selectedMessagePositions.size() - 1;
            binding.recycler.scrollToPosition(selectedMessagePositions.get(lastIndex));

            selectedMessages.clear();
            selectedMessages.addAll(autoSelectedMessages);
            setActionBarTitle(selectedMessages.size());
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.no_code_parts_found)
                    .setPositiveButton(R.string.btn_ok, null)
                    .show();
        }
    }

    private void showAutoSelectBetweenDialog() {
        Calendar calendar = Calendar.getInstance();
        long endDate = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_MONTH, -2); // last twoDay
        long selectionStartDate = calendar.getTimeInMillis();

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        CalendarConstraints.Builder constraintBuilder = new CalendarConstraints.Builder();
        constraintBuilder.setEnd(endDate);
        constraintBuilder.setOpenAt(endDate);
        constraintBuilder.setValidator(new CalendarConstraints.DateValidator() {
            @Override
            public boolean isValid(long date) {
                return date <= endDate;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
            }
        });
        builder.setCalendarConstraints(constraintBuilder.build());
        builder.setSelection(new Pair<>(selectionStartDate, endDate));
        MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> autoSelectBetween(Objects.requireNonNull(selection.first), Objects.requireNonNull(selection.second)));

        picker.show(getSupportFragmentManager(), picker.toString());
    }

    private void autoSelectBetween(long first, long second) {
        TaskRunner taskRunner = new TaskRunner();
        LoadingDialog dialog = new LoadingDialog(getString(R.string.auto_selecting_messages));
        dialog.setOnCancelClickListener(taskRunner::shutdown);
        getSupportFragmentManager().beginTransaction().add(dialog, LoadingDialog.class.getName()).commit();

        taskRunner.execute(
                new AutoSelectSmsBetweenTask(
                        PreferenceUtils.getAutoSelectType(this),
                        first,
                        second,
                        userMessages),
                result -> {
                    handleAutoSelectedResults(result.getAutoSelectedMessages(), result.getSelectedMessagePositions());
                    dialog.dismiss();
                });
    }

    private void selectAllMessages() {
        selectedMessages.clear();
        selectedMessages.addAll(userMessages);
        for (int i = 0; i < userMessages.size(); i++) {
            checkedStates.set(i, true);
        }
        setActionBarTitle(selectedMessages.size());
        chatAdapter.notifyItemRangeChanged(0, userMessages.size());
    }

    private void deselectAllMessages() {
        selectedMessages.clear();
        for (int i = 0; i < userMessages.size(); i++) {
            checkedStates.set(i, false);
        }
        setActionBarTitle(0);
        chatAdapter.notifyItemRangeChanged(0, userMessages.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pic_sms_menu, menu);
        autoSelectMenu = menu.findItem(R.id.pic_sms_menu_auto_select);
        doneMenu = menu.findItem(R.id.pic_sms_menu_done);
        autoSelectBetweenMenu = menu.findItem(R.id.pic_sms_menu_auto_select_between);
        selectAllMenu = menu.findItem(R.id.pic_sms_menu_select_all);
        deselectAllMenu = menu.findItem(R.id.pic_sms_menu_deselect_all);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) finish();
        else if (id == R.id.pic_sms_menu_done) pickSelectedMessages();
        else if (id == R.id.pic_sms_menu_auto_select) autoSelectMessages();
        else if (id == R.id.pic_sms_menu_auto_select_between) showAutoSelectBetweenDialog();
        else if (id == R.id.pic_sms_menu_select_all) selectAllMessages();
        else if (id == R.id.pic_sms_menu_deselect_all) deselectAllMessages();
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onBackPressed() {
        if (doneMenu != null && doneMenu.isEnabled()) checkReadSmsPermission();
        else super.onBackPressed();
    }
}