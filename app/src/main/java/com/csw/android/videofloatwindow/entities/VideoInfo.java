package com.csw.android.videofloatwindow.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class VideoInfo implements Serializable {

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

