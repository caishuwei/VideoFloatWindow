package com.csw.android.videofloatwindow.ui.list

import android.view.ViewGroup
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.video.CustomVideoView
import com.csw.android.videofloatwindow.util.Utils
import com.csw.android.videofloatwindow.view.ListVideoContainer

class LargeVideosAdapter : VideosAdapter(R.layout.item_large_video) {

    private val videoContainerSet = HashSet<ListVideoContainer>()
    var onVideoPlayListener: CustomVideoView.OnVideoPlayListener? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    fun releaseAllVideoView() {
        for (videoContainer in videoContainerSet) {
            videoContainer.releaseVideoView()
        }
    }

    fun pauseAllVideoView() {
        for (videoContainer in videoContainerSet) {
            videoContainer.pause()
        }
    }

    override fun createBaseViewHolder(parent: ViewGroup?, layoutResId: Int): BaseViewHolder {
        val result = super.createBaseViewHolder(parent, layoutResId)
        val listVideoContainer = result.getView<ListVideoContainer>(R.id.mv_video_container)
        listVideoContainer?.let {
            videoContainerSet.add(listVideoContainer)
        }
        return result
    }

    override fun convert(helper: BaseViewHolder?, item: VideoInfo?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            h.setText(R.id.tv_name, i.fileName)
            h.setText(R.id.tv_desc, i.filePath)
            val listVideoContainer = h.getView<ListVideoContainer>(R.id.mv_video_container)
            listVideoContainer.videoInfo = i
            listVideoContainer.onVideoPlayListener = onVideoPlayListener
            listVideoContainer.pause()
            loadVideoPreviewImage(listVideoContainer.whRatioImageView, i)
        }
    }


}