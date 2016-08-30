package com.ppyy.weathertest.test;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.nineoldandroids.animation.ValueAnimator;
import com.ppyy.weathertest.R;

/**
 * Created by Administrator on 2016/8/23.
 */

public class TestView extends View {
    public static final String DEFAULT_TEXT = "--";
    public static final String DEFAULT_TIME = "00/00";
    public static final int MAX_TEMP = 45;
    private int mDayPointColor = Color.parseColor("#FFCC00");
    private int mNightPointColor = Color.parseColor("#FE9C34");
    private int mDateTextColor = Color.parseColor("#8193A4");
    private int mTextColor = Color.parseColor("#CDD2D8");
    private int mTempTextColor = Color.parseColor("#FFFFFF");

    /**
     * 周几文本
     * sample：周三
     */
    private String mWeekText = "周三";
    /**
     * 日期文本
     * sample：08/23
     */
    private String mDateText = "08/23";
    /**
     * 白天天气描述文本
     * sample：多云
     */
    private String mDayWeatherText = "晴天";
    /**
     * 晚上天气描述文本
     * sample：多云
     */
    private String mNightWeatherText = "晴朗";

    /**
     * 白天天气图标
     */
    private int mDayWeatherIcon;
    /**
     * 晚上天气图标
     */
    private int mNightWeatherIcon;
    /**
     * 白天最高温度
     * sample：37°
     */
    private int mDayTemp;
    /**
     * 晚上最高温度
     * sample：22°
     */
    private int mNightTemp;
    /**
     * 后一天温度(白天)
     */
    private int mNextDayTemp = -1;
    /**
     * 后一天温度(晚上)
     */
    private int mNextNightTemp = -1;

    public float mPreDayTempY = -1;
    public float mPreNightTempY = -1;
    public float mNextDayTempY = -1;
    public float mNextNightTempY = -1;

    /**
     * 行间距
     */
    private int mLineSpace = 20;
    /**
     * 折线图的高度
     */
    private float mChartHeight;
    /**
     * 中心点的温度
     */
    private int mCenterTemp;
    /**
     * 圆点的半径
     */
    private int mPointRadio;
    /**
     * 折现宽度
     */
    private int mStrokeWidth;

    private int mTextSize, mTempTextSize, mDateTextSize;

    private Paint mTextPaint, mLinePaint;

    private Rect mTextRect, mTempTextRect, mDateTextRect;
    private Bitmap mDayIcon, mNightIcon;

    public void setDayTemp(int dayTemp) {
        mDayTemp = dayTemp;
    }

    public void setNightTemp(int nightTemp) {
        mNightTemp = nightTemp;
    }

    public void setCenterTemp(int centerTemp) {
        mCenterTemp = centerTemp;
    }

    public void setNextDayTemp(int nextDayTemp) {
        mNextDayTemp = nextDayTemp;
    }

    public void setNextNightTemp(int nextNightTemp) {
        mNextNightTemp = nextNightTemp;
    }

    public TestView(Context context) {
        this(context, null);
    }

