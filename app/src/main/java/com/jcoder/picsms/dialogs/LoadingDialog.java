package com.jcoder.picsms.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jcoder.picsms.databinding.DialogLoadingBinding;

public class LoadingDialog extends BottomSheetDialogFragment {
    private DialogLoadingBinding binding;
    private final String message;
    private OnCancelClickListener onCancelClickListener;

    public LoadingDialog(String message) {
        this.message = message;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogLoadingBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tv.setText(message);
        binding.btnCancel.setOnClickListener(v -> {
            if (onCancelClickListener != null) onCancelClickListener.onCancelClicked();
            dismiss();
        });
    }

    public interface OnCancelClickListener {
        void onCancelClicked();
    }

    public void setOnCancelClickListener(OnCancelClickListener onCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener;
    }

}
