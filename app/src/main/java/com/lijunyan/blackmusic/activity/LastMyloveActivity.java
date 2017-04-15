package com.lijunyan.blackmusic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.adapter.DividerItemDecoration;
import com.lijunyan.blackmusic.adapter.RecyclerViewAdapter;
import com.lijunyan.blackmusic.database.DBManager;
import com.lijunyan.blackmusic.entity.MusicInfo;
import com.lijunyan.blackmusic.receiver.PlayerManagerReceiver;
import com.lijunyan.blackmusic.util.Constant;
import com.lijunyan.blackmusic.util.MyMusicUtil;

import java.util.List;
/*
*   最近播放和我喜爱 复用同一个Activity
* 
* */

public class LastMyloveActivity extends BaseActivity {

    private static final String TAG = LocalMusicActivity.class.getName();
    private Toolbar toolbar;
    private RelativeLayout playModeRl;
    private ImageView playModeIv;
    private TextView playModeTv;
    private RecyclerView recyclerView;
    public static RecyclerViewAdapter recyclerViewAdapter;
    private List<MusicInfo> musicInfoList;
    private DBManager dbManager;
    private String label;
    private UpdateReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_mylove);
        dbManager = DBManager.getInstance(LastMyloveActivity.this);
        label = getIntent().getStringExtra(Constant.LABEL);
        recyclerView = (RecyclerView)findViewById(R.id.last_mylove_recycler_view);
        toolbar = (Toolbar)findViewById(R.id.last_mylove_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (label != null){
                actionBar.setTitle(label);
                if (label.equals(Constant.LABEL_LAST)){
                    musicInfoList = dbManager.getAllMusicFromTable(Constant.LIST_LASTPLAY);
                }else if (label.equals(Constant.LABEL_MYLOVE)){
                    musicInfoList = dbManager.getAllMusicFromTable(Constant.LIST_MYLOVE);
                }
            }
        }
        recyclerViewAdapter = new RecyclerViewAdapter(LastMyloveActivity.this, musicInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LastMyloveActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        //设置Item增加、移除动画
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(
                LastMyloveActivity.this, DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(recyclerViewAdapter);

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
                        playModeIv.setImageResource(R.drawable.random);
                        playModeTv.setText(Constant.PLAYMODE_RANDOM_TEXT);
                        MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_RANDOM);
                        break;
                    case Constant.PLAYMODE_RANDOM:
                        playModeIv.setImageResource(R.drawable.single_cycle);
                        playModeTv.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
                        MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_SINGLE_REPEAT);
                        break;
                    case Constant.PLAYMODE_SINGLE_REPEAT:
                        playModeIv.setImageResource(R.drawable.sequence);
                        playModeTv.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
                        MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_SEQUENCE);
                        break;
                    default:
                        Log.e(TAG, "onClick: play mode default");
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: " );
        if (label != null){
            if (label.equals(Constant.LABEL_LAST)){
                musicInfoList = dbManager.getAllMusicFromTable(Constant.LIST_LASTPLAY);
            }else if (label.equals(Constant.LABEL_MYLOVE)){
                musicInfoList = dbManager.getAllMusicFromTable(Constant.LIST_MYLOVE);
            }
        }
        recyclerViewAdapter.updateMusicInfoList(musicInfoList);
    }

    private void initDefaultPlayModeView(){
        int playMode = MyMusicUtil.getIntShared(Constant.KEY_MODE);
        switch (playMode){
            case Constant.PLAYMODE_SEQUENCE:
                playModeIv.setImageResource(R.drawable.sequence);
                playModeTv.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
                break;
            case Constant.PLAYMODE_RANDOM:
                playModeIv.setImageResource(R.drawable.random);
                playModeTv.setText(Constant.PLAYMODE_RANDOM_TEXT);
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                playModeIv.setImageResource(R.drawable.single_cycle);
                playModeTv.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
                break;
            default:
                Log.e(TAG, "onClick: play mode default");
                break;
        }
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

    @Override
    protected void onStart() {
        super.onStart();
        register();

    }

    @Override
    protected void onStop() {
        super.onStop();
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
