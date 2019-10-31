package com.csw.android.videofloatwindow.ui.video.list

import com.csw.android.videofloatwindow.dagger.IBasePresenter
import com.csw.android.videofloatwindow.dagger.IBaseView
import com.csw.android.videofloatwindow.entities.VideoInfo

/**
 * 本地视频VP协议，定义View与Presenter之间比如提供的方法
 */
public interface VideoListContract {


    public interface Presenter : IBasePresenter {
        fun isPlaySheetExist(name: String): Boolean
        fun requestPlaySheetById(playSheetId: Long?)
        fun updatePlaySheetName(playSheetId: Long?,newName: String)
        fun setPlaySheetId(playSheetId: Long)
    }

    public interface View : IBaseView {
        fun refreshPlaySheetVideos()
        fun updatePlaySheetVideos(videoList: ArrayList<VideoInfo>)
        fun onRequestPlaySheetFailed(errorMsg: String)
        fun onRequestPlaySheetSucceed()
    }
}