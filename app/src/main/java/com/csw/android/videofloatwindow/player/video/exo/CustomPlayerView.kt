package com.csw.android.videofloatwindow.player.video.exo

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.exoplayer2.ui.PlayerView

/**
 * 自定义ExoPlayerView，由于他自己拦截了所有事件，导致我很多对播放器的手势和控制都无法实现，这里直接让其不处理触摸事件
 */
class CustomPlayerView : PlayerView {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        //瞎jb拦截，这里直接不拦截
        return false
    }

}