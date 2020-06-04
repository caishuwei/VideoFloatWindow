package com.csw.android.videofloatwindow.player.video.impl.exo.timebar;

import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.csw.android.videofloatwindow.R;
import com.csw.android.videofloatwindow.app.MyApplication;
import com.csw.android.videofloatwindow.util.ScreenInfo;

/**
 * 自定义BitmapDrawable，实现按下与抬起时有不同的状态表现
 */
public class MySeekBarIndicateDrawable extends BitmapDrawable {

    private int sizeInPressed = ScreenInfo.Companion.dp2Px(25);//按压时Drawable自身的尺寸
    private int sizeInNormal = ScreenInfo.Companion.dp2Px(12);//普通状态下Drawable自身的尺寸

    public MySeekBarIndicateDrawable() {
        super(MyApplication.instance.getResources(), BitmapFactory.decodeResource(MyApplication.instance.getResources(), R.drawable.player_seekbar));
    }

    private boolean isPressed = false;

    @Override
    public boolean isStateful() {
        return super.isStateful() | true;//设置这个Drawable是携带状态的
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        //获取按下状态的改变
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

    /**
     * 根据按压状态，修改自身尺寸
     */
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

    /**
     * 绘制不用我们操心，BitmapDrawable在绘制时已经将图片缩放到自身尺寸能完全显示的大小
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }


}
