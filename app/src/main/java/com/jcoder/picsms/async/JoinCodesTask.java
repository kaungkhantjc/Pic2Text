package com.jcoder.picsms.async;

import com.jcoder.picsms.models.CodePart;
import com.jcoder.picsms.utils.Constants;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class JoinCodesTask implements Callable<Void> {

    private final ArrayList<CodePart> codeList;

    public JoinCodesTask(ArrayList<CodePart> codeList) {
        this.codeList = codeList;
    }

    @Override
    public Void call() {
        StringBuilder sb = new StringBuilder();
        for (CodePart model : codeList)
            sb.append(model.getCode());
        Constants.text = sb.toString();
        return null;
    }
}
