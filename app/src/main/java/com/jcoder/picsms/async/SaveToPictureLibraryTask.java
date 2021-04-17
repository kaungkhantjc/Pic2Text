package com.jcoder.picsms.async;

import android.content.Context;

import androidx.core.util.Pair;

import com.jcoder.picsms.R;
import com.jcoder.picsms.encoding.EncodingType;
import com.jcoder.picsms.models.PictureLibraryItem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Callable;

public class SaveToPictureLibraryTask implements Callable<Pair<Boolean, String>> {
    private final Context context;
    private final File file;
    private final boolean overrideFile;
    private final EncodingType encodingType;
    private final int sendingSmsPosition;

    public SaveToPictureLibraryTask(Context context, File file, boolean overrideFile, EncodingType encodingType, int sendingSmsPosition) {
        this.context = context;
        this.file = file;
        this.overrideFile = overrideFile;
        this.encodingType = encodingType;
        this.sendingSmsPosition = sendingSmsPosition;
    }

    @Override
    public Pair<Boolean, String> call() {

        if (!overrideFile && file.exists())
            return new Pair<>(false, context.getString(R.string.file_name_already_exists));

        Pair<Boolean, String> result = PictureLibraryItem.toString(encodingType, sendingSmsPosition);
        if (Objects.requireNonNull(result.first)) {
            String codes = Objects.requireNonNull(result.second);
            try {
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter writer = new BufferedWriter(fileWriter);
                writer.write(codes);
                writer.flush();
                writer.close();
                fileWriter.close();
                return new Pair<>(true, null);
            } catch (IOException e) {
                return new Pair<>(false, e.toString());
            }
        } else return result;
    }
}
