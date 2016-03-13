package com.asus.wellness.ui.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.asus.wellness.R;

public class UserHeadImageView extends ImageView {
	
	private Paint mPaint;
	private Bitmap mMask;

	public UserHeadImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
		mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		
		mMask = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_wellness_photo_mask);
		
	}

	public UserHeadImageView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public UserHeadImageView(Context context) {
		this(context,null);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(mMask.getWidth(), mMask.getWidth());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		BitmapDrawable bd = (BitmapDrawable)getDrawable();
		Bitmap head = Bitmap.createBitmap(mMask.getWidth(), mMask.getHeight(), Bitmap.Config.ARGB_8888);
		
		Canvas c = new Canvas(head);
		float min;
		if (bd.getIntrinsicWidth() < bd.getIntrinsicHeight()){
			min = bd.getIntrinsicWidth();
		}else{
			min = bd.getIntrinsicHeight();
		}
		
		float scale = min/mMask.getWidth();
		
		bd.setBounds(0, 0, Math.round(bd.getIntrinsicWidth()/scale), Math.round(bd.getIntrinsicHeight()/scale));
		bd.draw(c);
		c.drawBitmap(mMask, 0, 0, mPaint);
		
		canvas.drawBitmap(head, 0, 0, null);
		head.recycle();
	}

	
	
}
