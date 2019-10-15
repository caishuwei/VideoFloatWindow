package com.csw.android.videofloatwindow.util

import android.database.sqlite.SQLiteDatabase
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.entities.PlaySheetVideo
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.greendao.*

class DBUtils {
    companion object {

        private val dbHelper: DaoMaster.DevOpenHelper = DaoMaster.DevOpenHelper(MyApplication.instance, "video_db", null)
        private val db: SQLiteDatabase = dbHelper.writableDatabase
        private val daoMaster: DaoMaster = DaoMaster(db)
        private val daoSession: DaoSession = daoMaster.newSession()
        private val videoInfoDao = daoSession.videoInfoDao
        private val playSheetDao = daoSession.playSheetDao
        private val playSheetVideoDao = daoSession.playSheetVideoDao


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
            daoSession.clear()
        }

        fun isVideoInPlaySheet(playSheetId: Long, videoInfoId: Long): Boolean {
            return playSheetVideoDao.queryBuilder()
                    .where(PlaySheetVideoDao.Properties.PlaySheetId.eq(playSheetId),
                            PlaySheetVideoDao.Properties.VideoInfoId.eq(videoInfoId))
                    .count() > 0
        }

        fun insetPlaySheetVideo(playSheetId: Long, videoInfoId: Long) {
            if (!isVideoInPlaySheet(playSheetId, videoInfoId)) {
                playSheetVideoDao.insert(PlaySheetVideo(playSheetId, videoInfoId))
                daoSession.clear()
            }
        }

        fun insertVideoInfo(videoInfo: VideoInfo): VideoInfo {
            val exitsRow = videoInfoDao.queryBuilder().where(VideoInfoDao.Properties.MediaDbId.eq(videoInfo.mediaDbId)).build().unique()
            exitsRow?.let {
                videoInfo.id = it.id
                return videoInfo
            }
            val id = videoInfoDao.insert(videoInfo)
            videoInfo.id = id
            daoSession.clear()
            return videoInfo
        }

        fun getVideosByPlaySheetId(playSheetId: Long): ArrayList<VideoInfo> {
            val result = arrayListOf<VideoInfo>()
            val playSheet = playSheetDao.queryBuilder().where(PlaySheetDao.Properties.Id.eq(playSheetId)).build().unique()
            playSheet?.let {
                for (playSheetVideo in it.playSheetVideos) {
                    result.add(playSheetVideo.videoInfo)
                }
            }
            return result
        }

        /**
         * 插入一个播放列表（如果不存在）
         */
        fun insertPlaySheetIfNoExist(insertPlaySheet: PlaySheet) {
            val playSheet = playSheetDao.queryBuilder().where(PlaySheetDao.Properties.Name.eq(insertPlaySheet.name)).build().unique()
            if (playSheet != null) {
                insertPlaySheet.id = playSheet.id
            } else {
                playSheetDao.insert(insertPlaySheet)
                daoSession.clear()
            }
        }

        /**
         * 取得歌单
         */
        fun getPlaySheet(playSheetId: Long): PlaySheet? {
            val queryList = playSheetDao.queryBuilder().where(PlaySheetDao.Properties.Id.eq(playSheetId)).build().list()
            return if (queryList.isEmpty()) {
                null
            } else {
                queryList[0]
            }
        }

        /**
         * 更新播放列表
         */
        fun updatePlaySheetVideos(playSheetId: Long, videos: ArrayList<VideoInfo>) {
            //清除现在的记录
            playSheetVideoDao.deleteInTx(playSheetVideoDao.queryBuilder().where(PlaySheetVideoDao.Properties.PlaySheetId.eq(playSheetId)).list())
            //插入新记录
            for (vi in videos) {
                playSheetVideoDao.insert(PlaySheetVideo(playSheetId, vi.id))
            }
            daoSession.clear()
        }
    }
}