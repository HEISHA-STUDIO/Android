package com.hs.uav.logic.listener;

public interface OnRefreshFlyDataLinstener {
    /****
     * 飞机运行数据刷新
     * @param d
     * @param h
     * @param hs
     * @param vs
     */
    void refreshFlyData(int d,int h,float hs,float vs);

    /***
     * 备飞进度
     * @param command
     * @param step 当前已完成步骤
     * @param total 指令总步数
     */
    void readyFlyProgress(int command,int step,int total);

    /***
     * 充电板相关操作按钮状态
     */
    void refreshMenuStatus();
}
