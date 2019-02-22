package com.csw.android.videofloatwindow.ui.list

import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayerHelper
import com.csw.android.videofloatwindow.util.Utils
import com.csw.android.videofloatwindow.view.ListVideoContainer

class LargeVideosAdapter : VideosAdapter(R.layout.item_large_video) {

    var onVideoPlayListener: PlayerHelper.OnVideoPlayListener? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun convert(helper: BaseViewHolder?, item: VideoInfo?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            h.setText(R.id.tv_name, i.fileName)
            h.setText(R.id.tv_desc, i.filePath)
            val mv_video_container = h.getView<ListVideoContainer>(R.id.mv_video_container)
            mv_video_container.unBindPlayer()
            mv_video_container.setVideoInfo(i)
            mv_video_container.whRatio = i.whRatio
            mv_video_container.onVideoPlayListener = onVideoPlayListener
            loadVideoPreviewImage(mv_video_container.whRatioImageView, i)
        }
    }

}