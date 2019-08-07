package com.tencent.liteav.demo.videoediter.paster;

/**
 * Created by vinsonswang on 2017/10/27.
 */

public class TCPasterInfo {
    private String iconPath;
    private String name;
    private int pasterType;
    private String pasterPath;

    public int getPasterType() {
        return pasterType;
    }

    public void setPasterType(int pasterType) {
        this.pasterType = pasterType;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
