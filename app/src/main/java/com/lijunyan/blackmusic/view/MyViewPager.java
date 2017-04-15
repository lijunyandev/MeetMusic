package com.lijunyan.blackmusic.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by lijunyan on 2017/3/10.
 */

public class MyViewPager extends ViewPager {

    boolean isSliding = false;

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isSliding){
            return false;
        }else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isSliding){
            return false;
        }else {
            return super.onTouchEvent(ev);
        }
    }

    public void setSliding(boolean sliding) {
        isSliding = sliding;
    }
}
