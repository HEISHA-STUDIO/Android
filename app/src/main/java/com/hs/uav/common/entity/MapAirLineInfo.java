package com.hs.uav.common.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/***
 * 飞行地图航线实体
 */
@Entity
public class MapAirLineInfo{
    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    private String preImagePath;//预览图地址
    @NotNull
    private String pointsJson; //航线对应的标记点集合
    @NotNull
    private String createTime; //航线创建时间
    @NotNull
    private String pinCode;    //无人机设备的CODE
    @NotNull
    private String UUID;       //航线自定义编码

    public boolean isCheck;

    @Generated(hash = 897366414)
    public MapAirLineInfo(Long _id, @NotNull String preImagePath,
            @NotNull String pointsJson, @NotNull String createTime,
            @NotNull String pinCode, @NotNull String UUID, boolean isCheck) {
        this._id = _id;
        this.preImagePath = preImagePath;
        this.pointsJson = pointsJson;
        this.createTime = createTime;
        this.pinCode = pinCode;
        this.UUID = UUID;
        this.isCheck = isCheck;
    }
    @Generated(hash = 1702851550)
    public MapAirLineInfo() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getPreImagePath() {
        return this.preImagePath;
    }
    public void setPreImagePath(String preImagePath) {
        this.preImagePath = preImagePath;
    }
    public String getPointsJson() {
        return this.pointsJson;
    }
    public void setPointsJson(String pointsJson) {
        this.pointsJson = pointsJson;
    }
    public String getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
    public String getPinCode() {
        return this.pinCode;
    }
    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }
    public String getUUID() {
        return this.UUID;
    }
    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public boolean getIsCheck() {
        return this.isCheck;
    }
    public void setIsCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }
}
