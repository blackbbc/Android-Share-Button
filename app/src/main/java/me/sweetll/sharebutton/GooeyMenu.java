package me.sweetll.sharebutton;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anshul on 24/06/15.
 */
public class GooeyMenu extends View {

    private static final long ANIMATION_DURATION = 1000;
    private static final int DEFUALT_MENU_NO = 5;
    private int mNumberOfMenu;//Todo
    private final float BEZIER_CONSTANT = 0.551915024494f;// pre-calculated value

    private int mFabButtonRadius;
    private int mMenuButtonRadius;
    private int mGab;
    private int mCenterX;
    private int mCenterY;
    private Paint mCirclePaint;
    private ArrayList<CirclePoint> mMenuPoints = new ArrayList<>();
    private ArrayList<ObjectAnimator> mShowAnimation = new ArrayList<>();
    private ArrayList<ObjectAnimator> mHideAnimation = new ArrayList<>();
    private ValueAnimator mBezierAnimation, mBezierEndAnimation;
    private boolean isMenuVisible = false;
    private Float bezierConstant = BEZIER_CONSTANT;
    private ValueAnimator mRotationAnimation,mRotationReverseAnimation;
    private ValueAnimator mCircle1Animation, mCircle2Animation, mCircle3Animation;
    private ValueAnimator mCircle1ReverseAnimation, mCircle2ReverseAnimation, mCircle3ReverseAnimation;
    private GooeyMenuInterface mGooeyMenuInterface;
    private boolean gooeyMenuTouch;
    private Paint mCircleBorder;
    private List<Drawable> mDrawableArray;

    /* For Share Drawable Start */

    private final Paint sPaint = new Paint();

    private float sRadius;
    private float sCenterX;
    private float sCenterY;
    float circleRadius;

    private float sProgress = 0f;
//    private float mProgress = 0f;

    private MyLine line1;
    private MyLine line2;
    private MyCircle circle1;
    private MyCircle circle2;
    private MyCircle circle3;

    private void initShareDrawable() {
        circleRadius = 10f;
        sRadius = mFabButtonRadius / 2;

        sCenterX = 0f;
        sCenterY = 0f;

        line1 = new MyLine((float)Math.PI, (float)Math.PI * 7 / 4, (float)Math.PI * 5 / 3, (float)Math.PI * 11 / 4);
        line2 = new MyLine((float)Math.PI, (float)Math.PI * 5 / 4, (float)Math.PI * 1 / 3, (float)Math.PI * 1 / 4);

        circle1 = new MyCircle(Math.PI * 5 / 3);
        circle2 = new MyCircle(Math.PI);
        circle3 = new MyCircle(Math.PI * 1 / 3);

    }

    private void setupLinePaint() {
        sPaint.reset();
        sPaint.setColor(Color.WHITE);
        sPaint.setAntiAlias(true);
        sPaint.setStrokeWidth(5);
        sPaint.setStyle(Paint.Style.STROKE);
        sPaint.setStrokeJoin(Paint.Join.ROUND);
        sPaint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void setupCirclePaint() {
        sPaint.reset();
        sPaint.setColor(Color.WHITE);
        sPaint.setAntiAlias(true);
        sPaint.setStyle(Paint.Style.FILL);
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
            canvas.drawLine(point1.x, point1.y, point2.x, point2.y, sPaint);
        }

        private void update() {
            float point1CurrentAngle = lerp(point1StartAngle, point1EndAngle, sProgress);
            float point2CurrentAngle = lerp(point2StartAngle, point2EndAngle, sProgress);
            point1.set((int)(sCenterX + sRadius * Math.cos(point1CurrentAngle)), (int)(sCenterY - sRadius * Math.sin(point1CurrentAngle)));
            point2.set((int)(sCenterX + sRadius * Math.cos(point2CurrentAngle)), (int)(sCenterY - sRadius * Math.sin(point2CurrentAngle)));
        }
    }

    private class MyCircle {
        Point mCenterPoint = new Point();
        //0 is share, 1 is close
        float mProgress = 0;

