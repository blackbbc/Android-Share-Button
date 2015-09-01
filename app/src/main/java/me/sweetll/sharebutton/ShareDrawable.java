package me.sweetll.sharebutton;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by sweet on 15-9-1.
 */
public class ShareDrawable extends Drawable {
    private final Paint mPaint = new Paint();

    private float mWidth;
    private float mHeight;
    private float mRadius;
    private float mCenterX;
    private float mCenterY;
    float circleRadius = 16f;

    private float mProgress = 0f;

    private MyLine line1;
    private MyLine line2;
    private MyCircle threeCircle;

    ShareDrawable(Resources resources) {
        mWidth = resources.getDimension(R.dimen.share_drawable_width);
        mHeight = resources.getDimension(R.dimen.share_drawable_height);
        mRadius = resources.getDimension(R.dimen.share_drawable_radius);

        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;

        line1 = new MyLine((float)Math.PI, (float)Math.PI * 7 / 4, (float)Math.PI * 5 / 3, (float)Math.PI * 11 / 4);
        line2 = new MyLine((float)Math.PI, (float)Math.PI * 5 / 4, (float)Math.PI * 1 / 3, (float)Math.PI * 1 / 4);
        threeCircle = new MyCircle();
    }

    private void setupLinePaint() {
        mPaint.reset();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void setupCirclePaint() {
        mPaint.reset();
        mPaint.setColor(Color.BLACK);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private class MyLine {
        float point1StartAngle;
        float point1EndAngle;
        float point2StartAngle;
        float point2EndAngle;

        Point point1 = new Point();
        Point point2 = new Point();

        MyLine(float point1StartAngle, float point1EndAngle, float point2StartAngle, float point2EndAngle) {
            this.point1StartAngle = point1StartAngle;
            this.point1EndAngle = point1EndAngle;
            this.point2StartAngle = point2StartAngle;
            this.point2EndAngle = point2EndAngle;
        }

        public void draw(Canvas canvas) {
            update();
            setupLinePaint();
            canvas.drawLine(point1.x, point1.y, point2.x, point2.y, mPaint);
        }

        private void update() {
            float point1CurrentAngle = lerp(point1StartAngle, point1EndAngle, mProgress);
            float point2CurrentAngle = lerp(point2StartAngle, point2EndAngle, mProgress);
            point1.set((int)(mCenterX + mRadius * Math.cos(point1CurrentAngle)), (int)(mCenterY - mRadius * Math.sin(point1CurrentAngle)));
            point2.set((int)(mCenterX + mRadius * Math.cos(point2CurrentAngle)), (int)(mCenterY - mRadius * Math.sin(point2CurrentAngle)));
        }
    }

    private class MyCircle {
        Point point1 = new Point();
        Point point2 = new Point();
        Point point3 = new Point();

        MyCircle() {
            point1.set((int)(mCenterX + mRadius * Math.cos(Math.PI)), (int)(mCenterY - mRadius * Math.sin(Math.PI)));
            point2.set((int)(mCenterX + mRadius * Math.cos(Math.PI * 1 / 3)), (int)(mCenterY - mRadius * Math.sin(Math.PI * 1 / 3)));
            point3.set((int)(mCenterX + mRadius * Math.cos(Math.PI * 5 / 3)), (int)(mCenterY - mRadius * Math.sin(Math.PI * 5 / 3)));
        }

        public void draw(Canvas canvas) {
            setupCirclePaint();

            float currentRadius = lerp(circleRadius, 0, mProgress);

            canvas.drawCircle(point1.x, point1.y, currentRadius, mPaint);
            canvas.drawCircle(point2.x, point2.y, currentRadius, mPaint);
            canvas.drawCircle(point3.x, point3.y, currentRadius, mPaint);
        }
    }

    @Override
    public void draw(Canvas canvas) {

        canvas.save();

        line1.draw(canvas);
        line2.draw(canvas);
        threeCircle.draw(canvas);

        canvas.restore();
    }

    @Override
    public void setAlpha(int i) {
        mPaint.setAlpha(i);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        mProgress = progress;
        invalidateSelf();
    }

    /**
     * Linear interpolate between a and b with parameter t.
     */
    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
