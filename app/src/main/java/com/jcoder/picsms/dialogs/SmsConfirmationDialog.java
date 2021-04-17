package com.jcoder.picsms.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.SmsDetailAdapter;
import com.jcoder.picsms.databinding.DialogSmsConfirmationBinding;
import com.jcoder.picsms.decorations.SpacingItemDecoration;
import com.jcoder.picsms.models.SmsDetailModel;
import com.jcoder.picsms.preferences.PreferenceUtils;
import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.ClipboardUtils;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.SMSUtil;
import com.jcoder.picsms.utils.TextLayoutErrorRemover;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SmsConfirmationDialog extends BottomSheetDialogFragment {

    private DialogSmsConfirmationBinding binding;
    private OnSendClickedListener onSendClickedListener;

    final List<SmsDetailModel> smsDetails = new ArrayList<>();
    private final String DEFAULT_SIM = "Default SIM";
    private final ArrayList<String> SIMs = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSmsConfirmationBinding.bind(inflater.inflate(R.layout.dialog_sms_confirmation, container, false));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addDetailItems();

        binding.recycler.setNestedScrollingEnabled(false);
        SpacingItemDecoration decoration = new SpacingItemDecoration(1, SpacingItemDecoration.toPx(requireContext(), 6), true);
        binding.recycler.addItemDecoration(decoration);
        SmsDetailAdapter adapter = new SmsDetailAdapter(smsDetails);
        binding.recycler.setAdapter(adapter);

        binding.btnDefaultSmsAppLearnMore.setOnClickListener(v -> showDefaultSmsAppNoticeDialog());
        binding.btnSetAsDefaultSmsApp.setOnClickListener(v -> SMSUtil.requestDefaultSmsApp(requireActivity()));
        binding.switchDoNotSaveMsg.setEnabled(SMSUtil.isDefaultApp(requireContext()));
        binding.switchDoNotSaveMsg.setChecked(PreferenceUtils.doNotSaveSentMessagesEnabled(requireContext()));
        binding.switchDoNotSaveMsg.setOnCheckedChangeListener((buttonView, isChecked) -> PreferenceUtils.setDoNotSaveSentMessages(requireContext(), isChecked));

        binding.edt.addTextChangedListener(new TextLayoutErrorRemover(binding.edtLayout));
        binding.edtLayout.setEndIconOnClickListener(v -> {
            String text = ClipboardUtils.getTextFromClipboard(requireContext());
            if (!TextUtils.isEmpty(text) && Patterns.PHONE.matcher(text).matches()) {
                binding.edt.setText(text);
                binding.edt.setSelection(text.length());
            } else binding.edtLayout.setError(getString(R.string.invalid_phone));
        });

        binding.btnCancel.setOnClickListener(v -> {
            hideKeyboard();
            dismiss();
        });

        if (onSendClickedListener != null)
            binding.btnSendAll.setOnClickListener(v -> checkPhoneNumber());

        binding.btnHowSmsWorksLearnMore.setOnClickListener(v -> showHowSmsWorksDialog());
        setupSIMAdapter();

        if (PreferenceUtils.rememberPhoneNumberEnabled(requireContext())) {
            String phoneNumber = PreferenceUtils.getPhoneNumber(requireContext());
            if (phoneNumber != null) binding.edt.setText(phoneNumber);
        }

        if (PreferenceUtils.rememberSimSlotEnabled(requireContext())) {
            int simSlot = PreferenceUtils.getSimSlotPosition(requireContext());
            if (simSlot != -1 && SIMs.size() >= simSlot) {
                binding.actvSIM.setText(SIMs.get(simSlot), false);
            }
        }
    }

    private void showDefaultSmsAppNoticeDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.default_sms_app_notice)
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }

    private void addDetailItems() {
        smsDetails.add(
                new SmsDetailModel(
                        getString(R.string.one_chunk),
                        String.format(Locale.getDefault(), getString(R.string.up_to_n_characters), Constants.MAX_CHARACTERS_PER_CHUNK),
                        ContextCompat.getColor(requireContext(), R.color.number_badge_color)
                )
        );

        smsDetails.add(
                new SmsDetailModel(
                        getString(R.string.one_sms),
                        String.format(Locale.getDefault(), getString(R.string.up_to_n_characters), Constants.MAX_CHARACTERS_PER_SMS),
                        ContextCompat.getColor(requireContext(), R.color.number_badge_color)
                )
        );

        int smsCount = Constants.list.length;
        smsDetails.add(
                new SmsDetailModel(
                        getString(R.string.total_sms),
                        String.format(Locale.getDefault(), "%d", smsCount),
                        -1
                )
        );

        int lastSmsChunks = ChunkUtils.getChunkCount(Constants.list[smsCount - 1]);
        int totalChunks = smsCount == 1 ? lastSmsChunks : ((smsCount - 1) * 8) + lastSmsChunks;

        smsDetails.add(
                new SmsDetailModel(
                        getString(R.string.total_chunks),
                        String.format(Locale.getDefault(), "%d", totalChunks),
                        -1
                )
        );
    }

    private void showHowSmsWorksDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.how_sms_works_title)
                .setMessage(R.string.how_sms_works_message)
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("MissingPermission")
    private List<SubscriptionInfo> getSubscriptions() {
        SubscriptionManager localSubscriptionManager = (SubscriptionManager) requireContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        return localSubscriptionManager.getActiveSubscriptionInfoList();
    }

    private void setupSIMAdapter() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            List<SubscriptionInfo> subscriptions = getSubscriptions();
            // subscriptions return null when SIM is not inserted
            if (subscriptions != null && subscriptions.size() >= 2) {
                for (SubscriptionInfo info : subscriptions) {
                    SIMs.add(info.getCarrierName().toString());
                }
            } else SIMs.add(DEFAULT_SIM);
        } else SIMs.add(DEFAULT_SIM);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, SIMs);
        binding.actvSIM.setAdapter(adapter);
    }

    private void checkPhoneNumber() {
        String phoneNumber = Objects.requireNonNull(binding.edt.getText()).toString().trim();
        if (!TextUtils.isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches()) {
            hideKeyboard();

            String sim = binding.actvSIM.getText().toString();
            int simState = TextUtils.equals(sim, DEFAULT_SIM) ? -1 : SIMs.indexOf(sim);

            if (PreferenceUtils.rememberPhoneNumberEnabled(requireContext()))
                PreferenceUtils.setPhoneNumber(requireContext(), phoneNumber);
            if (PreferenceUtils.rememberSimSlotEnabled(requireContext()))
                PreferenceUtils.setSimSlot(requireContext(), simState);

            onSendClickedListener.onSendClicked(phoneNumber, simState);
            dismiss();
        } else binding.edtLayout.setError(getString(R.string.invalid_phone));
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (getView() != null) {
            View view = getView().getRootView();
            if (view != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public interface OnSendClickedListener {
        void onSendClicked(String phoneNumber, int simState);
    }

    public void setOnSendClickedListener(OnSendClickedListener onSendClickedListener) {
        this.onSendClickedListener = onSendClickedListener;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.switchDoNotSaveMsg.setEnabled(SMSUtil.isDefaultApp(requireContext()));
    }


}
