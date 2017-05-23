package com.lijunyan.blackmusic.activity;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.database.DBManager;
import com.lijunyan.blackmusic.entity.MusicInfo;
import com.lijunyan.blackmusic.util.ChineseToEnglish;
import com.lijunyan.blackmusic.util.Constant;
import com.lijunyan.blackmusic.view.ScanView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 考虑本地没有歌曲的情况（待处理）
 */
public class ScanActivity extends AppCompatActivity {

    private static final String TAG = ScanActivity.class.getName();
    private DBManager dbManager;
    private Toolbar toolbar;
    private Button scanBtn;
    private TextView scanProgressTv;
    private TextView scanPathTv;
    private TextView scanCountTv;
    private CheckBox filterCb;
    private ScanView scanView;
    private Handler handler;
    private Message msg;
    private String scanPath;
    private int progress = 0;
    private int musicCount = 0;
    private boolean scanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        dbManager = DBManager.getInstance(ScanActivity.this);
        scanBtn = (Button) findViewById(R.id.start_scan_btn);
        toolbar = (Toolbar) findViewById(R.id.scan_music_toolbar);
        scanProgressTv = (TextView) findViewById(R.id.scan_progress);
        scanCountTv = (TextView) findViewById(R.id.scan_count);
        scanPathTv = (TextView) findViewById(R.id.scan_path);
        filterCb = (CheckBox) findViewById(R.id.scan_filter_cb);
        scanView = (ScanView) findViewById(R.id.scan_view);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!scanning) {
                    scanPathTv.setVisibility(View.VISIBLE);
                    scanning = true;
                    startScanLocalMusic();
                    scanView.start();
                    scanBtn.setText("停止扫描");
                }else {
                    scanPathTv.setVisibility(View.GONE);
                    scanning = false;
                    scanView.stop();
                    scanCountTv.setText("");
                    scanBtn.setText("开始扫描");
                }
            }
        });


        handler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Constant.SCAN_NO_MUSIC:
                        Toast.makeText(ScanActivity.this,"本地没有歌曲，快去下载吧",Toast.LENGTH_SHORT).show();
                        scanComplete();
                        break;
                    case Constant.SCAN_ERROR:
                        Toast.makeText(ScanActivity.this, "数据库错误", Toast.LENGTH_LONG).show();
                        scanComplete();
                        break;
                    case Constant.SCAN_COMPLETE:
                        scanComplete();
                        break;
                    case Constant.SCAN_UPDATE:
                        int updateProgress = msg.getData().getInt("progress");
                        String path = msg.getData().getString("scanPath");
                        scanCountTv.setText("已扫描到" + updateProgress + "首歌曲");
                        scanPathTv.setText(path);
                        break;
                }
            }
        };

    }

    private void scanComplete(){
        scanBtn.setText("完成");
        scanning = false;
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!scanning){
                    ScanActivity.this.finish();
                }
            }
        });
        scanView.stop();
    }
    public void startScanLocalMusic() {
        new Thread() {

            @Override
            public void run() {
                super.run();
                try {
                    String[] muiscInfoArray = new String[]{
                            MediaStore.Audio.Media.TITLE,               //歌曲名称
                            MediaStore.Audio.Media.ARTIST,              //歌曲歌手
                            MediaStore.Audio.Media.ALBUM,               //歌曲的专辑名
                            MediaStore.Audio.Media.DURATION,            //歌曲时长
                            MediaStore.Audio.Media.DATA};               //歌曲文件的全路径
                    Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            muiscInfoArray, null, null, null);
                    if (cursor.getCount() != 0){
                        List<MusicInfo> musicInfoList = new ArrayList<MusicInfo>();
                        Log.i(TAG, "run: cursor.getCount() = " + cursor.getCount());
                        while (cursor.moveToNext()) {
                            if (!scanning){
                                return;
                            }
                            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
                            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
                            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
                            String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));

                            if (filterCb.isChecked() && duration != null && Long.valueOf(duration) < 1000 * 60){
                                Log.e(TAG, "run: name = "+name+" duration < 1000 * 60" );
                                continue;
                            }

                            File file = new File(path);
                            String parentPath = file.getParentFile().getPath();

                            name = replaseUnKnowe(name);
                            singer = replaseUnKnowe(singer);
                            album = replaseUnKnowe(album);
                            path = replaseUnKnowe(path);

                            MusicInfo musicInfo = new MusicInfo();

                            musicInfo.setName(name);
                            musicInfo.setSinger(singer);
                            musicInfo.setAlbum(album);
                            musicInfo.setPath(path);
                            Log.e(TAG, "run: parentPath = "+parentPath );
                            musicInfo.setParentPath(parentPath);
                            musicInfo.setFirstLetter(ChineseToEnglish.StringToPinyinSpecial(name).toUpperCase().charAt(0)+"");

                            musicInfoList.add(musicInfo);
                            progress++;
                            scanPath = path;

                            musicCount = cursor.getCount();
                            msg = new Message();    //每次都必须new，必须发送新对象，不然会报错
                            msg.what = Constant.SCAN_UPDATE;
                            msg.arg1 = musicCount;
                            Bundle data = new Bundle();
                            data.putInt("progress", progress);
                            data.putString("scanPath", scanPath);
                            msg.setData(data);
                            handler.sendMessage(msg);  //更新UI界面

                            try {
                                sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        //扫描完成
                        msg = new Message();
                        msg.what = Constant.SCAN_COMPLETE;
                        handler.sendMessage(msg);  //更新UI界面

                        // 根据a-z进行排序源数据
                        Collections.sort(musicInfoList);
                        dbManager.updateAllMusic(musicInfoList);
                    }else {
                        msg = new Message();
                        msg.what = Constant.SCAN_NO_MUSIC;
                        handler.sendMessage(msg);  //更新UI界面
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e(TAG, "run: error = ",e );
                    //扫描出错
                    msg = new Message();
                    msg.what = Constant.SCAN_ERROR;
                    handler.sendMessage(msg);
                }
            }
        }.start();
    }

    public static String replaseUnKnowe(String oldStr){
        try {
            if (oldStr != null){
                if (oldStr.equals("<unknown>")){
                    oldStr = oldStr.replaceAll("<unknown>", "未知");
                }
            }
        }catch (Exception e){
            Log.e(TAG, "replaseUnKnowe: error = ",e );
        }
        return oldStr;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
