package com.csw.android.videofloatwindow.ui.list

import android.Manifest
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.dagger.localvideos.LocalVideosContract
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.ui.base.MvpFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_local_videos.*
import javax.inject.Inject

class LocalVideosFragment() : MvpFragment(), LocalVideosContract.View {

    companion object {
        fun newInstance(): LocalVideosFragment {
            return LocalVideosFragment()
        }
    }

    @Inject
    lateinit var presenter: LocalVideosContract.Presenter

    init {
        //初始化时，注入切片对象
        MyApplication.appComponent.getLocalVideosComponentBuilder()
                .setView(this)
                .build()
                .inject(this)
    }

    override fun getContentViewID(): Int {
        return R.layout.activity_local_videos
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        recyclerView.layoutManager = LinearLayoutManager(
                rootView.context,
                LinearLayoutManager.VERTICAL,
                false)
    }

    private lateinit var videosAdapter: VideosAdapter

    override fun initAdapter() {
        super.initAdapter()
        videosAdapter = LargeVideosAdapter()
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
        smartRefreshLayout.isEnableRefresh = true
        smartRefreshLayout.isEnableLoadMore = false
        smartRefreshLayout.setOnRefreshListener {
            videosAdapter.setNewData(null)
            requestLocalVideos()
        }
    }

    override fun initData() {
        super.initData()
        smartRefreshLayout.autoRefresh()
    }


    private fun requestLocalVideos() {
        val a = activity
        a?.let {
            addLifecycleTask(
                    RxPermissions(it)
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
                                        smartRefreshLayout.finishRefresh()
                                        videosAdapter.setNewData(it)
                                        MyApplication.instance.playerHelper.playList = it
                                    },
                                    {
                                        smartRefreshLayout.finishRefresh()
                                        Snackbar.make(recyclerView, it.message
                                                ?: "未知异常", Snackbar.LENGTH_SHORT).show()
                                    }
                            )
            )
        }
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
                    data.add(VideoInfo.readFromCursor(cursor))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return data
    }
}