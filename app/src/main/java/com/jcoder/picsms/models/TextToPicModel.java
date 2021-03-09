package com.jcoder.picsms.models;

public class TextToPicModel {
    private final long order;
    private final String code;

    public TextToPicModel(long order, String code) {
        this.order = order;
        this.code = code;
    }

    public long getOrder() {
        ;
        return order;
    }

    public String getCode() {
        return code;
    }

    public String getFormattedOrder() {
        return order < 999999 ? String.valueOf(order + 1) : "#";
    }
}
