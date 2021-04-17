package com.jcoder.picsms.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.jcoder.picsms.R;
import com.jcoder.picsms.databinding.DialogSendingSmsBinding;
import com.jcoder.picsms.ui.sendsms.SendingSmsViewModel;

import java.util.Locale;
import java.util.Objects;

public class SendingSMSDialog extends BottomSheetDialogFragment {

    private DialogSendingSmsBinding binding;
    private SendingSmsViewModel model;
    private OnCancelledListener onCancelledListener;
    private final String phoneNumber;

    private int maxSmsProgress;
    private int currentChunkCount;

    private long cancelPressedTime;

    public SendingSMSDialog(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSendingSmsBinding.bind(inflater.inflate(R.layout.dialog_sending_sms, container, false));
        model = new ViewModelProvider(requireActivity()).get(SendingSmsViewModel.class);
        setCancelable(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        maxSmsProgress = SendingSmsViewModel.getMaxProgress();
        currentChunkCount = Objects.requireNonNull(model.getCurrentChunkCount().getValue());
        int smsProgress = Objects.requireNonNull(model.getProgress().getValue());
        int chunkProgress = Objects.requireNonNull(model.getCurrentChunk().getValue());

        binding.smsProgress.setMax(maxSmsProgress);
        updateSmsProgress(smsProgress);
        binding.chunkProgress.setMax(currentChunkCount);
        updateChunkProgress(chunkProgress);

        binding.message.setText(String.format(getString(R.string.sending_sms), phoneNumber));
        binding.smsCount.setText(String.format(Locale.getDefault(), "%d/%d", 0, maxSmsProgress));

        model.getProgress().observe(getViewLifecycleOwner(), this::updateSmsProgress);
        model.getCurrentChunk().observe(getViewLifecycleOwner(), this::updateChunkProgress);

        model.getCurrentChunkCount().observe(getViewLifecycleOwner(), chunkCount -> {
            this.currentChunkCount = chunkCount;
            binding.chunkProgress.setMax(chunkCount);
        });

        binding.btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());

    }

    private void updateChunkProgress(int currentChunk) {
        setProgress(binding.chunkProgress, currentChunk);
        int percent = (currentChunk * 100) / currentChunkCount;
        binding.chunkPercent.setText(String.format(Locale.getDefault(), "%d%%", percent));
        binding.chunkCount.setText(String.format(Locale.getDefault(), "%d/%d", currentChunk, currentChunkCount));

    }

    private void updateSmsProgress(int progress) {
        setProgress(binding.smsProgress, progress);
        int percent = (progress * 100) / maxSmsProgress;

        binding.smsPercent.setText(String.format(Locale.getDefault(), "%d%%", percent));
        binding.smsCount.setText(String.format(Locale.getDefault(), "%d/%d", progress, maxSmsProgress));

    }

    private void showCancelConfirmationDialog() {
        if (System.currentTimeMillis() - cancelPressedTime > 2000) {
            binding.tvDoubleTabWarning.setVisibility(View.VISIBLE);
            new Handler(Looper.myLooper()).postDelayed(() -> binding.tvDoubleTabWarning.setVisibility(View.GONE), 2000);
            cancelPressedTime = System.currentTimeMillis();
        } else {
            dismiss();
            onCancelledListener.onCancelled();
        }
    }

    private void setProgress(LinearProgressIndicator indicator, int progress) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            indicator.setProgress(progress, true);
        } else indicator.setProgress(progress);
    }

    public interface OnCancelledListener {
        void onCancelled();
    }

    public void setOnCancelledListener(OnCancelledListener onCancelledListener) {
        this.onCancelledListener = onCancelledListener;
    }
}
