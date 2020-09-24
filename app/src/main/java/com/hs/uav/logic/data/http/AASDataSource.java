package com.hs.uav.logic.data.http;

import com.hs.uav.logic.data.http.service.LoginReq;

import io.reactivex.Observable;
import me.goldze.mvvmhabit.http.BaseResponse;

/**
 * 用户相关数据接口定义
 * appKey 暂用abcabc
 */
public interface AASDataSource {
    /***
     * 短信，google验证码校验
     * @return
     */
    Observable<BaseResponse> getServerConfig(LoginReq loginReq);
}
