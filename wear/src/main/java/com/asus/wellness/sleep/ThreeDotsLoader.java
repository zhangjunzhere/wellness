package com.asus.wellness.sleep;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class ThreeDotsLoader extends View {

	Paint mDot1;
	Paint mDot2;
	Paint mDot3;

	int mHighlightColor;
	int mTransColor;
	boolean mAnimationStarted = false;
    int mGrayColor;
	int mCount = 1;
	int mOneDotWidth = 6;
	int mYStart = 0;
	int mJiege = 3;

	final Handler mHandler = new Handler();
	Runnable mUpdateDots;

	public ThreeDotsLoader(Context context, AttributeSet attrs) {
		super(context, attrs);
		mHighlightColor = Color.WHITE;
		mTransColor = Color.TRANSPARENT;
		mGrayColor = Color.parseColor("#EAEAEA");
		initPaint();

		mUpdateDots = new Runnable() {
			public void run() {
				updateThreeDots();
			}
		};
	}

	private void updateThreeDots() {
		switch (mCount) {
            case 1:
				mDot1.setColor(mHighlightColor);
				mDot2.setColor(mTransColor);
				mDot3.setColor(mTransColor);
				mCount++;
                break;
			case 2:
				mDot1.setColor(mHighlightColor);
				mDot2.setColor(mHighlightColor);
				mDot3.setColor(mTransColor);
				mCount++;
				break;
			case 3:
				initColor();
				mCount++;
				break;
			case 4:
				initColor();
				mCount = 1;
				break;
        }
		postInvalidate();
		if(mAnimationStarted){
			mHandler.postDelayed(mUpdateDots, 500);
		}

	}

	//Three dots are all white.
	private void initColor(){
		mDot1.setColor(mHighlightColor);
		mDot2.setColor(mHighlightColor);
		mDot3.setColor(mHighlightColor);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRoundRect(new RectF(0, mYStart, 6, mOneDotWidth), 1, 1, mDot1);
		canvas.drawRoundRect(new RectF(9, mYStart, 15, mOneDotWidth), 1, 1, mDot2);
		canvas.drawRoundRect(new RectF(18, mYStart, 24, mOneDotWidth), 1, 1, mDot3);

	}

	private void initPaint() {
		mDot1 = new Paint();
		//Shader mShader = new LinearGradient(5,12,10,12,new int[] {Color.WHITE,Color.WHITE, mGrayColor},null,Shader.TileMode.REPEAT);
		mDot1.setColor(mTransColor);
		mDot1.setStyle(Paint.Style.FILL);
		mDot1.setAntiAlias(true);
		//mDot1.setShader(mShader);

		mDot2 = new Paint();
		mDot2.setColor(mTransColor);
		mDot2.setStyle(Paint.Style.FILL);
		mDot2.setAntiAlias(true);

		mDot3 = new Paint();
		mDot3.setColor(mTransColor);
		mDot3.setStyle(Paint.Style.FILL);
		mDot3.setAntiAlias(true);

	}

	public void startLoading() {
		mAnimationStarted = true;
		updateThreeDots();
	}



	public void stopLoading() {
		mAnimationStarted = false;
		mHandler.removeCallbacksAndMessages(null);
		initColor();
		postInvalidate();
	}
}
