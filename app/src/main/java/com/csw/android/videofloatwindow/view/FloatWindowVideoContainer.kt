package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.ui.FullScreenActivity

class FloatWindowVideoContainer : VideoContainer {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onBindPlayer(playerBindHelper: PlayerHelper.PlayerBindHelper) {
        super.onBindPlayer(playerBindHelper)
        videoInfo?.let { it ->
            playerBindHelper.setBackClickListener(null)
                    .setTitle(it.fileName)
                    .setCloseClickListener(OnClickListener { _ ->
                        MyApplication.instance.playerHelper.hideFloatWindow()
                    })
                    .setFullScreenClickListener(OnClickListener { _ ->
                        playInFullScreen()
                    })
                    .setPreviousClickListener(
                            if (MyApplication.instance.playerHelper.hasPrevious()) {
                                OnClickListener { _ ->
                                    playPre()
                                }
                            } else {
                                null
                            }
                    )
                    .setNextClickListener(
                            if (MyApplication.instance.playerHelper.hasNext()) {
                                OnClickListener { _ ->
                                    playNext()
                                }
                            } else {
                                null
                            }
                    )
        }
    }

    private fun playNext() {
        val nextVideo = MyApplication.instance.playerHelper.getNext()
        if (nextVideo != null) {
            MyApplication.instance.playerHelper.playInFloatWindow(nextVideo)
        }
    }

    private fun playPre() {
        val preVideo = MyApplication.instance.playerHelper.getPrevious()
        if (preVideo != null) {
            MyApplication.instance.playerHelper.playInFloatWindow(preVideo)
        }
    }

    private fun playInFullScreen() {
        videoInfo?.let {
            FullScreenActivity.openActivity(context, it)
        }
    }

}