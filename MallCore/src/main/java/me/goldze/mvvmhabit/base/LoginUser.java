package me.goldze.mvvmhabit.base;

import java.io.Serializable;

public class LoginUser implements Serializable {
    private int id;//用户ID
    private String appId;
    private String nickname;
    private String phone;
    private String email;
    private String lastLoginPreviousTime;//上一次登录时间
    private String lastLoginTime;//最后登陆时间
    private int lockStatus;//是否锁定
    private String code;//邀请码
    private int type;//用户类型 1为普通用户
    private int level;//用户级别(0普通用户1广告主审核中2广告主3广告主失败）
    private int checkStatus;
    private String checkTime;
    private int countryId;
    private CountryAreaVo countryAreaVO;
    private String headImage;
    private String createTime;
    private String updateTime;
    private String username;
    private String idNumber;//身份证号码
    private String token;
    private String publicKey;
    private String privateKey;
    private String aesKey;
    private UserOrderDealVO userOrderDealVO;
    private String priceLimit;
    private String deposit;
    private String depositUnit;
    private UserPayListVO userPayListVO;
    private String inviteCode; //邀请码
    private String inviteUrl;
    private String videoProve;
    private String imageFront;
    private String imageBack;
    private String updateNicknameTag;
    private int messageStatus;
    private String appLoginStatus;
    private String serviceKey;
    private String realName;
    private PopUserExtendVO popUserExtendVO;
    private String language;//语言标识 en_us 为中文、en_us为英文
    private int phoneStatus;//电话状态 1 为开启状态 0 为关闭状态
    private int emailStatus;//邮箱状态 1 为开启状态 0 为关闭状态
    private int kycStatus;//0未实名1审核中2审核成功3审核失败
    private int transactionPassword;//交易密码 返回 0 未设置,或 1已设置
    private String loginPassword;//登陆密码 返回 0 未设置,或 1已设置
    private String highAuthentication;//是否高级认证(0未认证1审核中2审核成功3审核失败
    private String inviteValid;//邀请是否有效 1 为有效，其他为无效
    private String kycAuthentication;  //KYC认证状态 0 未审核 1 审核中 2 普通审核成功 3 普通审核失败 4 高级审核中 5高级审核成功 6高级审核失败
    private int adStatus;//广告激活状态 0 未激活 1 已激活
    private int googleStatus;//google 是否开启 -1为默认状态 1 为开启状态 0 为关闭状态

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGoogleStatus() {
        return googleStatus;
    }

    public void setGoogleStatus(int googleStatus) {
        this.googleStatus = googleStatus;
    }

    public String getLastLoginPreviousTime() {
        return lastLoginPreviousTime;
    }

