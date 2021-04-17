package com.jcoder.picsms.ui.sendsms;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class SendingSmsViewModel extends ViewModel {
    private static int maxProgress;

    private final MutableLiveData<Integer> progressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentChunkLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentChunkCountLiveData = new MutableLiveData<>();

    public static void setMaxProgress(int maxProgress) {
        SendingSmsViewModel.maxProgress = maxProgress;
    }

    public void setProgress(int progress) {
        progressLiveData.setValue(progress);
    }

    public void setCurrentChunk(int currentChunk) {
        currentChunkLiveData.setValue(currentChunk);
    }

    public void setCurrentChunkCount(int chunkCount) {
        currentChunkCountLiveData.setValue(chunkCount);
    }

    public LiveData<Integer> getProgress() {
        return progressLiveData;
    }

    public static int getMaxProgress() {
        return maxProgress;
    }

    public LiveData<Integer> getCurrentChunk() {
        return currentChunkLiveData;
    }

    public LiveData<Integer> getCurrentChunkCount() {
        return currentChunkCountLiveData;
    }
}
