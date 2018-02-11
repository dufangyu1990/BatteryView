package com.example.batteryview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by dufangyu on 2018/2/7.
 */

public class CustomView extends SurfaceView implements SurfaceHolder.Callback,Runnable {


    private Paint paintS,paintF;
    private int bWidth,bHeight;//整个电池宽高
    private int distance ;//电池头部那个小矩形的宽

    //电池头部的小矩形距离电池矩形框distance的距离，小矩形的宽也是distance。所以
    //bWidth = 电池的矩形框的宽+2*distance
    private int dx;//电池增量
    private int bColor,backgroundColor;
    private  float bRadius;
    private RectF SrectF,FrectF;

    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private boolean isRunning;//线程循环
    private boolean canDrawFlag;//是否允许画电池
    private int centerX,centerY;
    private Thread thread;
    private int contentValue = 0;
    private int tempContetnValue = 0;
    private int disProgress = 0;

    private int rightLocation;
    private int leftLocation;


    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }



    private void init( AttributeSet attrs)
    {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        SrectF = new RectF();//电池框矩形
        FrectF = new RectF();//电池容量矩形
        distance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,4,getContext().getResources().getDisplayMetrics());
        TypedArray attrArray = getContext().obtainStyledAttributes(attrs,R.styleable.BatteryLoadingView);
        if(attrArray != null)
        {
            bWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,60,getContext().getResources().getDisplayMetrics());
            bHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,20,getContext().getResources().getDisplayMetrics());
            bWidth = attrArray.getDimensionPixelSize(R.styleable.BatteryLoadingView_outBWidth, bWidth);
            bHeight = attrArray.getDimensionPixelSize(R.styleable.BatteryLoadingView_outBHeight, bHeight);
            bColor = attrArray.getColor(R.styleable.BatteryLoadingView_bColor, Color.GREEN);
            backgroundColor = attrArray.getColor(R.styleable.BatteryLoadingView_backgroundColor, Color.WHITE);
            bRadius = attrArray.getFloat(R.styleable.BatteryLoadingView_bRadius,0f);
            attrArray.recycle();
        }

        initPaint();


    }


    private void initPaint()
    {
        paintS = new Paint();
        paintF = new Paint();
        paintS.setColor(bColor);
        paintF.setColor(bColor);
        paintS.setStyle(Paint.Style.STROKE);
        paintS.setStrokeWidth(4);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        thread = new Thread(this);
        thread.start();
        isRunning = true;
        canDrawFlag = true;

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        dx = 0;
        centerX = getMeasuredWidth()/2;
        centerY = getMeasuredHeight()/2;
        SrectF.left = centerX-bWidth/2;
        leftLocation = centerX-bWidth/2;
        SrectF.top = centerY-bHeight/2;
        rightLocation = centerX+bWidth/2-distance*2;
        SrectF.right = rightLocation;
        SrectF.bottom = centerY+bHeight/2;

        FrectF.left = SrectF.left;
        FrectF.top = SrectF.top;
        FrectF.right = SrectF.left;
        FrectF.bottom = SrectF.bottom;


    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        if(modeWidth == MeasureSpec.AT_MOST || modeWidth == MeasureSpec.UNSPECIFIED)
        {
            sizeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,bWidth,getContext().getResources().getDisplayMetrics());
            sizeWidth += getPaddingLeft()+getPaddingRight();
        }


        if(modeHeight == MeasureSpec.AT_MOST || modeHeight == MeasureSpec.UNSPECIFIED)
        {
            sizeHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,bHeight,getContext().getResources().getDisplayMetrics());
            sizeHeight += getPaddingBottom()+getPaddingTop();
        }
        setMeasuredDimension(sizeWidth,sizeHeight);

    }


    private void drawView(Canvas canvas)
    {
        // 绘制背景图
        canvas.drawColor(backgroundColor);
        //绘制电池矩形框
        canvas.drawRoundRect(SrectF,bRadius,bRadius,paintS);
        //绘制电池小矩形框
        canvas.drawRect(centerX+bWidth/2-distance,centerY-bHeight/4,centerX+bWidth/2,centerY+bHeight/4,paintF);








        if(disProgress>0)
        {

            tempContetnValue+=2;

        }else{
            tempContetnValue-=2;
        }
        dx = (rightLocation-leftLocation)*tempContetnValue/100;

        if(disProgress>0)
        {
            if(tempContetnValue>=contentValue)
            {
                tempContetnValue = contentValue;
                canDrawFlag = false;
            }

        }else{
            if(tempContetnValue<=contentValue)
            {
                tempContetnValue = contentValue;
                canDrawFlag = false;
            }
        }


        //更新电池容量
        FrectF.right = FrectF.left+dx;
        canvas.drawRoundRect(FrectF,bRadius,bRadius,paintF);


    }




    public void setContentValue(int value)
    {
        canDrawFlag = true;
        contentValue = value;
        disProgress = value-tempContetnValue;
    }


    public void setThreadStop()
    {
        isRunning = false;
    }


    @Override
    public void run() {

        while(isRunning)
        {
            if(canDrawFlag)
            {
                canvas = surfaceHolder.lockCanvas();
                if(canvas!=null)
                {
                    drawView(canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
