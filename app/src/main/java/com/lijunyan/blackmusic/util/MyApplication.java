package com.lijunyan.blackmusic.util;

import android.app.Application;
import android.content.Context;

/**
 * Created by lijunyan on 2017/2/8.
 */

public class MyApplication extends Application{
    private static Context context;
    @Override
    public void onCreate() {
        context = getApplicationContext();
    }
    public static Context getContext() {
        return context;
    }
}
