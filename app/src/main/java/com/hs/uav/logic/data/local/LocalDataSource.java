package com.hs.uav.logic.data.local;

public interface LocalDataSource {
    /*** 保存是否有初始引导画面*/
    void saveInitPage(boolean flag);

    /*** 保存用户名*/
    void saveUserName(String userName);

    /*** 保存用户密码*/
    void savePassword(String password);

    /*** 保存授权token*/
    void saveToken(String token);

    /*** 设置配置信息*/
    void saveConfigInfo(String config);

    /*** 保存公钥**/
    void savePublicKey(String publicKey);

    /*** 保存当前无人机最后上传的航线ID**/
    void saveLastMapLineId(String deviceID,String mapLineId);

    /*** 当前无人机所在位置***/
    void saveCurrentUAVPoint(String deviceID,String location);

    /*** 获取用户名*/
    String getUserName();

    /*** 获取用户密码*/
    String getPassword();

    /*** 获取授权token */
    String getToken();

    /*** 获取配置信息 */
    String getConfigInfo();

    /*** 获取公钥*/
    String getPublicKey();

    /*** 获取当前无人机诸侯上传的航线ID**/
    String getLastMapLineId(String deviceID);

    /*** 获取当前无人机所在位置**/
    String getCurrentUAVPoint(String deviceID);
}
