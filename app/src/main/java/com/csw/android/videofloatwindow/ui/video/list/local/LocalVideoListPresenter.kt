package com.csw.android.videofloatwindow.ui.video.list.local

import android.provider.MediaStore
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.ui.video.list.VideoListPresenter
import com.csw.android.videofloatwindow.util.DBUtils
import com.csw.android.videofloatwindow.util.LogUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 本地视频MVP切面实现
 *
 * Inject注解，将本类的实例构建方式注入Dagger框架中
 */
class LocalVideoListPresenter<T : LocalVideoListContract.View> @Inject constructor(view: T) :
        VideoListPresenter<LocalVideoListContract.View>(view),
        LocalVideoListContract.Presenter {


    override fun scanLocalVideos(localPlaySheetId: Long?) {
        view.onScanLocalVideosStart()
        if (localPlaySheetId == null) {
            view.onScanLocalVideosFailed("localPlaySheetId 为空")
            return
        }
        addRxJavaTaskRunOnUILive(Observable.create(ObservableOnSubscribe<ArrayList<VideoInfo>> { emitter ->
            //查媒体库
            val result = getLocalVideos()
            LogUtils.i("addRxJavaTaskRunOnUILive",""+emitter.isDisposed)
            if (emitter.isDisposed) {
                return@ObservableOnSubscribe
            }
            //数据库写入
            DBUtils.updatePlaySheetVideos(localPlaySheetId, result)
            if (emitter.isDisposed) {
                return@ObservableOnSubscribe
            }
            //回调结果
            emitter.onNext(result)
            emitter.onComplete()
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            view.updatePlaySheetVideos(it)
                            view.onScanLocalVideosSucceed()
                        },
                        {
                            view.onScanLocalVideosFailed("媒体库扫描失败")
                        }
                ))
    }


    /**
     * 查询设备媒体库获取所有视频文件数据记录，数据量大的时候耗时较长
     */
    private fun getLocalVideos(): ArrayList<VideoInfo> {
        val data = arrayListOf<VideoInfo>()
        val cursor = MyApplication.instance.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,//要查询的列，null所有列
                null,//筛选条件（用占位符代替参数值）
                null,//筛选参数
                MediaStore.Video.Media.DATE_MODIFIED + " DESC "//按数据修改日期反向排序
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    data.add(DBUtils.insertVideoInfo(VideoInfo.readFromCursor(cursor)))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return data
    }

}