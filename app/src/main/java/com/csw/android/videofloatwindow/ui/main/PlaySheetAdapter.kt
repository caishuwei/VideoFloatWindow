package com.csw.android.videofloatwindow.ui.main

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.util.Utils

class PlaySheetAdapter : BaseQuickAdapter<PlaySheet, BaseViewHolder> {

    constructor() : super(android.R.layout.simple_list_item_1)

    override fun convert(helper: BaseViewHolder?, item: PlaySheet?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            h.setText(android.R.id.text1, i.name)
        }
    }

}