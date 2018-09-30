package com.csw.android.videofloatwindow.ui.list

import android.graphics.Bitmap
import android.provider.MediaStore
import android.provider.MediaStore.Images.Thumbnails.MINI_KIND
import android.support.design.widget.Snackbar
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.util.Utils
import com.csw.android.videofloatwindow.view.WHRatioImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class VideosAdapter : BaseQuickAdapter<VideoInfo, BaseViewHolder> {

    constructor() : super(R.layout.item_video)

    override fun convert(helper: BaseViewHolder?, item: VideoInfo?) {
        Utils.runIfNotNull(helper, item) { h, i ->
            h.setText(R.id.tv_name, i.fileName)
            h.setText(R.id.tv_desc, i.filePath)
            loadVideoPreviewImage(h.getView(R.id.iv_image), i)
        }
    }

    private fun loadVideoPreviewImage(view: WHRatioImageView?, videoInfo: VideoInfo) {
        view?.let { image ->
            image.whRatio = videoInfo.whRatio
            image.setImageDrawable(null)//置空图片
            //取消上个加载任务
            val disposable = image.tag
            disposable?.let {
                if (disposable is Disposable && !disposable.isDisposed) {
                    disposable.dispose()
                }
                image.tag = null
            }
            //加载当前图片
            val videoDbId = videoInfo.mediaDbId
            image.tag = Observable.create<Bitmap> {
                val bitmap = MediaStore.Video.Thumbnails.getThumbnail(
                        image.context.contentResolver,
                        videoDbId,
                        MINI_KIND,
                        null
                )
                if (bitmap != null) {
                    it.onNext(bitmap)
                    it.onComplete()
                } else {
                    it.onError(Throwable("no video image has found"))
                }
            }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                image.setImageBitmap(it)
                            },
                            {
                                Snackbar.make(image, it.message
                                        ?: "未知异常", Snackbar.LENGTH_SHORT).show()
                            })
        }
    }

}
