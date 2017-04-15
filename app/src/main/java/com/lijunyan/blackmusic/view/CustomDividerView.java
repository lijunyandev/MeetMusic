package com.lijunyan.blackmusic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;


/**
 * Created by lijunyan on 2017/2/19.
 */

public class CustomDividerView extends View {

    private static final String TAG = CustomDividerView.class.getName();
    private Context mContext;

    public CustomDividerView(Context context) {
        super(context);
        this.mContext = context;
    }


    public CustomDividerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public CustomDividerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        int phoneWidth = outMetrics.widthPixels;
        int phoneHeight = outMetrics.heightPixels;
        int widthPx = dp2px(mContext,45);
        int heightPx = dp2px(mContext,0.5f);
        setMeasuredDimension(phoneWidth-widthPx, heightPx);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
