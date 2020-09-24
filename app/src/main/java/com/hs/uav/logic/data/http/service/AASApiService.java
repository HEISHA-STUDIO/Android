package com.hs.uav.logic.data.http.service;


import io.reactivex.Observable;
import me.goldze.mvvmhabit.http.BaseResponse;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.POST;

/**
 * http数据接口定义
 */
public interface AASApiService {

    /***
     * 获取服务器MQTT 服务配置信息
     * {“username”:”heisha1”,”password”:”111111”}
     * @return
     */
    @POST("http://118.190.91.165:65530/usercfg")
    Observable<BaseResponse> getServerConfig(@Body LoginReq loginReq);
}