package com.csw.android.videofloatwindow.ui.video.list

import com.csw.android.videofloatwindow.dagger.BasePresenterImpl
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.util.DBUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * 本地视频MVP切面实现
 *
 * Inject注解，将本类的实例构建方式注入Dagger框架中
 */
open class VideoListPresenter<T : VideoListContract.View> constructor(view: T) :
        BasePresenterImpl<T>(view),
        VideoListContract.Presenter {

    override fun loadPlaySheetById(playSheetId: Long?) {
        if (playSheetId == null) {
            view.onLoadPlaySheetFailed("歌单Id为空")
        } else {
            addRxJavaTaskDisposable(Observable.create(ObservableOnSubscribe<ArrayList<VideoInfo>> { emitter ->
                emitter.onNext(DBUtils.getVideosByPlaySheetId(playSheetId))
                emitter.onComplete()
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                view.updatePlaySheetVideos(it)
                                view.onLoadPlaySheetSucceed()
                            },
                            {
                                view.onLoadPlaySheetFailed("歌单 $playSheetId 数据加载失败")
                            }
                    ))
        }
    }


}