        MyCircle(double angle) {
            mCenterPoint.set((int) (sCenterX + sRadius * Math.cos(angle)), (int) (sCenterY - sRadius * Math.sin(angle)));
        }

        public void setProgress(float progress) {
            this.mProgress = progress;
        }

        public void draw(Canvas canvas) {
            setupCirclePaint();

            float currentRadius = lerp(circleRadius, 0, mProgress);

            canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, currentRadius, sPaint);

        }

    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
    /*For Share Drawable End */

    public static final int[] STATE_ACTIVE =
            {android.R.attr.state_enabled, android.R.attr.state_active};
    public static final int[] STATE_PRESSED =
            {android.R.attr.state_enabled, -android.R.attr.state_active,
                    android.R.attr.state_pressed};

    public GooeyMenu(Context context) {
        super(context);
        init(null);
    }

    public GooeyMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }


    public GooeyMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }


    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.GooeyMenu,
                    0, 0);
            try {


                mNumberOfMenu = typedArray.getInt(R.styleable.GooeyMenu_no_of_menu, DEFUALT_MENU_NO);
                mFabButtonRadius = (int) typedArray.getDimension(R.styleable.GooeyMenu_fab_radius, getResources().getDimension(R.dimen.big_circle_radius));
                mMenuButtonRadius = (int) typedArray.getDimension(R.styleable.GooeyMenu_menu_radius, getResources().getDimension(R.dimen.small_circle_radius));
                mGab = (int) typedArray.getDimension(R.styleable.GooeyMenu_gap_between_menu_fab, getResources().getDimensionPixelSize(R.dimen.min_gap));

                TypedValue outValue = new TypedValue();
                // Read array of target drawables
                if (typedArray.getValue(R.styleable.GooeyMenu_menu_drawable, outValue)) {
                    Resources res = getContext().getResources();
                    TypedArray array = res.obtainTypedArray(outValue.resourceId);
                    mDrawableArray = new ArrayList<>(array.length());
                    for (int i = 0; i < array.length(); i++) {
                        TypedValue value = array.peekValue(i);
                        mDrawableArray.add(getResources().getDrawable(value != null ? value.resourceId : 0));
                    }
                    array.recycle();
                }

            } finally {
                typedArray.recycle();
                typedArray = null;
            }

        }

        mCirclePaint = new Paint();
        mCirclePaint.setColor(getResources().getColor(R.color.default_color));
        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mCircleBorder = new Paint(mCirclePaint);
        mCircleBorder.setStyle(Paint.Style.STROKE);
        mCircleBorder.setStrokeWidth(1f);
        mCircleBorder.setColor(getResources().getColor(R.color.default_color_dark));

        mBezierEndAnimation = ValueAnimator.ofFloat(BEZIER_CONSTANT + .2f, BEZIER_CONSTANT);
        mBezierEndAnimation.setInterpolator(new LinearInterpolator());
        mBezierEndAnimation.setDuration(300);
        mBezierEndAnimation.addUpdateListener(mBezierUpdateListener);

        mBezierAnimation = ValueAnimator.ofFloat(BEZIER_CONSTANT - .02f, BEZIER_CONSTANT + .2f);
        mBezierAnimation.setDuration(ANIMATION_DURATION / 4);
        mBezierAnimation.setRepeatCount(4);
        mBezierAnimation.setInterpolator(new LinearInterpolator());
        mBezierAnimation.addUpdateListener(mBezierUpdateListener);
        mBezierAnimation.addListener(mBezierAnimationListener);

        mRotationAnimation = ValueAnimator.ofFloat(0, 1);
        mRotationAnimation.setDuration(ANIMATION_DURATION / 4);
        mRotationAnimation.setInterpolator(new AccelerateInterpolator());
        mRotationAnimation.addUpdateListener(mRotationUpdateListener);

        mRotationReverseAnimation = ValueAnimator.ofFloat(1, 0);
        mRotationReverseAnimation.setDuration(ANIMATION_DURATION / 4);
        mRotationReverseAnimation.setInterpolator(new AccelerateInterpolator());
        mRotationReverseAnimation.addUpdateListener(mRotationUpdateListener);

        //消失动画
        mCircle1Animation = ValueAnimator.ofFloat(0, 1);
        mCircle1Animation.setDuration(150);
        mCircle1Animation.setStartDelay(75);
        mCircle1Animation.setInterpolator(new AccelerateInterpolator());
        mCircle1Animation.addUpdateListener(mCircle1UpdateListener);

        mCircle2Animation = ValueAnimator.ofFloat(0, 1);
        mCircle2Animation.setDuration(200);
        mCircle2Animation.setStartDelay(150);
        mCircle2Animation.setInterpolator(new AccelerateInterpolator());
        mCircle2Animation.addUpdateListener(mCircle2UpdateListener);

        mCircle3Animation = ValueAnimator.ofFloat(0, 1);
        mCircle3Animation.setDuration(150);
        mCircle3Animation.setStartDelay(0);
        mCircle3Animation.setInterpolator(new AccelerateInterpolator());
        mCircle3Animation.addUpdateListener(mCircle3UpdateListener);

        //出现动画
        mCircle1ReverseAnimation = ValueAnimator.ofFloat(1, 0);
        mCircle1ReverseAnimation.setDuration(200);
        mCircle1ReverseAnimation.setStartDelay(0);
        mCircle1ReverseAnimation.setInterpolator(new DecelerateInterpolator());
        mCircle1ReverseAnimation.addUpdateListener(mCircle1UpdateListener);

        mCircle2ReverseAnimation = ValueAnimator.ofFloat(1, 0);
        mCircle2ReverseAnimation.setDuration(200);
        mCircle2ReverseAnimation.setStartDelay(100);
        mCircle2ReverseAnimation.setInterpolator(new DecelerateInterpolator());
        mCircle2ReverseAnimation.addUpdateListener(mCircle2UpdateListener);

        mCircle3ReverseAnimation = ValueAnimator.ofFloat(1, 0);
        mCircle3ReverseAnimation.setDuration(200);
        mCircle3ReverseAnimation.setStartDelay(200);
        mCircle3ReverseAnimation.setInterpolator(new DecelerateInterpolator());
        mCircle3ReverseAnimation.addUpdateListener(mCircle3UpdateListener);

        initShareDrawable();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int desiredWidth;
        int desiredHeight;
        desiredWidth = getMeasuredWidth();
        desiredHeight = getContext().getResources().getDimensionPixelSize(R.dimen.min_height);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        mCenterX = w / 2;
        mCenterX = w - mFabButtonRadius;
        mCenterY = h - mFabButtonRadius;
        for (int i = 0; i < mNumberOfMenu; i++) {
            CirclePoint circlePoint = new CirclePoint();
//            circlePoint.setRadius(mGab);
            circlePoint.setRadius(0);
            circlePoint.setAngle(Math.PI / 2 + (Math.PI / 2 / (mNumberOfMenu - 1)) * i);
            mMenuPoints.add(circlePoint);
            ObjectAnimator animShow = ObjectAnimator.ofFloat(mMenuPoints.get(i), "Radius", 0f, mGab);
            animShow.setDuration(ANIMATION_DURATION);
            animShow.setInterpolator(new AnticipateOvershootInterpolator());
            animShow.setStartDelay((ANIMATION_DURATION * (mNumberOfMenu - i)) / 10);
            animShow.addUpdateListener(mUpdateListener);
            mShowAnimation.add(animShow);
            ObjectAnimator animHide = animShow.clone();
            animHide.setFloatValues(mGab, 0f);
            animHide.setStartDelay((ANIMATION_DURATION * i) / 10);
            mHideAnimation.add(animHide);

            if (mDrawableArray != null) {
                for (Drawable drawable : mDrawableArray)
                    drawable.setBounds(0, 0, /*2 * */mMenuButtonRadius,/* 2 * */mMenuButtonRadius);
            }
        }

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBezierAnimation = null;
        mHideAnimation.clear();
        mHideAnimation = null;
        mShowAnimation.clear();
        mHideAnimation = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mNumberOfMenu; i++) {
            CirclePoint circlePoint = mMenuPoints.get(i);
            float x = (float) (circlePoint.radius * Math.cos(circlePoint.angle));
            float y = (float) (circlePoint.radius * Math.sin(circlePoint.angle));
            canvas.drawCircle(x + mCenterX, mCenterY - y, mMenuButtonRadius, mCirclePaint);
            if (i < mDrawableArray.size()) {
                canvas.save();
                canvas.translate(x + mCenterX - mMenuButtonRadius / 2, mCenterY - y - mMenuButtonRadius / 2);
                mDrawableArray.get(i).draw(canvas);
                canvas.restore();
            }
        }

        canvas.save();
        canvas.translate(mCenterX, mCenterY);
        Path path = createPath();
        canvas.drawPath(path, mCirclePaint);
        canvas.drawPath(path, mCircleBorder);

        line1.draw(canvas);
        line2.draw(canvas);

        circle1.draw(canvas);
        circle2.draw(canvas);
        circle3.draw(canvas);

        canvas.restore();
    }

    // Use Bezier path to create circle,
    /*    P_0 = (0,1), P_1 = (c,1), P_2 = (1,c), P_3 = (1,0)
        P_0 = (1,0), P_1 = (1,-c), P_2 = (c,-1), P_3 = (0,-1)
        P_0 = (0,-1), P_1 = (-c,-1), P_3 = (-1,-c), P_4 = (-1,0)
        P_0 = (-1,0), P_1 = (-1,c), P_2 = (-c,1), P_3 = (0,1)
        with c = 0.551915024494*/

    private Path createPath() {
        Path path = new Path();
        float c = bezierConstant * mFabButtonRadius;

        path.moveTo(0, mFabButtonRadius);
        path.cubicTo(bezierConstant * mFabButtonRadius, mFabButtonRadius, mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius, mFabButtonRadius, 0);
        path.cubicTo(mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius * (-1), c, (-1) * mFabButtonRadius, 0, (-1) * mFabButtonRadius);
        path.cubicTo((-1) * c, (-1) * mFabButtonRadius, (-1) * mFabButtonRadius, (-1) * BEZIER_CONSTANT * mFabButtonRadius, (-1) * mFabButtonRadius, 0);
        path.cubicTo((-1) * mFabButtonRadius, BEZIER_CONSTANT * mFabButtonRadius, (-1) * bezierConstant * mFabButtonRadius, mFabButtonRadius, 0, mFabButtonRadius);

        return path;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isGooeyMenuTouch(event)) {
                    return true;
                }
                int menuItem = isMenuItemTouched(event);
                if (isMenuVisible && menuItem > 0) {
                    if (menuItem <= mDrawableArray.size()) {
                        mDrawableArray.get(mMenuPoints.size() - menuItem).setState(STATE_PRESSED);
                        invalidate();
                    }

                    return true;
                }
                return false;
            case MotionEvent.ACTION_UP:
                if (isGooeyMenuTouch(event)) {
                    mBezierAnimation.start();
                    cancelAllAnimation();
                    if (isMenuVisible) {
                        startHideAnimate();
                        if (mGooeyMenuInterface != null) {
                            mGooeyMenuInterface.menuClose();
                        }
                    } else {
                        startShowAnimate();
                        if (mGooeyMenuInterface != null) {
                            mGooeyMenuInterface.menuOpen();
                        }
                    }
                    isMenuVisible = !isMenuVisible;
                    return true;
                }

                if (isMenuVisible) {
                    menuItem = isMenuItemTouched(event);
                    invalidate();
                    if (menuItem > 0) {
                        if (menuItem <= mDrawableArray.size()) {
                            mDrawableArray.get(mMenuPoints.size() - menuItem).setState(STATE_ACTIVE);
                            postInvalidateDelayed(1000);
                        }
                        if (mGooeyMenuInterface != null) {
                            mGooeyMenuInterface.menuItemClicked(menuItem);
                        }
                        return true;
                    }
                }
                return false;

        }
        return true;
    }

    private int isMenuItemTouched(MotionEvent event) {

        if (!isMenuVisible) {
            return -1;
        }

        for (int i = 0; i < mMenuPoints.size(); i++) {
            CirclePoint circlePoint = mMenuPoints.get(i);
            float x = (float) (mGab * Math.cos(circlePoint.angle)) + mCenterX;
            float y = mCenterY - (float) (mGab * Math.sin(circlePoint.angle));
            if (event.getX() >= x - mMenuButtonRadius && event.getX() <= x + mMenuButtonRadius) {
                if (event.getY() >= y - mMenuButtonRadius && event.getY() <= y + mMenuButtonRadius) {
                    return mMenuPoints.size() - i;
                }
            }
        }

        return -1;
    }

    public void setOnMenuListener(GooeyMenuInterface onMenuListener) {
        mGooeyMenuInterface = onMenuListener;
    }

    public boolean isGooeyMenuTouch(MotionEvent event) {
        if (event.getX() >= mCenterX - mFabButtonRadius && event.getX() <= mCenterX + mFabButtonRadius) {
            if (event.getY() >= mCenterY - mFabButtonRadius && event.getY() <= mCenterY + mFabButtonRadius) {
                return true;
            }
        }
        return false;
    }

    // Helper class for animation and Menu Item cicle center Points
    public class CirclePoint {
        private float x;
        private float y;
        private float radius = 0.0f;
        private double angle = 0.0f;

        public void setX(float x1) {
            x = x1;
        }

        public float getX() {
            return x;
        }

        public void setY(float y1) {
            y = y1;
        }

        public float getY() {
            return y;
        }

        public void setRadius(float r) {
            radius = r;
        }

        public float getRadius() {
            return radius;
        }

        public void setAngle(double angle) {
            this.angle = angle;
        }

        public double getAngle() {
            return angle;
        }
    }

    private void startShowAnimate() {
        mRotationAnimation.start();
        mCircle1Animation.start();
        mCircle2Animation.start();
        mCircle3Animation.start();
        for (ObjectAnimator objectAnimator : mShowAnimation) {
            objectAnimator.start();
        }
    }

    private void startHideAnimate() {
        mRotationReverseAnimation.start();
        mCircle1ReverseAnimation.start();
        mCircle2ReverseAnimation.start();
        mCircle3ReverseAnimation.start();
        for (ObjectAnimator objectAnimator : mHideAnimation) {
            objectAnimator.start();
        }
    }

    private void cancelAllAnimation() {
        for (ObjectAnimator objectAnimator : mHideAnimation) {
            objectAnimator.cancel();
        }
        for (ObjectAnimator objectAnimator : mShowAnimation) {
            objectAnimator.cancel();
        }
    }

    ValueAnimator.AnimatorUpdateListener mUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            invalidate();
        }
    };

    ValueAnimator.AnimatorUpdateListener mBezierUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            bezierConstant = (float) valueAnimator.getAnimatedValue();
            invalidate();
        }
    };

    ValueAnimator.AnimatorUpdateListener mCircle1UpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float mProgress = (float)valueAnimator.getAnimatedValue();
            circle1.setProgress(mProgress);

            invalidate();
        }
    };

    ValueAnimator.AnimatorUpdateListener mCircle2UpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float mProgress = (float)valueAnimator.getAnimatedValue();
            circle2.setProgress(mProgress);
            invalidate();
        }
    };

    ValueAnimator.AnimatorUpdateListener mCircle3UpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float mProgress = (float)valueAnimator.getAnimatedValue();
            circle3.setProgress(mProgress);
            invalidate();
        }
    };

    ValueAnimator.AnimatorUpdateListener mRotationUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            sProgress = (float)valueAnimator.getAnimatedValue();
            invalidate();
        }
    };

    ValueAnimator.AnimatorListener mBezierAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {
            mBezierEndAnimation.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {

        }
    };


    public interface GooeyMenuInterface {
        /**
         * Called when menu opened
         */
        void menuOpen();

        /**
         * Called when menu Closed
         */
        void menuClose();

        /**
         * Called when Menu item Clicked
         *
         * @param menuNumber give menu number which clicked.
         */
        void menuItemClicked(int menuNumber);
    }
}
