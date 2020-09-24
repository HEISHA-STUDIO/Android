package com.hs.uav.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

public class DragImageView extends AppCompatImageView implements View.OnTouchListener {
    protected int screenWidth;
    protected int screenHeight;
    protected int lastX;
    protected int lastY;
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private int dragDirection;
    private static final int CENTER = 0x19;
    private int offset = 20;
    protected Paint paint = new Paint();
    private OnRefreshViewChangeListener onRefreshViewChangeListener;

    public void setChangeViewListener(OnRefreshViewChangeListener onRefreshViewChangeListener) {
        this.onRefreshViewChangeListener = onRefreshViewChangeListener;
    }

    /**
     * 初始化获取屏幕宽高
     */
    protected void initScreenW_H() {
        screenHeight = getResources().getDisplayMetrics().heightPixels - 40;
        screenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    public DragImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
        initScreenW_H();
    }

    public DragImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initScreenW_H();
    }

    public DragImageView(Context context) {
        super(context);
        setOnTouchListener(this);
        initScreenW_H();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(4.0f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(offset, offset, getWidth() - offset, getHeight()
                - offset, paint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            oriLeft = v.getLeft();
            oriRight = v.getRight();
            oriTop = v.getTop();
            oriBottom = v.getBottom();
            lastY = (int) event.getRawY();
            lastX = (int) event.getRawX();
            dragDirection = getDirection(v, (int) event.getX(),
                    (int) event.getY());
        }
        // 处理拖动事件
        delDrag(v, event, action);
        invalidate();
        return false;
    }

    /**
     * 处理拖动事件
     *
     * @param v
     * @param event
     * @param action
     */
    protected void delDrag(View v, MotionEvent event, int action) {
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;
                switch (dragDirection) {
                    case CENTER: // 点击中心-->>移动
                        center(v, dx, dy);
                        break;
                }
                if (onRefreshViewChangeListener != null) {
                    onRefreshViewChangeListener.refreshSizeView(oriLeft, oriRight, oriTop, oriBottom);
                }
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                dragDirection = 0;
                break;
        }
    }

    /**
     * 触摸点为中心->>移动
     *
     * @param v
     * @param dx
     * @param dy
     */
    private void center(View v, int dx, int dy) {
        int left = v.getLeft() + dx;
        int top = v.getTop() + dy;
        int right = v.getRight() + dx;
        int bottom = v.getBottom() + dy;
        if (left < -offset) {
            left = -offset;
            right = left + v.getWidth();
        }
        if (right > screenWidth + offset) {
            right = screenWidth + offset;
            left = right - v.getWidth();
        }
        if (top < -offset) {
            top = -offset;
            bottom = top + v.getHeight();
        }
        if (bottom > screenHeight + offset) {
            bottom = screenHeight + offset;
            top = bottom - v.getHeight();
        }
        v.layout(left, top, right, bottom);
    }

    /**
     * 获取触摸点flag
     *
     * @param v
     * @param x
     * @param y
     * @return
     */
    protected int getDirection(View v, int x, int y) {
        return CENTER;
    }


    public interface OnRefreshViewChangeListener {

        void refreshSizeView(int oriLeft, int oriRight, int oriTop, int oriBottom);
    }
}