package com.tencent.liteav.demo.videoediter.paster;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinsonswang on 2017/10/27.
 */

public class TCPasterViewInfoManager {
    private static TCPasterViewInfoManager mInstance;
    private List<TCPasterViewInfo> mPasterViewInfoList;

    public static TCPasterViewInfoManager getInstance() {
        if (mInstance == null) {
            synchronized (TCPasterViewInfoManager.class) {
                if (mInstance == null) {
                    mInstance = new TCPasterViewInfoManager();
                }
            }
        }
        return mInstance;
    }

    private TCPasterViewInfoManager() {
        mPasterViewInfoList = new ArrayList<>();
    }

    public void add(TCPasterViewInfo tcPasterViewInfo) {
        mPasterViewInfoList.add(tcPasterViewInfo);
    }

    public void remove(int index) {
        mPasterViewInfoList.remove(index);
    }

    public void clear() {
        mPasterViewInfoList.clear();
    }

    public TCPasterViewInfo get(int index) {
        return mPasterViewInfoList.get(index);
    }

    public int getSize() {
        return mPasterViewInfoList.size();
    }

}
