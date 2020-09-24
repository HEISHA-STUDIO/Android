package com.hs.uav.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/***
 * 自定义圆弧进度条
 *
 * @author liujing
 */
public class ProgressView extends View {
    //分段颜色
    private static final int[] SECTION_COLORS = {Color.YELLOW, Color.GREEN};
    private float maxCount;
    private float currentCount;
    private Paint mPaint;
    private int mWidth, mHeight;

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public ProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgressView(Context context) {
        this(context, null);
    }

    private void init(Context context) {
        mPaint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        initPaint();
        RectF rectBlackBg = new RectF(20, 20, mWidth - 20, mHeight - 20);
        canvas.drawArc(rectBlackBg, 0, 360, false, mPaint);
        mPaint.setColor(Color.BLACK);
        float section = currentCount / maxCount;
        if (section <= 1.0f / 3.0f) {
            if (section != 0.0f) {
                mPaint.setColor(SECTION_COLORS[0]);
            } else {
                mPaint.setColor(Color.TRANSPARENT);
            }
        } else {
            int[] colors = new int[2];
            System.arraycopy(SECTION_COLORS, 0, colors, 0, 2);
            float[] positions = new float[2];
            positions[0] = 0.0f;
            positions[1] = 1.0f - positions[0];
            positions[positions.length - 1] = 1.0f;
            LinearGradient shader = new LinearGradient(3, 3, (mWidth - 3)
                    * section, mHeight - 3, colors, null,
                    Shader.TileMode.MIRROR);
            mPaint.setShader(shader);
        }
        try {
            canvas.drawArc(rectBlackBg, 270, section * 360, false, mPaint);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initPaint() {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth((float) 8.0);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.TRANSPARENT);

    }

    private int dipToPx(int dip) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f * (dip >= 0 ? 1 : -1));
    }


    public float getMaxCount() {
        return maxCount;
    }

    public float getCurrentCount() {
        return currentCount;
    }

    /***
     * 设置最大的进度值
     *
     * @param maxCount
     */
    public void setMaxCount(float maxCount) {
        this.maxCount = maxCount;
    }

    /***
     * 设置当前的进度值
     *
     * @param currentCount
     */
    public void setCurrentCount(float currentCount) {
        this.currentCount = currentCount > maxCount ? maxCount : currentCount;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.EXACTLY
                || widthSpecMode == MeasureSpec.AT_MOST) {
            mWidth = widthSpecSize;
        } else {
            mWidth = 0;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST
                || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            mHeight = dipToPx(15);
        } else {
            mHeight = heightSpecSize;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

}