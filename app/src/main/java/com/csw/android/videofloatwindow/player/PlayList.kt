package com.csw.android.videofloatwindow.player

import com.csw.android.videofloatwindow.entities.VideoInfo
import java.util.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * 播放列表，用于存储当前正在播放的视频列表，实现上一个与下一个视频的查找，优化查找性能
 */
class PlayList {

    companion object {

        private var maxIndex = -1
        private var currIndex = Int.MIN_VALUE
        private var videoPos = HashMap<String, Int>()
        /**
         * 设置播放列表,ArrayList采用数组的方式，可以提高查询速度
         */
        var data: ArrayList<VideoInfo>? = null
            set(value) {
                field = value
                videoPos.clear()
                if (value != null) {
                    maxIndex = value.size - 1
                    for ((i, vi) in value.withIndex()) {
                        videoPos[vi.target] = i
                    }
                    updateCurrIndex()
                } else {
                    maxIndex = -1
                    currIndex = Int.MIN_VALUE
                }
            }


        fun updateCurrIndex() {
            PlayHelper.lastPlayVideo?.let {
                val ci = videoPos[it.getVideoInfo().target]
                currIndex = if (ci == null) {
                    Int.MIN_VALUE
                } else {
                    ci
                }
                return
            }
            currIndex = Int.MIN_VALUE
        }


        /**
         * 有上一曲
         */
        fun hasPrevious(): Boolean {
            return getPrevious() != null
        }

        /**
         * 获取上一个
         */
        fun getPrevious(): VideoInfo? {
            data?.let {
                if (currIndex in 1..maxIndex) {
                    return it[currIndex - 1]
                }
            }
            return null
        }

        /**
         * 有下一曲
         */
        fun hasNext(): Boolean {
            return getNext() != null
        }

        /**
         * 获取下一个
         */
        fun getNext(): VideoInfo? {
            data?.let {
                if (currIndex in 0 until maxIndex) {
                    return it[currIndex + 1]
                }
            }
            return null
        }
    }

}