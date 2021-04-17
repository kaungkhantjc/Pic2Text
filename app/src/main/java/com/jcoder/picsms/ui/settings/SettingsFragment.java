package com.jcoder.picsms.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.jcoder.picsms.BuildConfig;
import com.jcoder.picsms.R;
import com.jcoder.picsms.databinding.DialogEditorBinding;
import com.jcoder.picsms.preferences.PreferenceUtils;
import com.jcoder.picsms.utils.ClipboardUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case "contact_developer":
                contactDeveloper();
                break;

            case "open_source_licences":
                startActivity(new Intent(requireContext(), OssLicensesMenuActivity.class));
                break;

            case "range_of_auto_select":
                showRangeOfAutoSelectEditorDialog();
                break;

            case "ignore_battery_optimization":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    openBatteryOptimizationSettings();
                else
                    Snackbar.make(requireView(), R.string.android_version_not_supported, 2000).show();
                break;

            case "build_version":
                showVersionInfoDialog();
                break;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void showRangeOfAutoSelectEditorDialog() {
        DialogEditorBinding binding = DialogEditorBinding.inflate(getLayoutInflater());
        new MaterialAlertDialogBuilder(requireContext())
                .setView(binding.getRoot())
                .setTitle(R.string.range_of_auto_select_title)
                .setNegativeButton(R.string.btn_cancel, null)
                .setNeutralButton(R.string.btn_default, ((dialog, which) -> PreferenceUtils.setRangeOfAutoSelect(requireContext(), getResources().getInteger(R.integer.default_range_of_auto_select))))
                .setPositiveButton(R.string.btn_ok, ((dialog, which) -> {
                    String inputText = Objects.requireNonNull(binding.edt.getText()).toString().trim();
                    if (!inputText.isEmpty()) {
                        int minute = Integer.parseInt(inputText);
                        if (minute > 0)
                            PreferenceUtils.setRangeOfAutoSelect(requireContext(), Integer.parseInt(inputText));
                        else
                            Snackbar.make(requireView(), R.string.range_of_auto_select_helper, 2000).show();
                    }
                }))
                .show();

        binding.edtLayout.setHint(R.string.range_of_auto_select_hint);
        binding.edt.setInputType(InputType.TYPE_CLASS_NUMBER);
        binding.edtLayout.setHelperText(getString(R.string.range_of_auto_select_helper));
        binding.edt.setText(String.valueOf(PreferenceUtils.getRangeOfAutoSelect(requireContext())));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openBatteryOptimizationSettings() {
        String packageName = requireContext().getPackageName();
        PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            Snackbar.make(requireView(), R.string.battery_optimization_ignored, 4000)
                    .setAction(R.string.menu_settings, v -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivity(intent);
                    })
                    .show();
        } else {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            startActivity(intent);
        }
    }

    private void showVersionInfoDialog() {
        String info = String.format(Locale.ENGLISH,
                "App version : %s\n" +
                        "Build : %d\n" +
                        "Android API : %d (%s)\n" +
                        "Device : %s %s (%s)",
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                Build.VERSION.SDK_INT, Build.CPU_ABI,
                Build.MANUFACTURER, Build.MODEL, Build.PRODUCT);

        new MaterialAlertDialogBuilder(requireContext())
                .setMessage(info)
                .setNegativeButton(R.string.btn_copy, ((dialog, which) -> ClipboardUtils.copy(requireContext(), info)))
                .setPositiveButton(R.string.btn_ok, null)
                .show();
    }

    private void changeAppTheme() {
        AppCompatDelegate.setDefaultNightMode(PreferenceUtils.getTheme(requireContext()));
        requireActivity().finish();
        requireActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        requireContext().startActivity(new Intent(requireContext(), SettingsActivity.class));
    }

    private void restartApp() {
        final Intent intent = requireContext().getPackageManager().getLaunchIntentForPackage(requireContext().getPackageName());
        requireActivity().finishAffinity();
        requireActivity().startActivity(intent);
        System.exit(0);
    }

    private void contactDeveloper() {
        Intent intent;
        try {
            ApplicationInfo applicationInfo = requireContext().getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            intent = applicationInfo.enabled ?
                    new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dev_fb_url))) :
                    new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dev_fb_url_external)));
        } catch (PackageManager.NameNotFoundException ignored) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dev_fb_url_external)));
        }
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtils.THEME:
                changeAppTheme();
                break;

            case PreferenceUtils.LANGUAGE:
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        restartApp();
                    }
                }, 200);
                break;

            case PreferenceUtils.REMEMBER_PHONE_NUMBER:
                if (!PreferenceUtils.rememberPhoneNumberEnabled(requireContext()))
                    PreferenceUtils.setPhoneNumber(requireContext(), null);
                break;

            case PreferenceUtils.REMEMBER_SIM_SLOT:
                if (!PreferenceUtils.rememberSimSlotEnabled(requireContext()))
                    PreferenceUtils.setSimSlot(requireContext(), -1);
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


}