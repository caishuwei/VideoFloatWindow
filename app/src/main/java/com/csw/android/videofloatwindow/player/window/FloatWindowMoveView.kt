package com.csw.android.videofloatwindow.player.window

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import com.csw.android.videofloatwindow.R

/**
 * 用于控制浮动窗口移动的视图
 */
class FloatWindowMoveView : AppCompatImageButton {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        visibility = View.INVISIBLE
        setImageResource(R.drawable.icon_move)
        setBackgroundColor(Color.BLACK)
    }

    private val rect = Rect()

    /**
     * 判断按下时是否在这个View上
     */
    fun isDownOnThisView(downX: Float, downY: Float): Boolean {
        getHitRect(rect)
        return rect.contains(downX.toInt(), downY.toInt())
    }

    /**
     * 设置是否处于滑动状态
     * <p>
     * 滑动时显示，不滑动时设置为INVISIBLE，这样仍旧可以判断点击是否在视图上
     */
    fun setInMoving(inMoving: Boolean) {
        visibility = if (inMoving) View.VISIBLE else View.INVISIBLE
    }

    /**
     * 是否处于滑动中
     */
    fun isInMoving(): Boolean {
        return visibility == View.VISIBLE
    }
}