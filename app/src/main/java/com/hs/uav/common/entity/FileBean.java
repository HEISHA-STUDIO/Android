package com.hs.uav.common.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FileBean {
    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    private int fileType;//0-图片，1-视频
    @NotNull
    private String fileName;
    @NotNull
    private String filePath;
    @NotNull
    private int index; //序号
    private long totalLength;
    private long currentProgress;
    @Generated(hash = 1432680676)
    public FileBean(Long _id, int fileType, @NotNull String fileName,
            @NotNull String filePath, int index, long totalLength,
            long currentProgress) {
        this._id = _id;
        this.fileType = fileType;
        this.fileName = fileName;
        this.filePath = filePath;
        this.index = index;
        this.totalLength = totalLength;
        this.currentProgress = currentProgress;
    }
    @Generated(hash = 1910776192)
    public FileBean() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public int getFileType() {
        return this.fileType;
    }
    public void setFileType(int fileType) {
        this.fileType = fileType;
    }
    public String getFileName() {
        return this.fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFilePath() {
        return this.filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public int getIndex() {
        return this.index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public long getTotalLength() {
        return this.totalLength;
    }
    public void setTotalLength(long totalLength) {
        this.totalLength = totalLength;
    }
    public long getCurrentProgress() {
        return this.currentProgress;
    }
    public void setCurrentProgress(long currentProgress) {
        this.currentProgress = currentProgress;
    }

}
