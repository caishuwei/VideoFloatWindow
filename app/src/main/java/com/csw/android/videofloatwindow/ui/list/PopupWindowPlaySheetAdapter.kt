package com.csw.android.videofloatwindow.ui.list

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.AddItem
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.util.Utils

class PopupWindowPlaySheetAdapter : BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {

    constructor(data: MutableList<MultiItemEntity>?) : super(data) {
        addItemType(PlaySheet.ITEM_TYPE, android.R.layout.simple_list_item_1)
        addItemType(AddItem.ITEM_TYPE, R.layout.item_add_play_sheet)
    }

    override fun convert(helper: BaseViewHolder?, item: MultiItemEntity?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            when (h.itemViewType) {
                PlaySheet.ITEM_TYPE -> {
                    val playSheet = item as PlaySheet
                    h.setText(android.R.id.text1, playSheet.name)
                }
            }
        }
    }

}