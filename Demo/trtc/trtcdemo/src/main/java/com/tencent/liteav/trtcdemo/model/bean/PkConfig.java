package com.tencent.liteav.trtcdemo.model.bean;

import java.io.Serializable;

/**
 * PK Tab页 相关参数配置
 *
 * @author : xander
 * @date : 2021/5/31
 */
public class PkConfig implements Serializable {


    private String  mConnectRoomId   = "";    //需要连麦的房间号
    private String  mConnectUserName = "";    //需要连麦的用户名
    private boolean mIsConnected     = false;    //连麦状态，false 连接断开 true 连接成功

    public void reset() {
        mConnectRoomId = "";
        mConnectUserName = "";
        mIsConnected = false;
    }

    public String getConnectRoomId() {
        return mConnectRoomId;
    }

    public void setConnectRoomId(String connectRoomId) {
        mConnectRoomId = connectRoomId;
    }

    public String getConnectUserName() {
        return mConnectUserName;
    }

    public void setConnectUserName(String connectUserName) {
        mConnectUserName = connectUserName;
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void setConnected(boolean connected) {
        mIsConnected = connected;
    }
}
