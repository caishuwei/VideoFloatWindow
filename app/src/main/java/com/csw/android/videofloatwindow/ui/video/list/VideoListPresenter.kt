package com.csw.android.videofloatwindow.ui.video.list

import com.csw.android.videofloatwindow.dagger.BasePresenterImpl
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.entities.rxbus.OnPlaySheetNameUpdate
import com.csw.android.videofloatwindow.entities.rxbus.OnPlaySheetVideosUpdate
import com.csw.android.videofloatwindow.util.DBUtils
import com.csw.android.videofloatwindow.util.RxBus
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * 本地视频MVP切面实现
 *
 * Inject注解，将本类的实例构建方式注入Dagger框架中
 */
open class VideoListPresenter<T : VideoListContract.View> @Inject constructor(view: T) :
        BasePresenterImpl<T>(view),
        VideoListContract.Presenter {

    private var playSheetId: Long? = null
    private val data = ArrayList<VideoInfo>()

    override fun onCreated() {
        super.onCreated()
        addRxJavaTaskRunOnPresenterLive(
                RxBus.getDefault().toObservable(OnPlaySheetVideosUpdate::class.java)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            if (it.playSheetId == playSheetId) {
                                if (uiIsAlive) {
                                    view.refreshPlaySheetVideos()
                                } else {
                                    data.clear()
                                }
                                return@subscribe
                            }
                        }
        )
    }

    override fun setPlaySheetId(playSheetId: Long) {
        this.playSheetId = playSheetId
    }

    override fun initUIData() {
        super.initUIData()
        if (data.isEmpty()) {
            view.refreshPlaySheetVideos()
        } else {
            view.updatePlaySheetVideos(data)
        }
    }

    override fun updatePlaySheetName(playSheetId: Long?, newName: String) {
        playSheetId?.let {
            val playSheet = DBUtils.getPlaySheet(it)
            playSheet?.let { ps ->
                ps.name = newName
                DBUtils.updatePlaySheet(ps)
                RxBus.getDefault().post(OnPlaySheetNameUpdate(ps.id, ps.name))
            }
        }
    }

    override fun isPlaySheetExist(name: String): Boolean {
        return DBUtils.getPlaySheetByName(name) != null
    }

    override fun requestPlaySheetById(playSheetId: Long?) {
        if (playSheetId == null) {
            view.onRequestPlaySheetFailed("歌单Id为空")
        } else {
            addRxJavaTaskRunOnUILive(Observable.create(ObservableOnSubscribe<ArrayList<VideoInfo>> { emitter ->
                emitter.onNext(DBUtils.getVideosByPlaySheetId(playSheetId))
                emitter.onComplete()
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                view.updatePlaySheetVideos(it)
                                view.onRequestPlaySheetSucceed()
                            },
                            {
                                view.onRequestPlaySheetFailed("歌单 $playSheetId 数据加载失败")
                            }
                    ))
        }
    }


}