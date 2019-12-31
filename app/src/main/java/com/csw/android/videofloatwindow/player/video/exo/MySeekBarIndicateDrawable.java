package com.csw.android.videofloatwindow.player.video.exo;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.csw.android.videofloatwindow.R;
import com.csw.android.videofloatwindow.app.MyApplication;
import com.csw.android.videofloatwindow.util.ScreenInfo;

public class MySeekBarIndicateDrawable extends BitmapDrawable {

    private int sizeInPressed = ScreenInfo.Companion.dp2Px(MyTimeBar.DEFAULT_SCRUBBER_DRAGGED_SIZE_DP);
    private int sizeInNormal = ScreenInfo.Companion.dp2Px(MyTimeBar.DEFAULT_SCRUBBER_ENABLED_SIZE_DP);

    public MySeekBarIndicateDrawable() {
        super(MyApplication.instance.getResources(), BitmapFactory.decodeResource(MyApplication.instance.getResources(), R.drawable.player_seekbar));
    }

    private boolean isPressed = false;

    @Override
    public boolean isStateful() {
        return super.isStateful() | true;
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        boolean pressStateChanged = false;
        if (stateSet != null) {
            boolean hasPressed = false;
            for (int state : stateSet) {
                if (state == android.R.attr.state_pressed) {
                    hasPressed = true;
                    if (!isPressed) {
                        pressStateChanged = true;
                        isPressed = true;
                    }
                }
            }
            if (!hasPressed && isPressed) {
                pressStateChanged = true;
                isPressed = false;
            }
        }
        return super.onStateChange(stateSet) | pressStateChanged;
    }

    @Override
    public int getIntrinsicWidth() {
        return isPressed ? sizeInPressed : sizeInNormal;
    }

    @Override
    public int getIntrinsicHeight() {
        return isPressed ? sizeInPressed : sizeInNormal;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }


}
