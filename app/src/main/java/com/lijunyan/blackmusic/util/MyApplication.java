package com.lijunyan.blackmusic.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.lijunyan.blackmusic.service.MusicPlayerService;

/**
 * Created by lijunyan on 2017/2/8.
 */

public class MyApplication extends Application{
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Intent startIntent = new Intent(MyApplication.this,MusicPlayerService.class);
        startService(startIntent);
    }



    public static Context getContext() {
        return context;
    }
}
