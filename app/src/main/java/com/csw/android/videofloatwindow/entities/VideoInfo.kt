package com.csw.android.videofloatwindow.entities

class VideoInfo(
        val filePath: String,
        val duration: Long,
        val fileSize: Long,
        val fileName: String,
        val id: Long,
        val width: Int,
        val height: Int
) {
    fun getWHRatio(): Float {
        if (width > 0 && height > 0) {
            return width * 1f / height
        } else {
            return 0f
        }
    }
}