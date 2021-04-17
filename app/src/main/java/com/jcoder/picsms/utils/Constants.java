package com.jcoder.picsms.utils;

public class Constants {
    // hold data in text variable because Intent.putExtra(data) can only handle less than 1MB size
    public static String text = null;
    public static String[] list = null;

    public static final int MAX_CHARACTERS_PER_SMS = 1224;
    public static final int MAX_CHARACTERS_PER_CHUNK = 160;

}
