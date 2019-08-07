package com.tencent.liteav.demo.videorecord.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;

import com.tencent.liteav.demo.videorecord.R;

import java.util.List;

/**
 * Created by Link on 2016/9/14.
 */
public class TCMusicSelectView extends LinearLayout{

    static private final  String TAG = TCMusicSelectView.class.getSimpleName();
    private TCAudioControl mAudioCtrl;
    private TCActivityTitle atTitle;
    private Button          mBtnMenuSelect;
    private Context         mContext;
    public MusicListView    mMusicList;
    public Button           mBtnAutoSearch;
    public TCMusicSelectView(Context context, AttributeSet attrs){
        super(context,attrs);
        mContext = context;
    }

    public TCMusicSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public TCMusicSelectView(Context context) {
        super(context);
        mContext = context;
    }

    public void init(TCAudioControl audioControl, List<TCAudioControl.MediaEntity> data){
        mAudioCtrl = audioControl;
        LayoutInflater.from(mContext).inflate(R.layout.audio_ctrl_music_list,this);
        mMusicList = (MusicListView)findViewById(R.id.xml_music_list_view);
        mMusicList.setData(data);
        mBtnAutoSearch = (Button)findViewById(R.id.btn_auto_search);
        atTitle = (TCActivityTitle)findViewById(R.id.xml_music_select_activity);
    }

    public void setReturnListener(OnClickListener onClickListener){
        atTitle.setReturnListener(onClickListener);
    }
}
