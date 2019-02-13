package com.csw.android.videofloatwindow.player.base

import android.provider.Settings
import android.view.Window
import com.csw.android.videofloatwindow.app.MyApplication
import java.util.*

class BrightnessController {
    private var currValue = -1
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
        listeners.put(listener,listener)
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