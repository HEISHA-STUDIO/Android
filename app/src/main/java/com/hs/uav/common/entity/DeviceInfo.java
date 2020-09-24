package com.hs.uav.common.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/***
 * 设备信息实体
 */
@Entity
public class DeviceInfo {
    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    private String deviceCode;//设备本地编号别称
    @NotNull
    private String devicePin;//设备编码
    @NotNull
    private String deviceName;//别名
    @Generated(hash = 1092783775)
    public DeviceInfo(Long _id, @NotNull String deviceCode,
            @NotNull String devicePin, @NotNull String deviceName) {
        this._id = _id;
        this.deviceCode = deviceCode;
        this.devicePin = devicePin;
        this.deviceName = deviceName;
    }
    @Generated(hash = 2125166935)
    public DeviceInfo() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getDeviceCode() {
        return this.deviceCode;
    }
    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }
    public String getDevicePin() {
        return this.devicePin;
    }
    public void setDevicePin(String devicePin) {
        this.devicePin = devicePin;
    }
    public String getDeviceName() {
        return this.deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


}
