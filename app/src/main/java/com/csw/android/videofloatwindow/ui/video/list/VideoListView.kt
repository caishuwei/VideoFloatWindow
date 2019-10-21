package com.csw.android.videofloatwindow.ui.video.list

import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseViewHolder
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.PlayList
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.ui.base.BaseMVPFragment
import com.csw.android.videofloatwindow.ui.video.full_screen.FullScreenActivity
import com.csw.android.videofloatwindow.util.ScreenInfo
import com.csw.android.videofloatwindow.util.Utils
import com.csw.android.videofloatwindow.view.ListVideoContainer
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_local_videos.*

open class VideoListView<T : VideoListContract.Presenter>() : BaseMVPFragment<T>(), VideoListContract.View {

    companion object {
        fun createData(playSheet: PlaySheet): Bundle {
            val data = Bundle()
            data.putLong("playSheetId", playSheet.id)
            data.putString("playSheetName", playSheet.name)
            return data
        }
    }

    private var linearLayoutManager: LinearLayoutManager? = null
    private var videosAdapter: LargeVideosAdapter? = null

    /**
     * 监听顶层视频容器变化，实现悬浮窗关闭时在列表播放视频
     */
    private val onTopLevelVideoContainerChangeListener = object : PlayHelper.OnTopLevelVideoContainerChangeListener {
        override fun onTopLevelVideoContainerChanged(videoContainer: VideoContainer?) {
            if (videoContainer == null) {
                play()
            }
        }
    }
    /**
     * 当前播放列表，视频播放结束处理，用于取代默认的后台播放下一个
     */
    private val onPlayEndHandler = object : PlayHelper.OnPlayEndHandler {
        override fun handlePlayEnd(videoInfo: VideoInfo?) {
            Utils.runIfNotNull(PlayHelper.lastPlayVideo, videosAdapter?.data) { video, data ->
                if (video.isEnd()) {
                    var currIndex = -1
                    for ((index, vi) in data.withIndex()) {
                        if (Utils.videoEquals(video.getVideoInfo(), vi)) {
                            currIndex = index + 1
                            break
                        }
                    }
                    if (currIndex >= 0 && currIndex < data.size) {
                        //滚动到下个视频item出现在屏幕上
                        playByPosition(currIndex)
                    }
                }
            }
        }
    }
    //当前的歌单Id
    var playSheetId: Long? = null

    override fun getContentViewID(): Int {
        return R.layout.fragment_video_list
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        super.initView(rootView, savedInstanceState)
        linearLayoutManager = LinearLayoutManager(
                rootView.context,
                RecyclerView.VERTICAL,
                false)
        recyclerView.layoutManager = linearLayoutManager
    }

    override fun initAdapter() {
        super.initAdapter()
        videosAdapter = LargeVideosAdapter(this)
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
        videosAdapter?.let { videosAdapter ->
            videosAdapter.setOnItemClickListener { _, view, position ->
                val videoInfo = videosAdapter.getItem(position)
                videoInfo?.let {
                    FullScreenActivity.openActivity(view.context, it)
                }
            }

        }
        smartRefreshLayout.setEnableRefresh(true)
        smartRefreshLayout.setEnableLoadMore(false)
        smartRefreshLayout.setOnRefreshListener {
            videosAdapter?.setNewData(null)
            runIfExternalStoragePermissionGranted { granted ->
                if (granted) {
                    presenter.loadPlaySheetById(playSheetId)
                } else {
                    smartRefreshLayout.finishRefresh(false)
                }
            }
        }
        PlayHelper.addOnTopLevelVideoContainerChangeListener(onTopLevelVideoContainerChangeListener)
    }


