package com.csw.android.videofloatwindow.ui.main

import com.csw.android.videofloatwindow.app.Constants
import com.csw.android.videofloatwindow.dagger.BasePresenterImpl
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.entities.rxbus.OnPlaySheetNameUpdate
import com.csw.android.videofloatwindow.entities.rxbus.OnPlaySheetVideosUpdate
import com.csw.android.videofloatwindow.util.DBUtils
import com.csw.android.videofloatwindow.util.RxBus
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主界面切面，通过注解构造方法，可以实现将该类的实例化方式加入dagger框架中，构造函数的参数由dagger框架提供
 */
class MainPresenter @Inject constructor(view: MainContract.View) : BasePresenterImpl<MainContract.View>(view), MainContract.Presenter {

    private var data = ArrayList<PlaySheet>()

    override fun onCreated() {
        super.onCreated()
        addRxJavaTaskRunOnPresenterLive(
                RxBus.getDefault().toObservable(OnPlaySheetVideosUpdate::class.java)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            for (ps in data) {
                                if (it.playSheetId == ps.id) {
                                    if (uiIsAlive) {
                                        view.refreshPlaySheetList()
                                    } else {
                                        data.clear()
                                    }
                                    return@subscribe
                                }
                            }
                        }
        )
        addRxJavaTaskRunOnPresenterLive(
                RxBus.getDefault().toObservable(OnPlaySheetNameUpdate::class.java)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            for (ps in data) {
                                if (it.playSheetId == ps.id) {
                                    ps.name = it.newName
                                    if (uiIsAlive) {
                                        view.updatePlaySheets(data)
                                    }
                                    return@subscribe
                                }
                            }
                        }
        )
    }

    override fun initUIData() {
        super.initUIData()
        if (data.isEmpty()) {
            view.refreshPlaySheetList()
        } else {
            view.updatePlaySheets(data)
        }
    }

    override fun isPlaySheetExist(name: String): Boolean {
        return DBUtils.getPlaySheetByName(name) != null
    }

    override fun addPlaySheet(playSheet: PlaySheet) {
        DBUtils.insertPlaySheetIfNotExist(playSheet)
        data.add(playSheet)
        view.updatePlaySheets(data)
    }

    override fun removePlaySheet(it: PlaySheet) {
        DBUtils.deletePlaySheet(it)
        data.remove(it)
        view.updatePlaySheets(data)
    }

    override fun requestPlaySheets() {
        data.clear()
        addRxJavaTaskRunOnUILive(
                Observable.create(
                        ObservableOnSubscribe<List<PlaySheet>> {
                            val result = arrayListOf<PlaySheet>()
                            DBUtils.insertPlaySheetIfNotExist(PlaySheet(Constants.LOCAL_VIDEO_PLAY_SHEET))//插入本地视频播放列表
                            result.addAll(DBUtils.getPlaySheets())
                            for (ps in result) {
                                ps.playSheetVideos//调用一次，获取数据库该歌单的视频列表，这里是子线程执行，反正回调后也需要视频列表，先行调用一次减少主线程工作量
                            }
                            it.onNext(result)
                            it.onComplete()
                        }
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                {
                                    data.addAll(it)
                                    view.updatePlaySheets(data)
                                    view.onRequestPlaySheetsSucceed()
                                }
                                ,
                                {
                                    view.onRequestPlaySheetsFailed("读取播放列表失败")
                                }
                        )
        )
    }

}