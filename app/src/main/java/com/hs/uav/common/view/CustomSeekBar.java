package com.hs.uav.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import com.hs.uav.R;

@SuppressLint("AppCompatCustomView")
public class CustomSeekBar extends SeekBar {
    private Paint paint;
    private Drawable mThumb;
    private String temp_str = "0";
    public CustomSeekBar(Context context) {
        this(context, null);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(getResources().getColor(R.color.black));
        paint.setTextSize(24);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        this.mThumb = thumb;
    }

    public Drawable getSeekBarThumb() {
        return mThumb;
    }

    //设置thumb的偏移数值
    @Override
    public void setThumbOffset(int thumbOffset) {
        super.setThumbOffset(thumbOffset / 3);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        Rect rect = getSeekBarThumb().getBounds();
        canvas.drawText(temp_str, rect.left + (rect.width()), rect.top - paint.ascent() + (rect.height() - (paint.descent() - paint.ascent())) / 2.0F, paint);
        canvas.restore();
    }

    public void SetValue(String value) {
        StringBuffer sb = new StringBuffer();
        sb.append(value);
        temp_str = sb.toString();
        invalidate();
    }

    @SuppressLint("NewApi")
    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false ;
    }

    @Override
    public void setOnSeekBarChangeListener(final OnSeekBarChangeListener l) {
        super.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (l != null) {
                    l.onProgressChanged(seekBar, progress, fromUser);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (l != null) {
                    l.onStartTrackingTouch(seekBar);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (l != null) {
                    l.onStopTrackingTouch(seekBar);
                }
            }
        });
    }
}