package com.csw.android.videofloatwindow.player.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import com.csw.android.videofloatwindow.app.MyApplication
import java.util.*

/**
 * 音量变化监听
 */
class VolumeController {
    companion object {
        val instance = VolumeController()
    }

    private val audioManager: AudioManager
    val deviceMaxVolume: Int
    private val listeners: WeakHashMap<VolumeChangeListener, Any> = WeakHashMap()

    init {
        val context = MyApplication.instance.applicationContext
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        deviceMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val intentFilter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent?.let {
                    if ("android.media.VOLUME_CHANGED_ACTION".equals(it.action)) {
                        noticeVolumeChanged(getValue())
                    }
                }
            }
        }
        context.registerReceiver(receiver, intentFilter)
    }

    fun getValue(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }

    fun setValue(value: Int) {
        var volume = Math.min(deviceMaxVolume, value)
        volume = Math.max(0, volume)
        if (getValue() != volume) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        }
    }

    fun addListener(listener: VolumeChangeListener) {
        listeners.put(listener,listener)
    }

    fun removeListener(listener: VolumeChangeListener) {
        listeners.remove(listener)
    }

    private fun noticeVolumeChanged(currValue: Int) {
        val iter = listeners.iterator()
        while (iter.hasNext()) {
            iter.next().key.onVolumeChanged(currValue)
        }
    }

    interface VolumeChangeListener {
        fun onVolumeChanged(currValue: Int)
    }
}