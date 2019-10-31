package com.csw.android.videofloatwindow.ui.video.sheet

import com.csw.android.videofloatwindow.dagger.IBasePresenter
import com.csw.android.videofloatwindow.dagger.IBaseView
import com.csw.android.videofloatwindow.entities.VideoInfo

public interface PlaySheetEditContract {

    public interface Presenter : IBasePresenter {
        fun setPlaySheetId(playSheetId: Long)
        fun loadPlaySheet()
        fun savePlaySheetVideos(videoList: List<VideoInfo>)
    }

    public interface View : IBaseView {
        fun updatePlaySheet1(videoInfoList: ArrayList<VideoInfo>)
        fun updatePlaySheet2(videoInfoList: ArrayList<VideoInfo>)
    }
}