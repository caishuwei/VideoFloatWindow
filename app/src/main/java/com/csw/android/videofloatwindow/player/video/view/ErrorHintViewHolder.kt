package com.csw.android.videofloatwindow.player.video.view

import android.view.View
import android.view.ViewGroup

/**
 * 播放视频出现错误时使用的提示视图持有者
 */
class ErrorHintViewHolder : HintViewHolder {
    var clickListener: View.OnClickListener? = null

    constructor(parent: ViewGroup) : super(parent) {
        mView.setOnClickListener { view ->
            clickListener?.onClick(view)
        }
    }
}