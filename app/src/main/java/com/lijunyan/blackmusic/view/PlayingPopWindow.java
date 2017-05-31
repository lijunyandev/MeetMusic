package com.lijunyan.blackmusic.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.database.DBManager;
import com.lijunyan.blackmusic.entity.MusicInfo;
import com.lijunyan.blackmusic.service.MusicPlayerService;
import com.lijunyan.blackmusic.util.Constant;
import com.lijunyan.blackmusic.util.MyMusicUtil;

import java.util.List;

/**
 * Created by lijunyan on 2017/4/5.
 */

public class PlayingPopWindow extends PopupWindow{
    
    private static final String TAG = PlayingPopWindow.class.getName();
    private View view;
    private Activity activity;
//    private ImageView playModeIv;
    private TextView countTv;
//    private TextView playModeTv;
    private RelativeLayout closeRv;
    private RecyclerView recyclerView;
    private Adapter adapter;
    private List<MusicInfo> musicInfoList;
    private DBManager dbManager;

    public PlayingPopWindow(Activity activity) {
        super(activity);
        this.activity = activity;
        dbManager = DBManager.getInstance(activity);
        musicInfoList = MyMusicUtil.getCurPlayList(activity);
        initView();
    }

    private void initView(){
        this.view = LayoutInflater.from(activity).inflate(R.layout.playbar_menu_window, null);
        this.setContentView(this.view);
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        int height = (int)(size.y * 0.5);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(height);

        this.setFocusable(true);
        this.setOutsideTouchable(true);

        // 设置弹出窗体的背景
        this.setBackgroundDrawable(activity.getResources().getDrawable(R.color.colorWhite));
        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.pop_window_animation);

        // 添加OnTouchListener监听判断获取触屏位置，如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int height = view.getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.playing_list_rv);
        adapter = new Adapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

//        playModeIv = (ImageView)view.findViewById(R.id.playing_list_playmode_iv);
//        playModeTv = (TextView) view.findViewById(R.id.playing_list_playmode_Tv);
        closeRv = (RelativeLayout) view.findViewById(R.id.playing_list_close_rv);
        countTv = (TextView)view.findViewById(R.id.playing_list_count_tv);
        countTv.setText("("+musicInfoList.size()+")");

//        initPlayMode();

//        playModeIv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switchPlayMode();
//            }
//        });

        closeRv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


//    private void initPlayMode() {
//        int playMode = MyMusicUtil.getIntShared(Constant.KEY_MODE);
//        if (playMode == -1) {
//            playMode = 0;
//        }
//        playModeIv.setImageLevel(playMode);
//        switch (playMode){
//            case Constant.PLAYMODE_SEQUENCE:
//                playModeTv.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
//                break;
//            case Constant.PLAYMODE_RANDOM:
//                playModeTv.setText(Constant.PLAYMODE_RANDOM_TEXT);
//                break;
//            case Constant.PLAYMODE_SINGLE_REPEAT:
//                playModeTv.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
//                break;
//        }
//    }

//    private void switchPlayMode() {
//        int playMode = MyMusicUtil.getIntShared(Constant.KEY_MODE);
//        switch (playMode){
//            case Constant.PLAYMODE_SEQUENCE:
//                playModeTv.setText(Constant.PLAYMODE_RANDOM_TEXT);
//                MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_RANDOM);
//                break;
//            case Constant.PLAYMODE_RANDOM:
//                playModeTv.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
//                MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_SINGLE_REPEAT);
//                break;
//            case Constant.PLAYMODE_SINGLE_REPEAT:
//                playModeTv.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
//                MyMusicUtil.setShared(Constant.KEY_MODE,Constant.PLAYMODE_SEQUENCE);
//                break;
//        }
//        initPlayMode();
//    }


    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder{
            LinearLayout contentLl;
            TextView nameTv;
            TextView singerTv;

            public ViewHolder(View itemView) {
                super(itemView);
                this.contentLl = (LinearLayout) itemView.findViewById(R.id.palybar_list_item_ll);
                this.nameTv = (TextView) itemView.findViewById(R.id.palybar_list_item_name_tv);
                this.singerTv = (TextView) itemView.findViewById(R.id.palybar_list_item_singer_tv);
            }
        }

        @Override
        public int getItemCount() {
            return musicInfoList.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_playbar_rv_list,parent,false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder,final int position) {
            Log.d(TAG, "onBindViewHolder: position = "+position);
            final MusicInfo musicInfo = musicInfoList.get(position);
            holder.nameTv.setText(musicInfo.getName());
            holder.singerTv.setText("-"+musicInfo.getSinger());

            if (musicInfo.getId() == MyMusicUtil.getIntShared(Constant.KEY_ID)){
                holder.nameTv.setTextColor(activity.getResources().getColor(R.color.colorAccent));
                holder.singerTv.setTextColor(activity.getResources().getColor(R.color.colorAccent));
            }else {
                holder.nameTv.setTextColor(activity.getResources().getColor(R.color.grey700));
                holder.singerTv.setTextColor(activity.getResources().getColor(R.color.grey500));
            }

            holder.contentLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: 播放 "+musicInfo.getName());
                    String path = dbManager.getMusicPath(musicInfo.getId());
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    intent.putExtra(Constant.KEY_PATH, path);
                    activity.sendBroadcast(intent);
                    MyMusicUtil.setShared(Constant.KEY_ID,musicInfo.getId());
                    notifyDataSetChanged();
                }
            });

        }

    }



}
