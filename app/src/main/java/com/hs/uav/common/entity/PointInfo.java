package com.hs.uav.common.entity;

import java.io.Serializable;

/***
 * 地图标记点实体
 */
public class PointInfo implements Serializable {
    private int pointID;
    private float lat;
    private float lag;
    private int altitude = 10;
    private boolean isCheck = false;
    public int getPointID() {
        return pointID;
    }

    public void setPointID(int pointID) {
        this.pointID = pointID;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLag() {
        return lag;
    }

    public void setLag(float lag) {
        this.lag = lag;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
