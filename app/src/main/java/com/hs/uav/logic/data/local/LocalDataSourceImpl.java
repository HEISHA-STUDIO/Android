package com.hs.uav.logic.data.local;

import me.goldze.mvvmhabit.utils.SPUtils;

/**
 * 本地数据源，可配合Room框架使用
 */
public class LocalDataSourceImpl implements LocalDataSource {
    private volatile static LocalDataSourceImpl INSTANCE = null;

    public static LocalDataSourceImpl getInstance() {
        if (INSTANCE == null) {
            synchronized (LocalDataSourceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LocalDataSourceImpl();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private LocalDataSourceImpl() {
        //数据库Helper构建
    }

    @Override
    public void saveInitPage(boolean flag) {
        SPUtils.getInstance().put("initPage", flag);
    }

    @Override
    public void saveUserName(String userName) {
        SPUtils.getInstance().put("UserName", userName);
    }

    @Override
    public void savePassword(String password) {
        SPUtils.getInstance().put("password", password);
    }

    @Override
    public void saveToken(String token) {
        SPUtils.getInstance().put("token", token);
    }

    @Override
    public void saveConfigInfo(String config) {
        SPUtils.getInstance().put("config", config);
    }

    @Override
    public void savePublicKey(String publicKey) {
        SPUtils.getInstance().put("publicKey", publicKey);
    }

    @Override
    public void saveLastMapLineId(String deviceID, String mapLineId) {
        SPUtils.getInstance().put(deviceID + "_mapline_id", mapLineId);
    }

    @Override
    public void saveCurrentUAVPoint(String deviceID, String location) {
        SPUtils.getInstance().put(deviceID + "_current_uav_point", location);
    }

    @Override
    public String getUserName() {
        return SPUtils.getInstance().getString("UserName");
    }

    @Override
    public String getPassword() {
        return SPUtils.getInstance().getString("password");
    }

    @Override
    public String getToken() {
        return SPUtils.getInstance().getString("token");
    }

    @Override
    public String getConfigInfo() {
        return SPUtils.getInstance().getString("config");
    }

    @Override
    public String getPublicKey() {
        return SPUtils.getInstance().getString("publicKey");
    }

    @Override
    public String getLastMapLineId(String deviceID) {
        return SPUtils.getInstance().getString(deviceID + "_mapline_id");
    }

    @Override
    public String getCurrentUAVPoint(String deviceID) {
        return SPUtils.getInstance().getString(deviceID + "_current_uav_point");
    }
}
