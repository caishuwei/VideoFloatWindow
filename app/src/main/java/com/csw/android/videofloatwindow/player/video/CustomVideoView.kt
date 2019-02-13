package com.csw.android.videofloatwindow.player.video

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.player.base.BrightnessController
import com.csw.android.videofloatwindow.player.base.VolumeController
import com.csw.android.videofloatwindow.player.video.layer.AutoHintLayerController
import com.csw.android.videofloatwindow.player.video.layer.HintLayerController
import com.csw.android.videofloatwindow.player.video.view.ErrorHintViewHolder
import com.csw.android.videofloatwindow.player.video.view.HintViewHolder
import com.csw.android.videofloatwindow.player.video.view.LoadingHintViewHolder
import com.csw.android.videofloatwindow.ui.base.IUICreator
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter

class CustomVideoView : RelativeLayout, IUICreator {
    //播放器相关视图
    lateinit var playerView: PlayerView
        private set
    lateinit var vBack: View
        private set
    lateinit var tvTitle: TextView
        private set
    lateinit var vClose: View
        private set
    lateinit var vFullScreen: View
        private set
    lateinit var vFloatWindow: View
        private set
    lateinit var vPrevious: View
        private set
    lateinit var vNext: View
        private set
    //手动关闭提示层
    private lateinit var fl_hint_layer: FrameLayout
    //自动关闭提示层
    private lateinit var fl_auto_hide_layer: FrameLayout

    //播放器
    lateinit var player: SimpleExoPlayer
        private set

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        LayoutInflater.from(context).inflate(getContentViewID(), this)
        initView(this, null)
        initAdapter()
        initListener()
        initData()
    }

    override fun getContentViewID(): Int {
        return R.layout.view_custom_video
    }

    private lateinit var hintLayerController: HintLayerController
    private lateinit var loadingHintViewHolder: LoadingHintViewHolder
    private lateinit var errorHintViewHolder: ErrorHintViewHolder

    private lateinit var autoHintLayerController: AutoHintLayerController
    private lateinit var autoHintViewHolder: HintViewHolder

    override fun initView(rootView: View, savedInstanceState: Bundle?) {
        playerView = rootView.findViewById(R.id.player_view)
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
    }


    override fun initAdapter() {

    }

    override fun initListener() {
        MyApplication.instance.brightnessController.addListener(object : BrightnessController.BrightnessChangeListener {
            override fun onBrightnessChanged(value: Int) {
                autoHintViewHolder.setHintInfo(R.drawable.player_light, "亮度:$value%");
                autoHintLayerController.show();
            }
        })
        MyApplication.instance.volumeController.addListener(object : VolumeController.VolumeChangeListener {
            override fun onVolumeChanged(value: Int) {
                autoHintViewHolder.setHintInfo(R.drawable.player_volume, "音量:${(value * 100f / MyApplication.instance.volumeController.deviceMaxVolume).toInt()}%");
                autoHintLayerController.show();
            }
        })
        errorHintViewHolder.clickListener = OnClickListener {
            MyApplication.instance.playerHelper.tryPlayCurr()
            errorHintViewHolder.removeFromLayer(hintLayerController)
        }
        player.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
            }

            override fun onSeekProcessed() {
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
                when (playbackState) {
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_BUFFERING -> {
                        loadingHintViewHolder.addToLayer(hintLayerController)
                        errorHintViewHolder.removeFromLayer(hintLayerController)
                    }
                    Player.STATE_READY -> {
                        loadingHintViewHolder.removeFromLayer(hintLayerController)
                        errorHintViewHolder.removeFromLayer(hintLayerController)
                    }
                    Player.STATE_ENDED -> {
                    }
                }
            }
        })
    }

    override fun initData() {
    }

}