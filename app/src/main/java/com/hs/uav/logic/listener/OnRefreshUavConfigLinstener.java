package com.hs.uav.logic.listener;

/****
 * 无人机相关配置信息刷新
 * 含电量，信号状态
 */
public interface OnRefreshUavConfigLinstener {
    /***
     * 当前无人机的电量信息
     * @param currentBattery
     */
    void refreshBattery(int currentBattery);

    /***
     * 无人机电量用于飞行时长数据
     * @param outTimeBattery
     * @param totalTimeBattery
     */
    void refreshBatteryOutTime(int outTimeBattery,int totalTimeBattery);

    /***
     * 遥控器状态是否连上无人机
     * @param signStatus
     */
    void refreshSignStatus(int signStatus);

    /****
     * publish航点数据
     * @param seq
     * @param isFinish
     */
    void publishCurrentAirLinePoint(int seq,boolean isFinish);

    /****
     * 显示当前卫星连接数量
     * @param satellites_visible
     */
    void showGpsNumView(int satellites_visible);

    /****
     * 当前系统提示
     * @param tips
     */
    void currentSystemTips(int MAV_SEVERITY,String tips);

    /****
     * 遥控器到飞机之间的信号强度
     * @param rssi
     */
    void currentSignerStrong(int rssi);

    /***
     * 刷新地图缩略图
     */
    void refreshPointSmallImgView();
}
