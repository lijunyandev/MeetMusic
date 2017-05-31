package com.lijunyan.blackmusic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.adapter.DividerItemDecoration;
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
/*
*   最近播放和我喜爱 复用同一个Activity
* 
* */

public class LastMyloveActivity extends PlayBarBaseActivity {

    private static final String TAG = LocalMusicActivity.class.getName();
    private Toolbar toolbar;
    private RelativeLayout playModeRl;
    private ImageView playModeIv;
    private TextView playModeTv;
    private RecyclerView recyclerView;
    private SideBar sideBar;
    public  RecyclerViewAdapter recyclerViewAdapter;
    private List<MusicInfo> musicInfoList = new ArrayList<>();
    private DBManager dbManager;
    private String label;
    private UpdateReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_mylove);
        dbManager = DBManager.getInstance(LastMyloveActivity.this);
        label = getIntent().getStringExtra(Constant.LABEL);
        toolbar = (Toolbar)findViewById(R.id.last_mylove_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (label != null){
                actionBar.setTitle(label);
            }
        }
        init();
        register();
    }

    private void init(){
        recyclerView = (RecyclerView)findViewById(R.id.last_mylove_recycler_view);
        recyclerViewAdapter = new RecyclerViewAdapter(LastMyloveActivity.this, musicInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LastMyloveActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        //设置Item增加、移除动画
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(
                LastMyloveActivity.this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(recyclerViewAdapter);

        recyclerViewAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onOpenMenuClick(int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                showPopFormBottom(musicInfo);
            }

            @Override
            public void onDeleteMenuClick(View swipeView, int position) {
                deleteOperate(swipeView,position,LastMyloveActivity.this);
            }

            @Override
            public void onContentClick(int position) {
                if (label != null){
                    if (label.equals(Constant.LABEL_LAST)){
                        MyMusicUtil.setShared(Constant.KEY_LIST,Constant.LIST_LASTPLAY);
                    }else if (label.equals(Constant.LABEL_MYLOVE)){
                        MyMusicUtil.setShared(Constant.KEY_LIST,Constant.LIST_MYLOVE);
                    }
                }
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

        sideBar = (SideBar) findViewById(R.id.last_mylove_music_siderbar);
        sideBar.setOnListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String letter) {
                Log.i(TAG, "onTouchingLetterChanged: letter = " + letter);
                //该字母首次出现的位置
                int position = recyclerViewAdapter.getPositionForSection(letter.charAt(0));
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position);
                }
            }
        });

        playModeRl = (RelativeLayout)findViewById(R.id.last_mylove_playmode_rl);
        playModeIv = (ImageView)findViewById(R.id.last_mylove_playmode_iv);
        playModeTv = (TextView)findViewById(R.id.last_mylove_playmode_tv);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: " );
        updateView();
        initDefaultPlayModeView();
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    public void updateView(){
        try {
            if (label != null) {
                if (label.equals(Constant.LABEL_LAST)) {
                    musicInfoList = dbManager.getAllMusicFromTable(Constant.LIST_LASTPLAY);
                } else if (label.equals(Constant.LABEL_MYLOVE)) {
                    musicInfoList = dbManager.getAllMusicFromTable(Constant.LIST_MYLOVE);
                    Collections.sort(musicInfoList);
                }
            }
            recyclerViewAdapter.updateMusicInfoList(musicInfoList);

            if (musicInfoList.size() == 0) {
                sideBar.setVisibility(View.GONE);
                playModeRl.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
            } else {
                sideBar.setVisibility(View.VISIBLE);
                playModeRl.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void showPopFormBottom(MusicInfo musicInfo) {
        MusicPopMenuWindow menuPopupWindow;
        if (label.equals(Constant.LABEL_LAST)){
            menuPopupWindow = new MusicPopMenuWindow(LastMyloveActivity.this,musicInfo,findViewById(R.id.activity_last_mylove),Constant.ACTIVITY_RECENTPLAY);
        }else {
            menuPopupWindow = new MusicPopMenuWindow(LastMyloveActivity.this,musicInfo,findViewById(R.id.activity_last_mylove),Constant.ACTIVITY_MYLOVE);
        }

//      设置Popupwindow显示位置（从底部弹出）
        menuPopupWindow.showAtLocation(findViewById(R.id.activity_last_mylove), Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha=0.7f;
        getWindow().setAttributes(params);

        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        menuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha=1f;
                getWindow().setAttributes(params);
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
            if (label.equals(Constant.LABEL_LAST)){
                dbManager.removeMusic(musicInfo.getId(),Constant.ACTIVITY_RECENTPLAY);
            }else if (label.equals(Constant.ACTIVITY_MYLOVE)){
                dbManager.removeMusic(musicInfo.getId(),Constant.LIST_LASTPLAY);
            }
            if (curId == musicId) {
                //移除的是当前播放的音乐
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                sendBroadcast(intent);
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
    protected void onDestroy() {
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
            this.registerReceiver(mReceiver, intentFilter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unRegister() {
        try {
            if (mReceiver != null) {
                this.unregisterReceiver(mReceiver);
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
