package com.csw.android.videofloatwindow.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.csw.android.videofloatwindow.app.MyApplication
import com.csw.android.videofloatwindow.entities.PlaySheet
import com.csw.android.videofloatwindow.entities.PlaySheetVideo
import com.csw.android.videofloatwindow.entities.VideoInfo
import com.csw.android.videofloatwindow.greendao.*
import com.github.yuweiguocn.library.greendao.MigrationHelper
import org.greenrobot.greendao.database.Database

class DBUtils {
    companion object {

        //覆写数据库连接,greendao默认的数据库更新是把表全删了重建,这里借用第三方sdk,
        //进行表数据备份,建表,还原数据
        private class NySQLiteOpenHelper(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?) : DaoMaster.DevOpenHelper(context, name, factory) {
            override fun onUpgrade(db: Database?, oldVersion: Int, newVersion: Int) {
                try {

                    MigrationHelper.migrate(
                            db,
                            object : MigrationHelper.ReCreateAllTableListener {
                                override fun onCreateAllTables(db: Database?, ifNotExists: Boolean) {
                                    DaoMaster.createAllTables(db, ifNotExists)
                                }

                                override fun onDropAllTables(db: Database?, ifExists: Boolean) {
                                    DaoMaster.dropAllTables(db, ifExists)
                                }
                            },
                            PlaySheetDao::class.java,
                            PlaySheetVideoDao::class.java,
                            VideoInfoDao::class.java
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                super.onUpgrade(db, oldVersion, newVersion)
            }
        }

        private val dbHelper: DaoMaster.DevOpenHelper = NySQLiteOpenHelper(MyApplication.instance, "video_db", null)
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
            return playSheetDao.queryBuilder().orderAsc(PlaySheetDao.Properties.CreateTime).build().list()
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
        fun insertPlaySheetIfNotExist(insertPlaySheet: PlaySheet) {
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
        fun updatePlaySheetVideos(playSheetId: Long, videos: List<VideoInfo>) {
            //清除现在的记录
            playSheetVideoDao.deleteInTx(playSheetVideoDao.queryBuilder().where(PlaySheetVideoDao.Properties.PlaySheetId.eq(playSheetId)).list())
            //插入新记录
            for (vi in videos) {
                playSheetVideoDao.insert(PlaySheetVideo(playSheetId, vi.id))
            }
            daoSession.clear()
        }

        /**
         * 删除歌单
         */
        fun deletePlaySheet(playSheet: PlaySheet) {
            playSheetDao.delete(playSheet)
            playSheetVideoDao.deleteInTx(playSheetVideoDao.queryBuilder().where(PlaySheetVideoDao.Properties.PlaySheetId.eq(playSheet.id)).list())
            daoSession.clear()
        }

        fun getPlaySheetByName(name: String): PlaySheet? {
            return playSheetDao.queryBuilder().where(PlaySheetDao.Properties.Name.eq(name)).unique()
        }

        fun getVideosByPlaySheetName(name: String): ArrayList<VideoInfo> {
            val playSheet = getPlaySheetByName(name)
            val result = ArrayList<VideoInfo>()
            playSheet?.let {
                for (playSheetVideo in it.playSheetVideos) {
                    result.add(playSheetVideo.videoInfo)
                }
            }
            return result
        }

        fun updatePlaySheet(playSheet: PlaySheet) {
            playSheetDao.update(playSheet)
            daoSession.clear()
        }
    }
}