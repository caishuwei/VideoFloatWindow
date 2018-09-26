package com.csw.android.videofloatwindow.ui

import android.Manifest
import android.media.MediaMetadataRetriever
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.services.video.VideoService
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_local_videos.*

class LocalVideosActivity : AppCompatActivity() {

    private lateinit var adapter: VideosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_videos)
        recyclerView.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false)
        adapter = VideosAdapter()
        recyclerView.adapter = adapter

        RxPermissions(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .map {
                    if (it) {
                        return@map getLocalVideos()
                    } else {
                        Snackbar.make(recyclerView, "SD卡文件读取权限被拒绝", Snackbar.LENGTH_SHORT).show()
                        return@map arrayListOf<VideoInfo>()
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            adapter.setNewData(it)
                            MyApplication.instance.playerHelper.playList = it
                        },
                        {
                            Snackbar.make(recyclerView, it.message
                                    ?: "未知异常", Snackbar.LENGTH_SHORT).show()
                        })

    }

    private fun getLocalVideos(): ArrayList<VideoInfo> {
        val data = arrayListOf<VideoInfo>()
        val projection = arrayOf(MediaStore.Video.Media.DATA,//文件地址
                MediaStore.Video.Media.DURATION,//视频长度
                MediaStore.Video.Media.SIZE,//视频文件大小
                MediaStore.Video.Media.DISPLAY_NAME,//视频文件名称
                MediaStore.Video.Media._ID,//数据库纪录ID
                MediaStore.Video.Media.WIDTH,//视频宽度
                MediaStore.Video.Media.HEIGHT,//视频高度
                MediaStore.Video.Media.RESOLUTION//视频分辨率
        )
        val cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Video.Media.DATE_MODIFIED + " DESC "//按数据修改日期反向排序
        )
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val metaData = MediaMetadataRetriever()
                var filePath: String?
                var rotation: Float
                var width: Int
                var height: Int
                while (cursor.moveToNext()) {
                    filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    metaData.setDataSource(filePath)
                    val rotationStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    } else {
                        "0"
                    }
                    rotation = try {
                        rotationStr.toFloat()
                    } catch (e: Exception) {
                        0f
                    }
                    when (rotation) {
                        90f, -90f, 270f -> {
                            height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH))
                            width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT))
                        }
                        else -> {
                            width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH))
                            height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT))
                        }
                    }

                    data.add(VideoInfo(
                            filePath,
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)),
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)),
                            cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID)),
                            width,
                            height,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION))
                    ))
                }
            }
            cursor.close()
        }
        return data
    }
}