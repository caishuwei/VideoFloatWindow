package com.csw.android.videofloatwindow.ui.main

import android.view.View.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.Constants
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.entities.PlaySheetVideo
import com.csw.android.videofloatwindow.util.ImageLoader
import com.csw.android.videofloatwindow.util.Utils

class PlaySheetAdapter(val activity: MainActivity) : BaseItemDraggableAdapter<PlaySheet, BaseViewHolder>(R.layout.item_play_sheet_list, null) {

    override fun convert(helper: BaseViewHolder?, item: PlaySheet?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            when (i.itemType) {
                Constants.ItemTypeEnum.PLAY_SHEET -> {
                    h.setText(R.id.tv_name, i.name)
                    h.setText(R.id.tv_count, "+${i.playSheetVideos.size}")
                    val recyclerView = h.getView<RecyclerView>(R.id.recyclerView)
                    recyclerView.visibility = VISIBLE
                    val oldAdapter = recyclerView.adapter
                    val videosAdapter =
                            if (oldAdapter is VideosAdapter) {
                                oldAdapter
                            } else {
                                val va = VideosAdapter()
                                recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
                                recyclerView.adapter = va
                                va
                            }
                    videosAdapter.setNewData(i.playSheetVideos)
                }
                Constants.ItemTypeEnum.EMPTY_PLAY_SHEET -> {
                    h.setText(R.id.tv_name, i.name)
                    h.setVisible(R.id.tv_count, false)
                    h.setVisible(R.id.recyclerView, false)
                }
            }
        }
    }

    private inner class VideosAdapter() : BaseQuickAdapter<PlaySheetVideo, BaseViewHolder>(R.layout.item_main_play_sheet_video) {
        override fun convert(helper: BaseViewHolder?, item: PlaySheetVideo?) {
            Utils.runIfNotNull(helper, item) { h, psv ->
                ImageLoader.loadImage(activity, h.getView(R.id.iv_image), psv.videoInfo.imageUri)
            }
        }
    }

}