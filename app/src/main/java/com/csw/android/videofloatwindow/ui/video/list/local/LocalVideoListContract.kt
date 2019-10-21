package com.csw.android.videofloatwindow.ui.video.list.local

import com.csw.android.videofloatwindow.ui.video.list.VideoListContract

interface LocalVideoListContract {
    interface Presenter : VideoListContract.Presenter {
        /**
         * 扫描本地视频
         */
        fun scanLocalVideos(localPlaySheetId:Long?)
    }

    interface View : VideoListContract.View {
        fun onScanLocalVideosStart()
        fun onScanLocalVideosSucceed()
        fun onScanLocalVideosFailed(errorMsg: String)
    }
}