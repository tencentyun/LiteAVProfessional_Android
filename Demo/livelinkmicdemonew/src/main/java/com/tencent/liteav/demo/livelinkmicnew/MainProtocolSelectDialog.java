package com.tencent.liteav.demo.livelinkmicnew;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainProtocolSelectDialog extends DialogFragment {

    private TabLayout mTopTl;
    private ViewPager mContentVp;
    private List<Fragment> mFragmentList;
    private List<String> mTitleList;
    private PagerAdapter mPagerAdapter;
    /**
     * 控件布局相关
     */
    public final String[] TITLE_LIST = {"RTC", "CDN"};
    private CDNFragment mCDNFragment;
    //    private      RoomFragment          mRoomFragment;
    private TRTCFragment mTRTCFragment;
    private boolean mIsPlay = true;
    private OnFragmentClickListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.V2BaseFragmentDialogTheme);
        initFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        // 设置弹窗占据屏幕的大小
        Window window = getDialog().getWindow();
        if (window != null) {
            WindowManager.LayoutParams windowParams = window.getAttributes();
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            window.setAttributes(windowParams);
            Dialog dialog = getDialog();
            if (dialog != null) {
                dialog.getWindow().setLayout(getWidth(dm), getHeight(dm));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.live_link_mic_new_fragment_base_tab_setting, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    private void initData() {
        mFragmentList = getFragments();
        mTitleList = getTitleList();

        if (mFragmentList == null) {
            mFragmentList = new ArrayList<>();
        }
        mTopTl.setupWithViewPager(mContentVp, false);
        mPagerAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragmentList == null ? null : mFragmentList.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentList == null ? 0 : mFragmentList.size();
            }
        };
        mContentVp.setAdapter(mPagerAdapter);
        for (int i = 0; i < mTitleList.size(); i++) {
            TabLayout.Tab tab = mTopTl.getTabAt(i);
            if (tab != null) {
                tab.setText(mTitleList.get(i));
            }
        }
    }

    public void addFragment(Fragment fragment) {
        if (mFragmentList == null) {
            return;
        }
        mFragmentList.add(fragment);
    }

    private void initView(@NonNull final View itemView) {
        mTopTl = (TabLayout) itemView.findViewById(R.id.tl_top);
        mContentVp = (ViewPager) itemView.findViewById(R.id.vp_content);
    }

    private void initFragment() {
        if (mFragmentList == null) {
            mFragmentList = new ArrayList<>();
            mCDNFragment = new CDNFragment();
            mCDNFragment.setIsPlay(mIsPlay);
            mCDNFragment.setOnDismissListener(mListener);

//            mRoomFragment = new RoomFragment();
//            mRoomFragment.setIsPlay(mIsPlay);
//            mRoomFragment.setOnDismissListener(mListener);

            mTRTCFragment = new TRTCFragment();
            mTRTCFragment.setIsPlay(mIsPlay);
            mTRTCFragment.setOnDismissListener(mListener);

            mFragmentList.add(mTRTCFragment);
//            mFragmentList.add(mRoomFragment);
            mFragmentList.add(mCDNFragment);
        }
    }

    public void setIsPlay(boolean isPlay) {
        mIsPlay = isPlay;
    }

    public void setOnDismissListener(OnFragmentClickListener listener) {
        mListener = listener;
    }

    public void setCDNPusherURL(String url) {
        if (mCDNFragment != null) {
            mCDNFragment.setPusherURL(url);
        }
    }

    protected List<Fragment> getFragments() {
        return mFragmentList;
    }

    protected List<String> getTitleList() {
        return Arrays.asList(TITLE_LIST);
    }

    protected int getHeight(DisplayMetrics dm) {
        return (int) (dm.heightPixels * 0.5);
    }

    /**
     * 可以通过覆盖这个函数达到改变弹窗大小的效果
     *
     * @param dm DisplayMetrics
     * @return 界面宽度
     */
    protected int getWidth(DisplayMetrics dm) {
        return (int) (dm.widthPixels * 0.9);
    }

    public static class CDNFragment extends Fragment {

        private ImageView mQrCodeButton;
        private boolean mIsPlay;
        private TextView mStreamText;
        private Button mPlayButton;
        private Button mCancelButton;
        private EditText mEditUrl;
        private TextView mTextStreamTips;
        private Button mDefaultUrlButton;
        private OnFragmentClickListener mListener;

        public void setIsPlay(boolean isPlay) {
            mIsPlay = isPlay;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mQrCodeButton = (ImageView) view.findViewById(R.id.iv_cdn_btn_qr_code_scan);
            mQrCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onScanQRCode();
                    }
                }
            });
            mStreamText = (TextView) view.findViewById(R.id.tv_cdn_stream_text);
            mDefaultUrlButton = (Button) view.findViewById(R.id.tv_cdn_btn_normal_url);
            mDefaultUrlButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onCDNEditTextChange(mEditUrl);
                        mListener.onAutoFetchUrl();
                    }
                }
            });
            mTextStreamTips = (TextView) view.findViewById(R.id.tv_cdn_stream_tips);
            mEditUrl = (EditText) view.findViewById(R.id.et_input_cdn_url);
            mPlayButton = (Button) view.findViewById(R.id.cdn_btn_play);
            mPlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onCDNEditTextChange(mEditUrl);
                        mListener.onStart(false);
                    }
                }
            });
            mCancelButton = (Button) view.findViewById(R.id.cdn_btn_close);
            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onDismiss(true);
                    }
                }
            });

            if (!mIsPlay) {
                mStreamText.setText("自动生成推流地址或手动输入");
                mDefaultUrlButton.setText("自动生成推流地址");
                mTextStreamTips.setText("我有推流地址");
                mPlayButton.setText("开始推流");
                mEditUrl.setHint("请扫码输入推流地址");
                view.findViewById(R.id.tv_cdn_play_hint).setVisibility(View.VISIBLE);
                view.findViewById(R.id.tv_cdn_play_hint1).setVisibility(View.VISIBLE);
                view.findViewById(R.id.tv_cdn_play_hint2).setVisibility(View.VISIBLE);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.live_link_mic_new_protocol_cdn_entrance, container, false);
        }

        public void setOnDismissListener(OnFragmentClickListener listener) {
            mListener = listener;
        }

        public void setPusherURL(String url) {
            if (mEditUrl != null) {
                mEditUrl.setText(url);

                mListener.onCDNEditTextChange(mEditUrl);
                mListener.onStart(false);
            }
        }
    }


    public static class RoomFragment extends Fragment {

        private boolean mIsPlay;
        private ImageView mQrCodeButton;
        private OnFragmentClickListener mListener;
        private Button mStartButton;
        private Button mDefaultUrlButton;
        private Button mCancelButton;
        private EditText mEditRoomId;
        private EditText mEditUserId;
        private TextView mStreamText;
        private CheckBox mLinkMicCheckBox;
        private TextView mCheckTextView;

        public void setIsPlay(boolean isPlay) {
            mIsPlay = isPlay;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mQrCodeButton = (ImageView) view.findViewById(R.id.room_btn_qr_code_scan);
            mLinkMicCheckBox = (CheckBox) view.findViewById(R.id.room_cb_link_mic);
            mQrCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onScanQRCode();
                    }
                }
            });
            mDefaultUrlButton = (Button) view.findViewById(R.id.tv_room_btn_normal_url);
            mDefaultUrlButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onROOMEditTextChange(mEditRoomId, mEditUserId);
                        mListener.onAutoFetchUrl();
                    }
                }
            });
            mStartButton = (Button) view.findViewById(R.id.room_btn_play);
            mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onROOMEditTextChange(mEditRoomId, mEditUserId);
                        mListener.onStart(mLinkMicCheckBox.isChecked());
                    }
                }
            });
            mCancelButton = (Button) view.findViewById(R.id.room_btn_close);
            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onDismiss(true);
                    }
                }
            });
            mEditRoomId = (EditText) view.findViewById(R.id.room_et_input_roomid);
            mEditUserId = (EditText) view.findViewById(R.id.room_et_input_userid);
            mStreamText = (TextView) view.findViewById(R.id.tv_room_btn_normal_url);
            if (!mIsPlay) {
                view.findViewById(R.id.ll_link_mic).setVisibility(View.GONE);
                mCheckTextView = (TextView) view.findViewById(R.id.room_tv_link_mic);
                mCheckTextView.setText("开启录屏推流");
                mStartButton.setText("开始推流");
                mStreamText.setText("自动生成推流地址或手动输入");
                mDefaultUrlButton.setText("自动生成房间号和用户ID");
                mQrCodeButton.setVisibility(View.GONE);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.live_link_mic_new_protocol_room_entrance, container, false);
        }

        public void setOnDismissListener(OnFragmentClickListener listener) {
            mListener = listener;
        }

    }

    public static class TRTCFragment extends Fragment {

        private boolean mIsPlay;
        private ImageView mQrCodeButton;
        private OnFragmentClickListener mListener;
        private Button mStartButton;
        private Button mCancelButton;
        private EditText mInputSteamId;
        private CheckBox mLinkMicCheckBox;
        private TextView mCheckTextView;


        public void setIsPlay(boolean isPlay) {
            mIsPlay = isPlay;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            mQrCodeButton = (ImageView) view.findViewById(R.id.rtc_btn_qr_code_scan);
            mLinkMicCheckBox = (CheckBox) view.findViewById(R.id.rtc_cb_link_mic);
            mQrCodeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onScanQRCode();
                    }
                }
            });
            mQrCodeButton.setVisibility(View.GONE);
            mStartButton = (Button) view.findViewById(R.id.rtc_btn_play);
            mStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onTRTCEditTextChange(mInputSteamId);
                        mListener.onStart(mLinkMicCheckBox.isChecked());
                    }
                }
            });
            mCancelButton = (Button) view.findViewById(R.id.rtc_btn_close);
            mCancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onDismiss(true);
                    }
                }
            });
            mInputSteamId = (EditText) view.findViewById(R.id.rtc_et_input_streamid);
            if (!mIsPlay) {
                view.findViewById(R.id.ll_link_mic).setVisibility(View.GONE);
                mCheckTextView = (TextView) view.findViewById(R.id.rtc_tv_link_mic);
                mCheckTextView.setText("开启录屏推流");
                mStartButton.setText("开始推流");
                mQrCodeButton.setVisibility(View.GONE);
            }
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.live_link_mic_new_protocol_rtc_entrance, container, false);
        }

        public void setOnDismissListener(OnFragmentClickListener listener) {
            mListener = listener;
        }
    }

    public interface OnFragmentClickListener {
        void onDismiss(boolean isCancel);

        void onCDNEditTextChange(EditText editText);

        void onROOMEditTextChange(EditText editRoomId, EditText editUserId);

        void onTRTCEditTextChange(EditText editText);

        void onStart(boolean isLinkMic);

        void onAutoFetchUrl();

        void onScanQRCode();
    }

}