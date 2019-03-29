package com.csw.android.videofloatwindow.player.video.exo

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.NestedScrollingChild
import android.support.v4.view.NestedScrollingChildHelper
import android.support.v4.view.ViewCompat
import android.view.*
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayHelper
import com.csw.android.videofloatwindow.player.PlayList
import com.csw.android.videofloatwindow.player.base.BrightnessController
import com.csw.android.videofloatwindow.player.base.VideoContainer
import com.csw.android.videofloatwindow.player.base.VolumeController
import com.csw.android.videofloatwindow.player.video.base.IControllerSettingHelper
import com.csw.android.videofloatwindow.player.video.base.IVideo
import com.csw.android.videofloatwindow.player.video.layer.AutoHintLayerController
import com.csw.android.videofloatwindow.player.video.layer.HintLayerController
import com.csw.android.videofloatwindow.player.video.view.ErrorHintViewHolder
import com.csw.android.videofloatwindow.player.video.view.HintViewHolder
import com.csw.android.videofloatwindow.player.video.view.LoadingHintViewHolder
import com.csw.android.videofloatwindow.ui.base.IUICreator
import com.csw.android.videofloatwindow.util.LogUtils
import com.csw.android.videofloatwindow.util.Utils
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

/**
 * 自定义VideoView对播放器进行封装
 * <br/>
 * 实现事件提醒
 * <br/>
 * 实现点击切换播放器控制器状态
 * 实现滑动控制屏幕亮度与音量
 * 嵌套滑动分发
 * <br/>
 * 构造方法弃用传进来的context，统一使用ApplicationContext，避免视图移动到别的界面，旧界面引用被持有无法释放
 * 由于没有context参数的构造方法，所以不能在布局文件中使用
 */
class ExoVideoView internal constructor(videoInfo: VideoInfo) : RelativeLayout(MyApplication.instance), IVideo, IUICreator, NestedScrollingChild {

    //播放器相关视图
    private lateinit var playerView: PlayerView
    private lateinit var vBack: View
    private lateinit var tvTitle: TextView
    private lateinit var vClose: View
    private lateinit var vFullScreen: View
    private lateinit var vFloatWindow: View
    private lateinit var vPrevious: View
    private lateinit var vNext: View
    //手动关闭提示层
    private lateinit var fl_hint_layer: FrameLayout
    //自动关闭提示层
    private lateinit var fl_auto_hide_layer: FrameLayout
    //播放器
    lateinit var player: SimpleExoPlayer
        private set
    private lateinit var mediaDataSourceFactory: DefaultDataSourceFactory

    //启用音量与亮度控制器
    var enableVolumeAndBrightnessController = false

    private var isControllerVisible: Boolean = false//播放控制器当前是否可见

    //提示层
    private lateinit var hintLayerController: HintLayerController
    private lateinit var loadingHintViewHolder: LoadingHintViewHolder
    private lateinit var errorHintViewHolder: ErrorHintViewHolder

    private lateinit var autoHintLayerController: AutoHintLayerController
    private lateinit var autoHintViewHolder: HintViewHolder

