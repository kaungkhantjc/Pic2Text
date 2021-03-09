package com.jcoder.picsms.dialogs;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.jcoder.picsms.R;
import com.jcoder.picsms.ui.TextViewModel;
import com.jcoder.picsms.databinding.DialogSendingSmsBinding;

import java.util.Locale;

public class SendingSMSDialog extends BottomSheetDialogFragment {

    private DialogSendingSmsBinding binding;
    private TextViewModel model;
    private OnCancelledListener onCancelledListener;

    private int currentChunkCount;
    private final String phoneNumber;

    private long cancelPressedTime;

    public SendingSMSDialog(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSendingSmsBinding.bind(inflater.inflate(R.layout.dialog_sending_sms, container, false));
        model = new ViewModelProvider(requireActivity()).get(TextViewModel.class);
        setCancelable(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int maxSmsProgress = TextViewModel.getMaxProgress();

        binding.message.setText(String.format(getString(R.string.sending_sms), phoneNumber));
        binding.smsCount.setText(String.format(Locale.ENGLISH, "%d/%d", 0, maxSmsProgress));

        binding.smsProgress.setMax(maxSmsProgress);
        model.getProgress().observe(getViewLifecycleOwner(), progress -> {
            setProgress(binding.smsProgress, progress);
            int percent = (progress * 100) / maxSmsProgress;

            binding.smsPercent.setText(String.format(Locale.ENGLISH, "%d%%", percent));
            binding.smsCount.setText(String.format(Locale.ENGLISH, "%d/%d", progress, maxSmsProgress));
        });

        model.getCurrentChunk().observe(getViewLifecycleOwner(), currentChunk -> {
            setProgress(binding.chunkProgress, currentChunk);

            int percent = (currentChunk * 100) / currentChunkCount;
            binding.chunkPercent.setText(String.format(Locale.ENGLISH, "%d%%", percent));
            binding.chunkCount.setText(String.format(Locale.ENGLISH, "%d/%d", currentChunk, currentChunkCount));

        });

        model.getCurrentChunkCount().observe(getViewLifecycleOwner(), chunkCount -> {
            binding.chunkProgress.setMax(chunkCount);
            this.currentChunkCount = chunkCount;
        });

        binding.btnCancel.setOnClickListener(v -> showCancelConfirmationDialog());

    }

    private void showCancelConfirmationDialog() {
        if (System.currentTimeMillis() - cancelPressedTime > 2000) {
            binding.tvDoubleTabWarning.setVisibility(View.VISIBLE);
            new Handler().postDelayed(() -> binding.tvDoubleTabWarning.setVisibility(View.GONE), 2000);
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
