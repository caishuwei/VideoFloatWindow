package com.csw.android.videofloatwindow.ui

import android.graphics.Bitmap
import android.provider.MediaStore
import android.provider.MediaStore.Images.Thumbnails.MINI_KIND
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.view.WHRatioImageView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class VideosAdapter : RecyclerView.Adapter<MyViewHolder>() {

    private val data: ArrayList<VideoInfo> = arrayListOf()

    fun setNewData(newData: List<VideoInfo>?) {
        data.clear()
        newData?.let { data.addAll(newData) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(p0.context)
                .inflate(R.layout.item_video, p0, false))
    }

    override fun onBindViewHolder(p0: MyViewHolder, p1: Int) {
        p0.updateView(data[p1])
    }

    override fun getItemCount(): Int {
        return data.size
    }

}

class MyViewHolder : RecyclerView.ViewHolder {

    companion object {
        val thumbColumns = Array<String>(1) {
            MediaStore.Video.Thumbnails._ID
        }
    }

    private val image: WHRatioImageView = itemView.findViewById(R.id.iv_image)
    private val name: TextView = itemView.findViewById(R.id.tv_name)
    private val desc: TextView = itemView.findViewById(R.id.tv_desc)
    private var videoInfo: VideoInfo? = null

    constructor(itemView: View) : super(itemView) {
        itemView.setOnClickListener { v ->
            videoInfo?.let {
                FullScreenActivity.openActivity(v.context, it)
            }
        }
    }


    fun updateView(videoInfo: VideoInfo) {
        this.videoInfo = videoInfo
        name.text = videoInfo.fileName
        desc.text = videoInfo.filePath

        image.whRatio = videoInfo.getWHRatio()
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
