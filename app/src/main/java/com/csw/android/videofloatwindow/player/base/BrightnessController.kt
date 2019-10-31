package com.csw.android.videofloatwindow.player.base

import android.provider.Settings
import android.view.Window
import com.csw.android.videofloatwindow.app.MyApplication
import java.util.*

/**
 * 亮度控制器，用于屏幕亮度的控制，监听
 */
class BrightnessController {
    companion object {
        val instance = BrightnessController()
    }

    private var currValue = -1
    //采用弱引用，当key不存在强引用或软引用时可以被回收，WeakHashMap会将这个键值对移除，不会造成内存泄露
    //也可以直接用引用队列与虚引用实现，但懒得写了，就用WeakHashMap吧
    private val listeners: WeakHashMap<BrightnessChangeListener, Any> = WeakHashMap()

    /**
     * 设置屏幕亮度值[1~100]
     */
    fun setValue(window: Window?, value: Int) {
        var brightness = Math.min(100, value)
        brightness = Math.max(1, brightness)
        if (getValue(window) != brightness) {
            currValue = brightness
            window?.let {
                val lp = it.attributes
                lp.screenBrightness = currValue / 100f
                window.attributes = lp
            }
            noticeBrightnessChanged(currValue)
        }
    }


    /**
     * 获取亮度值
     */
    fun getValue(window: Window?): Int {
        var brightness = -1f
        //读取窗口亮度
        window?.let {
            brightness = it.attributes.screenBrightness
        }
        if (brightness < 0) {
            //读不到窗口亮度，我们读取系统亮度
            try {
                val brightness255 = Settings.System.getInt(
                        MyApplication.instance.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS
                )
                val isAutoBrightness = Settings.System.getInt(
                        MyApplication.instance.contentResolver,
                        Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
                if (isAutoBrightness && brightness255 <= 0) {
                    //在自动亮度模式下，没有获取到亮度值
                    brightness = -1f
                } else {
                    brightness = brightness255 / 255f
                }
            } catch (e: Exception) {
                brightness = -1f
            }
        }
        var value = (brightness * 100).toInt()
        if (value <= 0) {
            //还是没有亮度值
            if (currValue >= 0) {
                value = currValue
            } else {
                value = 50
            }
        }
        return value
    }

    fun addListener(listener: BrightnessChangeListener) {
        listeners.put(listener, listener)
    }

    fun removeListener(listener: BrightnessChangeListener) {
        listeners.remove(listener)
    }

    private fun noticeBrightnessChanged(currValue: Int) {
        val iter = listeners.iterator()
        while (iter.hasNext()) {
            iter.next().key.onBrightnessChanged(currValue)
        }
    }

    interface BrightnessChangeListener {
        fun onBrightnessChanged(value: Int)
    }
}