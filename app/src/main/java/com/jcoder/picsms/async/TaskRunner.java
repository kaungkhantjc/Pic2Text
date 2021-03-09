package com.jcoder.picsms.async;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRunner {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new android.os.Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onTaskCompleted(R result);
    }

    public <R> void execute(final Callable<R> callable, final Callback<R> callback) {
        executor.execute(() -> {
            try {
                R result = callable.call();
                handler.post(() -> callback.onTaskCompleted(result));
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
