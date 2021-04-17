package com.jcoder.picsms.async;

import android.content.res.AssetManager;

import com.jcoder.picsms.models.WhatIsNewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class WhatIsNewReaderTask implements Callable<ArrayList<WhatIsNewModel>> {
    private static final String KEY_VERSION = "version";
    private static final String KEY_CHANGES = "changes";

    private final AssetManager assetManager;

    public WhatIsNewReaderTask(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    @Override
    public ArrayList<WhatIsNewModel> call() {
        ArrayList<WhatIsNewModel> whatIsNewModels = new ArrayList<>();

        String json = readFromAssets();
        if (json != null) {
            try {
                JSONArray jsonArray = new JSONArray(json);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String version = jsonObject.getString(KEY_VERSION);
                    JSONArray changesArray = jsonObject.getJSONArray(KEY_CHANGES);

                    StringBuilder stringBuilder = new StringBuilder();
                    for (int j = 0; j < changesArray.length(); j++) {
                        stringBuilder.append("\u25fe ")
                                .append(changesArray.getString(j))
                                .append("\n\n");
                    }

                    whatIsNewModels.add(new WhatIsNewModel(version, stringBuilder.toString().trim()));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return whatIsNewModels;
    }

    private String readFromAssets() {
        try {
            InputStream inputStream = assetManager.open("what_is_new.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder buffer = new StringBuilder();

            int charCodePoint;
            while ((charCodePoint = bufferedReader.read()) != -1) {
                buffer.append((char) charCodePoint);
            }

            bufferedReader.close();
            inputStream.close();
            return buffer.toString().trim();
        } catch (IOException e) {
            return null;
        }
    }
}
