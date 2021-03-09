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
import com.jcoder.picsms.R;
import com.jcoder.picsms.adapters.SmsDetailAdapter;
import com.jcoder.picsms.databinding.DialogSmsConfirmationBinding;
import com.jcoder.picsms.decorations.SpacingItemDecoration;
import com.jcoder.picsms.models.SmsDetailModel;
import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.ClipboardUtils;
import com.jcoder.picsms.utils.Constants;
import com.jcoder.picsms.utils.TextLayoutErrorRemover;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SmsConfirmationDialog extends BottomSheetDialogFragment {

    private DialogSmsConfirmationBinding binding;
    private OnSendClickedListener onSendClickedListener;

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

        final List<SmsDetailModel> smsDetails = new ArrayList<>();
        smsDetails.add(
                new SmsDetailModel(
                        "1 Chunk",
                        String.format(Locale.ENGLISH, "up to %d characters", Constants.MAX_CHARACTERS_PER_CHUNK),
                        ContextCompat.getColor(requireContext(), R.color.number_badge_color)
                )
        );
        smsDetails.add(
                new SmsDetailModel(
                        "1 SMS",
                        String.format(Locale.ENGLISH, "up to %d characters", Constants.MAX_CHARACTERS_PER_SMS + 3),
                        ContextCompat.getColor(requireContext(), R.color.number_badge_color)
                )
        );

        int smsCount = Constants.list.length;
        smsDetails.add(
                new SmsDetailModel(
                        "Total SMS",
                        String.valueOf(smsCount),
                        -1
                )
        );

        int lastSmsChunks = ChunkUtils.getChunkCount(Constants.list[smsCount - 1]);
        int totalChunks = smsCount == 1 ? lastSmsChunks : ((smsCount - 1) * 8) + lastSmsChunks;

        smsDetails.add(
                new SmsDetailModel(
                        "Total Chunks",
                        String.valueOf(totalChunks),
                        -1
                )
        );

        binding.recycler.setNestedScrollingEnabled(false);
        SpacingItemDecoration decoration = new SpacingItemDecoration(1, SpacingItemDecoration.toPx(requireContext(), 10), true);
        binding.recycler.addItemDecoration(decoration);
        SmsDetailAdapter adapter = new SmsDetailAdapter(smsDetails);
        binding.recycler.setAdapter(adapter);

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

        binding.actvSIM.setOnClickListener(v -> selectSIMCard());
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("MissingPermission")
    private List<SubscriptionInfo> getSubscriptions() {
        SubscriptionManager localSubscriptionManager = (SubscriptionManager) requireContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        return localSubscriptionManager.getActiveSubscriptionInfoList();
    }


    private void selectSIMCard() {
        binding.actvSIM.setOnClickListener(v -> binding.actvSIM.showDropDown());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            List<SubscriptionInfo> subscriptions = getSubscriptions();
            if (subscriptions.size() >= 2) {
                for (SubscriptionInfo info : subscriptions) {
                    SIMs.add(info.getCarrierName().toString());
                }
            } else SIMs.add(DEFAULT_SIM);
        } else SIMs.add(DEFAULT_SIM);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, SIMs);
        binding.actvSIM.setAdapter(adapter);

        binding.actvSIM.performClick();
    }


    private void checkPhoneNumber() {
        String phoneNumber = Objects.requireNonNull(binding.edt.getText()).toString().trim();
        if (!TextUtils.isEmpty(phoneNumber) && Patterns.PHONE.matcher(phoneNumber).matches()) {
            hideKeyboard();

            String sim = binding.actvSIM.getText().toString();
            int simState = TextUtils.equals(sim, DEFAULT_SIM) ? -1 : SIMs.indexOf(sim);
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
}
