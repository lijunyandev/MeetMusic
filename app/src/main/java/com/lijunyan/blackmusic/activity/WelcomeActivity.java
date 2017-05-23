package com.lijunyan.blackmusic.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.lijunyan.blackmusic.R;
import com.lijunyan.blackmusic.database.DBManager;

import java.util.Timer;
import java.util.TimerTask;


public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";
    private DBManager dbManager;
    private SharedPreferences sharepreferences;
    private SharedPreferences.Editor editor;
    private static final int PERMISSON_REQUESTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        dbManager = DBManager.getInstance(getApplicationContext());
        sharepreferences=this.getSharedPreferences("check", MODE_PRIVATE);
        editor=sharepreferences.edit();
        initPermission();
    }

    private void checkSkip(){
//        boolean fristload=sharepreferences.getBoolean("fristload", true);
//        Log.d(TAG, "checkSkip: fristload = "+fristload);
//        if (fristload) {
//            startScanLocalMusic();
//            editor.putBoolean("fristload", false);
//            editor.commit();
//        } else {
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    startMusicActivity();
                }
            };
            timer.schedule(task,1500);
//        }
    }

    private void startMusicActivity() {
        Intent intent = new Intent();
        intent.setClass(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

//    public void startScanLocalMusic() {
//
//        new Thread() {
//
//            @Override
//            public void run() {
//                super.run();
//                Date startDate = new Date(System.currentTimeMillis());
//                try {
//                    String[] muiscInfoArray = new String[]{
//                            MediaStore.Audio.Media._ID,                 //歌曲ID
//                            MediaStore.Audio.Media.TITLE,               //歌曲名称
//                            MediaStore.Audio.Media.ARTIST,              //歌曲歌手
//                            MediaStore.Audio.Media.ALBUM,               //歌曲的专辑名
//                            MediaStore.Audio.Media.DURATION,            //歌曲时长
//                            MediaStore.Audio.Media.DATA};               //歌曲文件的全路径
//                    Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                            muiscInfoArray, null, null, null);
//                    if (cursor.getCount() != 0){
//                        List<MusicInfo> musicInfoList = new ArrayList<MusicInfo>();
//                        while (cursor.moveToNext()) {
//                            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
//                            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
//                            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
//                            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
//                            File file = new File(path);
//                            String parentPath = file.getParentFile().getPath();
//
//                            name = ScanActivity.replaseUnKnowe(name);
//                            singer = ScanActivity.replaseUnKnowe(singer);
//                            album = ScanActivity.replaseUnKnowe(album);
//                            path = ScanActivity.replaseUnKnowe(path);
//
//                            MusicInfo musicInfo = new MusicInfo();
//
//                            musicInfo.setName(name);
//                            musicInfo.setSinger(singer);
//                            musicInfo.setAlbum(album);
//                            musicInfo.setPath(path);
//                            musicInfo.setParentPath(parentPath);
//                            musicInfo.setFirstLetter(ChineseToEnglish.StringToPinyinSpecial(name).toUpperCase().charAt(0)+"");
//
//                            musicInfoList.add(musicInfo);
//
//                            try {
//                                sleep(50);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        // 根据a-z进行排序源数据
//                        Collections.sort(musicInfoList);
//                        dbManager.updateAllMusic(musicInfoList);
//                    }else {
//                        Toast.makeText(WelcomeActivity.this,"本地没有歌曲，快去下载吧",Toast.LENGTH_SHORT).show();
//                    }
//
//                    if (cursor != null) {
//                        cursor.close();
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                    Log.e(TAG, "run: error = ",e );
//                }finally {
//                    Date endDate = new Date(System.currentTimeMillis());
//                    long diff = endDate.getTime() - startDate.getTime();
//                    Log.e(TAG, "run: diff = "+ diff );
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            startMusicActivity();
//                        }
//                    });
//                }
//            }
//        }.start();
//    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            checkSkip();
            return;
        }
        if (ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSON_REQUESTCODE);
        }else {
            checkSkip();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSON_REQUESTCODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSkip();
                } else {
                    Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

}
