package com.hs.uav.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DragScaleView extends View implements View.OnTouchListener {
    protected int screenWidth;
    protected int screenHeight;
    protected int lastX;
    protected int lastY;
    private int oriLeft;
    private int oriRight;
    private int oriTop;
    private int oriBottom;
    private int dragDirection;
    private static final int RIGHT_TOP = 0x12;
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

    public DragScaleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
        initScreenW_H();
    }

    public DragScaleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
        initScreenW_H();
    }

    public DragScaleView(Context context) {
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
            lastY = (int) event.getY();
            lastX = (int) event.getX();
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
                int dx = (int) event.getX() - lastX;
                int dy = (int) event.getY() - lastY;
                switch (dragDirection) {
                    case RIGHT_TOP: // 右上
                        right(v, dx);
                        top(v, dy);
                        break;
                }
                if (onRefreshViewChangeListener != null) {
                    onRefreshViewChangeListener.refreshSizeView(oriLeft, oriRight, oriTop, oriBottom);
                }
                v.layout(oriLeft, oriTop, oriRight, oriBottom);
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                dragDirection = 0;
                if (onRefreshViewChangeListener != null) {
                    onRefreshViewChangeListener.refreshStop(oriLeft, oriRight, oriTop, oriBottom);
                }
                break;
        }
    }

    /**
     * 触摸点为上边缘
     *
     * @param v
     * @param dy
     */
    private void top(View v, int dy) {
        oriTop += dy;
        if (oriTop < -offset) {
            oriTop = -offset;
        }
        if (oriBottom - oriTop - 2 * offset < 200) {
            oriTop = oriBottom - 2 * offset - 200;
        }
    }


    /**
     * 触摸点为右边缘
     *
     * @param v
     * @param dx
     */
    private void right(View v, int dx) {
        oriRight += dx;
        if (oriRight > screenWidth + offset) {
            oriRight = screenWidth + offset;
        }
        if (oriRight - oriLeft - 2 * offset < 200) {
            oriRight = oriLeft + 2 * offset + 200;
        }
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
        int left = v.getLeft();
        int right = v.getRight();
        if (y < 80 && right - left - x < 80) {
            return RIGHT_TOP;
        }
        return 0;
    }

    public interface OnRefreshViewChangeListener {
        /***
         * 刷新窗口大小
         * @param oriLeft
         * @param oriRight
         * @param oriTop
         * @param oriBottom
         */
        void refreshSizeView(int oriLeft, int oriRight, int oriTop, int oriBottom);

        /***
         * 拖动停止时刷新窗口大小
         * @param oriLeft
         * @param oriRight
         * @param oriTop
         * @param oriBottom
         */
        void refreshStop(int oriLeft, int oriRight, int oriTop, int oriBottom);
    }
}