package com.jcoder.picsms.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;

import java.util.Locale;

public class LocaleUtils {
    public static final LocaleUtils INSTANCE = new LocaleUtils();

    @NonNull
    public final Context updateResources(@NonNull Context context, @NonNull String str) {
        Locale locale = new Locale(str);
        Locale.setDefault(locale);
        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }


}