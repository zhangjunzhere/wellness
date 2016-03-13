package com.asus.wellness.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.asus.wellness.R;

public class DrawerUserHeadImageView extends ImageView {
	
	private Paint mPaint;
	private Bitmap mMask;
	private Bitmap head;
	int mWidth;
	int mHeight;
	private Boolean mUseSpeImage;
	public DrawerUserHeadImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mPaint = new Paint();
		mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
		
		mMask = BitmapFactory.decodeResource(context.getResources(), R.drawable.asus_wellness_photo_mask_80);
		
	}

	public DrawerUserHeadImageView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public DrawerUserHeadImageView(Context context) {
		this(context,null);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		Log.i("smile","Drawer user onAttachedToWindow");
		if(mMask== null || mMask.isRecycled())
		{
			mMask = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.asus_wellness_photo_mask_80);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		Log.i("smile","Drawer user onDetach");
		recycleBitmap(mMask);
		recycleBitmap(head);
	}
	private void recycleBitmap(Bitmap bmp)
	{
		if(bmp!=null && !bmp.isRecycled())
		{
			bmp.recycle();
			bmp = null;
		}
	}


	public void setUseSpeImage(Boolean useSpeImage)
	{
		mUseSpeImage = useSpeImage;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		int width = MeasureSpec.getSize(widthMeasureSpec);
//		int height = MeasureSpec.getSize(heightMeasureSpec);
		super.onMeasure(heightMeasureSpec, heightMeasureSpec);
		//setMeasuredDimension(mMask.getWidth(), mMask.getWidth());
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		initHead();
	}
	private void initHead()
	{
		if(head == null || head.isRecycled())
			head = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
	}


	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		BitmapDrawable bd = (BitmapDrawable)getDrawable();
        if(bd != null) {
         //   Bitmap head = Bitmap.createBitmap(mMask.getWidth(), mMask.getHeight(), Bitmap.Config.ARGB_8888);

            Canvas c = new Canvas(head);
            float min;
            if (bd.getIntrinsicWidth() < bd.getIntrinsicHeight()){
                min = bd.getIntrinsicWidth();
            }else{
                min = bd.getIntrinsicHeight();
            }

        //    float scale = min/mMask.getWidth();
			float scale = min/mWidth;

            bd.setBounds(0, 0, Math.round(bd.getIntrinsicWidth() / scale), Math.round(bd.getIntrinsicHeight() / scale));
            bd.draw(c);

			Matrix matrix = new Matrix();
			matrix.postScale(1.0f * mWidth / mMask.getWidth(), 1.0f * mHeight / mMask.getWidth());
			if(mUseSpeImage) {
				Bitmap tempMask = Bitmap.createBitmap(mMask,0,0,mMask.getWidth(),mMask.getHeight(),matrix,true);
	            c.drawBitmap(tempMask, 0, 0, mPaint);
				tempMask.recycle();
			}

            canvas.drawBitmap(head, 0, 0, null);
           // head.recycle();
        }
	}

}
