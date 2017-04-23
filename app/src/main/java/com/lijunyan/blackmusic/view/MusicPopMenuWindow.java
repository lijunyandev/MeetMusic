package com.lijunyan.blackmusic.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.database.DBManager;
import com.lijunyan.blackmusic.entity.MusicInfo;
import com.lijunyan.blackmusic.entity.PlayListInfo;
import com.lijunyan.blackmusic.service.MusicPlayerService;
import com.lijunyan.blackmusic.util.Constant;
import com.lijunyan.blackmusic.util.MyMusicUtil;

import java.io.File;


/**
 * Created by lijunyan on 2017/2/22.
 */

public class MusicPopMenuWindow extends PopupWindow{

    private static final String TAG = MusicPopMenuWindow.class.getName();
    private View view;
    private Activity activity;
    private TextView nameTv;
//    private LinearLayout playLl;
    private LinearLayout addLl;
    private LinearLayout loveLl;
//    private LinearLayout ringLl;
    private LinearLayout deleteLl;
    private LinearLayout cancelLl;
    private MusicInfo musicInfo;
    private PlayListInfo playListInfo;
    private int witchActivity = Constant.ACTIVITY_LOCAL;
    private View parentView;

    public MusicPopMenuWindow(Activity activity, MusicInfo musicInfo,View parentView,int witchActivity) {
        super(activity);
        this.activity = activity;
        this.musicInfo = musicInfo;
        this.parentView = parentView;
        this.witchActivity = witchActivity;
        initView();
    }

    public MusicPopMenuWindow(Activity activity, MusicInfo musicInfo,View parentView,int witchActivity,PlayListInfo playListInfo) {
        super(activity);
        this.activity = activity;
        this.musicInfo = musicInfo;
        this.parentView = parentView;
        this.witchActivity = witchActivity;
        this.playListInfo = playListInfo;
        initView();
    }

    private void initView(){
        this.view = LayoutInflater.from(activity).inflate(R.layout.pop_window_menu, null);
        // 设置视图
        this.setContentView(this.view);
        // 设置弹出窗体的宽和高,不设置显示不出来
        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);

        // 设置弹出窗体可点击
        this.setFocusable(true);
        // 设置外部可点击
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


        nameTv = (TextView)view.findViewById(R.id.popwin_name_tv);
//        playLl = (LinearLayout) view.findViewById(R.id.popwin_play_ll);
        addLl = (LinearLayout) view.findViewById(R.id.popwin_add_rl);
        loveLl = (LinearLayout) view.findViewById(R.id.popwin_love_ll);
//        ringLl = (LinearLayout) view.findViewById(R.id.popwin_ring_ll);
        deleteLl = (LinearLayout) view.findViewById(R.id.popwin_delete_ll);
        cancelLl = (LinearLayout) view.findViewById(R.id.popwin_cancel_ll);

        nameTv.setText("歌曲： " + musicInfo.getName());

//        playLl.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                MyMusicUtil.playNextMusic(activity);
//
//                dismiss();
//            }
//        });

        addLl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dismiss();
                AddPlaylistWindow addPlaylistWindow = new AddPlaylistWindow(activity,musicInfo);
                addPlaylistWindow.showAtLocation(parentView, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
                WindowManager.LayoutParams params = activity.getWindow().getAttributes();
                //当弹出Popupwindow时，背景变半透明
                params.alpha=0.7f;
                activity.getWindow().setAttributes(params);
                //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
                addPlaylistWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
                        params.alpha=1f;
                        activity.getWindow().setAttributes(params);
                    }
                });
            }
        });

        loveLl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                MyMusicUtil.setMusicMylove(activity,musicInfo.getId());
                dismiss();
                View view = LayoutInflater.from(activity).inflate(R.layout.my_love_toast,null);
                Toast toast = new Toast(activity);
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });

//        ringLl.setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                MyMusicUtil.setMyRingtone(activity);
//                dismiss();
//            }
//        });

        deleteLl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                deleteOperate(musicInfo,activity);
                dismiss();
            }
        });

        cancelLl.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                dismiss();
            }
        });



    }

    public void deleteOperate(MusicInfo musicInfo,final Context context){
        final int curId = musicInfo.getId();
        final int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
        final DBManager dbManager = DBManager.getInstance(context);
        final String path = dbManager.getMusicPath(curId);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_delete_file,null);
        final CheckBox deleteFile = (CheckBox)view.findViewById(R.id.dialog_delete_cb);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.create();

        builder.setView(view);

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deleteFile.isChecked()){
                    //同时删除文件
                    //删除的是当前播放的音乐
                    File file = new File(path);
                    if (file.exists()) {
                        deleteMediaDB(file,context);
                        boolean ret = file.delete();
                        Log.e(TAG, "onClick: ret = "+ ret);
                        dbManager.deleteMusic(curId);
                    }
                    if (curId == musicId){
                        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                        context.sendBroadcast(intent);
                        MyMusicUtil.setShared(Constant.KEY_ID,dbManager.getFirstId(Constant.LIST_ALLMUSIC));
                    }
                }else {
                    //从列表移除
                    if (witchActivity == Constant.ACTIVITY_MYLIST){
                        dbManager.removeMusicFromPlaylist(curId,playListInfo.getId());
                    }else {
                        dbManager.removeMusic(curId,witchActivity);
                    }

                    if (curId == musicId) {
                        //移除的是当前播放的音乐
                        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                        context.sendBroadcast(intent);
                    }
                }
                if(onDeleteUpdateListener != null){
                    onDeleteUpdateListener.onDeleteUpdate();
                }
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public static void deleteMediaDB(File file,Context context){
        String filePath = file.getPath();
//        if(filePath.endsWith(".mp3")){
            int res = context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Audio.Media.DATA + "= \"" + filePath+"\"",
                    null);
            Log.i(TAG, "deleteMediaDB: res = "+res);
//        }
    }

    private OnDeleteUpdateListener onDeleteUpdateListener;

    public void setOnDeleteUpdateListener(OnDeleteUpdateListener onDeleteUpdateListener){
        this.onDeleteUpdateListener = onDeleteUpdateListener;
    }

    public interface OnDeleteUpdateListener {
        void onDeleteUpdate();
    }
}
