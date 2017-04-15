package com.lijunyan.blackmusic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.lijunyan.blackmusic.R;

/**
 * Created by lijunyan on 2017/3/7.
 */

public class ScanView extends View {

    private static final String TAG = "ScanView";

    /**
     * 画圆的笔
     */
    private Paint circlePaint;

    /**
     * 画扇形渲染的笔
     */
    private Paint sectorPaint;

    /**
     * 扫描线程
     */
    private ScanThread mThread;

    /**
     * 线程进行标志
     */
    private boolean threadFlag = false;

    /**
     * 线程启动标志
     */
    private boolean start = false;

    /**
     * 扇形转动的角度
     */
    private int angle = 0;

    /**
     * 当前视图的宽高，这里相同
     */
    private int viewSize;

    /**
     * 画在view中间的图片
     */
    private Bitmap bitmap;

    /**
     * 对图形进行处理的矩阵类
     */
    private Matrix matrix;

    public ScanView(Context context) {
        this(context, null);
    }

    public ScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBitmap();
    }

    /**
     * 此处设置viewSize固定值为600
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewSize = 600;
        setMeasuredDimension(viewSize, viewSize);
    }


    /**
     * 从资源中解码bitmap
     */
    private void initBitmap() {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_note);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint mPaint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rectd = new Rect((viewSize / 10) * 4, (viewSize / 10) * 4, viewSize - (viewSize / 10) * 4, viewSize - (viewSize / 10) * 4);
        canvas.drawBitmap(bitmap, rect, rectd, mPaint);

        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth((viewSize / 10));

        circlePaint.setColor(Color.parseColor("#DDFA7298"));
        canvas.drawCircle(viewSize / 2, viewSize / 2, (viewSize / 10) * 2.5f, circlePaint);

        circlePaint.setColor(Color.parseColor("#AAFA7298"));
        canvas.drawCircle(viewSize / 2, viewSize / 2, (viewSize / 10) * 3.5f, circlePaint);

        circlePaint.setColor(Color.parseColor("#66FA7298"));
        canvas.drawCircle(viewSize / 2, viewSize / 2, (viewSize / 10) * 4.5f, circlePaint);

        sectorPaint = new Paint();
        sectorPaint.setAntiAlias(true);
        sectorPaint.setStyle(Paint.Style.STROKE);
        sectorPaint.setStrokeWidth((viewSize / 10) * 3);
        Shader sectorShader = new SweepGradient(viewSize / 2, viewSize / 2, new int[]{Color.TRANSPARENT, 0x00FA7298, 0xFFFA7298},
                new float[]{0, 0.875f, 1f});
        sectorPaint.setShader(sectorShader);

        if (threadFlag) {
            canvas.concat(matrix);
            canvas.drawCircle(viewSize / 2, viewSize / 2, (viewSize / 10) * 3.5f, sectorPaint);
        }
    }


    public void start() {
        mThread = new ScanThread(this);
        mThread.start();
        threadFlag = true;
        start = true;
    }

    public void stop() {
        if (start) {
            threadFlag = false;
            start = false;
            invalidate();
        }
    }


    class ScanThread extends Thread {

        private ScanView view;

        public ScanThread(ScanView view) {
            this.view = view;
        }

        @Override
        public void run() {
            super.run();
            while (threadFlag) {
                if (start) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            angle = angle + 5;
                            if (angle == 360) {
                                angle = 0;
                            }
                            matrix = new Matrix();
                            matrix.setRotate(angle, viewSize / 2, viewSize / 2);
                            view.invalidate();
                        }
                    });
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}

