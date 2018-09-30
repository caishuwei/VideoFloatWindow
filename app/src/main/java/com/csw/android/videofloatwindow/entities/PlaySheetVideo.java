package com.csw.android.videofloatwindow.entities;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 用于记录播放列表下包含的视频
 */
@Entity
public class PlaySheetVideo implements Serializable {

    public static final long serialVersionUID = 1;

    @Id//主键 自增id
    private long id;

    private long playSheetId;//播放列表Id

    private long videoInfoId;//视频记录Id


    public PlaySheetVideo(long playSheetId, long videoInfoId) {
        this.playSheetId = playSheetId;
        this.videoInfoId = videoInfoId;
    }

    @Generated(hash = 854951757)
    public PlaySheetVideo(long id, long playSheetId, long videoInfoId) {
        this.id = id;
        this.playSheetId = playSheetId;
        this.videoInfoId = videoInfoId;
    }

    @Generated(hash = 2054126929)
    public PlaySheetVideo() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
