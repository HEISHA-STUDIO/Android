package com.hs.uav.logic.listener;

public interface CommandAckLinstener {
    /***
     * 拍照
     */
    void takeCameraPic();

    /***
     * 无人机操作按钮状态
     * @param ack
     * @param isChange
     */
    void refreshMenuStatus(int ack,boolean isChange);
}
