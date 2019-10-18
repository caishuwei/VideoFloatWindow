package com.csw.android.videofloatwindow.ui.list

import android.provider.MediaStore
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.util.ImageLoader
import com.csw.android.videofloatwindow.util.Utils
import com.csw.android.videofloatwindow.view.WHRatioImageView

open class VideosAdapter : BaseQuickAdapter<VideoInfo, BaseViewHolder> {

    constructor() : this(R.layout.item_video)
    constructor(layoutResId: Int) : super(layoutResId)


    override fun convert(helper: BaseViewHolder?, item: VideoInfo?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            h.setText(R.id.tv_name, i.fileName)
            h.setText(R.id.tv_desc, i.filePath)
            loadVideoPreviewImage(h.getView(R.id.iv_image), i)
        }
    }

    fun loadVideoPreviewImage(view: WHRatioImageView?, videoInfo: VideoInfo) {

        view?.let { image ->
            //"content://media/external/video/media/450433"
            ImageLoader.loadImage(null, image, "${MediaStore.Video.Media.EXTERNAL_CONTENT_URI}/${videoInfo.mediaDbId}")
        }
    }

}

