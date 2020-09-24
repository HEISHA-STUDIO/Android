package com.hs.uav.logic.data.db;

import com.hs.uav.common.entity.DeviceInfo;
import com.hs.uav.common.entity.FileBean;
import com.hs.uav.common.entity.MapAirLineInfo;
import com.hs.uav.greendao.gen.DeviceInfoDao;
import com.hs.uav.greendao.gen.FileBeanDao;
import com.hs.uav.greendao.gen.MapAirLineInfoDao;

/**
 * 数据库操作存放DaoUtils
 *
 * @author tony
 */
public class DaoUtilsStore {
    private volatile static DaoUtilsStore instance = new DaoUtilsStore();
    //设备信息数据库操作
    private CommonDaoUtils<DeviceInfo> deviceDaoUtils;
    private CommonDaoUtils<MapAirLineInfo> mapAirLineInfoDaoUtils;
    private CommonDaoUtils<FileBean> fileBeanDaoUtils;

    public static DaoUtilsStore getInstance() {
        return instance;
    }

    private DaoUtilsStore() {
        DaoManager mManager = DaoManager.getInstance();
        DeviceInfoDao deviceInfoDao = mManager.getDaoSession().getDeviceInfoDao();
        MapAirLineInfoDao mapAirLineInfoDao = mManager.getDaoSession().getMapAirLineInfoDao();
        FileBeanDao fileBeanDao = mManager.getDaoSession().getFileBeanDao();
        deviceDaoUtils = new CommonDaoUtils(DeviceInfo.class, deviceInfoDao);
        mapAirLineInfoDaoUtils = new CommonDaoUtils<>(MapAirLineInfo.class, mapAirLineInfoDao);
        fileBeanDaoUtils = new CommonDaoUtils<>(FileBean.class, fileBeanDao);

    }

    public CommonDaoUtils<DeviceInfo> getDeviceDaoUtils() {
        return deviceDaoUtils;
    }

    public CommonDaoUtils<MapAirLineInfo> getmapAirLineInfoDaoUtils() {
        return mapAirLineInfoDaoUtils;
    }

    public CommonDaoUtils<FileBean> getFileBeanDaoUtils() {
        return fileBeanDaoUtils;
    }

}