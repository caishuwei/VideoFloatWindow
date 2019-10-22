package com.csw.android.videofloatwindow.ui.video.sheet

import com.csw.android.videofloatwindow.app.Constants
import com.csw.android.videofloatwindow.dagger.BasePresenterImpl
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.util.DBUtils
import com.csw.android.videofloatwindow.util.Utils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class PlaySheetEditPresenter @Inject constructor(view: PlaySheetEditContract.View) : BasePresenterImpl<PlaySheetEditContract.View>(view), PlaySheetEditContract.Presenter {


    override fun loadPlaySheet(playSheetId: Long) {
        addRxJavaTaskDisposable(
                Observable.create(ObservableOnSubscribe<Pair<ArrayList<VideoInfo>,ArrayList<VideoInfo>>> { emitter ->
                    val videoInfoList1 = DBUtils.getVideosByPlaySheetId(playSheetId)
                    val videoInfoList2 = DBUtils.getVideosByPlaySheetName(Constants.LOCAL_VIDEO_PLAY_SHEET)
                    val iterator = videoInfoList2.iterator()
                    var vi: VideoInfo
                    while (iterator.hasNext()) {
                        vi = iterator.next()
                        for (vi1 in videoInfoList1) {
                            if (Utils.videoEquals(vi, vi1)) {
                                iterator.remove()
                            }
                        }
                    }
                    emitter.onNext(Pair(videoInfoList1,videoInfoList2))
                    emitter.onComplete()
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            view.updatePlaySheet1(it.first)
                            view.updatePlaySheet2(it.second)
                        }
        )
    }

    override fun savePlaySheetVideos(id: Long, videoList: List<VideoInfo>) {
        DBUtils.updatePlaySheetVideos(id,videoList)
    }

}