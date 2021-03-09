package com.jcoder.picsms.ui;

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
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.TextAdapter;
import com.jcoder.picsms.databinding.ActivityTextBinding;
import com.jcoder.picsms.dialogs.SendingSMSDialog;
import com.jcoder.picsms.dialogs.SmsConfirmationDialog;
import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.ClipboardUtils;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.MIUIUtils;
import com.jcoder.picsms.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TextActivity extends AppCompatActivity {

    private ActivityTextBinding binding;

    private final String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    private int messagePosition = 0;
    private SendingSMSDialog sendingSMSDialog;

    private String phoneNumber;
    private int simState;
    private TextViewModel textViewModel;

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
        binding = ActivityTextBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;

        actionBar.setDisplayHomeAsUpEnabled(true);
        String totalSMS = String.format(Locale.ENGLISH, "Total SMS : %d", Constants.list.length);
        actionBar.setTitle(totalSMS);


        binding.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        TextAdapter adapter = new TextAdapter();
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
                .setMessage(R.string.miui_warning_send_sms_read_phone_state)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> permissionUtil.openPermissionSettings()))
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.text_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) onBackPressed();
        else if (id == R.id.text_menu_preview) launchPictureActivity();
        else if (id == R.id.text_menu_all_text) startActivity(new Intent(this, WebActivity.class));
        return super.onOptionsItemSelected(item);
    }

    private void launchPictureActivity() {
        Intent intent = new Intent(this, PictureActivity.class);
        intent.putExtra(PictureActivity.EXTRA_TITLE, getString(R.string.preview));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        Constants.text = null;
        Constants.list = null;
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        this.permissionUtil.onRequestPermissionsResult(requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showDetailDialog() {
        SmsConfirmationDialog dialog = new SmsConfirmationDialog();
        dialog.setOnSendClickedListener(this::sendAllSMS);
        getSupportFragmentManager().beginTransaction().add(dialog, SmsConfirmationDialog.class.getName()).commit();
    }


    private void showPermissionAlert() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Need Some Permissions")
                .setMessage("App needs \n- Send SMS Permission (to send messages in background automatically)\n\n" +
                        "- Read Phone State Permission (to select default SIM for SMS).")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, ((dialog, which) -> permissionUtil.requestPermissions()))
                .show();
    }

    private void sendAllSMS(String phoneNumber, int simState) {
        this.phoneNumber = phoneNumber;
        this.simState = simState;

        textViewModel = new ViewModelProvider(this).get(TextViewModel.class);
        TextViewModel.setMaxProgress(Constants.list.length);

        sendingSMSDialog = new SendingSMSDialog(phoneNumber);
        sendingSMSDialog.setOnCancelledListener(() -> cancelSendingSMSProgress(String.format(Locale.ENGLISH, "Sending SMS progress cancelled.\nReason : User cancelled. \nCurrent SMS position : %d\nCurrent Chunk position : %d", messagePosition, currentChunk)));
        getSupportFragmentManager().beginTransaction().add(sendingSMSDialog, SendingSMSDialog.class.getName()).commit();

        registerReceiver(smsSentBroadcast, new IntentFilter(SENT_SMS_ACTION));
        sendMessage(Constants.list[messagePosition]);
    }

    private int currentChunk;
    private int currentChunkCount;

    BroadcastReceiver smsSentBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            switch (getResultCode()) {
                case Activity.RESULT_OK:

                    currentChunk++;
                    textViewModel.setCurrentChunkCount(currentChunkCount);
                    textViewModel.setCurrentChunk(currentChunk);

                    if (currentChunk == currentChunkCount) {
                        currentChunk = 0;
                        textViewModel.setProgress(messagePosition + 1);

                        if (messagePosition == Constants.list.length - 1) {
                            new Handler().postDelayed(() -> {
                                sendingSMSDialog.dismiss();
                                showInfoDialog("All SMS have been sent.");
                            }, 500);
                        } else {
                            messagePosition++;
                            sendMessage(Constants.list[messagePosition]);
                        }
                    }


                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    cancelSendingSMSProgress(String.format(Locale.ENGLISH, "Sending SMS progress cancelled.\nReason : Generic failure\nFailure SMS position : %d", messagePosition));
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    cancelSendingSMSProgress(String.format(Locale.ENGLISH, "Sending SMS progress cancelled.\nReason : No service\nFailure SMS position : %d", messagePosition));
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    cancelSendingSMSProgress(String.format(Locale.ENGLISH, "Sending SMS progress cancelled.\nReason : NULL PDU\nFailure SMS position : %d", messagePosition));
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    cancelSendingSMSProgress(String.format(Locale.ENGLISH, "Sending SMS progress cancelled.\nReason : Radio Off\nFailure SMS position : %d", messagePosition));
                    break;
            }
        }
    };

    private void cancelSendingSMSProgress(String message) {
        try {
            unregisterReceiver(smsSentBroadcast);
        } catch (IllegalArgumentException ignored) {
        }
        showInfoDialog(message);
    }

    private void showInfoDialog(String message) {
        new MaterialAlertDialogBuilder(this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("MissingPermission")
    private List<SubscriptionInfo> getSubscriptions() {
        SubscriptionManager localSubscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        return localSubscriptionManager.getActiveSubscriptionInfoList();
    }

    private void sendMessage(String text) {
        text = ChunkUtils.getSortedText(messagePosition, text);
        SmsManager smsManager;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            int subscriptionId = simState == -1 ? SmsManager.getDefaultSmsSubscriptionId() : getSubscriptions().get(simState).getSubscriptionId();
            smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);

        } else
            smsManager = SmsManager.getDefault();

        ArrayList<String> msgArray = smsManager.divideMessage(text);

        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<>();

        Intent intent = new Intent(SENT_SMS_ACTION);
        this.currentChunkCount = msgArray.size();

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