package com.csw.android.videofloatwindow.ui.list

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.util.DBUtils
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_local_videos.*

class PlaySheetVideosActivity : AppCompatActivity() {
    private lateinit var videosAdapter: VideosAdapter

    companion object {
        fun openActivity(context: Context, playSheetId: Long) {
            val intent = Intent(context, PlaySheetVideosActivity::class.java)
            intent.putExtra("playSheetId", playSheetId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_local_videos)
        recyclerView.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false)
        videosAdapter = VideosAdapter()
        videosAdapter.setOnItemClickListener { _, view, position ->
            val videoInfo = videosAdapter.getItem(position)
            videoInfo?.let {
                FullScreenActivity.openActivity(view.context, it)
            }
        }
        recyclerView.adapter = videosAdapter
        val playSheetId = intent.getLongExtra("playSheetId", 0)
        if (playSheetId > 0) {
            RxPermissions(this)
                    .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .map {
                        if (it) {
                            return@map DBUtils.getVideosByPlaySheetId(playSheetId)
                        } else {
                            Snackbar.make(recyclerView, "SD卡文件读取权限被拒绝", Snackbar.LENGTH_SHORT).show()
                            return@map arrayListOf<VideoInfo>()
                        }
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                videosAdapter.setNewData(it)
                                MyApplication.instance.playerHelper.playList = it
                            },
                            {
                                Snackbar.make(recyclerView, it.message
                                        ?: "未知异常", Snackbar.LENGTH_SHORT).show()
                            })
        }
    }

}