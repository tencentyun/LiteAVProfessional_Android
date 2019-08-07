package com.tencent.liteav.demo.videorecord.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.liteav.demo.videorecord.utils.TCUtils;
import com.tencent.liteav.demo.videorecord.R;
import com.tencent.liteav.demo.videorecord.RecordDef;
import com.tencent.ugc.TXRecordCommon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Link on 2016/9/8.
 */
public class TCAudioControl extends LinearLayout implements SeekBar.OnSeekBarChangeListener, Button.OnClickListener {
    public static final int NEXTBGM = 1;
    public static final int PREVIOUSBGM = 2;
    public static final int RANDOMBGM = 3;
    //Audio Control
    public static final String TAG = TCAudioControl.class.getSimpleName();
    private SeekBar mMicVolumeSeekBar;
    private SeekBar mBGMVolumeSeekBar;

    private Button mBtnReverbDefalult;
    private Button mBtnReverb1;
    private Button mBtnReverb2;
    private Button mBtnReverb3;
    private Button mBtnReverb4;
    private Button mBtnReverb5;
    private Button mBtnReverb6;
    private int mLastReverbIndex;

    private Button           mBtnVoiceChangerDefault;
    private Button           mBtnVoiceChanger1;
    private Button           mBtnVoiceChanger2;
    private Button           mBtnVoiceChanger3;
    private Button           mBtnVoiceChanger4;
//    private Button           mBtnVoiceChanger5;
    private Button           mBtnVoiceChanger6;
    private Button           mBtnVoiceChanger7;
    private Button           mBtnVoiceChanger8;
    private Button           mBtnVoiceChanger9;
    private Button           mBtnVoiceChanger10;
    private Button           mBtnVoiceChanger11;
    private int              mLastVoiceChangerIndex;

    private Button mBtnStopBgm;
    private Button mBtnAutoSearch;
    private Button mBtnSelectActivity;
    private int mMicVolume = 100;
    private int mBGMVolume = 100;
    private boolean mBGMSwitch = false;
    private boolean mScanning = false;
    Context mContext;
    List<MediaEntity> mMusicListData;
    MusicListView mMusicList;
    public TCMusicSelectView mMusicSelectView;
    public LinearLayout mMusicControlPart;
    private int mSelectItemPos = -1;
    private int mLastPlayingItemPos = -1;
    public static final int REQUESTCODE = 1;
    private Map<String, String> mPathSet;
    private RecordDef.OnItemClickListener mOnItemClickListener;
    private AudioListener mAudioListener;

    public interface AudioListener{
        void onSetReverb(int reverbType);
        void onSetVoiceChangerType(int voiceChangeType);
        void onClickStopBgm();
        void onSetMicVolume(float volume);
        void onSetBGMVolume(float volume);
        int onGetMusicDuration(String musicPath);
    }

    public void setAudioListener(AudioListener audioListener){
        mAudioListener = audioListener;
        if(mAudioListener != null){
            mAudioListener.onSetBGMVolume(mBGMVolume / (float)100);
            mAudioListener.onSetMicVolume(mMicVolume / (float)100);
        }
    }

