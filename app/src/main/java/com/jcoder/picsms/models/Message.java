package com.jcoder.picsms.models;

import android.database.Cursor;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

public class Message {
    private String address = null;
    private String threadId = null;
    private String date = null;
    private String body = null;
    private int type;

    public Message(Cursor cursor) {
        this.address = cursor.getString(cursor.getColumnIndex("address"));
        this.threadId = cursor.getString(cursor.getColumnIndex("thread_id"));

        this.date = cursor.getString(cursor.getColumnIndex("date"));
        this.date = new PrettyTime().format(new Date(Long.parseLong(this.date)));

        this.body = cursor.getString(cursor.getColumnIndex("body"));
        this.type = Integer.parseInt(cursor.getString(cursor.getColumnIndex("type")));
    }

    public String getAddress() {
        return address;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getDate() {
        return date;
    }

    public String getBody() {
        return body;
    }

    public int getType() {
        return type;
    }
}