    public void setLastLoginPreviousTime(String lastLoginPreviousTime) {
        this.lastLoginPreviousTime = lastLoginPreviousTime;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public int getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(int lockStatus) {
        this.lockStatus = lockStatus;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(String checkTime) {
        this.checkTime = checkTime;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCountryId() {
        return countryId;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public CountryAreaVo getCountryAreaVO() {
        return countryAreaVO;
    }

    public void setCountryAreaVO(CountryAreaVo countryAreaVO) {
        this.countryAreaVO = countryAreaVO;
    }

    public String getHeadImage() {
        return headImage;
    }

    public void setHeadImage(String headImage) {
        this.headImage = headImage;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getTransactionPassword() {
        return transactionPassword;
    }

    public void setTransactionPassword(int transactionPassword) {
        this.transactionPassword = transactionPassword;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getHighAuthentication() {
        return highAuthentication;
    }

    public void setHighAuthentication(String highAuthentication) {
        this.highAuthentication = highAuthentication;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public UserOrderDealVO getUserOrderDealVO() {
        return userOrderDealVO;
    }

    public void setUserOrderDealVO(UserOrderDealVO userOrderDealVO) {
        this.userOrderDealVO = userOrderDealVO;
    }

    public String getPriceLimit() {
        return priceLimit;
    }

    public void setPriceLimit(String priceLimit) {
        this.priceLimit = priceLimit;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getDepositUnit() {
        return depositUnit;
    }

    public void setDepositUnit(String depositUnit) {
        this.depositUnit = depositUnit;
    }

    public UserPayListVO getUserPayListVO() {
        return userPayListVO;
    }

    public void setUserPayListVO(UserPayListVO userPayListVO) {
        this.userPayListVO = userPayListVO;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getInviteValid() {
        return inviteValid;
    }

    public void setInviteValid(String inviteValid) {
        this.inviteValid = inviteValid;
    }

    public String getVideoProve() {
        return videoProve;
    }

    public void setVideoProve(String videoProve) {
        this.videoProve = videoProve;
    }

    public String getImageFront() {
        return imageFront;
    }

    public void setImageFront(String imageFront) {
        this.imageFront = imageFront;
    }

    public String getImageBack() {
        return imageBack;
    }

    public void setImageBack(String imageBack) {
        this.imageBack = imageBack;
    }

    public String getUpdateNicknameTag() {
        return updateNicknameTag;
    }

    public void setUpdateNicknameTag(String updateNicknameTag) {
        this.updateNicknameTag = updateNicknameTag;
    }

    public String getKycAuthentication() {
        return kycAuthentication;
    }

    public void setKycAuthentication(String kycAuthentication) {
        this.kycAuthentication = kycAuthentication;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public int getAdStatus() {
        return adStatus;
    }

    public void setAdStatus(int adStatus) {
        this.adStatus = adStatus;
    }

    public String getAppLoginStatus() {
        return appLoginStatus;
    }

    public void setAppLoginStatus(String appLoginStatus) {
        this.appLoginStatus = appLoginStatus;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public PopUserExtendVO getPopUserExtendVO() {
        return popUserExtendVO;
    }

    public void setPopUserExtendVO(PopUserExtendVO popUserExtendVO) {
        this.popUserExtendVO = popUserExtendVO;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getPhoneStatus() {
        return phoneStatus;
    }

    public void setPhoneStatus(int phoneStatus) {
        this.phoneStatus = phoneStatus;
    }

    public int getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(int emailStatus) {
        this.emailStatus = emailStatus;
    }

    public int getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(int kycStatus) {
        this.kycStatus = kycStatus;
    }

    public class CountryAreaVo implements Serializable {
        private int id;
        private String zhName;
        private String countryLanguage;
        private String enName;
        private String areaCode;
        private String icon;
        private int status;
        private int language;
        private String remark;
        private String updateTime;
        private String createTime;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getZhName() {
            return zhName;
        }

        public void setZhName(String zhName) {
            this.zhName = zhName;
        }

        public String getCountryLanguage() {
            return countryLanguage;
        }

        public void setCountryLanguage(String countryLanguage) {
            this.countryLanguage = countryLanguage;
        }

        public String getEnName() {
            return enName;
        }

        public void setEnName(String enName) {
            this.enName = enName;
        }

        public String getAreaCode() {
            return areaCode;
        }

        public void setAreaCode(String areaCode) {
            this.areaCode = areaCode;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public int getLanguage() {
            return language;
        }

        public void setLanguage(int language) {
            this.language = language;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(String updateTime) {
            this.updateTime = updateTime;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }
    }

    public String getInviteUrl() {
        return inviteUrl;
    }

    public void setInviteUrl(String inviteUrl) {
        this.inviteUrl = inviteUrl;
    }

    public class UserOrderDealVO implements Serializable {
        private String orderVolume;
        private String turnoverRate;
        private String orderByPurchaseTotal;
        private String orderBySalesTotal;
        private String avgRelease;

        public String getOrderVolume() {
            return orderVolume;
        }

        public void setOrderVolume(String orderVolume) {
            this.orderVolume = orderVolume;
        }

        public String getTurnoverRate() {
            return turnoverRate;
        }

        public void setTurnoverRate(String turnoverRate) {
            this.turnoverRate = turnoverRate;
        }

        public String getOrderByPurchaseTotal() {
            return orderByPurchaseTotal;
        }

        public void setOrderByPurchaseTotal(String orderByPurchaseTotal) {
            this.orderByPurchaseTotal = orderByPurchaseTotal;
        }

        public String getOrderBySalesTotal() {
            return orderBySalesTotal;
        }

        public void setOrderBySalesTotal(String orderBySalesTotal) {
            this.orderBySalesTotal = orderBySalesTotal;
        }

        public String getAvgRelease() {
            return avgRelease;
        }

        public void setAvgRelease(String avgRelease) {
            this.avgRelease = avgRelease;
        }
    }

    public class UserPayListVO implements Serializable {
        private String alipayInfo;
        private String weChatInfo;
        private String bankCardInfo;
        private String aggregateCodeInfo;

        public String getAlipayInfo() {
            return alipayInfo;
        }

        public void setAlipayInfo(String alipayInfo) {
            this.alipayInfo = alipayInfo;
        }

        public String getWeChatInfo() {
            return weChatInfo;
        }

        public void setWeChatInfo(String weChatInfo) {
            this.weChatInfo = weChatInfo;
        }

        public String getBankCardInfo() {
            return bankCardInfo;
        }

        public void setBankCardInfo(String bankCardInfo) {
            this.bankCardInfo = bankCardInfo;
        }

        public String getAggregateCodeInfo() {
            return aggregateCodeInfo;
        }

        public void setAggregateCodeInfo(String aggregateCodeInfo) {
            this.aggregateCodeInfo = aggregateCodeInfo;
        }
    }

    public class PopUserExtendVO implements Serializable {
        private int id;
        private int userId;
        private String extractStatus;
        private String loginStatus;
        private String tradeStatus;
        private String popHighAuthentication;
        private String pricingCurrency;
        private String createTime;
        private String updateTime;
        private String showTips;
    }
}
