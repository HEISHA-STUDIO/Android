package com.hs.uav.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.hs.uav.R;

/**
 * 带刻度尺的 SeekBar
 */
@SuppressLint("AppCompatCustomView")
public class TickMarkSeekBar extends VerticalSeekBar {
    private Paint paint;
    private int mTickMarkCount;
    private int mColor;
    public TickMarkSeekBar(Context context) {
        super(context);
    }

    public TickMarkSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TickMarkSeekBar);
        try {
            mTickMarkCount = typedArray.getInt(R.styleable.TickMarkSeekBar_tick_mark_count, 0);
            mColor = typedArray.getInt(R.styleable.TickMarkSeekBar_tick_mark_color, Color.WHITE);

            paint = new Paint();
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(getResources().getColor(R.color.white));
            paint.setTextSize(44);
        } finally {
            typedArray.recycle();
        }
    }

    public TickMarkSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
    }


    public void SetValue(int value) {
        invalidate();
    }

    @Override
    public void setThumbOffset(int thumbOffset) {
        super.setThumbOffset(thumbOffset / 3);
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        float startX;
        float stopX;
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(mColor);
        float mTickDeliver = getMeasuredHeight() / mTickMarkCount;
        // 画刻度线
        for (int i = 0; i < mTickMarkCount; i++) {
            stopX = getPaddingLeft() + (i * mTickDeliver);
            startX = stopX;
            c.drawLine(startX, getMeasuredWidth() - ((mTickMarkCount - i) * 5),
                    stopX, (mTickMarkCount - i - 1) * 7, linePaint);
        }

        //画进度
//        Rect rect = getSeekBarThumb().getBounds();
//        c.drawText(camera_level, rect.left + (rect.width()/2.0F), rect.top - paint.ascent() + (rect.height() - (paint.descent() - paint.ascent())) / 2.0F, paint);
    }
}
