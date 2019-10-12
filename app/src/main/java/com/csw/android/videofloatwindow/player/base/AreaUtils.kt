package com.csw.android.videofloatwindow.player.base

import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.csw.android.videofloatwindow.R
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.util.ScreenInfo

/**
 * 面积工具，用于悬浮窗口视频尺寸计算
 */
class AreaUtils {
    companion object {
        private val defaultArea: Int = ScreenInfo.WIDTH * ScreenInfo.WIDTH * 9 / 16 //默认面积
        val minVideoWidth: Int //视频最小宽度
        var maxVideoWidth: Int //视频最大宽度
        val minVideoHeight: Int //视频最小高度
        var maxVideoHeight: Int //视频最大高度

        var windowOffsetX: Int = 0 //窗口偏移量X
        var windowOffsetY: Int = 0 //窗口偏移量Y
        var windowWidth: Int = 0 //窗口宽度
        var windowHeight: Int = 0 //窗口高度
        var windowCenterX = 0
            //窗口中心X
            private set
        var windowCenterY = 0
            //窗口中心Y
            private set

        init {
            //根据播放控制器的控制按钮占据空间，计算最小高度和最小宽度
//            val playerControllor = LayoutInflater.from(MyApplication.instance).inflate(R.layout.exo_playback_control_view, null, false)
//            val titleAndBack = playerControllor.findViewById<View>(R.id.v_title_and_back)
//            val backButton = titleAndBack.findViewById<View>(R.id.v_back)
//            val playButtons = playerControllor.findViewById<View>(R.id.v_play_buttons)
//            val preButton = playButtons.findViewById<View>(R.id.exo_play)
//            val timeAndProgress = playerControllor.findViewById<View>(R.id.v_time_and_progress)
//            val tvPosition = timeAndProgress.findViewById<TextView>(R.id.exo_position)
//            tvPosition.text = "00:00:00"
//
//            playerControllor.measure(
//                    View.MeasureSpec.makeMeasureSpec(ScreenInfo.WIDTH, View.MeasureSpec.EXACTLY),
//                    View.MeasureSpec.makeMeasureSpec(ScreenInfo.HEIGHT, View.MeasureSpec.EXACTLY))
//
//            minVideoWidth = Math.max(
//                    backButton.measuredWidth,
//                    Math.max(
//                            preButton.measuredWidth * 4,
//                            tvPosition.measuredWidth * 3
//                    )
//            )
//            minVideoHeight = titleAndBack.measuredHeight + playButtons.measuredHeight + timeAndProgress.measuredHeight

            //只留VideoFloatWindow.moveView的显示空间
            val dp48 = ScreenInfo.dp2Px(48F)
            minVideoHeight = dp48
            minVideoWidth = dp48

            maxVideoWidth = ScreenInfo.WIDTH
            maxVideoHeight = ScreenInfo.HEIGHT


        }


        //面积
        var suggestArea: Int = defaultArea
            set(value) {
                field = if (value <= 0) defaultArea else value
            }

        /**
         * 计算视频的合理宽高
         */
        fun calcVideoWH(whRatio: Float, onResult: (width: Int, height: Int) -> (Unit)) {
            val videoWHRatio = if (whRatio <= 0) 16f / 9 else whRatio
            //面积 = W*H = W*W/videoWHRatio
            var suggestWidth = Math.sqrt(suggestArea * videoWHRatio.toDouble()).toFloat()
            var suggestHeight = suggestWidth / videoWHRatio

            //先根据最大宽高，限制宽高取值
            if (suggestWidth > maxVideoWidth) {
                suggestWidth = maxVideoWidth.toFloat()
                suggestHeight = suggestWidth / videoWHRatio
            }
            if (suggestHeight > maxVideoHeight) {
                suggestHeight = maxVideoHeight.toFloat()
                suggestWidth = suggestHeight * videoWHRatio
            }
            //根据视频最小宽高要求，限制宽高，高度比较重要，放后面
            if (suggestWidth < minVideoWidth) {
                suggestWidth = minVideoWidth.toFloat()
                suggestHeight = suggestWidth / videoWHRatio
            }
            if (suggestHeight < minVideoHeight) {
                suggestHeight = minVideoHeight.toFloat()
                suggestWidth = suggestHeight * videoWHRatio
            }
            //回调
            onResult(suggestWidth.toInt(), suggestHeight.toInt())
        }

        /**
         * 调整窗口位置
         */
        fun adjustWindowOffset() {
            if (windowOffsetX > maxVideoWidth - windowWidth) {
                windowOffsetX = maxVideoWidth - windowWidth
            }
            if (windowOffsetX < 0) {
                windowOffsetX = 0
            }
            if (windowOffsetY > maxVideoHeight - windowHeight) {
                windowOffsetY = maxVideoHeight - windowHeight
            }
            if (windowOffsetY < 0) {
                windowOffsetY = 0
            }
        }

        /**
         * 计算窗口中心点
         */
        fun calcWindowCenter() {
            windowCenterX = windowOffsetX + windowWidth / 2
            windowCenterY = windowOffsetY + windowHeight / 2
        }

        /**
         * 对齐到中心点
         */
        fun alignToCenter() {
            windowOffsetX = windowCenterX - windowWidth / 2
            windowOffsetY = windowCenterY - windowHeight / 2
        }

    }

}