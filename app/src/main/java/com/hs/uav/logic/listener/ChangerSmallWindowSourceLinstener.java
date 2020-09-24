package com.hs.uav.logic.listener;

/****
 * 小窗数据源切换
 */
public interface ChangerSmallWindowSourceLinstener {
    /***
     * 切换小窗数据源
     * @param smallWindow 1-地图；2-视频
     */
    void changeSmallWindow(int smallWindow);

    /***
     * 刷新小窗视图大小
     * @param oriLeft
     * @param oriRight
     * @param oriTop
     * @param oriBottom
     */
    void refreshSizeView(int oriLeft, int oriRight, int oriTop, int oriBottom);
}
