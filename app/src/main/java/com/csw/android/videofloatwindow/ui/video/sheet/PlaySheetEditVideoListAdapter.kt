package com.csw.android.videofloatwindow.ui.video.sheet

import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.util.ImageLoader
import com.csw.android.videofloatwindow.util.Utils

open class PlaySheetEditVideoListAdapter(val fragment: Fragment) : BaseItemDraggableAdapter<VideoInfo, BaseViewHolder>(R.layout.item_play_sheet_edit_video_list,null) {

    override fun convert(helper: BaseViewHolder?, item: VideoInfo?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            h.setText(R.id.tv_name, i.fileName)
            h.getView<ImageView>(R.id.iv_image)?.let {
                ImageLoader.loadImage(fragment, it, i.imageUri)
            }
        }
    }

}

