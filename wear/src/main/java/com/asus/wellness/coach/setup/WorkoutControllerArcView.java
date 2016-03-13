package com.asus.wellness.coach.setup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.asus.wellness.R;

/**
 * Created by jz on 2015/5/28.
 */
public class WorkoutControllerArcView extends View {
    private Paint paint;
    private float mSweepAngle = 0.0f;

    public WorkoutControllerArcView(Context context) {
        this(context, null);
    }

    public WorkoutControllerArcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorkoutControllerArcView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.workout_start_counter_timer_color));
    }

    public void setProgress(float percent){
        mSweepAngle = 360f*percent;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        RectF rect = new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight());
        canvas.drawArc(rect, 270, mSweepAngle, true, paint);
    }

}
