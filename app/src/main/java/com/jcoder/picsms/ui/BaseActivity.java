package com.jcoder.picsms.ui;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.jcoder.picsms.preferences.PreferenceUtils;
import com.jcoder.picsms.utils.LocaleUtils;

public class BaseActivity extends AppCompatActivity {

    public void attachBaseContext(@NonNull Context context) {
        LocaleUtils localeUtils = LocaleUtils.INSTANCE;
        super.attachBaseContext(localeUtils.updateResources(context, PreferenceUtils.getLanguage(context)));
    }

}
