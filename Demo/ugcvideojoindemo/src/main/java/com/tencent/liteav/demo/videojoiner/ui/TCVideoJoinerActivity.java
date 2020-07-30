package com.tencent.liteav.demo.videojoiner.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.liteav.demo.videojoiner.R;
import com.tencent.liteav.demo.videojoiner.ui.swipemenu.Closeable;
import com.tencent.liteav.demo.videojoiner.ui.swipemenu.OnSwipeMenuItemClickListener;
import com.tencent.liteav.demo.videojoiner.ui.swipemenu.SwipeMenu;
import com.tencent.liteav.demo.videojoiner.ui.swipemenu.SwipeMenuCreator;
import com.tencent.liteav.demo.videojoiner.ui.swipemenu.SwipeMenuItem;
import com.tencent.liteav.demo.videojoiner.ui.swipemenu.SwipeMenuRecyclerView;
import com.tencent.liteav.demo.videojoiner.ui.swipemenu.touch.OnItemMoveListener;
import com.tencent.qcloud.ugckit.UGCKitConstants;
import com.tencent.qcloud.ugckit.module.picker.data.TCVideoFileInfo;

import java.util.ArrayList;
import java.util.Collections;

public class TCVideoJoinerActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "TCVideoJoinerActivity";

    private Context                    mContext;
    private ArrayList<TCVideoFileInfo> mTCVideoFileInfoList;
    private MenuAdapter                mMenuAdapter;
    private Button                     mButtonPreview;
    private SwipeMenuRecyclerView     mSwipeMenuRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ugcjoin_activity_video_joiner);
        mTCVideoFileInfoList = getIntent().getParcelableArrayListExtra(UGCKitConstants.INTENT_KEY_MULTI_CHOOSE);
        if (mTCVideoFileInfoList == null || mTCVideoFileInfoList.size() == 0) {
            finish();
            return;
        }
        mContext = this;
        init();
    }

    private void init() {
        LinearLayout backLL = (LinearLayout) findViewById(R.id.back_ll);
        backLL.setOnClickListener(this);

        mButtonPreview = (Button) findViewById(R.id.segment_preview);
        mButtonPreview.setOnClickListener(this);

        mSwipeMenuRecyclerView = (SwipeMenuRecyclerView) findViewById(R.id.swipe_menu_recycler_view);
        mSwipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mSwipeMenuRecyclerView.setHasFixedSize(true);
        mSwipeMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画
        mSwipeMenuRecyclerView.addItemDecoration(new ListViewDecoration(this));// 添加分割线。

        mSwipeMenuRecyclerView.setSwipeMenuCreator(swipeMenuCreator);
        mSwipeMenuRecyclerView.setSwipeMenuItemClickListener(menuItemClickListener);

        mMenuAdapter = new MenuAdapter(this, mTCVideoFileInfoList);
        mMenuAdapter.setOnItemDeleteListener(onItemDeleteListener);
        mSwipeMenuRecyclerView.setAdapter(mMenuAdapter);

        mSwipeMenuRecyclerView.setLongPressDragEnabled(true);
        mSwipeMenuRecyclerView.setOnItemMoveListener(onItemMoveListener);
    }

    private OnItemMoveListener onItemMoveListener = new OnItemMoveListener() {
        @Override
        public boolean onItemMove(int fromPosition, int toPosition) {
            // 当Item被拖拽的时候。
            Collections.swap(mTCVideoFileInfoList, fromPosition, toPosition);
            mMenuAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onItemDismiss(int position) {
            // 当Item被滑动删除掉的时候，在这里是无效的，因为这里没有启用这个功能。
            // 使用Menu时就不用使用这个侧滑删除啦，两个是冲突的。
        }
    };


    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int width = getResources().getDimensionPixelSize(R.dimen.ugckit_qav_multi_video_friend_item_width);
            int height = getResources().getDimensionPixelSize(R.dimen.ugckit_qav_grid_view_item_width_audio);
            SwipeMenuItem deleteItem = new SwipeMenuItem(mContext)
                    .setBackgroundDrawable(R.color.ugckit_btn_red)
                    .setText("删除")
                    .setWidth(width)
                    .setHeight(height);

            swipeRightMenu.addMenuItem(deleteItem);
        }
    };

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.segment_preview) {
            if (mTCVideoFileInfoList == null || mTCVideoFileInfoList.size() < 2) {
                Toast.makeText(this, "必须选择两个以上视频文件", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(TCVideoJoinerActivity.this, TCVideoJoinerPreviewActivity.class);
            intent.putExtra(UGCKitConstants.INTENT_KEY_MULTI_CHOOSE, mTCVideoFileInfoList);
            startActivity(intent);
            finish();

        } else if (i == R.id.back_ll) {
            finish();

        }
    }

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    private OnDeleteListener onItemDeleteListener = new OnDeleteListener() {
        @Override
        public void onDelete(int position) {
            mSwipeMenuRecyclerView.smoothOpenRightMenu(position);
        }
    };

    private OnSwipeMenuItemClickListener menuItemClickListener = new OnSwipeMenuItemClickListener() {
        @Override
        public void onItemClick(Closeable closeable, int adapterPosition, int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。

            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
                mMenuAdapter.removeIndex(adapterPosition);
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return true;
    }
}
