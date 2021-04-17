package com.jcoder.picsms.async;

import com.jcoder.picsms.models.PictureLibraryItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;

public class ReadPictureLibraryTask implements Callable<ArrayList<PictureLibraryItem>> {

    private final ArrayList<PictureLibraryItem> codeLibraryItems = new ArrayList<>();
    private final File rootFile;

    public ReadPictureLibraryTask(File rootFile) {
        this.rootFile = rootFile;
    }

    @Override
    public ArrayList<PictureLibraryItem> call() {
        addCodeLibraryItemsFrom(rootFile);
        return codeLibraryItems;
    }

    private void addCodeLibraryItemsFrom(File rootFile) {
        if (rootFile == null) return;
        File[] files = rootFile.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) addCodeLibraryItemsFrom(file);
            else if (file.isFile()) {
                String fileName = file.getName();
                if (isJsonFile(fileName)) {
                    fileName = fileName.replace(".json", "");
                    PictureLibraryItem item = PictureLibraryItem.parse(file.getPath(), fileName, readFile(file));
                    if (item != null) codeLibraryItems.add(item);
                    else {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                }
            }
        }
    }

    private boolean isJsonFile(String fileName) {
        return fileName.toLowerCase().endsWith(".json");
    }

    private String readFile(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder buffer = new StringBuilder();

            int charCodePoint;
            while ((charCodePoint = bufferedReader.read()) != -1) {
                buffer.append((char) charCodePoint);
            }

            bufferedReader.close();
            fileReader.close();
            return buffer.toString().trim();
        } catch (IOException e) {
            return null;
        }

    }
}
