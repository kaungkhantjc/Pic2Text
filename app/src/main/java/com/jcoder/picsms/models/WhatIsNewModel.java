package com.jcoder.picsms.models;

import android.content.Context;

import java.io.IOException;

public class WhatIsNewModel {
    private final String versionName;
    private final String changes;

    public WhatIsNewModel(String versionName, String changes) {
        this.versionName = versionName;
        this.changes = changes;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getChanges() {
        return changes;
    }
}
