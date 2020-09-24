package com.hs.uav.common.app;


import com.hs.uav.logic.data.http.AASDataSource;
import com.hs.uav.logic.data.http.AASDataSourceImpl;
import com.hs.uav.logic.data.http.service.AASApiService;
import com.hs.uav.logic.data.local.LocalDataSource;
import com.hs.uav.logic.data.local.LocalDataSourceImpl;
import com.hs.uav.logic.data.repositor.AASDataRepository;
import com.hs.uav.logic.utils.RetrofitClient;

/**
 * 注入全局的数据仓库，可以考虑使用Dagger2。（根据项目实际情况搭建，千万不要为了架构而架构）
 */
public class Injection {
    /***
     * 登录及用户相关全局数据仓库获取
     * @return AASDataRepository
     */
    public static AASDataRepository aasDataRe() {
        //网络API服务
        AASApiService apiService = RetrofitClient.getInstance().create(AASApiService.class);
        //网络数据源
        AASDataSource httpDataSource = AASDataSourceImpl.getInstance(apiService);
        //本地数据源
        LocalDataSource localDataSource = LocalDataSourceImpl.getInstance();
        //两条分支组成一个数据仓库
        return AASDataRepository.getInstance(httpDataSource, localDataSource);
    }
}
