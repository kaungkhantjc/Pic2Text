package com.jcoder.picsms.models;

public class CodePart {
    private final long codeOrder;
    private final String code;

    public CodePart(long order, String code) {
        this.codeOrder = order;
        this.code = code;
    }

    public long getCodeOrder() {
        ;
        return codeOrder;
    }

    public String getCode() {
        return code;
    }

    public String getFormattedOrder() {
        return codeOrder < 999999 ? String.valueOf(codeOrder + 1) : "#";
    }
}
