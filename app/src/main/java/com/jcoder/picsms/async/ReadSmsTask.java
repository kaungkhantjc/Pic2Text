package com.jcoder.picsms.async;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.jcoder.picsms.models.Conversation;
import com.jcoder.picsms.models.Message;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ReadSmsTask implements Callable<ArrayList<Conversation>> {

    private final ContentResolver contentResolver;

    public ReadSmsTask(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    public ArrayList<Conversation> call() {

        final ArrayList<Message> messages = new ArrayList<>();
        final ArrayList<Conversation> conversations = new ArrayList<>();

        Uri smsUri = Uri.parse("content://sms/");
        Cursor cursor = contentResolver.query(smsUri,
                new String[]{"_id", "thread_id", "address", "date", "body",
                        "type"}, null, null, null);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Message message = new Message(cursor);
                messages.add(message);
            }
        }

        cursor.close();

        ArrayList<String> addressList = new ArrayList<>();

        for (Message message : messages) {
            String address = message.getAddress();

            if (!addressList.contains(address)) {
                addressList.add(address);
                Conversation conversation = new Conversation(
                        address,
                        message.getBody(),
                        message.getDate()
                );
                conversations.add(conversation);
            }

            int index = addressList.indexOf(address);
            Conversation conversation = conversations.get(index);
            conversation.addMessage(message);
            conversations.set(index, conversation);
        }

        return conversations;
    }
}
