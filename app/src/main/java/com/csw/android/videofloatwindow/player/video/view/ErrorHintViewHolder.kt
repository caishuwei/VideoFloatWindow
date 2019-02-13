package com.csw.android.videofloatwindow.player.video.view

import android.view.View
import android.view.ViewGroup

class ErrorHintViewHolder : HintViewHolder {
    var clickListener: View.OnClickListener? = null

    constructor(parent: ViewGroup) : super(parent) {
        mView.setOnClickListener { view ->
            clickListener?.onClick(view)
        }
    }
}