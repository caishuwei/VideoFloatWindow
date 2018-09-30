package com.csw.android.videofloatwindow.entities

import com.chad.library.adapter.base.entity.MultiItemEntity

class AddItem : MultiItemEntity {
    companion object {
        const val ITEM_TYPE = 2
    }

    override fun getItemType(): Int {
        return ITEM_TYPE
    }
}