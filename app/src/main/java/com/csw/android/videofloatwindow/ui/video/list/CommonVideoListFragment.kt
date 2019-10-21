package com.csw.android.videofloatwindow.ui.video.list

import android.os.Bundle
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.PlaySheet

/**
 * 通用的VideoList碎片实现
 */
class CommonVideoListFragment : VideoListView<VideoListContract.Presenter>() {
    companion object {
        fun createData(playSheet: PlaySheet): Bundle {
            val data = Bundle()
            data.putLong("playSheetId", playSheet.id)
            data.putString("playSheetName", playSheet.name)
            return data
        }
    }

    override fun initInject() {
        MyApplication.appComponent.getVideoListComponentBuilder()
                .setView(this)
                .build()
                .inject(this)
    }

}