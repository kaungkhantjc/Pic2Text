package com.jcoder.picsms.models;

import androidx.annotation.NonNull;

import com.jcoder.picsms.encoding.EncodingType;

public class Encoding {
    private final EncodingType encodingType;
    private final String encodingName;
    private final String encodingMessage;

    public Encoding(EncodingType encodingType, String encodingName, String encodingMessage) {
        this.encodingType = encodingType;
        this.encodingName = encodingName;
        this.encodingMessage = encodingMessage;
    }

    public EncodingType getEncodingType() {
        return encodingType;
    }

    public String getEncodingName() {
        return encodingName;
    }

    public String getEncodingMessage() {
        return encodingMessage;
    }

    @NonNull
    @Override
    public String toString() {
        return encodingName;
    }
}
