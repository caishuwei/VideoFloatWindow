package com.csw.android.videofloatwindow.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.DaoException;
import com.csw.android.videofloatwindow.greendao.DaoSession;
import com.csw.android.videofloatwindow.greendao.VideoInfoDao;
import org.greenrobot.greendao.annotation.NotNull;
import com.csw.android.videofloatwindow.greendao.PlaySheetDao;
import com.csw.android.videofloatwindow.greendao.PlaySheetVideoDao;

/**
 * 用于记录播放列表下包含的视频
 */
@Entity
public class PlaySheetVideo implements Serializable {

    public static final long serialVersionUID = 1;

    @Id(autoincrement = true)//主键 自增id
    private Long id;

    private long playSheetId;//播放列表Id
    @ToOne(joinProperty = "playSheetId")
    private PlaySheet playSheet;

    private long videoInfoId;//视频记录Id
    @ToOne(joinProperty = "videoInfoId")
    private VideoInfo videoInfo;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1497426574)
    private transient PlaySheetVideoDao myDao;

    @Generated(hash = 1500389660)
    private transient Long playSheet__resolvedKey;

    @Generated(hash = 460699487)
    private transient Long videoInfo__resolvedKey;


    public PlaySheetVideo(long playSheetId, long videoInfoId) {
        this.playSheetId = playSheetId;
        this.videoInfoId = videoInfoId;
    }

    @Generated(hash = 2088980330)
    public PlaySheetVideo(Long id, long playSheetId, long videoInfoId) {
        this.id = id;
        this.playSheetId = playSheetId;
        this.videoInfoId = videoInfoId;
    }

    @Generated(hash = 2054126929)
    public PlaySheetVideo() {
    }

    public Long getId() {
        return id;
    }

    public long getPlaySheetId() {
        return playSheetId;
    }

    public void setPlaySheetId(long playSheetId) {
        this.playSheetId = playSheetId;
    }

    public long getVideoInfoId() {
        return videoInfoId;
    }

    public void setVideoInfoId(long videoInfoId) {
        this.videoInfoId = videoInfoId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1843690183)
    public PlaySheet getPlaySheet() {
        long __key = this.playSheetId;
        if (playSheet__resolvedKey == null
                || !playSheet__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PlaySheetDao targetDao = daoSession.getPlaySheetDao();
            PlaySheet playSheetNew = targetDao.load(__key);
            synchronized (this) {
                playSheet = playSheetNew;
                playSheet__resolvedKey = __key;
            }
        }
        return playSheet;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 866175946)
    public void setPlaySheet(@NotNull PlaySheet playSheet) {
        if (playSheet == null) {
            throw new DaoException(
                    "To-one property 'playSheetId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.playSheet = playSheet;
            playSheetId = playSheet.getId();
            playSheet__resolvedKey = playSheetId;
        }
    }

    /** To-one relationship, resolved on first access. */
    @Generated(hash = 1787499387)
    public VideoInfo getVideoInfo() {
        long __key = this.videoInfoId;
        if (videoInfo__resolvedKey == null
                || !videoInfo__resolvedKey.equals(__key)) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            VideoInfoDao targetDao = daoSession.getVideoInfoDao();
            VideoInfo videoInfoNew = targetDao.load(__key);
            synchronized (this) {
                videoInfo = videoInfoNew;
                videoInfo__resolvedKey = __key;
            }
        }
        return videoInfo;
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1899912635)
    public void setVideoInfo(@NotNull VideoInfo videoInfo) {
        if (videoInfo == null) {
            throw new DaoException(
                    "To-one property 'videoInfoId' has not-null constraint; cannot set to-one to null");
        }
        synchronized (this) {
            this.videoInfo = videoInfo;
            videoInfoId = videoInfo.getId();
            videoInfo__resolvedKey = videoInfoId;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 417403359)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPlaySheetVideoDao() : null;
    }
}
