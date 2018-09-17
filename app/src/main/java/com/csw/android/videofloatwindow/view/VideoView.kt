package com.csw.android.videofloatwindow.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.player.PlayerFactory
import com.csw.android.videofloatwindow.ui.FullScreenActivity
import com.csw.android.videofloatwindow.util.Utils
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView


class VideoView : FrameLayout {
    private val ivPreview: ImageView;
    private val playerView: PlayerView;
    private val titleAndBack: View;
    val tvTitle: TextView;
    val vBack: View;
    val vFullScreen: View;


    var whRatio: Float = 16f / 9
        set(value) {
            var ratio = value
            if (ratio <= 0) {
                ratio = 16f / 9
            }
            if (whRatio != ratio) {
                field = ratio
                requestLayout()
            }
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setBackgroundColor(0x80000000.toInt())
        View.inflate(context, R.layout.view_video, this)
        ivPreview = findViewById(R.id.iv_preview)
        playerView = findViewById(R.id.player_view)

        titleAndBack = playerView.findViewById(R.id.v_title_and_back)
        tvTitle = playerView.findViewById(R.id.tv_title)
        vBack = playerView.findViewById(R.id.v_back)
        vFullScreen = playerView.findViewById(R.id.v_full_screen)
        vFullScreen.setOnClickListener { FullScreenActivity.openActivity(this@VideoView) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Utils.measureCenterInsideByScaleRatio(widthMeasureSpec, heightMeasureSpec, whRatio) { w, h ->
            super.onMeasure(w, h)
        }
    }

    private var videoInfo: VideoInfo? = null

    private var player: Player? = null

    fun setVideoInfo(videoInfo: VideoInfo) {
        this.videoInfo = videoInfo
        player?.let {
            it.release()
            player = null
        }
        player = PlayerFactory.newPlayer(context.applicationContext, videoInfo.filePath)
        playerView.player = player
        tvTitle.text = videoInfo.fileName
    }

    fun play() {
        player?.playWhenReady = true
    }

    fun pause() {
        player?.playWhenReady = false
    }

    fun release() {
        player?.let {
            it.release()
            player = null
        }
    }
}