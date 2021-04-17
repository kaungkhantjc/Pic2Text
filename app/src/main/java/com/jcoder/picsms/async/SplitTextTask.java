package com.jcoder.picsms.async;

import com.jcoder.picsms.utils.ChunkUtils;
import com.jcoder.picsms.utils.Constants;

import java.util.concurrent.Callable;

public class SplitTextTask implements Callable<Void> {

    private final String text;

    public SplitTextTask(String text) {
        this.text = text;
    }

    @Override
    public Void call() {
        Constants.text = text;
        int maxSortNumber = (int) (Math.ceil(Constants.text.length() / (double) Constants.MAX_CHARACTERS_PER_SMS));
        // +2 is for '(' and '-'
        int prefixLength = String.valueOf(maxSortNumber).length() + 2;
        Constants.list = ChunkUtils.split(Constants.text, Constants.MAX_CHARACTERS_PER_SMS - prefixLength);
        return null;
    }
}
