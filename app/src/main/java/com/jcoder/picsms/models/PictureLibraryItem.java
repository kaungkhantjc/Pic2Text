package com.jcoder.picsms.models;

import androidx.core.util.Pair;

import com.jcoder.picsms.async.TextToBytesTask;
import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

public class PictureLibraryItem {
    private static final String KEY_SENT_SMS_PROGRESS = "sentSmsProgress";
    private static final String KEY_MAX = "max";
    private static final String KEY_ENCODING_TYPE = "encodingType";
    private static final String KEY_PICTURE = "pic";

    private final String filePath;
    private final String fileName;
    private final int sendingSmsPosition;
    private final int max;
    private final EncodingType encodingType;
    private final String text;
    private final byte[] bytes;

    public PictureLibraryItem(String filePath, String fileName, int sendingSmsPosition, int max, EncodingType encodingType, String text, byte[] bytes) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.sendingSmsPosition = sendingSmsPosition;
        this.max = max;
        this.encodingType = encodingType;
        this.text = text;
        this.bytes = bytes;
    }

    public static PictureLibraryItem parse(String filePath, String fileName, String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has(KEY_SENT_SMS_PROGRESS) && jsonObject.has(KEY_MAX) && jsonObject.has(KEY_ENCODING_TYPE) && jsonObject.has(KEY_PICTURE)) {
                int sendingSmsPosition = jsonObject.getInt(KEY_SENT_SMS_PROGRESS);
                int max = jsonObject.getInt(KEY_MAX);
                EncodingType encodingType = EncodingType.valueOf(jsonObject.getString(KEY_ENCODING_TYPE));
                String text = jsonObject.getString(KEY_PICTURE);
                byte[] bytes = new TextToBytesTask(encodingType, text).call();
                return new PictureLibraryItem(filePath, fileName, sendingSmsPosition, max, encodingType, text, bytes);
            }
        } catch (JSONException ignored) {
        }
        return null;
    }

    public static Pair<Boolean, String> toString(EncodingType encodingType, int sendingSmsPosition) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KEY_SENT_SMS_PROGRESS, sendingSmsPosition);
            jsonObject.put(KEY_MAX, Constants.list.length);
            jsonObject.put(KEY_ENCODING_TYPE, encodingType);
            jsonObject.put(KEY_PICTURE, Constants.text);
            return new Pair<>(true, jsonObject.toString());
        } catch (JSONException e) {
            return new Pair<>(false, e.toString());
        }
    }

    public String getFileName() {
        return fileName;
    }

    public int getSendingSmsPosition() {
        return sendingSmsPosition;
    }

    public int getMax() {
        return max;
    }

    public String getText() {
        return text;
    }

    public String getFilePath() {
        return filePath;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public EncodingType getEncodingType() {
        return encodingType;
    }
}
