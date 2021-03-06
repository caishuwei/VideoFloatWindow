package com.csw.android.videofloatwindow.ui.video.list

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.util.ImageLoader
import com.csw.android.videofloatwindow.util.Utils
import com.csw.android.videofloatwindow.player.container.impl.ListVideoContainer

class LargeVideosAdapter(val fragment: Fragment) : BaseQuickAdapter<VideoInfo, BaseViewHolder>(R.layout.item_large_video) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val h = super.onCreateViewHolder(parent, viewType)
        val listVideoContainer = h.getView<ListVideoContainer>(R.id.mv_video_container)
        return h
    }

    override fun convert(helper: BaseViewHolder?, item: VideoInfo?) {
        Utils.runIfNotNull(helper, item) { h, i ->
//            h.itemView.
            h.setText(R.id.tv_name, i.fileName)
            h.setText(R.id.tv_desc, i.filePath)
            val listVideoContainer = h.getView<ListVideoContainer>(R.id.mv_video_container)
            listVideoContainer.setBindVideoOnViewEnterForeground(true)
            listVideoContainer.setPauseOnViewExitForeground(!PlayHelper.backgroundPlay)
            listVideoContainer.setReleaseOnViewDestroy(true)
            listVideoContainer.registerViewLifeCycleObserver(fragment.viewLifecycleOwner.lifecycle,true)
            listVideoContainer.setVideoInfo(i)
            ImageLoader.loadImage(fragment, listVideoContainer.whRatioImageView, i.imageUri)
        }
    }
}