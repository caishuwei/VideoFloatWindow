package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.util.Utils

class VideoView : FrameLayout {
    private var videoRatio = 16f / 9;

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setBackgroundColor(0x80000000.toInt())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Utils.measureCenterInsideByScaleRatio(widthMeasureSpec, heightMeasureSpec, videoRatio) { w, h ->
            super.onMeasure(w, h)
        }
    }
}