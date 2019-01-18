# MeetMusic

### 概述
这是一款Android 端的本地音乐播放器，界面风格有模仿网易云音乐、bilibili、酷安、酷狗等。整体设计遵循了 Material Design 设计风格，界面美观，轻便实用，目前实现了基本的播放控制功能，还有主题切换， 可以扫描本地所有音乐文件并按歌单分类播放，目前已经上架到酷安应用市场。

### App效果图
![](https://github.com/lijunyandev/MeetMusic/blob/master/pic/home.jpg)
![](https://github.com/lijunyandev/MeetMusic/blob/master/pic/home_nav.jpg)
![](https://github.com/lijunyandev/MeetMusic/blob/master/pic/theme.jpg)
![](https://github.com/lijunyandev/MeetMusic/blob/master/pic/local_music.jpg)
![](https://github.com/lijunyandev/MeetMusic/blob/master/pic/scan.jpg)
![](https://github.com/lijunyandev/MeetMusic/blob/master/pic/playlist.jpg)
![](https://github.com/lijunyandev/MeetMusic/blob/master/pic/playing.jpg)

### 项目的播放流程简要介绍
1.首先我们需要一个常驻在后台的播放服务，在播放服务中绑定一个播放广播，我们在打开播放器的时候就启动这个播放服务。

```
public class MusicPlayerService extends Service {
    private static final String TAG = MusicPlayerService.class.getName();
 
    public static final String PLAYER_MANAGER_ACTION = "com.lijunyan.blackmusic.service.MusicPlayerService.player.action";
 
    private PlayerManagerReceiver mReceiver;
 
    public MusicPlayerService() {
    }
 
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
 
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
        register();
    }
 
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        unRegister();
    }
 
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }
 
 
    private void register() {
        mReceiver = new PlayerManagerReceiver(MusicPlayerService.this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PLAYER_MANAGER_ACTION);
        registerReceiver(mReceiver, intentFilter);
    }
 
    private void unRegister() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
 
}

```

2.播放服务中的广播可以接受各种音频控制操作，包括播放、暂停、切歌等。程序在响应用户的音频控制操作时向这个播放广播发送对应的播放、暂停、停止等指令。广播收到不同的指令做不同的功能实现。

```

public class PlayerManagerReceiver extends BroadcastReceiver {
 
    private static final String TAG = PlayerManagerReceiver.class.getName();
    public static final String ACTION_UPDATE_UI_ADAPTER = "com.lijunyan.blackmusic.receiver.PlayerManagerReceiver:action_update_ui_adapter_broad_cast";
    private MediaPlayer mediaPlayer;
    private DBManager dbManager;
    public static int status = Constant.STATUS_STOP;
    private int playMode;
    private int threadNumber;
    private Context context;
 
    public PlayerManagerReceiver() {
    }
 
    public PlayerManagerReceiver(Context context) {
        super();
        this.context = context;
        dbManager = DBManager.getInstance(context);
        mediaPlayer = new MediaPlayer();
        Log.d(TAG, "create");
        initMediaPlayer();
    }
 
    @Override
    public void onReceive(Context context, Intent intent) {
        int cmd = intent.getIntExtra(Constant.COMMAND,Constant.COMMAND_INIT);
        Log.d(TAG, "cmd = " + cmd);
        switch (cmd) {
            case Constant.COMMAND_INIT:    
                Log.d(TAG, "COMMAND_INIT");
                break;
            case Constant.COMMAND_PLAY:
                Log.d(TAG, "COMMAND_PLAY");
                status = Constant.STATUS_PLAY;
                String musicPath = intent.getStringExtra(Constant.KEY_PATH);
                if (musicPath!=null) {
                    playMusic(musicPath);
                }else {
                    mediaPlayer.start();
                }
                break;
            case Constant.COMMAND_PAUSE:
                mediaPlayer.pause();
                status = Constant.STATUS_PAUSE;
                break;
            case Constant.COMMAND_STOP: 
                NumberRandom();
                status = Constant.STATUS_STOP;
                if(mediaPlayer!=null) {
                    mediaPlayer.stop();
                }
                initStopOperate();
                break;
            case Constant.COMMAND_PROGRESS://拖动进度
                int curProgress = intent.getIntExtra(Constant.KEY_CURRENT, 0);
                //异步的，可以设置完成监听来获取真正定位完成的时候
                mediaPlayer.seekTo(curProgress);
                break;
            case Constant.COMMAND_RELEASE:
                NumberRandom();
                status = Constant.STATUS_STOP;
                if(mediaPlayer!=null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                break;
        }
        UpdateUI();
    }
 
    private void initStopOperate(){
        MyMusicUtil.setShared(Constant.KEY_ID,dbManager.getFirstId(Constant.LIST_ALLMUSIC));
    }
 
    private void playMusic(String musicPath) {
        NumberRandom();
        if (mediaPlayer!=null) {
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
 
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d(TAG, "playMusic onCompletion: ");
                NumberRandom();             //切换线程
                onComplete();     //调用音乐切换模块，进行相应操作
                UpdateUI();             //更新界面
            }
        });
 
        try {
            File file = new File(musicPath);
            if(!file.exists()){
                Toast.makeText(context,"歌曲文件不存在，请重新扫描",Toast.LENGTH_SHORT).show();
                MyMusicUtil.playNextMusic(context);
                return;
            }
            mediaPlayer.setDataSource(musicPath);   //设置MediaPlayer数据源
            mediaPlayer.prepare();
            mediaPlayer.start();
 
            new UpdateUIThread(this, context, threadNumber).start();
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
    }
 
 
    //取一个（0，100）之间的不一样的随机数
    private void NumberRandom() {
        int count;
        do {
            count =(int)(Math.random()*100);
        } while (count == threadNumber);
        threadNumber = count;
    }
 
    private void onComplete() {
        MyMusicUtil.playNextMusic(context);
    }
 
    private void UpdateUI() {
        Intent playBarintent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);    //接收广播为MusicUpdateMain
        playBarintent.putExtra(Constant.STATUS, status);
        context.sendBroadcast(playBarintent);
 
        Intent intent = new Intent(ACTION_UPDATE_UI_ADAPTER);    //接收广播为所有歌曲列表的adapter
        context.sendBroadcast(intent);
 
    }
 
 
    private void initMediaPlayer() {
 
        NumberRandom(); // 改变线程号,使旧的播放线程停止
 
        int musicId = MyMusicUtil.getIntShared(Constant.KEY_ID);
        int current = MyMusicUtil.getIntShared(Constant.KEY_CURRENT);
        Log.d(TAG, "initMediaPlayer musicId = " + musicId);
 
        // 如果是没取到当前正在播放的音乐ID，则从数据库中获取第一首音乐的播放信息初始化
        if (musicId == -1) {
            return;
        }
        String path = dbManager.getMusicPath(musicId);
        if (path == null) {
            Log.e(TAG, "initMediaPlayer: path == null");
            return;
        }
        if (current == 0) {
            status = Constant.STATUS_STOP; // 设置播放状态为停止
        }else {
            status = Constant.STATUS_PAUSE; // 设置播放状态为暂停
        }
        Log.d(TAG, "initMediaPlayer status = " + status);
        MyMusicUtil.setShared(Constant.KEY_ID,musicId);
        MyMusicUtil.setShared(Constant.KEY_PATH,path);
 
        UpdateUI();
    }
 
    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
 
    public int getThreadNumber() {
        return threadNumber;
    }
}

```

3.项目在播放一个音频的同时维护了一个线程实时去通知界面刷新，该线程从MediaPlayer中获取当前的播放进度、总时间等信息发送给播放界面，播放界面拿到数据就可以刷新播放显示信息了。

```

public class UpdateUIThread extends Thread {
 
   private static final String TAG = UpdateUIThread.class.getName();
   private int threadNumber;
   private Context context;
   private PlayerManagerReceiver playerManagerReceiver;
   private int duration;
   private int curPosition;
    
   public UpdateUIThread(PlayerManagerReceiver playerManagerReceiver, Context context, int threadNumber) {
      Log.i(TAG, "UpdateUIThread: " );
      this.playerManagerReceiver = playerManagerReceiver;
      this.context = context;
      this.threadNumber = threadNumber;
   }
 
   @Override
   public void run() {
      try {
         while (playerManagerReceiver.getThreadNumber() == this.threadNumber) {
            if (playerManagerReceiver.status == Constant.STATUS_STOP) {
               Log.e(TAG, "run: Constant.STATUS_STOP");
               break;
            }
            if (playerManagerReceiver.status == Constant.STATUS_PLAY ||
                  playerManagerReceiver.status == Constant.STATUS_PAUSE) {
               if (!playerManagerReceiver.getMediaPlayer().isPlaying()) {
                  Log.i(TAG, "run: getMediaPlayer().isPlaying() = " + playerManagerReceiver.getMediaPlayer().isPlaying());
                  break;
               }
               duration = playerManagerReceiver.getMediaPlayer().getDuration();
               curPosition = playerManagerReceiver.getMediaPlayer().getCurrentPosition();
               Intent intent = new Intent(PlayBarFragment.ACTION_UPDATE_UI_PlayBar);
               intent.putExtra(Constant.STATUS, Constant.STATUS_RUN);
               intent.putExtra(Constant.KEY_DURATION, duration);
               intent.putExtra(Constant.KEY_CURRENT, curPosition);
               context.sendBroadcast(intent);
            }
            try {
               Thread.sleep(100);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      }catch (Exception e){
         e.printStackTrace();
      }
       
   }
}

```


### 下载地址
[Download](http://www.coolapk.com/apk/com.lijunyan.blackmusic)
本项目目前已上架到酷安应用市场，可到酷安应用市场搜索“听听音乐”下载

### 开源协议
本项目可以供大家学习使用，如果用作商业用途，请先联系本人，否则侵权必究

### 其他
如果对你有帮助的话，可以给我个star，谢谢。



