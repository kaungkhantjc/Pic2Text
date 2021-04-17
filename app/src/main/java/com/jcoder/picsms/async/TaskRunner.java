package com.jcoder.picsms.async;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRunner {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new android.os.Handler(Looper.myLooper());

    public interface Callback<R> {
        void onTaskCompleted(R result);
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public <R> void execute(final Callable<R> callable, final Callback<R> callback) {
        executorService.execute(() -> {
            try {
                R result = callable.call();
                handler.post(() -> callback.onTaskCompleted(result));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
