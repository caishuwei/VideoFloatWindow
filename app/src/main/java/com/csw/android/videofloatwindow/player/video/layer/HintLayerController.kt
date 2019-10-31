package com.csw.android.videofloatwindow.player.video.layer

import android.view.ViewGroup

/**
 * 实现在最后一个子视图移除时，隐藏视图层
 */
class HintLayerController(layer: ViewGroup) : LayerController(layer) {

    override fun hide() {
        //当图层所有子视图都移除时，才隐藏图层
        if (layer.childCount == 0) {
            super.hide()
        }
    }

}