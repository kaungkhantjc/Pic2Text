package com.jcoder.picsms.fake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Requirements to request PicSMS app as a Default SMS app

public class FakeMmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SmsReceiver.handleOnReceive(context, intent);
    }
}
