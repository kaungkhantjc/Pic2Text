package com.jcoder.picsms;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.jcoder.picsms.preferences.PreferenceUtils;
import com.jcoder.picsms.utils.LocaleUtils;


public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        AppCompatDelegate.setDefaultNightMode(PreferenceUtils.getTheme(base));
        super.attachBaseContext(LocaleUtils.INSTANCE.updateResources(base, PreferenceUtils.getLanguage(base)));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleUtils.INSTANCE.updateResources(this, PreferenceUtils.getLanguage(this));
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }
}
