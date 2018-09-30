package com.csw.android.videofloatwindow.entities;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

/**
 * 播放列表
 */
@Entity
public class PlaySheet implements Serializable, MultiItemEntity {

    public static final long serialVersionUID = 1;
    public static final int ITEM_TYPE = 1;

    @Id//主键 自增id
    private long id;
    @Unique//唯一约束 列表名称
    private String name;
    //列表创建时间
    private long createTime;

    public PlaySheet(String name) {
        this.name = name;
        createTime = System.currentTimeMillis();
    }

    @Generated(hash = 376358198)
    public PlaySheet(long id, String name, long createTime) {
        this.id = id;
        this.name = name;
        this.createTime = createTime;
    }

    @Generated(hash = 212871209)
    public PlaySheet() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
        return ITEM_TYPE;
    }
}
