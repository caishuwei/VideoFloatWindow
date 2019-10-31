package com.csw.android.videofloatwindow.player.video.view

import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import com.csw.android.videofloatwindow.player.video.layer.LayerController

/**
 * 视频加载中的提示视图显示
 */
class LoadingHintViewHolder(parent: ViewGroup) : HintViewHolder(parent) {
    private val loadingAnimation: RotateAnimation

    init {
        loadingAnimation = RotateAnimation(
                0f,
                180f,
                RotateAnimation.RELATIVE_TO_SELF,
                0.5f,
                RotateAnimation.RELATIVE_TO_SELF,
                0.5f
        )
        loadingAnimation.duration = 1000
        loadingAnimation.repeatMode = RotateAnimation.RESTART
        loadingAnimation.repeatCount = RotateAnimation.INFINITE
        loadingAnimation.interpolator = DecelerateInterpolator()
    }

    override fun addToLayer(layerController: LayerController) {
        super.addToLayer(layerController)
        iv_hint_img.startAnimation(loadingAnimation)
    }

    override fun removeFromLayer(layerController: LayerController) {
        super.removeFromLayer(layerController)
        iv_hint_img.clearAnimation()
    }
}