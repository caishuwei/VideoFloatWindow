package com.csw.android.videofloatwindow.ui.list

import android.Manifest
import android.graphics.Rect
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.view.View
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.dagger.localvideos.LocalVideosContract
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.video.CustomVideoView
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.ui.base.MvpFragment
import com.csw.android.videofloatwindow.util.LogUtils
import com.csw.android.videofloatwindow.util.Utils
import com.csw.android.videofloatwindow.view.ListVideoContainer
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_local_videos.*
import javax.inject.Inject

class LocalVideosFragment() : MvpFragment(), LocalVideosContract.View {

    private var linearLayoutManager: LinearLayoutManager? = null
    private var videosAdapter: LargeVideosAdapter? = null
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
        linearLayoutManager = LinearLayoutManager(
                rootView.context,
                LinearLayoutManager.VERTICAL,
                false)
        recyclerView.layoutManager = linearLayoutManager
    }


    override fun initAdapter() {
        super.initAdapter()
        videosAdapter = LargeVideosAdapter()
        recyclerView.adapter = videosAdapter
    }

    override fun initListener() {
        super.initListener()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var needFindPlayVideo = false

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (needFindPlayVideo) {
                    playVisibleVideo()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        playVisibleVideo()
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        needFindPlayVideo = true
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                        needFindPlayVideo = false
                    }
                }
            }
        })
        videosAdapter?.let {
            it.setOnItemClickListener { _, view, position ->
                val videoInfo = it.getItem(position)
                videoInfo?.let {
                    FullScreenActivity.openActivity(view.context, it)
                }
            }

        }
        CustomVideoView.addOnPlayVideoChangeListener(object : CustomVideoView.OnPlayChangeListener {
            override fun onPlayCompleted(videoInfo: VideoInfo) {
            }

            override fun onLastPlayVideoViewUpdate(old: CustomVideoView?, new: CustomVideoView?) {
            }

            override fun onPlayVideoInfoUpdated(videoInfo: VideoInfo?) {
                Utils.runIfNotNull(videoInfo,videosAdapter?.data){videoInfo,data->
                    var currIndex = -1
                    for ((index, vi) in data.withIndex()) {
                        if (Utils.videoEquals(videoInfo,vi)) {
                            currIndex = index
                            break
                        }
                    }
                    if (currIndex >= 0 && currIndex  < data.size) {
                        //滚动到下个视频item出现在屏幕上
                        playByPosition(currIndex)
                    }
                }
            }
        })
        smartRefreshLayout.isEnableRefresh = true
        smartRefreshLayout.isEnableLoadMore = false
        smartRefreshLayout.setOnRefreshListener {
            videosAdapter?.setNewData(null)
            videosAdapter?.pauseAllVideoView();
            requestLocalVideos()
        }
    }

    private fun playVisibleVideo() {
        val start = System.currentTimeMillis()
        Utils.runIfNotNull(videosAdapter, linearLayoutManager) { arg1, arg2 ->
            val fv = arg2.findFirstVisibleItemPosition()
            val lv = arg2.findLastVisibleItemPosition()
            if (fv >= 0) {
                if (fv == lv) {
                    //可见区域只有一个Item
                    val view = arg2.findViewByPosition(fv)
                    view?.let { itemView ->
                        val vh = recyclerView.getChildViewHolder(itemView) as BaseViewHolder
                        val listVideoContainer = vh.getView<ListVideoContainer>(R.id.mv_video_container)
                        listVideoContainer.play()
                    }
                } else {
                    val view1 = arg2.findViewByPosition(fv)
                    val view2 = arg2.findViewByPosition(fv + 1)
                    Utils.runIfNotNull(view1, view2) { v1, v2 ->
                        var intArray = IntArray(2)
                        val listRect = Rect(0, 0, recyclerView.width, recyclerView.height)
                        recyclerView.getLocationInWindow(intArray)
                        listRect.offset(intArray[0], intArray[1])
                        val videoRect = Rect()
                        val vh1 = recyclerView.getChildViewHolder(v1) as BaseViewHolder
                        val video1 = vh1.getView<ListVideoContainer>(R.id.mv_video_container)
                        videoRect.set(0, 0, video1.width, video1.height)
                        video1.getLocationInWindow(intArray)
                        videoRect.offset(intArray[0], intArray[1])
                        if (listRect.contains(videoRect)) {
                            //第一个可见item视频完全可见
                            video1.play()
                        } else {
                            val vh2 = recyclerView.getChildViewHolder(v2) as BaseViewHolder
                            val video2 = vh2.getView<ListVideoContainer>(R.id.mv_video_container)
                            if (videoRect.bottom <= listRect.top) {
                                //第一个可见item视频已经不可见
                                video2.play()
                            } else {
                                //第一个可见item视频部分可见
                                //如果第二个item视频已经完全可见，则播放第二个视频，否则播放第一个
                                videoRect.set(0, 0, video2.width, video2.height)
                                video2.getLocationInWindow(intArray)
                                videoRect.offset(intArray[0], intArray[1])
                                if (videoRect.bottom < listRect.bottom) {
                                    video2.play()
                                } else {
                                    video1.play()
                                }
                            }
                        }
                    }
                }
            }
        }
        LogUtils.e(msg = "playVisibleVideo->${System.currentTimeMillis() - start}")
    }

    private fun playByPosition(i: Int) {
        Utils.runIfNotNull(videosAdapter, linearLayoutManager) { arg1, arg2 ->
            val fv = arg2.findFirstVisibleItemPosition()
            val lv = arg2.findLastVisibleItemPosition()
            if (i in fv..lv) {
                val view = arg2.findViewByPosition(i)
                view?.let { itemView ->
                    val vh = recyclerView.getChildViewHolder(itemView) as BaseViewHolder
                    val listVideoContainer = vh.getView<ListVideoContainer>(R.id.mv_video_container)
                    listVideoContainer.play()
                }
                val smoothScroller = object : LinearSmoothScroller(activity) {

                    override fun getVerticalSnapPreference(): Int {
                        return LinearSmoothScroller.SNAP_TO_START
                    }

                    override fun calculateTimeForScrolling(dx: Int): Int {
                        //300毫秒完成滚动
                        return 300
                    }

                }
                smoothScroller.targetPosition = i
                arg2.startSmoothScroll(smoothScroller)
            } else if (i in 0 until arg1.data.size) {
                //当前视图不可见，直接定位到这个item
                arg2.scrollToPositionWithOffset(i, 0)
                recyclerView.post {
                    playByPosition(i)
                }
            }
        }
    }

    override fun initData() {
        super.initData()
        smartRefreshLayout.autoRefresh()
    }

    override fun onResume() {
        super.onResume()
        playVisibleVideo()
    }

    override fun onDestroyView() {
        linearLayoutManager = null
        videosAdapter?.releaseAllVideoView()
        videosAdapter = null
        super.onDestroyView()
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
                                        videosAdapter?.setNewData(it)
                                        MyApplication.instance.playerHelper.playList = it
                                        smartRefreshLayout.post {
                                            playVisibleVideo()
                                        }
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