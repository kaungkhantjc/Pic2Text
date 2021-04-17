package com.jcoder.picsms.models;

import android.database.Cursor;

public class Message {
    private final String address;
    private final String threadId;
    private final long date;
    private final String body;
    private final int type;

    public Message(Cursor cursor) {
        this.address = cursor.getString(cursor.getColumnIndex("address"));
        this.threadId = cursor.getString(cursor.getColumnIndex("thread_id"));
        this.date = Long.parseLong(cursor.getString(cursor.getColumnIndex("date")));
        this.body = cursor.getString(cursor.getColumnIndex("body"));
        this.type = Integer.parseInt(cursor.getString(cursor.getColumnIndex("type")));
    }

    public String getAddress() {
        return address;
    }

    @SuppressWarnings({"unused", "RedundantSuppression"})
    public String getThreadId() {
        return threadId;
    }

    public String getBody() {
        return body;
    }

    public int getType() {
        return type;
    }

    public long getDate() {
        return date;
    }
}