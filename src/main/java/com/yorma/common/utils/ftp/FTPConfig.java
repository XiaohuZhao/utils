package com.yorma.common.utils.ftp;

/**
 * ftp配置类
 *
 * @author zxh
 * @version 1.0.0
 * @date 2019/03/28
 * @since 1.0.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class FTPConfig {

    private String url;
    private Integer port;
    private String username;
    private String password;
    private String localReceiptPath;
    private String localSendPath;
    private String remoteReceiptPath;
    private String remoteSendPath;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLocalReceiptPath() {
        return localReceiptPath;
    }

    public void setLocalReceiptPath(String localReceiptPath) {
        this.localReceiptPath = localReceiptPath;
    }

    public String getLocalSendPath() {
        return localSendPath;
    }

    public void setLocalSendPath(String localSendPath) {
        this.localSendPath = localSendPath;
    }

    public String getRemoteReceiptPath() {
        return remoteReceiptPath;
    }

    public void setRemoteReceiptPath(String remoteReceiptPath) {
        this.remoteReceiptPath = remoteReceiptPath;
    }

    public String getRemoteSendPath() {
        return remoteSendPath;
    }

    public void setRemoteSendPath(String remoteSendPath) {
        this.remoteSendPath = remoteSendPath;
    }
}