    override fun getContentViewID(): Int {
        return R.layout.view_custom_video
    }

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        setBackgroundColor(Color.BLACK)
        //播放器视图中有些控件会古怪地获取焦点。。导致列表瞎滚动，这里禁用视图焦点获取
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        playerView = rootView.findViewById(R.id.player_view)
        playerView.controllerAutoShow = false
        vBack = playerView.findViewById(R.id.v_back)
        tvTitle = playerView.findViewById(R.id.tv_title)
        vClose = playerView.findViewById(R.id.v_close)
        vPrevious = playerView.findViewById(R.id.v_previous)
        vPrevious.isEnabled = true
        vNext = playerView.findViewById(R.id.v_next)
        vNext.isEnabled = true
        vFullScreen = playerView.findViewById(R.id.v_full_screen)
        vFloatWindow = playerView.findViewById(R.id.v_float_window)
        val bandwidthMeter = DefaultBandwidthMeter()
        //        val window = Timeline.Window()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        player = ExoPlayerFactory.newSimpleInstance(
                context,
                trackSelector)
        playerView.player = player
        playerView.controllerAutoShow = false
        mediaDataSourceFactory = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getString(R.string.app_name)),
                bandwidthMeter)


        fl_hint_layer = rootView.findViewById(R.id.fl_hint_layer)
        fl_hint_layer.visibility = GONE
        fl_auto_hide_layer = rootView.findViewById(R.id.fl_auto_hide_layer)
        fl_auto_hide_layer.visibility = GONE

        hintLayerController = HintLayerController(fl_hint_layer)
        loadingHintViewHolder = LoadingHintViewHolder(fl_hint_layer)
        loadingHintViewHolder.setHintInfo(R.drawable.player_loading, "加载中。。。")
        errorHintViewHolder = ErrorHintViewHolder(fl_hint_layer)
        errorHintViewHolder.setHintInfo(R.drawable.player_error, "视频错误")

        autoHintLayerController = AutoHintLayerController(fl_auto_hide_layer)
        autoHintViewHolder = HintViewHolder(fl_auto_hide_layer)
        autoHintViewHolder.addToLayer(autoHintLayerController)
        autoHintLayerController.hide()

        controllerSettingHelper.reset()
    }

    override fun initAdapter() {

    }

    override fun initListener() {
        BrightnessController.instance.addListener(object : BrightnessController.BrightnessChangeListener {
            override fun onBrightnessChanged(value: Int) {
                if (enableVolumeAndBrightnessController) {
                    autoHintViewHolder.setHintInfo(R.drawable.player_light, "亮度:$value%");
                    autoHintLayerController.show();
                }
            }
        })
        VolumeController.instance.addListener(object : VolumeController.VolumeChangeListener {
            override fun onVolumeChanged(currValue: Int) {
                if (enableVolumeAndBrightnessController) {
                    autoHintViewHolder.setHintInfo(R.drawable.player_volume, "音量:${(currValue * 100f / VolumeController.instance.deviceMaxVolume).toInt()}%");
                    autoHintLayerController.show();
                }
            }
        })
        errorHintViewHolder.clickListener = OnClickListener {
            errorHintViewHolder.removeFromLayer(hintLayerController)
            play()
        }
        player.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            }

            override fun onSeekProcessed() {
                if (player.playWhenReady) {
                    play()
                }
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                if (error != null) {
                    when (error.type) {
                        ExoPlaybackException.TYPE_SOURCE -> {
                            errorHintViewHolder.addToLayer(hintLayerController)
                            errorHintViewHolder.setHintInfo(R.drawable.player_error, "播放源异常")
                        }
                        ExoPlaybackException.TYPE_RENDERER -> {
                            errorHintViewHolder.addToLayer(hintLayerController)
                            errorHintViewHolder.setHintInfo(R.drawable.player_error, "视频渲染异常")
                        }
                        ExoPlaybackException.TYPE_UNEXPECTED -> {
                            errorHintViewHolder.addToLayer(hintLayerController)
                            errorHintViewHolder.setHintInfo(R.drawable.player_error, "视频错误")
                        }
                    }
                } else {
                    errorHintViewHolder.addToLayer(hintLayerController)
                    errorHintViewHolder.setHintInfo(R.drawable.player_error, "视频错误")
                }
                player.stop(true)
            }

            override fun onLoadingChanged(isLoading: Boolean) {
            }

            override fun onPositionDiscontinuity(reason: Int) {
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady) {
                    PlayHelper.lastPlayVideo = this@ExoVideoView
                }
                PlayHelper.onVideoViewPlayStateChanged(this@ExoVideoView)
                when (playbackState) {
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_BUFFERING -> {
                        loadingHintViewHolder.addToLayer(hintLayerController)
                        errorHintViewHolder.removeFromLayer(hintLayerController)
                    }
                    Player.STATE_READY -> {
                        keepScreenOn = playWhenReady
                        loadingHintViewHolder.removeFromLayer(hintLayerController)
                        errorHintViewHolder.removeFromLayer(hintLayerController)
                    }
                    Player.STATE_ENDED -> {
                        keepScreenOn = false
                        player.playWhenReady = false
                        player.seekTo(0)
                    }
                }
            }
        })
        playerView.setControllerVisibilityListener {
            isControllerVisible = (it == View.VISIBLE)
        }
        playerView.hideController()
    }

    override fun initData() {
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        nestedScrollingChildHelper.isNestedScrollingEnabled = true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    //---------------------------------------对外暴露方法-------------------------------------------

    override fun getView(): View {
        return this
    }

    //当前所在的播放容器
    private var currVideoContainer: VideoContainer? = null

    /**
     * 辅助控制器的设置
     */
    private val controllerSettingHelper: IControllerSettingHelper = ControllerSettingHelper()

    override fun getControllerSettingHelper(): IControllerSettingHelper {
        return controllerSettingHelper
    }

    /**
     * 视频信息
     */
    private var mVideoInfo: VideoInfo = videoInfo

    override fun getVideoInfo(): VideoInfo {
        return mVideoInfo;
    }


    override fun setVideoInfo(videoInfo: VideoInfo) {
        if (!Utils.videoEquals(mVideoInfo, videoInfo)) {
            val currKey = mVideoInfo.target
            mVideoInfo = videoInfo
            player.stop(true)
            PlayHelper.resetVideoKey(currKey, mVideoInfo.target, this);
            PlayHelper.onVideoViewVideoInfoChanged(this)
            if (PlayHelper.lastPlayVideo == this) {
                PlayList.updateCurrIndex()
            }
        } else {
            mVideoInfo = videoInfo
        }
    }

    override fun bindVideoContainer(videoContainer: VideoContainer) {
        if (currVideoContainer != videoContainer) {
            currVideoContainer?.let {
                unbindVideoContainer(it, false)
            }
            videoContainer.onBindVideo(this)
            currVideoContainer = videoContainer
        }
    }

    override fun unbindVideoContainer(videoContainer: VideoContainer, release: Boolean) {
        if (currVideoContainer == videoContainer) {
            videoContainer.onUnbindVideo(this)
            controllerSettingHelper.reset()
            currVideoContainer = null
            if (release) {
                release()
            }
        }
    }

    override fun inContainer(): Boolean {
        return currVideoContainer != null
    }

    override fun play() {
        mVideoInfo?.let {
            when (player.playbackState) {
                Player.STATE_IDLE -> {
                    val mediaSource = ExtractorMediaSource.Factory(mediaDataSourceFactory)
                            .createMediaSource(Uri.parse(it.filePath))
                    player.prepare(mediaSource, true, true)
                }
                Player.STATE_ENDED -> {
                    player.seekTo(0)
                }
            }
            player.playWhenReady = true
            PlayHelper.lastPlayVideo = this
        }
    }

    override fun isPlaying(): Boolean {
        return player.playWhenReady && (player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING)
    }


    override fun pause() {
        player.playWhenReady = false
    }

    override fun release() {
        if (!PlayHelper.isVideoViewCanBackgroundPlay(this)) {
            currVideoContainer?.let {
                unbindVideoContainer(it, false)
            }
            player.release()
            if (PlayHelper.lastPlayVideo == this) {
                PlayHelper.lastPlayVideo = null
            }
            PlayHelper.removeVideo(this)
        }
    }


    //---------------------------------------触摸事件处理-------------------------------------------
    private var scaledTouchSlop: Int = 0
    private var handleGesture: Int = 0//是否处理手势 -1不处理 0待判断 1亮度与音量处理 2嵌套滑动处理
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var preX: Float = 0f
    private var preY: Float = 0f
    private var gestureHandler: GestureHandler? = null
    private val volumeGestureHandler = VolumeGestureHandler()
    private val brightnessGestureHandler = BrightnessGestureHandler()
    private var downTime = 0L
    private var isSingleTap = false

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event?.let {
            when (it.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downTime = System.currentTimeMillis()
                    isSingleTap = true
                    downX = it.rawX
                    downY = it.rawY
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    isSingleTap = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (handleGesture == 0) {
                        val dx = Math.abs(downX - it.rawX)
                        val dy = Math.abs(downY - it.rawY)
                        if (dx > scaledTouchSlop || dy > scaledTouchSlop) {//用户目的是滑动
                            isSingleTap = false
                            val axes = if (dx > dy) ViewCompat.SCROLL_AXIS_HORIZONTAL else ViewCompat.SCROLL_AXIS_VERTICAL
                            if (enableVolumeAndBrightnessController && dy > dx) {
                                //启用音量与亮度控制的情况下 竖直方向的滑动，我们自己处理
                                if (downX >= width / 2) {
                                    gestureHandler = volumeGestureHandler
                                } else {
                                    gestureHandler = brightnessGestureHandler
                                }
                                gestureHandler?.startHandle(downY)
                                gestureHandler?.onNewPosReceive(it.rawY)
                                handleGesture = 1
                            } else {
                                if (nestedScrollingChildHelper.startNestedScroll(axes)) {
                                    dispatchScroll((it.rawX - downX).toInt(), (it.rawY - downY).toInt());
                                    preX = it.rawX
                                    preY = it.rawY
                                    handleGesture = 2
                                } else {
                                    handleGesture = -1
                                }
                            }
                        } else {
                            //为判断出用户意图，不用处理
                        }
                    } else if (handleGesture == 1) {
                        if (enableVolumeAndBrightnessController) {
                            gestureHandler?.onNewPosReceive(it.rawY)
                        } else {

                        }
                    } else if (handleGesture == 2) {// handleGesture == -1 已经判断不用自己处理，将所有滑动都分发出去
                        dispatchScroll((it.rawX - preX).toInt(), (it.rawY - preY).toInt());
                        preX = it.rawX
                        preY = it.rawY
                    } else {

                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                    if (isSingleTap) {
                        if ((System.currentTimeMillis() - downTime) <= ViewConfiguration.getTapTimeout()) {
                            if (isControllerVisible) {
                                playerView.hideController()
                            } else {
                                playerView.showController()
                            }
                        }
                    }
                    if (handleGesture == 2) {
                        nestedScrollingChildHelper.stopNestedScroll()
                    }
                    downX = 0f
                    downY = 0f
                    handleGesture = 0
                    gestureHandler = null
                }
                else -> {
                }
            }
        }
        return true
    }

    private fun dispatchScroll(dx: Int, dy: Int) {
        val consumed = IntArray(2)
        LogUtils.e(msg = "dispatchScroll dx = $dx,dy = $dy")
        nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, null)
        nestedScrollingChildHelper.dispatchNestedScroll(consumed[0], consumed[1], dx - consumed[0], dy - consumed[1], null)
    }

    //--------------------------- 作为子视图拦截触摸事件后向父视图分发嵌套滑动 -----------------------

    private val nestedScrollingChildHelper = NestedScrollingChildHelper(this)

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        super.setNestedScrollingEnabled(enabled)
        nestedScrollingChildHelper.isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return nestedScrollingChildHelper.isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return nestedScrollingChildHelper.startNestedScroll(axes)
    }

    override fun stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll()
    }

    override fun hasNestedScrollingParent(): Boolean {
        return nestedScrollingChildHelper.hasNestedScrollingParent()
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed)
    }


    //------------------------------------------ inner class ---------------------------------------
    private abstract inner class GestureHandler {
        private var startY: Float = 0f

        open fun startHandle(startY: Float) {
            this@GestureHandler.startY = startY
        }

        fun onNewPosReceive(y: Float) {
            val percentChanged = (startY - y) / height
            onPercentChanged(percentChanged)
        }

        abstract fun onPercentChanged(percentChanged: Float)
    }

    private inner class VolumeGestureHandler : GestureHandler() {
        private var startValue: Int = 0
        override fun startHandle(startY: Float) {
            super.startHandle(startY)
            startValue = VolumeController.instance.getValue()
        }

        override fun onPercentChanged(percentChanged: Float) {
            VolumeController.instance.setValue((VolumeController.instance.deviceMaxVolume * percentChanged + startValue).toInt())
        }
    }

    private inner class BrightnessGestureHandler : GestureHandler() {
        private var startValue: Int = 0
        override fun startHandle(startY: Float) {
            super.startHandle(startY)
            startValue = BrightnessController.instance.getValue(getWindow(this@ExoVideoView))
        }

        override fun onPercentChanged(percentChanged: Float) {
            BrightnessController.instance.setValue(getWindow(this@ExoVideoView), (100 * percentChanged + startValue).toInt())
        }

        private fun getWindow(view: View): Window? {
            val context = view.context
            if (context is Activity) {
                return context.window
            } else {
                val parent = view.parent
                if (parent is View) {
                    return getWindow(parent)
                } else {
                    return null
                }
            }
        }
    }

    inner class ControllerSettingHelper : IControllerSettingHelper {

        override fun setBackClickListener(listener: View.OnClickListener?): IControllerSettingHelper {
            return setClickListener(vBack, listener)
        }

        override fun setTitle(titleStr: String): IControllerSettingHelper {
            tvTitle.text = titleStr
            return this
        }

        override fun setCloseClickListener(listener: View.OnClickListener?): IControllerSettingHelper {
            return setClickListener(vClose, listener)
        }

        override fun setPreviousClickListener(listener: View.OnClickListener?): IControllerSettingHelper {
            return setClickListener(vPrevious, listener)
        }

        override fun setNextClickListener(listener: View.OnClickListener?): IControllerSettingHelper {
            return setClickListener(vNext, listener)
        }

        override fun setFullScreenClickListener(listener: View.OnClickListener?): IControllerSettingHelper {
            return setClickListener(vFullScreen, listener)
        }

        override fun setFloatWindowClickListener(listener: View.OnClickListener?): IControllerSettingHelper {
            return setClickListener(vFloatWindow, listener)
        }

        override fun setVolumeAndBrightnessControllerEnable(enable: Boolean): IControllerSettingHelper {
            enableVolumeAndBrightnessController = enable
            return this
        }

        override fun reset(): IControllerSettingHelper {
            this.setBackClickListener(null)
                    .setBackClickListener(null)
                    .setTitle("")
                    .setCloseClickListener(null)
                    .setPreviousClickListener(null)
                    .setNextClickListener(null)
                    .setFullScreenClickListener(null)
                    .setFloatWindowClickListener(null)
                    .setVolumeAndBrightnessControllerEnable(false)
            return this
        }
    }


    init {
        LayoutInflater.from(MyApplication.instance).inflate(getContentViewID(), this)
        initView(this, null)
        initAdapter()
        initListener()
        initData()
    }
}