    public TCAudioControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.audio_ctrl, this);
        init();
    }

    public TCAudioControl(Context context) {
        super(context);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.audio_ctrl, this);
        init();
    }

    public void setReturnListener(OnClickListener onClickListener){
        mMusicSelectView.setReturnListener(onClickListener);
    }

    public final Activity getActivity() {
        return (Activity) mContext;
    }

    private void playBGM(String name, String path, int pos) {
        if (mLastPlayingItemPos >= 0 && mLastPlayingItemPos != pos) {
            mMusicListData.get(mLastPlayingItemPos).state = 0;
        }
        if (mOnItemClickListener !=null){
            mOnItemClickListener.onBGMSelect(path);
        }
        mBGMSwitch = true;
        mMusicListData.get(pos).state = 1;
        mLastPlayingItemPos = pos;
        mMusicList.getAdapter().notifyDataSetChanged();
    }

    public void stopBGM() {
        mBGMSwitch = false;
        if (mAudioListener != null) {
            mAudioListener.onClickStopBgm();
        }

        if(mMusicListData.size() != 0 && mLastPlayingItemPos >= 0){
            mMusicListData.get(mLastPlayingItemPos).state = 0;
            mMusicList.getAdapter().notifyDataSetChanged();
        }

        if (mOnItemClickListener !=null){
            mOnItemClickListener.onBGMSelect(null);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_reverb_default) {
            if (mAudioListener != null) {
                mAudioListener.onSetReverb(TXRecordCommon.VIDOE_REVERB_TYPE_0);
            }

        } else if (i == R.id.btn_reverb_1) {
            if (mAudioListener != null) {
                mAudioListener.onSetReverb(TXRecordCommon.VIDOE_REVERB_TYPE_1);
            }

        } else if (i == R.id.btn_reverb_2) {
            if (mAudioListener != null) {
                mAudioListener.onSetReverb(TXRecordCommon.VIDOE_REVERB_TYPE_2);
            }

        } else if (i == R.id.btn_reverb_3) {
            if (mAudioListener != null) {
                mAudioListener.onSetReverb(TXRecordCommon.VIDOE_REVERB_TYPE_3);
            }

        } else if (i == R.id.btn_reverb_4) {
            if (mAudioListener != null) {
                mAudioListener.onSetReverb(TXRecordCommon.VIDOE_REVERB_TYPE_4);
            }

        } else if (i == R.id.btn_reverb_5) {
            if (mAudioListener != null) {
                mAudioListener.onSetReverb(TXRecordCommon.VIDOE_REVERB_TYPE_5);
            }

        } else if (i == R.id.btn_reverb_6) {
            if (mAudioListener != null) {
                mAudioListener.onSetReverb(TXRecordCommon.VIDOE_REVERB_TYPE_6);
            }

        } else if (i == R.id.btn_voicechanger_default) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_0);
            }

        } else if (i == R.id.btn_voicechanger_1) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_1);
            }

        } else if (i == R.id.btn_voicechanger_2) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_2);
            }

        } else if (i == R.id.btn_voicechanger_3) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_3);
            }

        } else if (i == R.id.btn_voicechanger_4) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_4);
            }

        } else if (i == R.id.btn_voicechanger_6) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_6);
            }

        } else if (i == R.id.btn_voicechanger_7) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_7);
            }

        } else if (i == R.id.btn_voicechanger_8) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_8);
            }

        } else if (i == R.id.btn_voicechanger_9) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_9);
            }

        } else if (i == R.id.btn_voicechanger_10) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_10);
            }

        } else if (i == R.id.btn_voicechanger_11) {
            if (mAudioListener != null) {
                mAudioListener.onSetVoiceChangerType(TXRecordCommon.VIDOE_VOICECHANGER_TYPE_11);
            }

        } else if (i == R.id.btn_stop_bgm) {
            stopBGM();

        }

        if(v.getId() == mLastReverbIndex || v.getId() == mLastVoiceChangerIndex){
            // 防止重复点击相同的按钮，导致的变调和变声二选一问题
            return;
        }

        if (R.id.btn_stop_bgm != v.getId() && v.getId() != mLastReverbIndex &&
                (v.getId() == R.id.btn_reverb_default || v.getId() == R.id.btn_reverb_1 ||
                        v.getId() == R.id.btn_reverb_2 || v.getId() == R.id.btn_reverb_3 ||
                        v.getId() == R.id.btn_reverb_4 || v.getId() == R.id.btn_reverb_5 ||
                        v.getId() == R.id.btn_reverb_6)) {   // 混响
            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_button_3));

            View lastV = findViewById(mLastReverbIndex);
            if (null != lastV) {
                lastV.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_button_2));
            }

            mLastReverbIndex = v.getId();

        } else if (R.id.btn_stop_bgm != v.getId() && v.getId() != mLastVoiceChangerIndex) {  // 变声
            v.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_button_3));

            View lastV = findViewById(mLastVoiceChangerIndex);
            if (null != lastV) {
                lastV.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_button_2));
            }

            mLastVoiceChangerIndex = v.getId();
        }
    }

    public void setOnItemClickListener(RecordDef.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void init() {
        mMicVolumeSeekBar = (SeekBar) findViewById(R.id.seekBar_voice_volume);
        mMicVolumeSeekBar.setOnSeekBarChangeListener(this);
        mBGMVolumeSeekBar = (SeekBar) findViewById(R.id.seekBar_bgm_volume);
        mBGMVolumeSeekBar.setOnSeekBarChangeListener(this);
        mBGMVolume = mBGMVolumeSeekBar.getProgress() * 2;
        mMicVolume = mMicVolumeSeekBar.getProgress() * 2;

        mBtnReverbDefalult = (Button) findViewById(R.id.btn_reverb_default);
        mBtnReverbDefalult.setOnClickListener(this);
        mBtnReverb1 = (Button) findViewById(R.id.btn_reverb_1);
        mBtnReverb1.setOnClickListener(this);
        mBtnReverb2 = (Button) findViewById(R.id.btn_reverb_2);
        mBtnReverb2.setOnClickListener(this);
        mBtnReverb3 = (Button) findViewById(R.id.btn_reverb_3);
        mBtnReverb3.setOnClickListener(this);
        mBtnReverb4 = (Button) findViewById(R.id.btn_reverb_4);
        mBtnReverb4.setOnClickListener(this);
        mBtnReverb5 = (Button) findViewById(R.id.btn_reverb_5);
        mBtnReverb5.setOnClickListener(this);
        mBtnReverb6 = (Button) findViewById(R.id.btn_reverb_6);
        mBtnReverb6.setOnClickListener(this);

        mBtnVoiceChangerDefault = (Button) findViewById(R.id.btn_voicechanger_default);
        mBtnVoiceChangerDefault.setOnClickListener(this);
        mBtnVoiceChanger1 = (Button) findViewById(R.id.btn_voicechanger_1);
        mBtnVoiceChanger1.setOnClickListener(this);
        mBtnVoiceChanger2 = (Button) findViewById(R.id.btn_voicechanger_2);
        mBtnVoiceChanger2.setOnClickListener(this);
        mBtnVoiceChanger3 = (Button) findViewById(R.id.btn_voicechanger_3);
        mBtnVoiceChanger3.setOnClickListener(this);
        mBtnVoiceChanger4 = (Button) findViewById(R.id.btn_voicechanger_4);
        mBtnVoiceChanger4.setOnClickListener(this);
//        mBtnVoiceChanger5 = (Button) findViewById(R.id.btn_voicechanger_5);
//        mBtnVoiceChanger5.setOnClickListener(this);
        mBtnVoiceChanger6 = (Button) findViewById(R.id.btn_voicechanger_6);
        mBtnVoiceChanger6.setOnClickListener(this);
        mBtnVoiceChanger7 = (Button) findViewById(R.id.btn_voicechanger_7);
        mBtnVoiceChanger7.setOnClickListener(this);
        mBtnVoiceChanger8 = (Button) findViewById(R.id.btn_voicechanger_8);
        mBtnVoiceChanger8.setOnClickListener(this);
        mBtnVoiceChanger9 = (Button) findViewById(R.id.btn_voicechanger_9);
        mBtnVoiceChanger9.setOnClickListener(this);
        mBtnVoiceChanger10 = (Button) findViewById(R.id.btn_voicechanger_10);
        mBtnVoiceChanger10.setOnClickListener(this);
        mBtnVoiceChanger11 = (Button) findViewById(R.id.btn_voicechanger_11);
        mBtnVoiceChanger11.setOnClickListener(this);
        mBtnStopBgm = (Button) findViewById(R.id.btn_stop_bgm);
        mBtnStopBgm.setOnClickListener(this);

        mBtnSelectActivity = (Button) findViewById(R.id.btn_select_bgm);
        mMusicSelectView = (TCMusicSelectView) findViewById(R.id.xml_music_select_view);
        mMusicControlPart = (LinearLayout) findViewById(R.id.xml_music_control_part);
        mMusicListData = new ArrayList<MediaEntity>();
        mMusicSelectView.init(this, mMusicListData);
        mMusicList = mMusicSelectView.mMusicList;
        mPathSet = new HashMap<String, String>();
        mBtnAutoSearch = mMusicSelectView.mBtnAutoSearch;
        mMusicSelectView.setBackgroundColor(0xffffffff);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        mMusicSelectView.setMinimumHeight(height);

        mBtnSelectActivity.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMusicSelectView.setVisibility(mMusicSelectView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                // mMusicControlPart.setVisibility(View.GONE);
//                mTCBgmRecordView.setVisibility(mTCBgmRecordView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        mMusicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playBGM(mMusicListData.get(position).title, mMusicListData.get(position).path, position);
                mSelectItemPos = position;
            }
        });


        mBtnAutoSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanning) {
                    mScanning = false;
                    fPause = true;
                } else {
                    mScanning = true;
                    getMusicList(mContext, mMusicListData);
                    mScanning = false;
                    //mMusicScanner.startScanner(mContext,mCurScanPath,mMusicListData);
                    if (mMusicListData.size() > 0) {
                        mMusicList.setupList(LayoutInflater.from(mContext), mMusicListData);
                        mSelectItemPos = 0;
                        mMusicList.requestFocus();
                        mMusicList.setItemChecked(0, true);
                    }
                }
            }
        });

        setDefaultRevertAndVoiceChange();
    }

    private void setDefaultRevertAndVoiceChange() {
        mBtnReverbDefalult.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_button_3));
        mLastReverbIndex = R.id.btn_reverb_default;

        mBtnVoiceChangerDefault.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_button_3));
        mLastVoiceChangerIndex = R.id.btn_voicechanger_default;
    }

    public void unInit() {
        if (mBGMSwitch) {
            stopBGM();
        }
    }

    public void processActivityResult(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri,
                new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.SIZE},
                null, null, null);
        MediaEntity mediaEntity = new MediaEntity();
        if (cursor == null) {
            Log.e(TAG, "GetMediaList cursor is null.");
            mediaEntity.duration = 0;
            mediaEntity.path = TCUtils.getPath(mContext, uri);
            String[] names = mediaEntity.path.split("/");
            if (names != null) {
                mediaEntity.display_name = names[names.length - 1];
                mediaEntity.title = mediaEntity.display_name;
            } else {
                mediaEntity.display_name = "未命名歌曲";
                mediaEntity.title = mediaEntity.display_name;
            }
        } else {
            int count = cursor.getCount();
            if (count <= 0) {
                cursor.close();
                Log.e(TAG, "GetMediaList cursor count is 0.");
                return;
            }
            cursor.moveToFirst();

            mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            mediaEntity.display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            String title = mediaEntity.display_name.split("\\.")[0];
            mediaEntity.title = title.equals("") ? mediaEntity.display_name : title;
            mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
            mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            if (mediaEntity.path == null) {
                mediaEntity.path = TCUtils.getPath(mContext, uri);
            }
            mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            cursor.close();
        }
        if (mediaEntity.path == null) {
            Toast.makeText(mContext, "Get Music Path Error", Toast.LENGTH_SHORT);
            return;
        } else {
            if (mPathSet.get(mediaEntity.path) != null) {
                Toast.makeText(mContext, "请勿重复添加", Toast.LENGTH_SHORT);
                return;
            }
        }
        mPathSet.put(mediaEntity.path, mediaEntity.display_name);
        if (mediaEntity.duration == 0) {
            if (mAudioListener != null) {
                mediaEntity.duration = mAudioListener.onGetMusicDuration(mediaEntity.path);
            }
        }
        mediaEntity.durationStr = longToStrTime(mediaEntity.duration);
        mMusicListData.add(mediaEntity);
        mSelectItemPos = mMusicListData.size() - 1;
        mMusicList.setupList(LayoutInflater.from(mContext), mMusicListData);
        mMusicList.requestFocus();
        mMusicList.setItemChecked(mSelectItemPos, true);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.seekBar_voice_volume) {
            mMicVolume = progress * 2;
            if (mAudioListener != null) {
                mAudioListener.onSetMicVolume(mMicVolume / (float) 100);
            }
        } else if (seekBar.getId() == R.id.seekBar_bgm_volume) {
            mBGMVolume = progress * 2;
            if (mAudioListener != null) {
                mAudioListener.onSetBGMVolume(mBGMVolume / (float) 100);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    class MusicScanner extends BroadcastReceiver {
        private AlertDialog.Builder builder = null;
        private AlertDialog ad = null;
        Context mContext;
        List<MediaEntity> mList;
        TextView mPathView;

        public void startScanner(Context context, TextView pathView, List<MediaEntity> list) {
            mContext = context;
            mList = list;
            mPathView = pathView;
            IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
            intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            intentfilter.addDataScheme("file");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String[] paths = new String[]{Environment.getExternalStorageDirectory().toString()};
                MediaScannerConnection.scanFile(mContext, paths, null, null);
            } else {
                mContext.registerReceiver(this, intentfilter);
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath())));
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                builder = new AlertDialog.Builder(context, R.style.ConfirmDialogStyle);
                builder.setMessage("正在扫描存储卡...");
                ad = builder.create();
                ad.show();
            } else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                getMusicList(mContext, mList);
                ad.dismiss();
            }
        }
    }

    String longToStrTime(long time) {
        time /= 1000;
        return (time / 60) + ":" + ((time % 60) > 9 ? (time % 60) : ("0" + (time % 60)));
    }

    static public boolean fPause = false;

    public void getMusicList(Context context, List<MediaEntity> list) {
        Cursor cursor = null;
        List<MediaEntity> mediaList = list;
        try {
            cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DISPLAY_NAME,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.SIZE},
                    null, null, MediaStore.Video.Media.DEFAULT_SORT_ORDER);
            if (cursor == null) {
                Log.e(TAG, "GetMediaList cursor is null.");
                return;
            }
            int count = cursor.getCount();
            if (count <= 0) {
                Log.e(TAG, "GetMediaList cursor count is 0.");
                return;
            }
            MediaEntity mediaEntity = null;
            while (!fPause && cursor.moveToNext()) {
                mediaEntity = new MediaEntity();
                mediaEntity.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                mediaEntity.title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                mediaEntity.display_name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                mediaEntity.size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                mediaEntity.artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                mediaEntity.path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if (mediaEntity.path == null) {
                    fPause = false;
                    Toast.makeText(mContext, "Get Music Path Error", Toast.LENGTH_SHORT);
                    return;
                } else {
                    if (mPathSet.get(mediaEntity.path) != null) {
                        Toast.makeText(mContext, "请勿重复添加", Toast.LENGTH_SHORT);
                        fPause = false;
                        return;
                    }
                }
                mPathSet.put(mediaEntity.path, mediaEntity.display_name);
                mediaEntity.duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                if (mediaEntity.duration == 0) {
                    if (mAudioListener != null) {
                        mediaEntity.duration = mAudioListener.onGetMusicDuration(mediaEntity.path);
                    }
                }
                mediaEntity.durationStr = longToStrTime(mediaEntity.duration);
                mediaList.add(mediaEntity);
            }
            fPause = false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return;
    }

    class MediaEntity implements Serializable {
        private static final long serialVersionUID = 1L;
        public int id; //id标识
        public String title; // 显示名称
        public String display_name; // 文件名称
        public String path; // 音乐文件的路径
        public int duration; // 媒体播放总时间
        public String albums; // 专辑
        public String artist; // 艺术家
        public String singer; //歌手
        public String durationStr;
        public long size;
        public char state = 0;//0:idle 1:playing

        MediaEntity() {

        }

    }
}

