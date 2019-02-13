package com.csw.android.videofloatwindow.player.base

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.Window
import android.widget.FrameLayout
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.util.LogUtils


class VolumeAndBrightnessControlView : FrameLayout{

    private var scaledTouchSlop: Int
    private var handleGesture: Int = 0//是否处理手势 -1不处理 0待判断 1处理
    private var downX: Float = 0f
    private var downY: Float = 0f
    private var gestureHandler: GestureHandler? = null
    private val volumeGestureHandler: VolumeGestureHandler
    private val brightnessGestureHandler: BrightnessGestureHandler

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        isFocusableInTouchMode = true
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        volumeGestureHandler = VolumeGestureHandler()
        brightnessGestureHandler = BrightnessGestureHandler()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        //exo直接拦截事件，那就别怪我先下手为强
        super.onInterceptTouchEvent(ev)
        return true
//        if (handleGesture == 0) {
//            ev?.let {
//                when (it.action) {
//                    MotionEvent.ACTION_DOWN -> {
//                        downX = it.x
//                        downY = it.y
//                    }
//                    MotionEvent.ACTION_MOVE -> {
//                        val dx = Math.abs(downX - it.x)
//                        val dy = Math.abs(downY - it.y)
//                        if (dx > scaledTouchSlop || dy > scaledTouchSlop) {//用户目的是滑动
//                            if (dy > dx) {//竖直方向的滑动
//                                LogUtils.e(msg = "InterceptTouchEvent")
//                                if (downX >= width / 2) {
//                                    gestureHandler = volumeGestureHandler
//                                } else {
//                                    gestureHandler = brightnessGestureHandler
//                                }
//                                gestureHandler?.startHandle(downY)
//                                handleGesture = 1
//                            } else {
//                                handleGesture = -1
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return super.onInterceptTouchEvent(ev) || handleGesture == 1
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        super.onTouchEvent(event)
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = it.x
                    downY = it.y
                }
                MotionEvent.ACTION_MOVE -> {
                    if (handleGesture == 0) {
                        val dx = Math.abs(downX - it.x)
                        val dy = Math.abs(downY - it.y)
                        if (dx > scaledTouchSlop || dy > scaledTouchSlop) {//用户目的是滑动
                            if (dy > dx) {//竖直方向的滑动
                                if (downX >= width / 2) {
                                    gestureHandler = volumeGestureHandler
                                } else {
                                    gestureHandler = brightnessGestureHandler
                                }
                                gestureHandler?.startHandle(downY)
                                handleGesture = 1
                            } else {
                                handleGesture = -1
                            }
                        }

                    }
                    if (handleGesture == 1) {
                        gestureHandler?.onNewPosReceive(it.y)
                    }
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
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
            startValue = MyApplication.instance.volumeController.getValue()
        }

        override fun onPercentChanged(percentChanged: Float) {
            MyApplication.instance.volumeController.setValue((MyApplication.instance.volumeController.deviceMaxVolume * percentChanged + startValue).toInt())
        }
    }

    private inner class BrightnessGestureHandler : GestureHandler() {
        private var startValue: Int = 0
        override fun startHandle(startY: Float) {
            super.startHandle(startY)
            startValue = MyApplication.instance.brightnessController.getValue(getWindow(this@VolumeAndBrightnessControlView))
        }

        override fun onPercentChanged(percentChanged: Float) {
            MyApplication.instance.brightnessController.setValue(getWindow(this@VolumeAndBrightnessControlView), (100 * percentChanged + startValue).toInt())
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


}