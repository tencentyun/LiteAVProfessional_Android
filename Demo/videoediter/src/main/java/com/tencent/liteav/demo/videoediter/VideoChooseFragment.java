package com.tencent.liteav.demo.videoediter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tencent.liteav.basic.log.TXCLog;
import com.tencent.liteav.demo.videoediter.common.utils.TCConstants;
import com.tencent.liteav.demo.videoediter.common.utils.TCVideoEditUtil;
import com.tencent.liteav.demo.videoediter.common.TCVideoEditerListAdapter;
import com.tencent.liteav.demo.videoediter.common.TCVideoEditerMgr;
import com.tencent.liteav.demo.videoediter.common.utils.TCVideoFileInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by liyuejiao on 2018/6/14.
 */

public class VideoChooseFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "VideoChooseFragment";
    private RecyclerView mRecyclerView;
    private TCVideoEditerListAdapter mAdapter;
    private boolean mLoad;
    private boolean reload;
    private Button mBtnImport;
    private Button mBtnFastImport;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_edit, null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        if (reload) {
            loadVideoList();
        }
    }

    private void initView() {
        mBtnImport = (Button) getView().findViewById(R.id.btn_import);
        mBtnImport.setOnClickListener(this);
        mBtnFastImport = (Button) getView().findViewById(R.id.btn_fast_import);
        mBtnFastImport.setOnClickListener(this);

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        mAdapter = new TCVideoEditerListAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setMultiplePick(false);
    }

    public void loadVideoList() {
        if (getActivity() == null) {
            reload = true;
            return;
        }
        ArrayList<TCVideoFileInfo> fileInfoArrayList = TCVideoEditerMgr.getAllVideo(getActivity());

        Message msg = new Message();
        msg.obj = fileInfoArrayList;
        mMainHandler.sendMessage(msg);
    }

    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<TCVideoFileInfo> fileInfoArrayList = (ArrayList<TCVideoFileInfo>) msg.obj;
            mAdapter.addAll(fileInfoArrayList);
        }
    };

    @Override
    public void onClick(View v) {
        boolean needProcessVideo = false;
        if (v.getId() == R.id.btn_fast_import) {
            needProcessVideo = false;
        } else if (v.getId() == R.id.btn_import) {
            needProcessVideo = true;
        }
        doSelect(needProcessVideo);
    }

    private void doSelect(boolean needProcessVideo) {
        Intent intent = new Intent(getActivity(), TCVideoPreprocessActivity.class);
        TCVideoFileInfo fileInfo = mAdapter.getSingleSelected();
        if (fileInfo == null) {
            TXCLog.d(TAG, "select file null");
            return;
        }
        if (TCVideoEditUtil.isVideoDamaged(fileInfo)) {
            TCVideoEditUtil.showErrorDialog(getActivity(), "该视频文件已经损坏");
            return;
        }
        File file = new File(fileInfo.getFilePath());
        if (!file.exists()) {
            TCVideoEditUtil.showErrorDialog(getActivity(), "选择的文件不存在");
            return;
        }
        intent.putExtra(TCConstants.VIDEO_EDITER_IMPORT, needProcessVideo);
        intent.putExtra(TCConstants.VIDEO_EDITER_PATH, fileInfo.getFilePath());
        startActivity(intent);
        getActivity().finish();
    }
}
