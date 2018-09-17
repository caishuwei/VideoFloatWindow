package com.csw.android.videofloatwindow.greendao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.csw.android.videofloatwindow.entities.VideoInfo;

import com.csw.android.videofloatwindow.greendao.VideoInfoDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig videoInfoDaoConfig;

    private final VideoInfoDao videoInfoDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        videoInfoDaoConfig = daoConfigMap.get(VideoInfoDao.class).clone();
        videoInfoDaoConfig.initIdentityScope(type);

        videoInfoDao = new VideoInfoDao(videoInfoDaoConfig, this);

        registerDao(VideoInfo.class, videoInfoDao);
    }
    
    public void clear() {
        videoInfoDaoConfig.clearIdentityScope();
    }

    public VideoInfoDao getVideoInfoDao() {
        return videoInfoDao;
    }

}
