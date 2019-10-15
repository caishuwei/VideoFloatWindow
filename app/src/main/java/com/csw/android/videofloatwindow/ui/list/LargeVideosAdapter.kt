package com.csw.android.videofloatwindow.ui.list

import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.util.ScreenInfo
import com.csw.android.videofloatwindow.util.Utils
import com.csw.android.videofloatwindow.view.ListVideoContainer

class LargeVideosAdapter(val fragment: Fragment) : VideosAdapter(R.layout.item_large_video) {
    var maxItemHeight: Int = ScreenInfo.HEIGHT

    override fun convert(helper: BaseViewHolder?, item: VideoInfo?) {
        Utils.runIfNotNull(helper, item) { h, i ->
//            h.itemView.
            h.setText(R.id.tv_name, i.fileName)
            h.setText(R.id.tv_desc, i.filePath)
            val listVideoContainer = h.getView<ListVideoContainer>(R.id.mv_video_container)
            listVideoContainer.releaseOnUiDestroy(fragment.childFragmentManager)
            listVideoContainer.setVideoInfo(i)
//            listVideoContainer.whRatio = i.whRatio
            listVideoContainer.bindVideoView()
            loadVideoPreviewImage(listVideoContainer.whRatioImageView, i)
        }
    }
}