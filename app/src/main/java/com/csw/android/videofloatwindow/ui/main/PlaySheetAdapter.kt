package com.csw.android.videofloatwindow.ui.main

import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.util.Utils

class PlaySheetAdapter : BaseItemDraggableAdapter<PlaySheet, BaseViewHolder> {

    constructor() : super(R.layout.item_play_sheet_list, null)

    override fun convert(helper: BaseViewHolder?, item: PlaySheet?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            h.setText(R.id.tv_name, i.name)
        }
    }


}