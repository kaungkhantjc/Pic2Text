package com.jcoder.picsms.ui.sendsms;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.TextAdapter;
import com.jcoder.picsms.async.SaveToPictureLibraryTask;
import com.jcoder.picsms.async.TaskRunner;
import com.jcoder.picsms.databinding.ActivitySendSmsBinding;
import com.jcoder.picsms.databinding.DialogEditorBinding;
import com.jcoder.picsms.dialogs.LoadingDialog;
import com.jcoder.picsms.dialogs.SendingSMSDialog;
import com.jcoder.picsms.dialogs.SmsConfirmationDialog;
import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.preferences.PreferenceUtils;
import com.jcoder.picsms.ui.BaseActivity;
import com.jcoder.picsms.ui.library.PictureLibraryActivity;
import com.jcoder.picsms.ui.picture.PictureActivity;
import com.jcoder.picsms.ui.settings.SettingsActivity;
import com.jcoder.picsms.ui.web.WebActivity;
import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.ClipboardUtils;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.MIUIUtils;
import com.jcoder.picsms.utils.PermissionUtil;
import com.jcoder.picsms.utils.SMSUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SendSmsActivity extends BaseActivity {

    /**
     * If user open from library {@link PictureLibraryActivity}, app will not ask to save picture
     */
    public static final String EXTRA_ENCODING_TYPE = "EXTRA_ENCODING_TYPE";
    public static final String EXTRA_OPEN_FROM_LIBRARY = "EXTRA_OPEN_FROM_LIBRARY";
    public static final String EXTRA_FILE_PATH = "EXTRA_FILE_PATH";
    public static final String EXTRA_SENDING_SMS_POSITION = "EXTRA_SENDING_SMS_POSITION";

    private ActivitySendSmsBinding binding;
    private boolean alreadySavedToPictureLibrary;

    /**
     * To listen SMS SENT pending intent
     */
    private final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private int sendingSmsPosition = 0;
    private SendingSMSDialog sendingSMSDialog;

    private TextAdapter adapter;
    private final ArrayList<Boolean> sentStates = new ArrayList<>();

    private String phoneNumber;
    private int simState;
    private SendingSmsViewModel sendingSmsViewModel;
    private final TaskRunner taskRunner = new TaskRunner();

    private boolean saveSentMessages;

    private final int REQ_CODE = 123;
    PermissionUtil.PermissionListener permissionListener = new PermissionUtil.PermissionListener() {
        @Override
        public void onPermissionGranted() {
            showDetailDialog();
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
            REQ_CODE,
            permissionListener,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendSmsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // EXTRA_ENCODING_TYPE is essential when the codes need to be saved
        if (!getIntent().hasExtra(EXTRA_ENCODING_TYPE))
            throw new RuntimeException("EXTRA_ENCODING_TYPE is essential when the codes need to be saved");

        if (Constants.text == null || Constants.list == null) finish();

        sendingSmsViewModel = new ViewModelProvider(this).get(SendingSmsViewModel.class);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        String totalSMS = String.format(Locale.getDefault(), getString(R.string.total_sms_count), Constants.list.length);
        actionBar.setTitle(totalSMS);

        // sending sms position = last sent sms count (because position start with 0)
        sendingSmsPosition = getIntent().getIntExtra(EXTRA_SENDING_SMS_POSITION, 0);

        for (int i = 0; i < Constants.list.length; i++) {
            sentStates.add(i < sendingSmsPosition);
        }

        binding.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        adapter = new TextAdapter(this, sentStates);
        binding.recycler.setAdapter(adapter);

        adapter.setOnCopyClickedListener(text -> {
            ClipboardUtils.copy(this, text);
            Snackbar.make(binding.getRoot(), R.string.text_copied, 1000).show();
        });

        adapter.setOnItemLongClickedListener(this::showCodeDialog);

        binding.btnCopyAll.setOnClickListener(v -> {
            ClipboardUtils.copy(this, Constants.text);
            Snackbar.make(binding.getRoot(), R.string.text_copied, 1000).show();
        });

        binding.btnSendAll.setOnClickListener(v -> checkRequiredPermissions());
        binding.btnEditSentSmsPosition.setOnClickListener(v -> showEditSentSmsPositionDialog());
        syncSmsSentProgressBar(false);
    }

    private void showEditSentSmsPositionDialog() {
        DialogEditorBinding binding = DialogEditorBinding.inflate(getLayoutInflater());
        new MaterialAlertDialogBuilder(this)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_ok, ((dialog, which) -> {
                    String input = Objects.requireNonNull(binding.edt.getText()).toString();
                    if (!input.isEmpty()) {
                        int pos = Integer.parseInt(input);
                        if (pos >= 0 && pos <= Constants.list.length) {
                            sendingSmsPosition = pos;
                            syncSmsSentProgressBar(true);
                        } else {
                            Snackbar.make(this.binding.getRoot(), R.string.invalid_sms_position, 2000).show();
                        }
                    }
                }))
                .show();

        binding.edt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.edtLayout.setHint(R.string.sent_sms_position);
        binding.edt.setText(String.valueOf(sendingSmsPosition));

    }

    private void syncSmsSentProgressBar(boolean alsoSyncList) {
        int max = Constants.list.length;

        String percent = String.format(Locale.getDefault(), "%d%%", (sendingSmsPosition * 100) / max);
        String numberProgress = String.format(Locale.getDefault(), "%1$d/%2$d", sendingSmsPosition, max);

        binding.progressSmsSent.setMax(max);
        binding.tvNumberProgress.setText(numberProgress);
        binding.tvPercent.setText(percent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            binding.progressSmsSent.setProgress(sendingSmsPosition, true);
        else binding.progressSmsSent.setProgress(sendingSmsPosition);

        if (alsoSyncList) {
            for (int i = 0; i < sentStates.size(); i++) {
                sentStates.set(i, i < sendingSmsPosition);
            }
            adapter.notifyItemRangeChanged(0, sentStates.size());
        }
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

    private void checkRequiredPermissions() {
        if (MIUIUtils.isMIUI()) {
            if (permissionUtil.permissionsGranted()) showDetailDialog();
            else showMIUIWarningDialog();
        } else {
            if (permissionUtil.permissionsGranted()) showDetailDialog();
            else if (permissionUtil.shouldShowAlert()) showPermissionAlert();
            else permissionUtil.requestPermissions();
        }
    }

    private void showMIUIWarningDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.miui_warning_title)
                .setMessage(R.string.miui_permission_send_sms_read_phone_state_message)
                .setPositiveButton(R.string.btn_ok, ((dialog, which) -> permissionUtil.openPermissionSettings()))
                .show();
    }

    private void showSaveToCodeLibDialog(boolean exitAfterSaved) {
        if (getIntent().hasExtra(EXTRA_OPEN_FROM_LIBRARY) && getIntent().hasExtra(EXTRA_FILE_PATH)) {
            saveToCodeLibrary(null, exitAfterSaved);
        } else {
            DialogEditorBinding binding = DialogEditorBinding.inflate(getLayoutInflater());
            String predefinedFileName = String.valueOf(System.currentTimeMillis());

            new MaterialAlertDialogBuilder(this)
                    .setView(binding.getRoot())
                    .setNegativeButton(R.string.btn_cancel, null)
                    .setPositiveButton(R.string.btn_ok, ((dialog, which) -> {
                        String fileName = Objects.requireNonNull(binding.edt.getText()).toString().trim();
                        saveToCodeLibrary(fileName.isEmpty() ? predefinedFileName : fileName, exitAfterSaved);
                    }))
                    .show();

            binding.edtLayout.setHint(R.string.picture_library_file_name);
            binding.edt.setText(predefinedFileName);
        }
    }

    private void saveToCodeLibrary(@Nullable String fileName, boolean exitAfterSaved) {
        LoadingDialog loadingDialog = new LoadingDialog(getString(R.string.please_wait_while_saving));
        loadingDialog.setOnCancelClickListener(() -> {
            taskRunner.shutdown();
            File file = new File(getExternalFilesDir(null), String.format("%s.json", fileName));
            if (file.exists()) //noinspection ResultOfMethodCallIgnored
                file.delete();
        });
        loadingDialog.show(getSupportFragmentManager(), LoadingDialog.class.toString());

        boolean shouldOverride = fileName == null && getIntent().hasExtra(EXTRA_FILE_PATH);
        File file = shouldOverride ?
                new File(getIntent().getStringExtra(EXTRA_FILE_PATH)) :
                new File(getExternalFilesDir(null), String.format("%s.json", fileName));

        taskRunner.execute(
                new SaveToPictureLibraryTask(
                        this,
                        file,
                        shouldOverride,
                        EncodingType.valueOf(getIntent().getStringExtra(EXTRA_ENCODING_TYPE)),
                        sendingSmsPosition),
                result -> {
                    alreadySavedToPictureLibrary = true;
                    loadingDialog.dismiss();
                    if (Objects.requireNonNull(result.first)) {
                        Snackbar.make(binding.getRoot(), R.string.saved_to_picture_library, 2000).show();
                        if (exitAfterSaved) {
                            setResult(RESULT_OK);
                            finish();
                        }
                    } else new MaterialAlertDialogBuilder(this)
                            .setMessage(result.second)
                            .setPositiveButton(R.string.btn_ok, null)
                            .show();
                });
    }

    private void launchPictureActivity() {
        Intent intent = new Intent(this, PictureActivity.class);
        intent.putExtra(PictureActivity.EXTRA_TITLE, getString(R.string.menu_preview));
        intent.putExtra(PictureActivity.EXTRA_ENCODING_TYPE, getIntent().getStringExtra(EXTRA_ENCODING_TYPE));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_sms_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) onBackPressed();
        else if (id == R.id.send_sms_menu_preview) launchPictureActivity();
        else if (id == R.id.send_sms_menu_all_text)
            startActivity(new Intent(this, WebActivity.class));
        else if (id == R.id.send_sms_menu_save_to_code_library) showSaveToCodeLibDialog(false);
        else if (id == R.id.send_sms_menu_settings)
            startActivity(new Intent(this, SettingsActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Constants.text = null;
        Constants.list = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        this.permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (getIntent().hasExtra(EXTRA_OPEN_FROM_LIBRARY)) {
            if (alreadySavedToPictureLibrary || sendingSmsPosition == getIntent().getIntExtra(EXTRA_SENDING_SMS_POSITION, 0)) {
                finish();
            } else {
                saveToCodeLibrary(null, true);
            }
        } else if (!alreadySavedToPictureLibrary && PreferenceUtils.askToSaveInPictureLibEnabled(this)) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(R.string.ask_to_save_in_picture_lib_message)
                    .setNegativeButton(R.string.btn_no, ((dialog, which) -> finish()))
                    .setNeutralButton(R.string.btn_do_not_ask, ((dialog, which) -> {
                        PreferenceUtils.disableAskToSaveInPictureLibrary(this);
                        finish();
                    }))
                    .setPositiveButton(R.string.btn_yes, ((dialog, which) -> showSaveToCodeLibDialog(true)))
                    .show();
        } else finish();
    }

    private void showDetailDialog() {
        if (sendingSmsPosition >= Constants.list.length) {
            Snackbar.make(binding.getRoot(), R.string.all_sms_sent, 2000).show();
        } else {
            SmsConfirmationDialog dialog = new SmsConfirmationDialog();
            dialog.setOnSendClickedListener(this::initSendingSms);
            getSupportFragmentManager().beginTransaction().add(dialog, SmsConfirmationDialog.class.getName()).commit();
        }
    }

    private void showPermissionAlert() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.permission_send_sms_read_phone_state_title)
                .setMessage(R.string.permission_send_sms_read_phone_state_message)
                .setNegativeButton(R.string.btn_cancel, null)
                .setPositiveButton(R.string.btn_ok, ((dialog, which) -> permissionUtil.requestPermissions()))
                .show();
    }

    private void initSendingSms(String phoneNumber, int simState) {
        saveSentMessages = SMSUtil.isDefaultApp(this) && !PreferenceUtils.doNotSaveSentMessagesEnabled(this);
        currentChunk = 0;
        currentChunkCount = 0;
        this.phoneNumber = phoneNumber;
        this.simState = simState;

        SendingSmsViewModel.setMaxProgress(Constants.list.length);

        sendingSMSDialog = new SendingSMSDialog(phoneNumber);
        sendingSMSDialog.setOnCancelledListener(() -> cancelSendingSMSProgress(String.format(Locale.getDefault(), getString(R.string.sending_sms_cancelled_message), getString(R.string.sms_error_reason_user_cancelled), sendingSmsPosition + 1)));
        getSupportFragmentManager().beginTransaction().add(sendingSMSDialog, SendingSMSDialog.class.getName()).commit();

        registerReceiver(smsSentBroadcast, new IntentFilter(SENT_SMS_ACTION));

        sendingSmsViewModel.setCurrentChunkCount(8); // just for temporary, will update at sendMessage method
        sendingSmsViewModel.setCurrentChunk(0);
        sendingSmsViewModel.setProgress(sendingSmsPosition);

        sendMessage(Constants.list[sendingSmsPosition]);
    }

    private int currentChunk;
    private int currentChunkCount;

    BroadcastReceiver smsSentBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    currentChunk++;
                    sendingSmsViewModel.setCurrentChunkCount(currentChunkCount);
                    sendingSmsViewModel.setCurrentChunk(currentChunk);

                    if (currentChunk == currentChunkCount) {
                        currentChunk = 0;

                        if (saveSentMessages) {
                            SMSUtil.insertSentText(SendSmsActivity.this, phoneNumber, Constants.list[sendingSmsPosition]);
                        }

                        sendingSmsPosition++;
                        sendingSmsViewModel.setProgress(sendingSmsPosition);

                        syncSmsSentProgressBar(true);

                        if (sendingSmsPosition == Constants.list.length) {
                            new Handler(Looper.myLooper()).postDelayed(() -> {
                                if (sendingSMSDialog != null) sendingSMSDialog.dismiss();
                                new MaterialAlertDialogBuilder(SendSmsActivity.this)
                                        .setMessage(R.string.all_sms_sent)
                                        .setPositiveButton(R.string.btn_ok, null)
                                        .show();
                            }, 500);
                        } else {
                            sendMessage(Constants.list[sendingSmsPosition]);
                        }
                    }

                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    cancelSendingSMSProgress(String.format(Locale.getDefault(), getString(R.string.sending_sms_cancelled_message), getString(R.string.sms_error_reason_generic_failure), sendingSmsPosition + 1));
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    cancelSendingSMSProgress(String.format(Locale.getDefault(), getString(R.string.sending_sms_cancelled_message), getString(R.string.sms_error_reason_no_service), sendingSmsPosition + 1));
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    cancelSendingSMSProgress(String.format(Locale.getDefault(), getString(R.string.sending_sms_cancelled_message), getString(R.string.sms_error_reason_null_pdu), sendingSmsPosition + 1));
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    cancelSendingSMSProgress(String.format(Locale.getDefault(), getString(R.string.sending_sms_cancelled_message), getString(R.string.sms_error_reason_radio_off), sendingSmsPosition + 1));
                    break;
            }
        }
    };

    private void cancelSendingSMSProgress(String message) {
        try {
            unregisterReceiver(smsSentBroadcast);
        } catch (IllegalArgumentException ignored) {
        }
        if (sendingSMSDialog != null) sendingSMSDialog.dismiss();
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setNegativeButton(R.string.btn_ok, null)
                .setPositiveButton(getString(R.string.btn_resend), ((dialog, which) -> initSendingSms(phoneNumber, simState)))
                .show();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("MissingPermission")
    private List<SubscriptionInfo> getSubscriptions() {
        SubscriptionManager localSubscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        return localSubscriptionManager.getActiveSubscriptionInfoList();
    }

    private void sendMessage(String text) {
        text = ChunkUtils.getSortedText(sendingSmsPosition, text);
        SmsManager smsManager;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            int subscriptionId = simState == -1 ? SmsManager.getDefaultSmsSubscriptionId() : getSubscriptions().get(simState).getSubscriptionId();
            smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
        } else {
            smsManager = SmsManager.getDefault();
        }

        ArrayList<String> msgArray = smsManager.divideMessage(text);

        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<>();

        Intent intent = new Intent(SENT_SMS_ACTION);
        this.currentChunkCount = msgArray.size();
        sendingSmsViewModel.setCurrentChunkCount(currentChunkCount);

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                intent, 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 11,
                new Intent("DELIVERED_SMS_ACTION"), 0);

        for (String ignored : msgArray) {
            sentPendingIntents.add(sentPI);
            deliveredPendingIntents.add(deliveredPI);
        }
        smsManager.sendMultipartTextMessage(phoneNumber, null, msgArray, sentPendingIntents, deliveredPendingIntents);
    }

}