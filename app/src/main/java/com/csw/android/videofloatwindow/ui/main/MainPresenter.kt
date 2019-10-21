package com.csw.android.videofloatwindow.ui.main

import com.csw.android.videofloatwindow.app.Constants
import com.csw.android.videofloatwindow.dagger.BasePresenterImpl
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.util.DBUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainPresenter(view: MainContract.View) : BasePresenterImpl<MainContract.View>(view), MainContract.Presenter {

    override fun removePlaySheet(it: PlaySheet) {
        DBUtils.deletePlaySheet(it)
    }

    override fun loadPlaySheets() {
        addRxJavaTaskDisposable(
                Observable.create(
                        ObservableOnSubscribe<List<PlaySheet>> {
                            val result = arrayListOf<PlaySheet>()
                            DBUtils.insertPlaySheetIfNotExist(PlaySheet(Constants.LOCAL_VIDEO_PLAY_SHEET))//插入本地视频播放列表
                            result.addAll(DBUtils.getPlaySheets())
                            it.onNext(result)
                            it.onComplete()
                        }
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    view.updatePlaySheets(it)
                                    view.onLoadPlaySheetsSucceed()
                                }
                                ,
                                {
                                    view.onLoadPlaySheetsFailed("读取播放列表失败")
                                }
                        )
        )
    }

}