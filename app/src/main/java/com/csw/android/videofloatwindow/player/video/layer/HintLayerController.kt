package com.csw.android.videofloatwindow.player.video.layer

import android.view.ViewGroup

class HintLayerController(layer: ViewGroup) : LayerController(layer) {

    override fun hide() {
        //当图层所有子视图都移除时，才隐藏图层
        if (layer.childCount == 0) {
            super.hide()
        }
    }

}