package com.jcoder.picsms.async;

import com.jcoder.picsms.models.CodePart;
import com.jcoder.picsms.utils.Text2CodeListUtil;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class Text2CodeListTask implements Callable<ArrayList<CodePart>> {

    private final String text;

    public Text2CodeListTask(String text) {
        this.text = text;
    }

    @Override
    public ArrayList<CodePart> call() {
        return Text2CodeListUtil.text2CodeParts(text, false);
    }
}
