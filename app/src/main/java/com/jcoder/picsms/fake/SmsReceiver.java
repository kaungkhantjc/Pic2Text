package com.jcoder.picsms.fake;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jcoder.picsms.BuildConfig;
import com.jcoder.picsms.R;
import com.jcoder.picsms.utils.SMSUtil;

// SmsReceiver will save incoming messages to inbox while PicSMS is Default SMS app

public class SmsReceiver extends BroadcastReceiver {
    /**
     * Intent.action for receiving SMS.
     */
    @SuppressLint("InlinedApi")
    private static final String ACTION_SMS_OLD = Telephony.Sms.Intents.SMS_RECEIVED_ACTION;

    @SuppressLint("InlinedApi")
    private static final String ACTION_SMS_NEW = Telephony.Sms.Intents.SMS_DELIVER_ACTION;

    /**
     * Intent.action for receiving MMS.
     */
    @SuppressLint("InlinedApi")
    private static final String ACTION_MMS_OLD = Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;

    @SuppressLint("InlinedApi")
    private static final String ACTION_MMS_MEW = Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION;

    /**
     * An unreadable MMS body.
     */
    private static final String MMS_BODY = "<MMS>";

    /**
     * Delay for spinlock, waiting for new messages.
     */
    private static final long SLEEP = 500;

    /**
     * ID for new message notification.
     */
    private static final int NOTIFICATION_ID_NEW = 1;

    @Override
    public final void onReceive(final Context context, final Intent intent) {
        if (SMSUtil.isDefaultApp(context)) {
            handleOnReceive(context, intent);
        }
    }

    private static boolean shouldHandleSmsAction(final Context context, final String action) {
        return ACTION_SMS_NEW.equals(action) // -> is >= android 4.4 and default app
                || ACTION_SMS_OLD.equals(action) // handle old action only if:
                && !BuildConfig.APPLICATION_ID // or not default app
                .equals(Telephony.Sms.getDefaultSmsPackage(context)); // handle old action only if:
    }

    static void handleOnReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        try {
            Thread.sleep(SLEEP);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String text;

        boolean silent = false;

        if (shouldHandleSmsAction(context, action)) {
            Bundle b = intent.getExtras();
            assert b != null;
            Object[] messages = (Object[]) b.get("pdus");
            SmsMessage[] smsMessage = new SmsMessage[messages.length];
            int l = messages.length;
            for (int i = 0; i < l; i++) {
                smsMessage[i] = SmsMessage.createFromPdu((byte[]) messages[i]);
            }
            text = null;
            String address = null;
            if (l > 0) {
                // concatenate multipart SMS body
                StringBuilder sbt = new StringBuilder();
                for (int i = 0; i < l; i++) {
                    sbt.append(smsMessage[i].getMessageBody());
                }
                text = sbt.toString();

                // ! Check in blacklist db - filter spam
                address = smsMessage[0].getDisplayOriginatingAddress();
                if (action.equals(ACTION_SMS_NEW)) {
                    ContentValues values = new ContentValues();
                    values.put("address", address);
                    values.put("body", text);
                    context.getContentResolver().insert(Uri.parse("content://sms/inbox"),
                            values);
                }
            }
            updateNotificationsWithNewText(context, text, address, silent);
        } else if (ACTION_MMS_OLD.equals(action) || ACTION_MMS_MEW.equals(action)) {
            text = MMS_BODY;
            // TODO API19+ MMS code
            updateNotificationsWithNewText(context, text, null, silent);
        }

    }

    private static void updateNotificationsWithNewText(final Context context, final String text, final String address,
                                                       final boolean silent) {
        if (!silent && text != null) {
            updateNewMessageNotification(context, text, address);
        }
    }

    /**
     * Update new message {@link Notification}.
     *
     * @param context {@link Context
     * @param text    text of the last assumed unread message
     */
    static void updateNewMessageNotification(final Context context, String text, String address) {
        String CHANNEL_ID_INCOMING_MESSAGES = "Incoming Messages";

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManagerCompat.getNotificationChannel(CHANNEL_ID_INCOMING_MESSAGES);
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID_INCOMING_MESSAGES, CHANNEL_ID_INCOMING_MESSAGES, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableVibration(true);
                notificationChannel.setShowBadge(false);
                notificationManagerCompat.createNotificationChannel(notificationChannel);
            }
        }

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (text != null) {
            notificationManager.cancel(NOTIFICATION_ID_NEW);
        }

        PendingIntent defaultPendingIntent;

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_MESSAGING);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID_INCOMING_MESSAGES);

        defaultPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
        notificationBuilder.setContentIntent(defaultPendingIntent);
        notificationBuilder.setDefaults(Notification.DEFAULT_ALL);
        notificationBuilder.setAutoCancel(true);
        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        if (address == null) address = "Unknown number";
        notificationBuilder.setContentTitle(address);
        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        notificationManager.notify(NOTIFICATION_ID_NEW, notificationBuilder.build());
    }
}
