package com.hs.uav.logic.data.repositor;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.hs.uav.logic.data.http.AASDataSource;
import com.hs.uav.logic.data.http.service.LoginReq;
import com.hs.uav.logic.data.local.LocalDataSource;

import io.reactivex.Observable;
import me.goldze.mvvmhabit.base.BaseModel;
import me.goldze.mvvmhabit.http.BaseResponse;

/**
 * MVVM的Model层，统一模块的数据仓库
 * 包含网络数据和本地数据（一个应用可以有多个Repositor）
 */
public class AASDataRepository extends BaseModel implements AASDataSource, LocalDataSource {

    private volatile static AASDataRepository INSTANCE = null;
    private final AASDataSource mHttpDataSource;
    private final LocalDataSource mLocalDataSource;

    private AASDataRepository(@NonNull AASDataSource httpDataSource,
                              @NonNull LocalDataSource localDataSource) {
        this.mHttpDataSource = httpDataSource;
        this.mLocalDataSource = localDataSource;
    }

    public static AASDataRepository getInstance(AASDataSource httpDataSource,
                                                LocalDataSource localDataSource) {
        if (INSTANCE == null) {
            synchronized (AASDataRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AASDataRepository(httpDataSource, localDataSource);
                }
            }
        }
        return INSTANCE;
    }

    @VisibleForTesting
    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<BaseResponse> getServerConfig(LoginReq loginReq) {
        return mHttpDataSource.getServerConfig(loginReq);
    }

    @Override
    public void saveInitPage(boolean flag) {
        mLocalDataSource.saveInitPage(flag);
    }

    @Override
    public void saveUserName(String userName) {
        mLocalDataSource.saveUserName(userName);
    }

    @Override
    public void savePassword(String password) {
        mLocalDataSource.savePassword(password);
    }

    @Override
    public void saveToken(String token) {
        mLocalDataSource.saveToken(token);
    }

    @Override
    public void saveConfigInfo(String config) {
        mLocalDataSource.saveConfigInfo(config);
    }

    @Override
    public void savePublicKey(String publicKey) {
        mLocalDataSource.savePublicKey(publicKey);
    }

    @Override
    public void saveLastMapLineId(String deviceID, String mapLineId) {
        mLocalDataSource.saveLastMapLineId(deviceID, mapLineId);
    }

    @Override
    public void saveCurrentUAVPoint(String deviceID, String location) {
        mLocalDataSource.saveCurrentUAVPoint(deviceID, location);
    }

    @Override
    public String getUserName() {
        return mLocalDataSource.getUserName();
    }

    @Override
    public String getPassword() {
        return mLocalDataSource.getPassword();
    }

    @Override
    public String getToken() {
        return mLocalDataSource.getToken();
    }

    @Override
    public String getConfigInfo() {
        return mLocalDataSource.getConfigInfo();
    }

    @Override
    public String getPublicKey() {
        return mLocalDataSource.getPublicKey();
    }

    @Override
    public String getLastMapLineId(String deviceID) {
        return mLocalDataSource.getLastMapLineId(deviceID);
    }

    @Override
    public String getCurrentUAVPoint(String deviceID) {
        return mLocalDataSource.getCurrentUAVPoint(deviceID);
    }

}
