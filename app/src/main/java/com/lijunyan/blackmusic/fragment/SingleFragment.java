package com.lijunyan.blackmusic.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.adapter.RecyclerViewAdapter;
import com.lijunyan.blackmusic.database.DBManager;
import com.lijunyan.blackmusic.entity.MusicInfo;
import com.lijunyan.blackmusic.receiver.PlayerManagerReceiver;
import com.lijunyan.blackmusic.service.MusicPlayerService;
import com.lijunyan.blackmusic.util.Constant;
import com.lijunyan.blackmusic.util.MyMusicUtil;
import com.lijunyan.blackmusic.view.MusicPopMenuWindow;
import com.lijunyan.blackmusic.view.SideBar;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lijunyan on 2017/3/9.
 */

public class SingleFragment extends Fragment {

    private static final String TAG = "SingleFragment";
    private RelativeLayout playModeRl;
    private ImageView playModeIv;
    private TextView playModeTv;
    private RecyclerView recyclerView;
    private SideBar sideBar;
    private TextView sideBarPreTv;
    public  RecyclerViewAdapter recyclerViewAdapter;
    private List<MusicInfo> musicInfoList = new ArrayList<>();
    private DBManager dbManager;
    private View view;
    private Context context;
    private UpdateReceiver mReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        register();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        dbManager = DBManager.getInstance(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " );
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView: " );
        view = inflater.inflate(R.layout.fragment_single,container,false);
        Collections.sort(musicInfoList);
        recyclerView = (RecyclerView)view.findViewById(R.id.local_recycler_view);
        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(),musicInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onOpenMenuClick(int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                showPopFormBottom(musicInfo);
            }

            @Override
            public void onDeleteMenuClick(View swipeView, int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                deleteOperate(swipeView,position,context);
            }

            @Override
            public void onContentClick(int position) {
                MyMusicUtil.setShared(Constant.KEY_LIST,Constant.LIST_ALLMUSIC);
            }
        });

        // 当点击外部空白处时，关闭正在展开的侧滑菜单
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    SwipeMenuLayout viewCache = SwipeMenuLayout.getViewCache();
                    if (null != viewCache) {
                        viewCache.smoothClose();
                    }
                }
                return false;
            }
        });

        playModeRl = (RelativeLayout)view.findViewById(R.id.local_music_playmode_rl);
        playModeIv = (ImageView)view.findViewById(R.id.local_music_playmode_iv);
        playModeTv = (TextView)view.findViewById(R.id.local_music_playmode_tv);

        initDefaultPlayModeView();

        //  顺序 --> 随机-- > 单曲
        playModeRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int playMode = MyMusicUtil.getIntShared(Constant.KEY_MODE);
                switch (playMode){
                    case Constant.PLAYMODE_SEQUENCE:
                        playModeTv.setText(Constant.PLAYMODE_RANDOM_TEXT);
                        MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_RANDOM);
                        break;
                    case Constant.PLAYMODE_RANDOM:
                        playModeTv.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
                        MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_SINGLE_REPEAT);
                        break;
                    case Constant.PLAYMODE_SINGLE_REPEAT:
                        playModeTv.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
                        MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_SEQUENCE);
                        break;
                }
                initPlayMode();
            }
        });
        sideBarPreTv = (TextView) view.findViewById(R.id.local_music_siderbar_pre_tv);
        sideBar = (SideBar)view.findViewById(R.id.local_music_siderbar);
        sideBar.setTextView(sideBarPreTv);
        sideBar.setOnListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String letter) {
                Log.i(TAG, "onTouchingLetterChanged: letter = "+letter);
                //该字母首次出现的位置
                int position = recyclerViewAdapter.getPositionForSection(letter.charAt(0));
                if(position != -1){
                    recyclerView.smoothScrollToPosition(position);
                }
            }
        });
        return view;
    }

    private void initDefaultPlayModeView(){
        int playMode = MyMusicUtil.getIntShared(Constant.KEY_MODE);
        switch (playMode){
            case Constant.PLAYMODE_SEQUENCE:
                playModeTv.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
                break;
            case Constant.PLAYMODE_RANDOM:
                playModeTv.setText(Constant.PLAYMODE_RANDOM_TEXT);
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                playModeTv.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
                break;
        }
        initPlayMode();
    }

    private void initPlayMode() {
        int playMode = MyMusicUtil.getIntShared(Constant.KEY_MODE);
        if (playMode == -1) {
            playMode = 0;
        }
        playModeIv.setImageLevel(playMode);
    }

    public void updateView(){
        musicInfoList = dbManager.getAllMusicFromMusicTable();
        Collections.sort(musicInfoList);
        recyclerViewAdapter.updateMusicInfoList(musicInfoList);
        Log.d(TAG, "updateView: musicInfoList.size() = "+musicInfoList.size());
        if (musicInfoList.size() == 0){
            sideBar.setVisibility(View.GONE);
            playModeRl.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }else {
            sideBar.setVisibility(View.VISIBLE);
            playModeRl.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        initDefaultPlayModeView();
    }

    public void showPopFormBottom(MusicInfo musicInfo) {
        MusicPopMenuWindow menuPopupWindow = new MusicPopMenuWindow(getActivity(),musicInfo,view,Constant.ACTIVITY_LOCAL);
//      设置Popupwindow显示位置（从底部弹出）
        menuPopupWindow.showAtLocation(view, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha=0.7f;
        getActivity().getWindow().setAttributes(params);

        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        menuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                params.alpha=1f;
                getActivity().getWindow().setAttributes(params);
            }
        });
        menuPopupWindow.setOnDeleteUpdateListener(new MusicPopMenuWindow.OnDeleteUpdateListener() {
            @Override
            public void onDeleteUpdate() {
                updateView();
            }
        });

    }

    public void deleteOperate(final View swipeView,final int position,final Context context){
        final MusicInfo musicInfo = musicInfoList.get(position);
        final int curId = musicInfo.getId();
        final int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
        final DBManager dbManager = DBManager.getInstance(context);
        final String path = dbManager.getMusicPath(curId);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_delete_file,null);
        final CheckBox deleteFile = (CheckBox)view.findViewById(R.id.dialog_delete_cb);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setView(view);

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                update(swipeView,position,musicInfo,true);
                if (deleteFile.isChecked()){
                    //同时删除文件
                    //删除的是当前播放的音乐
                    File file = new File(path);
                    if (file.exists()) {
                        MusicPopMenuWindow.deleteMediaDB(file,context);
                        boolean ret = file.delete();
                        Log.e(TAG, "onClick: ret = "+ ret);
                        dbManager.deleteMusic(curId);
                    }else {
                        Toast.makeText(context,"找不到文件",Toast.LENGTH_SHORT).show();
                    }
                    if (curId == musicId){
                        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                        context.sendBroadcast(intent);
                        MyMusicUtil.setShared(Constant.KEY_ID,dbManager.getFirstId(Constant.LIST_ALLMUSIC));
                    }
                }else {
                }
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                update(swipeView,position,musicInfo,false);
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void update(View swipeView, int position,MusicInfo musicInfo,boolean isDelete){
        if (isDelete){
            final int curId = musicInfo.getId();
            final int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
            //从列表移除
            dbManager.removeMusic(musicInfo.getId(),Constant.ACTIVITY_LOCAL);
            if (curId == musicId) {
                //移除的是当前播放的音乐
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                context.sendBroadcast(intent);
            }
            recyclerViewAdapter.notifyItemRemoved(position);//推荐用这个
            updateView();
        }else {

        }
        //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
        //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
        ((SwipeMenuLayout) swipeView).quickClose();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    private void register() {
        try {
            if (mReceiver != null) {
                this.unRegister();
            }
            mReceiver = new UpdateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PlayerManagerReceiver.ACTION_UPDATE_UI_ADAPTER);
            this.context.registerReceiver(mReceiver, intentFilter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unRegister() {
        try {
            if (mReceiver != null) {
                this.context.unregisterReceiver(mReceiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class UpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            recyclerViewAdapter.notifyDataSetChanged();
        }
    }

}
