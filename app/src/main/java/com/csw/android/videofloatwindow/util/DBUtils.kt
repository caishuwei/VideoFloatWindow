package com.csw.android.videofloatwindow.util

import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.entities.PlaySheetVideo
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.greendao.PlaySheetDao
import com.csw.android.videofloatwindow.greendao.PlaySheetVideoDao

class DBUtils {
    companion object {
        private val videoInfoDao = MyApplication.instance.daoSession.videoInfoDao
        private val playSheetDao = MyApplication.instance.daoSession.playSheetDao
        private val playSheetVideoDao = MyApplication.instance.daoSession.playSheetVideoDao

        /**
         * 获取用户创建的播放列表（依据时间进行排序，后创建的排在前面）
         */
        fun getPlaySheets(): List<PlaySheet> {
            return playSheetDao.queryBuilder().orderDesc(PlaySheetDao.Properties.CreateTime).build().list()
        }

        fun isPlaySheetExists(name: String): Boolean {
            return playSheetDao.queryBuilder().where(PlaySheetDao.Properties.Name.eq(name)).count() > 0
        }

        fun insetPlaySheet(playSheet: PlaySheet) {
            playSheetDao.insert(playSheet)
        }

        fun isVideoInPlaySheet(playSheetId: Long, videoInfoId: Long): Boolean {
            return playSheetVideoDao.queryBuilder()
                    .where(PlaySheetVideoDao.Properties.PlaySheetId.eq(playSheetId),
                            PlaySheetVideoDao.Properties.VideoInfoId.eq(videoInfoId))
                    .count() > 0
        }

        fun insetPlaySheetVideo(playSheetId: Long, videoInfoId: Long) {
            playSheetVideoDao.insert(PlaySheetVideo(playSheetId, videoInfoId))
        }

        fun insertVideoInfo(videoInfo: VideoInfo) {
            val id = videoInfoDao.insert(videoInfo)
            videoInfo.id = id
        }

    }
}