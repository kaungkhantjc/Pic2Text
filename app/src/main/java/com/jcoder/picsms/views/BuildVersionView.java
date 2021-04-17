package com.jcoder.picsms.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textview.MaterialTextView;
import com.jcoder.picsms.BuildConfig;

import java.util.Locale;

public class BuildVersionView extends MaterialTextView {


    public BuildVersionView(@NonNull Context context) {
        super(context);
        setText(getVersionText());
    }

    public BuildVersionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setText(getVersionText());
    }

    public BuildVersionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setText(getVersionText());
    }

    private String getVersionText() {
        return String.format(Locale.getDefault(), "%1$s (%2$s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);

    }
}
