package com.jcoder.picsms.async;

import com.jcoder.picsms.models.PictureLibraryItem;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class DeleteAllInPictureLibraryTask implements Callable<Void> {
    private final ArrayList<PictureLibraryItem> codeLibraryItems;

    public DeleteAllInPictureLibraryTask(ArrayList<PictureLibraryItem> codeLibraryItems) {
        this.codeLibraryItems = codeLibraryItems;
    }

    @Override
    public Void call() {
        for (PictureLibraryItem item : codeLibraryItems) {
            //noinspection ResultOfMethodCallIgnored
            new File(item.getFilePath()).delete();
        }

        return null;
    }
}
