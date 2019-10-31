package com.csw.android.videofloatwindow.player.video.layer

import android.os.Handler
import android.os.Looper
import android.view.ViewGroup

/**
 * 自动隐藏视图层控制器，用于显示一些简单的提示信息并在一段时间后自动隐藏掉，如亮度与音量的变化显示
 */
class AutoHintLayerController(layer: ViewGroup) : LayerController(layer) {
    private val autoHideTime = 2000L
    private val mainHandle = Handler(Looper.getMainLooper())
    private val autoHideTask = Runnable {
        hide()
    }

    override fun show() {
        super.show()
        mainHandle.removeCallbacks(autoHideTask)
        //一段时间后自动隐藏
        mainHandle.postDelayed(autoHideTask, autoHideTime)
    }

}