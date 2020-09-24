package com.hs.uav.logic.listener;

import com.MAVLink.DLink.msg_mediafile_information;

public interface DownloadFileLinster {
    /***
     * 获取文件数量
     * @param count
     */
    void getFileCount(int count);

    /***
     * 单个文件存储信息
     */
    void saveFileLocal(msg_mediafile_information msg_mediafile_information);

}
