package com.csw.android.videofloatwindow.ui.main

import com.csw.android.videofloatwindow.dagger.IBasePresenter
import com.csw.android.videofloatwindow.dagger.IBaseView
import com.csw.android.videofloatwindow.entities.PlaySheet

interface MainContract {

    interface Presenter : IBasePresenter {
        fun loadPlaySheets()
        fun removePlaySheet(it: PlaySheet)
    }

    interface View : IBaseView {
        fun updatePlaySheets(playSheets: List<PlaySheet>)
        fun onLoadPlaySheetsSucceed()
        fun onLoadPlaySheetsFailed(errorMsg: String)

    }

}