package com.lijunyan.blackmusic.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.lijunyan.blackmusic.receiver.PlayerManagerReceiver;

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

