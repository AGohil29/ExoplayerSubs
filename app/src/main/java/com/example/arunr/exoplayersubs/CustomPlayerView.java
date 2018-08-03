package com.example.arunr.exoplayersubs;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

/**
 * Created by arun.r on 31-07-2018.
 */

public final class CustomPlayerView extends PlayerView implements PlayerControlView.VisibilityListener {

    private static final float DRAG_THRESHOLD = 10;
    private static final long LONG_PRESS_THRESHOLD_MS = 500;

    private boolean controllerVisible;
    private long tapStartTimeMs;
    private float tapPositionX;
    private float tapPositionY;

    public CustomPlayerView(Context context) {
        this(context, null);
    }

    public CustomPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setControllerVisibilityListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                tapStartTimeMs = SystemClock.elapsedRealtime();
                tapPositionX = event.getX();
                tapPositionY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (tapStartTimeMs != 0 && (Math.abs(event.getX() - tapPositionX) > DRAG_THRESHOLD)) {
                    tapStartTimeMs = 0;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (tapStartTimeMs != 0) {
                    if (SystemClock.elapsedRealtime() - tapStartTimeMs < LONG_PRESS_THRESHOLD_MS) {
                        if (!controllerVisible) {
                            showController();
                        } else if (getControllerHideOnTouch()) {
                            hideController();
                        }
                    }
                    tapStartTimeMs = 0;
                }
                Log.d("CustomPlayerView", "onTouchUp");
        }
        return true;
    }


    @Override
    public void onVisibilityChange(int visibility) {
        controllerVisible = visibility == View.VISIBLE;
    }
}
