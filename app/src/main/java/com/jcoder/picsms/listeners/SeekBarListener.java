package com.jcoder.picsms.listeners;

import android.widget.SeekBar;

public class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

    private final Callback callback;

    public SeekBarListener(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onProgressChanged(int progress);

        void onStopTrackingTouch();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        callback.onProgressChanged(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        callback.onStopTrackingTouch();
    }
}
