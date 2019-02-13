package com.csw.android.videofloatwindow.player.video.layer

import android.view.View
import android.view.ViewGroup
import android.view.animation.*

open class LayerController {
    val layer: ViewGroup
    private val showAnimation: AnimationSet;
    private val hideAnimation: AnimationSet;

    constructor(layer: ViewGroup) {
        this.layer = layer
        showAnimation = AnimationSet(false)
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.interpolator = LinearInterpolator()
        val scaleAnimation = ScaleAnimation(
                0f,
                1f,
                0f,
                1f,
                ScaleAnimation.RELATIVE_TO_SELF,
                0.5f
                , ScaleAnimation.RELATIVE_TO_SELF,
                0.5f)
        scaleAnimation.interpolator = OvershootInterpolator()
        showAnimation.addAnimation(alphaAnimation)
        showAnimation.addAnimation(scaleAnimation)
        showAnimation.duration = 300

        hideAnimation = AnimationSet(false)
        val a = AlphaAnimation(1f, 0f)
        alphaAnimation.interpolator = LinearInterpolator()
        val s = ScaleAnimation(
                1f,
                0f,
                1f,
                0f,
                ScaleAnimation.RELATIVE_TO_SELF,
                0.5f
                , ScaleAnimation.RELATIVE_TO_SELF,
                0.5f)
        scaleAnimation.interpolator = AnticipateOvershootInterpolator()
        showAnimation.addAnimation(a)
        showAnimation.addAnimation(s)
        showAnimation.duration = 300
    }

    open fun show() {
        if (layer.visibility == View.GONE) {
            layer.visibility = View.VISIBLE
            layer.startAnimation(showAnimation)
        }
    }

    open fun hide() {
        if (layer.visibility == View.VISIBLE) {
            layer.visibility = View.GONE
            layer.startAnimation(hideAnimation)
        }
    }

}