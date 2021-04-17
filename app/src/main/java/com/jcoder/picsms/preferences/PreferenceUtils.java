package com.jcoder.picsms.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import androidx.preference.PreferenceManager;

import com.jcoder.picsms.BuildConfig;
import com.jcoder.picsms.R;

public class PreferenceUtils {
    public static final String THEME = "theme";
    public static final String LANGUAGE = "language";

    public static final String RANGE_OF_AUTO_SELECT = "range_of_auto_select";
    public static final String ASK_TO_SAVE_IN_PICTURE_LIBRARY = "ask_to_save_in_picture_library";
    public static final String REMEMBER_PHONE_NUMBER = "remember_phone_number";
    public static final String REMEMBER_SIM_SLOT = "remember_sim_slot";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String SIM_SLOT = "sim_slot";
    public static final String AUTO_SELECT_TYPE = "auto_select_type";
    public static final String DO_NOT_SAVE_SENT_MESSAGES = "do_not_save_sent_messages";

    public static final String WHAT_IS_NEW_VERSION = "WHAT_IS_NEW";

    public static boolean shouldShowWhatIsNew(Context context) {
        return !getSharedPreferences(context).contains(WHAT_IS_NEW_VERSION) ||
                !getSharedPreferences(context).getString(WHAT_IS_NEW_VERSION, null).equals(BuildConfig.VERSION_NAME);
    }

    public static void disableShouldShowWhatIsNew(Context context) {
        getSharedPreferences(context).edit().putString(WHAT_IS_NEW_VERSION, BuildConfig.VERSION_NAME).apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getTheme(Context context) {
        String theme = getSharedPreferences(context).getString(THEME, null);
        return theme == null ? -1 : Integer.parseInt(theme);
    }

    public static String getLanguage(Context context) {
        return getSharedPreferences(context).getString(LANGUAGE, "en");
    }

    public static int getRangeOfAutoSelect(Context context) {
        return getSharedPreferences(context).getInt(RANGE_OF_AUTO_SELECT, context.getResources().getInteger(R.integer.default_range_of_auto_select));
    }

    public static boolean askToSaveInPictureLibEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(ASK_TO_SAVE_IN_PICTURE_LIBRARY, true);
    }

    public static boolean rememberPhoneNumberEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(REMEMBER_PHONE_NUMBER, true);
    }

    public static boolean rememberSimSlotEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(REMEMBER_SIM_SLOT, true);
    }

    public static String getPhoneNumber(Context context) {
        String phoneNumber = getSharedPreferences(context).getString(PHONE_NUMBER, null);
        boolean isValid = phoneNumber != null && Patterns.PHONE.matcher(phoneNumber).matches();
        return isValid ? phoneNumber : null;
    }

    public static int getSimSlotPosition(Context context) {
        int simPosition = getSharedPreferences(context).getInt(SIM_SLOT, -1);
        return (simPosition >= -1 && simPosition <= 1) ? simPosition : -1;
    }

    public static int getAutoSelectType(Context context) {
        return Integer.parseInt(getSharedPreferences(context).getString(AUTO_SELECT_TYPE, "1"));
    }

    public static boolean doNotSaveSentMessagesEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(DO_NOT_SAVE_SENT_MESSAGES, false);
    }

    public static void setRangeOfAutoSelect(Context context, int minutes) {
        getSharedPreferences(context).edit().putInt(RANGE_OF_AUTO_SELECT, minutes).apply();
    }

    public static void disableAskToSaveInPictureLibrary(Context context) {
        getSharedPreferences(context).edit().putBoolean(ASK_TO_SAVE_IN_PICTURE_LIBRARY, false).apply();
    }

    public static void setPhoneNumber(Context context, String phoneNumber) {
        if (phoneNumber == null) getSharedPreferences(context).edit().remove(PHONE_NUMBER).apply();
        else getSharedPreferences(context).edit().putString(PHONE_NUMBER, phoneNumber).apply();
    }

    public static void setSimSlot(Context context, int simSlot) {
        if (simSlot == -1) getSharedPreferences(context).edit().remove(SIM_SLOT).apply();
        else getSharedPreferences(context).edit().putInt(SIM_SLOT, simSlot).apply();
    }

    public static void setDoNotSaveSentMessages(Context context, boolean enabled) {
        getSharedPreferences(context).edit().putBoolean(DO_NOT_SAVE_SENT_MESSAGES, enabled).apply();
    }
}