    override fun initData() {
        super.initData()
        playSheetId = arguments?.getLong("playSheetId")
        val playSheetName = arguments?.getString("playSheetName")
        setupTitleBar(playSheetName)
        if (playSheetId != null) {
            smartRefreshLayout.autoRefresh()
        } else {
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        PlayHelper.setOnPlayEndHandler(onPlayEndHandler)
        play()
    }

    override fun onPause() {
        super.onPause()
        PlayHelper.setOnPlayEndHandler(null)
    }

    override fun onDestroyView() {
        PlayHelper.removeOnTopLevelVideoContainerChangeListener(onTopLevelVideoContainerChangeListener)
        linearLayoutManager = null
        videosAdapter = null
        super.onDestroyView()
    }

    //play>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    private fun play() {
        val currVideo = PlayHelper.lastPlayVideo
        val data = videosAdapter?.data
        if (data == null || data.isEmpty()) {
            return
        }
        //当前有正在播放的歌曲，定位到该歌曲条目位置
        if (currVideo != null && currVideo.isPlaying()) {
            for ((index, vi) in data.withIndex()) {
                if (Utils.videoEquals(vi, currVideo.getVideoInfo())) {
                    playByPosition(index)
                    return
                }
            }
        }
        //条目中找不到当前播放歌曲，播放可见条目的视频
        recyclerView.post {
            playVisibleVideo()
        }
    }

    /**
     * 播放第一个完全可见的视频,若没有任何一个视频完全可见,播放第一个视频
     */
    private fun playVisibleVideo() {
        linearLayoutManager?.let { layoutManager ->
            val fv = layoutManager.findFirstVisibleItemPosition()
            val lv = layoutManager.findLastVisibleItemPosition()
            if (fv >= 0) {
                if (fv == lv) {
                    //可见区域只有一个Item
                    val view = layoutManager.findViewByPosition(fv)
                    view?.let { itemView ->
                        val vh = recyclerView.getChildViewHolder(itemView) as BaseViewHolder
                        val listVideoContainer = vh.getView<ListVideoContainer>(R.id.mv_video_container)
                        listVideoContainer.play()
                    }
                } else {
                    val view1 = layoutManager.findViewByPosition(fv)
                    val view2 = layoutManager.findViewByPosition(fv + 1)
                    Utils.runIfNotNull(view1, view2) { v1, v2 ->
                        val intArray = IntArray(2)
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
    }

    /**
     * 滚动到要播放的条目出现在屏幕中,获取条目开始播放,同时将条目移动到列表顶端
     */
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

    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    //TitleBar>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    protected open fun setupTitleBar(playSheetName: String?) {
        val activity = activity
        if (activity is AppCompatActivity) {
            activity.setSupportActionBar(toolbar)
            activity.supportActionBar?.setHomeButtonEnabled(true)
            toolbar.title = playSheetName
            toolbar.navigationIcon = Utils.getDrawableBySize(R.drawable.icon_menu_back, ScreenInfo.dp2Px(30f), ScreenInfo.dp2Px(30f))
            toolbar.inflateMenu(R.menu.toolbar_menu_local_videos)//填充菜单
            toolbar.overflowIcon = Utils.getDrawableBySize(R.drawable.icon_menu_more, ScreenInfo.dp2Px(30f), ScreenInfo.dp2Px(30f))
            setHasOptionsMenu(true)//设置碎片拥有菜单
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu_video_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.finish()
            R.id.menu_add_video -> {
                Snackbar.make(toolbar, "menu_add_to_play_sheet", Snackbar.LENGTH_SHORT).show()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    //View>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    override fun updatePlaySheetVideos(videoList: ArrayList<VideoInfo>) {
        videosAdapter?.setNewData(videoList)
        //设置播放列表
        PlayList.data = videoList
        play()
    }

    override fun onLoadPlaySheetFailed(errorMsg: String) {
        smartRefreshLayout.finishRefresh(false)
        Snackbar.make(recyclerView, errorMsg, Snackbar.LENGTH_SHORT).show()
    }

    override fun onLoadPlaySheetSucceed() {
        smartRefreshLayout.finishRefresh(true)
    }
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
}