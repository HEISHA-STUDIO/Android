package com.hs.uav.logic.listener;

import com.MAVLink.DLink.msg_gps_raw_int;
import com.hs.uav.common.entity.MapAirLineInfo;

/***
 * 刷新航线信息
 */
public interface OnRefreshLastAirLineListener {
    /***
     * 刷新航线信息
     * @param mapAirLineInfo
     */
    void refreshLastAirLine(MapAirLineInfo mapAirLineInfo);

    /***
     * 当前飞机飞行航线
     * @param gpsRaw
     */
    void refreshCurrentMapMarker(msg_gps_raw_int gpsRaw, int hdg);
}
