package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet

class WH11ImageView : WHRatioImageView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        whRatio = 1f
    }
}