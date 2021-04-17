package com.jcoder.picsms.utils;

import android.content.Context;

import com.jcoder.picsms.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PreciseTime {

    private String[] preciseResArray;
    private final Locale locale = Locale.getDefault();

    private final Calendar now = Calendar.getInstance();
    final long ONE_MINUTE = 60 * 1000L;
    final long TWO_MINUTE = 2 * ONE_MINUTE;
    final long ONE_HOUR = 60 * ONE_MINUTE;
    final long ONE_HOUR_AND_ONE_MINUTE = ONE_HOUR + ONE_MINUTE;
    final long ONE_DAY = 24 * ONE_HOUR;
    final long TWO_DAY = 2 * ONE_DAY;

    final String HOUR_MIN_PATTERN = "h:mm a";
    final String MONTH_DAY_PATTERN = "MMMM d";
    final String MONTH_DAY_YEAR_PATTERN = "MMM d, yyyy";
    final String MONTH_DAY_HOUR_MINUTE_PATTERN = "MMMM d h:mm a";
    final String MONTH_DAY_YEAR_HOUR_MINUTE_PATTERN = "MMMM d, yyyy h:mm a";

    public PreciseTime(Context context) {
        setPreciseTimeArray(context);
    }

    private void setPreciseTimeArray(Context context) {
        preciseResArray = context.getResources().getStringArray(R.array.precise_time_values);
        if (preciseResArray == null)
            throw new RuntimeException(String.format("Declare Precise Resource Array for %s locale.", Locale.getDefault().toString()));
        if (preciseResArray.length != 6)
            throw new RuntimeException("Precise Resource Array length must be 6.");
    }

    public String prettyFormat(Date date) {
        return prettyFormat(date.getTime());
    }

    public String preciseFormat(Date date) {
        return preciseFormat(date.getTime());
    }

    public String prettyFormat(long timestamp) {
        long nowTimestamp = now.getTimeInMillis();
        if (timestamp <= 0 || timestamp > nowTimestamp) return preciseResArray[0];

        long difference = nowTimestamp - timestamp;
        if (difference < ONE_MINUTE) {
            return preciseResArray[1];
        } else if (difference < TWO_MINUTE) {
            return preciseResArray[2];
        } else if (difference < ONE_HOUR) {
            return String.format(locale, preciseResArray[3], difference / ONE_MINUTE);
        } else if (difference < ONE_HOUR_AND_ONE_MINUTE) {
            return preciseResArray[4];
        } else if (difference < ONE_DAY) {
            return formatWith(HOUR_MIN_PATTERN, timestamp);
        } else if (difference < TWO_DAY) {
            return String.format("%1$s %2$s", preciseResArray[5], formatWith(HOUR_MIN_PATTERN, timestamp));
        } else if (thisYear(timestamp)) {
            return formatWith(MONTH_DAY_PATTERN, timestamp);
        } else {
            return formatWith(MONTH_DAY_YEAR_PATTERN, timestamp);
        }
    }

    public String preciseFormat(long timestamp) {
        long nowTimestamp = now.getTimeInMillis();
        if (timestamp <= 0 || timestamp > nowTimestamp)
            return formatWith(MONTH_DAY_YEAR_HOUR_MINUTE_PATTERN, timestamp);

        long difference = nowTimestamp - timestamp;
        if (difference < ONE_DAY) {
            return formatWith(HOUR_MIN_PATTERN, timestamp);
        } else if (thisYear(timestamp)) {
            return formatWith(MONTH_DAY_HOUR_MINUTE_PATTERN, timestamp);
        } else {
            return formatWith(MONTH_DAY_YEAR_HOUR_MINUTE_PATTERN, timestamp);
        }
    }

    private boolean thisYear(long timestamp) {
        Calendar inputCalendar = Calendar.getInstance();
        inputCalendar.setTimeInMillis(timestamp);
        return inputCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR);
    }

    private String formatWith(String pattern, long timestamp) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date(timestamp));
    }
}