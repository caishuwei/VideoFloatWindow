package com.csw.android.videofloatwindow.ui.list

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayList
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.ui.base.BaseActivity
import com.csw.android.videofloatwindow.util.DBUtils
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_local_videos.*

class PlaySheetVideosActivity : BaseActivity() {

    private lateinit var videosAdapter: VideosAdapter

    companion object {
        fun openActivity(context: Context, playSheetId: Long) {
            val intent = Intent(context, PlaySheetVideosActivity::class.java)
            intent.putExtra("playSheetId", playSheetId)
            context.startActivity(intent)
        }
    }

    override fun getContentViewID(): Int {
        return R.layout.activity_local_videos
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(
                this,
                RecyclerView.VERTICAL,
                false)
    }

    override fun initAdapter() {
        super.initAdapter()
        videosAdapter = VideosAdapter()
        recyclerView.adapter = videosAdapter
    }

    override fun initListener() {
        super.initListener()
        videosAdapter.setOnItemClickListener { _, view, position ->
            val videoInfo = videosAdapter.getItem(position)
            videoInfo?.let {
                FullScreenActivity.openActivity(view.context, it)
            }
        }
    }

    override fun initData() {
        super.initData()
        val playSheetId = intent.getLongExtra("playSheetId", 0)
        if (playSheetId > 0) {
            addLifecycleTask(RxPermissions(this)
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
                                PlayList.data = it
                            },
                            {
                                Snackbar.make(recyclerView, it.message
                                        ?: "未知异常", Snackbar.LENGTH_SHORT).show()
                            }
                    )
            )
        }
    }

}