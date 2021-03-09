package com.jcoder.picsms.models;

public class SmsDetailModel {
    private final String key, value;
    private final int color;

    public SmsDetailModel(String key, String value, int color) {
        this.key = key;
        this.value = value;
        this.color = color;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getColor() {
        return color;
    }
}
