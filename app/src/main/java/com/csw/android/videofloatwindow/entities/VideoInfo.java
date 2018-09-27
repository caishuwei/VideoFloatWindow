package com.csw.android.videofloatwindow.entities;

import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.provider.MediaStore;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Entity
public class VideoInfo implements Serializable {

    public static VideoInfo readFromCursor(@NotNull Cursor cursor) {
        String filePath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
        String rotationStr = "0";
        int rotation;
        int height;
        int width;
        MediaMetadataRetriever metaData = new MediaMetadataRetriever();
        metaData.setDataSource(filePath);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rotationStr = metaData.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        }
        try {
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

    @Id
    private long id;

    private String filePath;
    private long duration;
    private long fileSize;
    private String fileName;
    private long mediaDbId;
    private int width;
    private int height;
    private String resolution;

    public VideoInfo(String filePath, long duration, long fileSize, String fileName, long mediaDbId, int width, int height, String resolution) {
        this.filePath = filePath;
        this.duration = duration;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.mediaDbId = mediaDbId;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
    }

    @Generated(hash = 1262203967)
    public VideoInfo(long id, String filePath, long duration, long fileSize, String fileName, long mediaDbId, int width, int height,
                     String resolution) {
        this.id = id;
        this.filePath = filePath;
        this.duration = duration;
        this.fileSize = fileSize;
        this.fileName = fileName;
        this.mediaDbId = mediaDbId;
        this.width = width;
        this.height = height;
        this.resolution = resolution;
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

    public long getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getDuration() {
        return duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public long getMediaDbId() {
        return mediaDbId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getResolution() {
        return resolution;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMediaDbId(long mediaDbId) {
        this.mediaDbId = mediaDbId;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

}

