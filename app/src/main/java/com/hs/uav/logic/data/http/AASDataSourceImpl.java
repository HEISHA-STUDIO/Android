package com.hs.uav.logic.data.http;

import com.hs.uav.logic.data.http.service.AASApiService;
import com.hs.uav.logic.data.http.service.LoginReq;

import io.reactivex.Observable;
import me.goldze.mvvmhabit.http.BaseResponse;

/**
 * Http 数据源接口实现
 */
public class AASDataSourceImpl implements AASDataSource {
    private AASApiService apiService;
    private volatile static AASDataSourceImpl INSTANCE = null;

    public static AASDataSourceImpl getInstance(AASApiService apiService) {
        if (INSTANCE == null) {
            synchronized (AASDataSourceImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AASDataSourceImpl(apiService);
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private AASDataSourceImpl(AASApiService apiService) {
        this.apiService = apiService;
    }


    @Override
    public Observable<BaseResponse> getServerConfig(LoginReq loginReq) {
        return apiService.getServerConfig(loginReq);
    }
}
