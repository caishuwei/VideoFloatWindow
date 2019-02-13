package com.csw.android.videofloatwindow.player.video.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.player.video.layer.LayerController


open class HintViewHolder {
    protected var mView: View
    protected var iv_hint_img: ImageView
    protected var tv_hint_text: TextView

    constructor(parent: ViewGroup) {
        this.mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_player_hint, parent, false)
        iv_hint_img = mView.findViewById(R.id.iv_hint_img) as ImageView
        tv_hint_text = mView.findViewById(R.id.tv_hint_text) as TextView
    }

    fun setHintInfo(imgResId: Int, msg: String) {
        iv_hint_img.setImageResource(imgResId)
        tv_hint_text.text = msg
    }

    open fun addToLayer(layerController: LayerController) {
        removeFromLayer(layerController)
        layerController.layer.addView(mView)
        layerController.show()
    }

    open fun removeFromLayer(layerController: LayerController) {
        val parent = mView.parent
        parent?.let {
            if (it is ViewGroup) {
                it.removeView(mView)
            }
        }
        layerController.hide()
    }

}