    public TestView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mTextPaint = new Paint();
        mTextPaint.setDither(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mLinePaint = new Paint();
        mLinePaint.setDither(true);
        mLinePaint.setAntiAlias(true);

        mTextSize = (int) getRawSize(TypedValue.COMPLEX_UNIT_SP, 15);
        mTempTextSize = (int) getRawSize(TypedValue.COMPLEX_UNIT_SP, 13);
        mDateTextSize = (int) getRawSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mPointRadio = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 6);
        mStrokeWidth = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 2);

        mTextRect = new Rect();
        mDateTextRect = new Rect();
        mTempTextRect = new Rect();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        mDayIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.weather_sunny, options);
        mNightIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.weather_sunny, options);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 70);
        int height = (int) getRawSize(TypedValue.COMPLEX_UNIT_DIP, 300);
        // 如果宽高都是warp_content时，设置控件的宽高的大小
        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, height);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, heightSpecSize == 0 ? height : heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize == 0 ? width : widthSpecSize, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float x = getMeasuredWidth() * 0.5f;
        float y = 0;
        // 画“周三”
        setSizeAndColor(mTextPaint, mTextSize, mTextColor);
        getTextBounds(mTextPaint, mWeekText, mTextRect);
        y = drawText(canvas, mWeekText, x, mTextRect.height() + getPaddingTop(), mTextPaint);
        // 画“08/23”
        setSizeAndColor(mTextPaint, mDateTextSize, mDateTextColor);
        getTextBounds(mTextPaint, mDateText, mDateTextRect);
        y = drawText(canvas, mDateText, x, y + mDateTextRect.height() + mLineSpace, mTextPaint);

        // 画天气图标
        y = drawIcon(canvas, mDayIcon, x - mDayIcon.getWidth() * 0.5f, y + mLineSpace);

        // 画白天天气描述
        setSizeAndColor(mTextPaint, mTextSize, mTextColor);
        float partOneY = drawText(canvas, mDayWeatherText, x, y + mLineSpace + mDayIcon.getHeight() + mTextRect.height(), mTextPaint);

        // 从下开始画晚上天气描述
        y = drawText(canvas, mNightWeatherText, x, getMeasuredHeight() - getPaddingBottom(), mTextPaint);
        y = drawIcon(canvas, mNightIcon, x - mNightIcon.getWidth() * 0.5f, y - mTextRect.height() - mLineSpace - mNightIcon.getHeight());

        // 画折线图
        drawChart(canvas, y, partOneY);
    }

    /**
     * 画文本
     */
    private float drawText(Canvas canvas, String text, float x, float y, Paint paint) {
        canvas.drawText(text, x, y, paint);
        return y;
    }

    private float drawIcon(Canvas canvas, Bitmap icon, float left, float top) {
        canvas.drawBitmap(icon, left, top, null);
        return top;
    }

    private void drawChart(Canvas canvas, float y, float partOneY) {
        mLinePaint.setColor(mDayPointColor);
        mLinePaint.setStrokeWidth(mStrokeWidth);
        // 折线图中心点y坐标
        float center = (y - partOneY) * 0.5f + partOneY;
        mChartHeight = (center - partOneY) * 2;
        // 一摄氏度对应的高度
        float degreeHeight = mChartHeight / 2 / (MAX_TEMP - mCenterTemp);

        // 画出白天气温文本
        setSizeAndColor(mTextPaint, mTempTextSize, mTempTextColor);
        mTextPaint.getTextBounds(getTemp(mDayTemp), 0, getTemp(mDayTemp).length(), mTempTextRect);
        drawText(canvas, getTemp(mDayTemp), getMeasuredWidth() * 0.5f, center - (mDayTemp - mCenterTemp) * degreeHeight - mPointRadio - mLineSpace, mTextPaint);
        // 画白天温度圆点
        canvas.drawCircle(getMeasuredWidth() * 0.5f, center - (mDayTemp - mCenterTemp) * degreeHeight, mPointRadio, mLinePaint);

        // 画出晚上气温文本
        drawText(canvas, getTemp(mNightTemp), getMeasuredWidth() * 0.5f, center + (mCenterTemp - mNightTemp) * degreeHeight + mPointRadio + mTempTextRect.height() + mLineSpace, mTextPaint);
        // 画晚上温度圆点
        mLinePaint.setColor(mNightPointColor);
        canvas.drawCircle(getMeasuredWidth() * 0.5f, center + (mCenterTemp - mNightTemp) * degreeHeight, mPointRadio, mLinePaint);

        // 画折线
        mNextDayTempY = calculateLeftPoint(mNextDayTemp, mDayTemp, center, degreeHeight);
        mNextNightTempY = calculateLeftPoint(mNextNightTemp, mNightTemp, center, degreeHeight);
        if (mNextDayTempY != -1 && mNextNightTempY != -1) {
            if (mOnNextTempFinishedListener != null) {
                mOnNextTempFinishedListener.onNextTempFinished(mNextDayTempY, mNextNightTempY);
            }
            mLinePaint.setColor(mDayPointColor);
            canvas.drawLine(getMeasuredWidth() / 2, center - (mDayTemp - mCenterTemp) * degreeHeight, getMeasuredWidth(), mNextDayTempY, mLinePaint);
            mLinePaint.setColor(mNightPointColor);
            canvas.drawLine(getMeasuredWidth() / 2, center + (mCenterTemp - mNightTemp) * degreeHeight, getMeasuredWidth(), mNextNightTempY, mLinePaint);
        }
        if (mPreDayTempY != -1) {
            mLinePaint.setColor(mDayPointColor);
            canvas.drawLine(0, mPreDayTempY, getMeasuredWidth() / 2, center - (mDayTemp - mCenterTemp) * degreeHeight, mLinePaint);
        }
        if (mPreNightTempY != -1) {
            mLinePaint.setColor(mNightPointColor);
            ValueAnimator anim = ValueAnimator.ofFloat(0f, 1.0f);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    setAlpha((float) animation.getAnimatedValue());
                }
            });
            anim.setDuration(400);
            anim.start();
            canvas.drawLine(0, mPreNightTempY, getMeasuredWidth() / 2, center + (mCenterTemp - mNightTemp) * degreeHeight, mLinePaint);
        }
    }

    private void getTextBounds(Paint paint, String text, Rect rect) {
        paint.getTextBounds(text, 0, text.length(), rect);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(mDateText);
        }
        return super.onTouchEvent(event);
    }

    /**
     * 计算左边要连接的点
     *
     * @return
     */
    private float calculateLeftPoint(int temp, int refeTemp, float center, float degreeHeight) {
        if (temp == -1)
            return -1;
        float centerDayTemp = 0;
        if (temp > refeTemp) {
            centerDayTemp = Math.abs(temp - refeTemp) * 0.5f + refeTemp;
        } else {
            centerDayTemp = refeTemp - Math.abs(temp - refeTemp) * 0.5f;
        }
        return center - (centerDayTemp - mCenterTemp) * degreeHeight;
    }

    private void setSizeAndColor(Paint paint, float size, int color) {
        paint.setTextSize(size);
        paint.setColor(color);
    }

    private String getTemp(int temp) {
        return temp + "°";
    }

    private float getRawSize(int unit, float size) {
        Context context = getContext();
        Resources resources;
        if (context == null) {
            resources = Resources.getSystem();
        } else {
            resources = context.getResources();
        }
        return TypedValue.applyDimension(unit, size, resources.getDisplayMetrics());
    }

    /**
     * 刷新View
     */
    private void invalidateView() {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            // 当前线程是主UI线程，直接刷新。
            invalidate();
        } else {
            // 当前线程是非UI线程，post刷新。
            postInvalidate();
        }
    }

    private OnNextTempFinishedListener mOnNextTempFinishedListener;
    private OnClickListener mOnClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    public void setOnNextTempFinishedListener(OnNextTempFinishedListener onNextTempFinishedListener) {
        mOnNextTempFinishedListener = onNextTempFinishedListener;
    }

    public interface OnNextTempFinishedListener {
        void onNextTempFinished(float nextDayTempY, float nextNightTempY);
    }

    public interface OnClickListener {
        void onClick(String msg);
    }
}
