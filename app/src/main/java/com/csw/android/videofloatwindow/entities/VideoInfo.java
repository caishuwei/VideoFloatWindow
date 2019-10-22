package com.csw.android.videofloatwindow.entities;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.provider.MediaStore;

import com.csw.android.videofloatwindow.greendao.DaoSession;
import com.csw.android.videofloatwindow.greendao.PlaySheetVideoDao;
import com.csw.android.videofloatwindow.greendao.VideoInfoDao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Unique;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

@Entity
public class VideoInfo implements Serializable {

    public static VideoInfo readFromCursor(@NotNull Cursor cursor) {
        String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        int rotation;
        int height;
        int width;
        try {
            String rotationStr = "0";
            MediaMetadataRetriever metaData = new MediaMetadataRetriever();
            //setDataSource遇到损坏的视频文件抛出异常
            //java.lang.RuntimeException: setDataSource failed: status = 0xFFFFFFEA
            metaData.setDataSource(filePath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rotationStr = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            }
            metaData.release();
            rotation = Integer.parseInt(rotationStr);
        } catch (Exception e) {
            rotation = 0;
        }
        switch (rotation) {
            case 90:
            case -90:
            case 270:
                height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
                width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
                break;
            default:
                width = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.WIDTH));
                height = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT));
                break;
        }

        return new VideoInfo(
                filePath,
                cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)),
                cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media._ID)),
                width,
                height,
                cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION))
        );
    }

    public static final long serialVersionUID = 1;

    @Id(autoincrement = true)
    private Long id;

    private String filePath;//文件路径
    private long duration;//视频长度（毫秒）
    private long fileSize;//文件大小（字节）
    private String fileName;//文件名称
    @Unique
    private long mediaDbId;//媒体库id
    private int width;//视频宽
    private int height;//视频高
    private String resolution;//视频分辨率字符串
    private String imageUri;//预览图地址

    @ToMany(referencedJoinProperty = "videoInfoId")
    private List<PlaySheetVideo> playSheetVideos;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 785593785)
    private transient VideoInfoDao myDao;


    public VideoInfo(String filePath, long duration, long fileSize, String fileName, long mediaDbId, int width, int height, String resolution) {
        this.filePath = filePath;
        this.duration = duration;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.mediaDbId = mediaDbId;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
        imageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI + "/" + mediaDbId;
    }

    @Generated(hash = 1223459747)
    public VideoInfo(Long id, String filePath, long duration, long fileSize, String fileName, long mediaDbId, int width, int height,
            String resolution, String imageUri) {
        this.id = id;
        this.filePath = filePath;
        this.duration = duration;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.mediaDbId = mediaDbId;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
        this.imageUri = imageUri;
    }

    @Generated(hash = 296402066)
    public VideoInfo() {
    }

    public float getWHRatio() {
        if (width > 0 && height > 0) {
            return width * 1f / height;
        } else {
            return 0f;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getMediaDbId() {
        return mediaDbId;
    }

    public void setMediaDbId(long mediaDbId) {
        this.mediaDbId = mediaDbId;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1038105902)
    public List<PlaySheetVideo> getPlaySheetVideos() {
        if (playSheetVideos == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PlaySheetVideoDao targetDao = daoSession.getPlaySheetVideoDao();
            List<PlaySheetVideo> playSheetVideosNew = targetDao._queryVideoInfo_PlaySheetVideos(id);
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
    @Generated(hash = 1598960089)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getVideoInfoDao() : null;
    }

    public String getTarget() {
        return filePath;
    }
}

