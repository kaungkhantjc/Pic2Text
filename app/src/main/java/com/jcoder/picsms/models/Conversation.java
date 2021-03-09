package com.jcoder.picsms.models;

import java.util.ArrayList;

public class Conversation {
    private final String address, body;
    private final String date;

    private ArrayList<Message> messages = new ArrayList<>();

    public Conversation(String address, String body, String date) {
        this.address = address;
        this.body = body;
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public String getBody() {
        return body;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }
}
