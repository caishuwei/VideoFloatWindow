package com.csw.android.videofloatwindow.entities;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.csw.android.videofloatwindow.app.Constants;
import com.csw.android.videofloatwindow.greendao.DaoSession;
import com.csw.android.videofloatwindow.greendao.PlaySheetDao;
import com.csw.android.videofloatwindow.greendao.PlaySheetVideoDao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.util.List;

/**
 * 播放列表
 */
@Entity
public class PlaySheet implements Serializable, MultiItemEntity {

    public static final long serialVersionUID = 1;

    @Id(autoincrement = true)//主键 自增id
    private Long id;
    @Unique//唯一约束 列表名称
    private String name;
    //列表创建时间
    private long createTime;

    @ToMany(referencedJoinProperty = "playSheetId")
    private List<PlaySheetVideo> playSheetVideos;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1232769245)
    private transient PlaySheetDao myDao;

    public PlaySheet(String name) {
        this.name = name;
        createTime = System.currentTimeMillis();
    }

    @Generated(hash = 819208696)
    public PlaySheet(Long id, String name, long createTime) {
        this.id = id;
        this.name = name;
        this.createTime = createTime;
    }

    @Generated(hash = 212871209)
    public PlaySheet() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public int getItemType() {
        return (getPlaySheetVideos() == null || getPlaySheetVideos().isEmpty()) ?
                Constants.ItemTypeEnum.EMPTY_PLAY_SHEET :
                Constants.ItemTypeEnum.PLAY_SHEET;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1102971491)
    public List<PlaySheetVideo> getPlaySheetVideos() {
        if (playSheetVideos == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PlaySheetVideoDao targetDao = daoSession.getPlaySheetVideoDao();
            List<PlaySheetVideo> playSheetVideosNew = targetDao
                    ._queryPlaySheet_PlaySheetVideos(id);
            synchronized (this) {
                if (playSheetVideos == null) {
                    playSheetVideos = playSheetVideosNew;
                }
            }
        }
        return playSheetVideos;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 524656932)
    public synchronized void resetPlaySheetVideos() {
        playSheetVideos = null;
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1591773655)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPlaySheetDao() : null;
    }
}
