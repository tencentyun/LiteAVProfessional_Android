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
import android.widget.Toast;

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

public class PictureChooseFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "VideoChooseFragment";
    private RecyclerView mRecyclerView;
    private TCVideoEditerListAdapter mAdapter;
    private Button mBtnOk;
    private boolean reload;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picture_edit, null);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        if (reload) {
            loadPictureList();
        }
    }

    private void initView() {
        mBtnOk = (Button) getView().findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);

        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        mAdapter = new TCVideoEditerListAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setMultiplePick(true);
    }

    public void loadPictureList() {
        if (getActivity() == null) {
            reload = true;
            return;
        }
        ArrayList<TCVideoFileInfo> fileInfoArrayList = TCVideoEditerMgr.getAllPictrue(getActivity());

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
        if (v.getId() == R.id.btn_ok) {
            doSelect();
        }
    }

    private void doSelect() {
        Intent intent = new Intent(getActivity(), TCVideoEditerActivity.class);
        ArrayList<TCVideoFileInfo> pictureList = mAdapter.getInOrderMultiSelected();
        if (pictureList == null || pictureList.size() == 0) {
            TXCLog.d(TAG, "select file null");
            return;
        }
        if (pictureList.size() < 3) {
            Toast.makeText(getActivity(), "必须选择三个以上图片", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList picturePathList = new ArrayList();
        for (TCVideoFileInfo info : pictureList) {
            File file = new File(info.getFilePath());
            if (!file.exists()) {
                TCVideoEditUtil.showErrorDialog(getActivity(), "选择的文件不存在");
                return;
            }
            picturePathList.add(info.getFilePath());
        }
        intent.putExtra(TCConstants.INTENT_KEY_MULTI_PIC_CHOOSE, true);
        intent.putStringArrayListExtra(TCConstants.INTENT_KEY_MULTI_PIC_LIST, picturePathList);
        startActivity(intent);
        getActivity().finish();
    }

}
