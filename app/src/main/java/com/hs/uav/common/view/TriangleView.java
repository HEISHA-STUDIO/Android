package com.hs.uav.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class TriangleView extends ImageView {
    private Paint mPaint;
    private float angle;// 旋转角度
    private float mWidth;
    private float mHeight;
    public static final int LEFT = 270;
    public static final int RIGHT = 90;
    public static final int UP = 0;
    public static final int DOWN = 180;

    public TriangleView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public TriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public TriangleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3);
    }

    public TriangleView setWidth(float width) {
        this.mWidth = width;
        return this;
    }

    public TriangleView setHeight(float height) {
        this.mHeight = height;
        return this;
    }

    /**
     * @param @param angle
     * @return TriangleView this
     * @throws
     * @Title: setAngle
     * @Description: 按角度调整箭头方向 0-360
     */
    public TriangleView setAngle(float angle) {
        this.angle = angle;
        invalidate();
        return this;
    }

    /**
     * @param @param direction
     * @return TriangleView this
     * @throws
     * @Title: setDirection
     * @Description: 调整箭头方向  LEFT,RIGHT,UP,DOWN
     */
    public TriangleView setDirection(int direction) {
        this.angle = direction;
        invalidate();
        return this;
    }

    /**
     * @param @param color
     * @return TriangleView this
     * @throws
     * @Title: setColor
     * @Description: 给三角形填充颜色
     */
    public TriangleView setColor(int color) {
        mPaint.setColor(color);
        invalidate();
        return this;
    }

    private void drawTriangle(Canvas canvas) {
        mWidth = getWidth();
        mHeight = getHeight();
        float diameter = mWidth > mHeight ? mHeight : mWidth;// 直径
        RectF rf = new RectF(0, 0, diameter, diameter);
        float left = (mWidth - diameter) / 2;
        float top = (mHeight - diameter) / 2;
        rf.offsetTo(left, top);// 绘制区域移至控件中间，并且填充宽、高中较窄的边
        Path path = new Path();
        path.moveTo(rf.left + rf.width() / 2, 0);
        path.lineTo(rf.left + rf.width() / 2 + calTriangleSileLength(diameter)
                / 2, rf.top + 3 * diameter / 4);
        path.lineTo(rf.left + rf.width() / 2 - calTriangleSileLength(diameter)
                / 2, rf.top + 3 * diameter / 4);
        canvas.save();
        canvas.rotate(angle, rf.left + rf.width() / 2, rf.top + rf.height() / 2);
        canvas.drawPath(path, mPaint);
        canvas.restore();
    }

    /**
     * @param @param  diameter
     * @param @return
     * @return float
     * @throws
     * @Title: calTriangleSileLength
     * @Description: 计算三角形边长
     */
    private float calTriangleSileLength(float diameter) {
        return (float) (2 * Math.sqrt(Math.pow(diameter / 2, 2)
                - Math.pow(diameter / 4, 2)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawTriangle(canvas);
    }

}