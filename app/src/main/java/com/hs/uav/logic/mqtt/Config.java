package com.hs.uav.logic.mqtt;


import io.reactivex.Observer;

public class Config {
    /**
     * serverUrl : 服务器地址（协议+地址+端口号）
     */
    private String serverUrl = "";
    /**
     * USERNAME : 用户名
     */
    private String USERNAME = "";

    /**
     * PASSWORD : 密码
     */
    private String PASSWORD = "";

    /**
     * clientID : 用户唯一标识
     */
    private String clientID;

    /**
     * startObserver : 启动监听
     */
    private Observer startObserver;

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setUSERNAME(String USERNAME) {
        this.USERNAME = USERNAME;
    }

    public void setPASSWORD(String PASSWORD) {
        this.PASSWORD = PASSWORD;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public void setStartObserver(Observer startObserver) {
        this.startObserver = startObserver;
    }


    public String getServerUrl() {
        return serverUrl;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    public String getClientID() {
        return clientID;
    }

    public Observer getStartObserver() {
        return startObserver;
    }

    public static class Builder {
        private String serverUrl = "";
        private String USERNAME = "";
        private String PASSWORD = "";
        private String clientID;
        private Observer startObserver;

        public Builder setServerUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder setUSERNAME(String USERNAME) {
            this.USERNAME = USERNAME;
            return this;
        }

        public Builder setPASSWORD(String PASSWORD) {
            this.PASSWORD = PASSWORD;
            return this;
        }

        public Builder setClientID(String clientID) {
            this.clientID = clientID;
            return this;
        }

        public Builder setStartObserver(Observer startObserver) {
            this.startObserver = startObserver;
            return this;
        }

        public Config create() {
            Config config = new Config();
            if (serverUrl != null) {
                config.setServerUrl(serverUrl);
            }
            if (USERNAME != null) {
                config.setUSERNAME(USERNAME);
            }
            if (PASSWORD != null) {
                config.setPASSWORD(PASSWORD);
            }
            if (clientID != null) {
                config.setClientID(clientID);
            }
            if (startObserver != null) {
                config.setStartObserver(startObserver);
            }
            return config;
        }
    }
}
