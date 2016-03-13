package com.asus.wellness.ui.daily;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.WellnessLocationManager;
import com.asus.wellness.provider.ActivityStateTable;

import java.util.ArrayList;

public class NewTimelineView extends View {
	
	private Context mContext;
	private int viewHeight=0;

	private int mDefaultDotsNum=1;
	private int mDefaultTimeLineLeftMargin=getContext().getResources().getDimensionPixelSize(R.dimen.default_start_position_x);
	private int mDefaultTimeLineTopMargin=getContext().getResources().getDimensionPixelSize(R.dimen.default_start_position_y);
	private int mDefaultLineLength=getResources().getDimensionPixelSize(R.dimen.daily_view_timeline_line_length);
	private int mDefaultMarginActivityLocation=getContext().getResources().getDimensionPixelSize(R.dimen.timeline_line_width)*2;
	private int mDefaultLineWidth=getContext().getResources().getDimensionPixelSize(R.dimen.timeline_line_width);
	private int mDefaultDotsRadius=getContext().getResources().getDimensionPixelSize(R.dimen.time_line_circle_radius);
	private int mDefaultOffset=getContext().getResources().getDimensionPixelSize(R.dimen.timeline_item_left_margin);
	private ArrayList<ArrayList<TimeItem>> mData=null;//ordered array
	private ArrayList<Rect> mLastDimention=new ArrayList<Rect>();
	
	public static final String TAG_ACTIVTY_START="ACTIVITY START";
	public static final String TAG_ACTIVTY_END="ACTIVITY END";
	public static final String TAG_ECG="ECG";
	public static class TimeItem{
		public String timeString;
		public View view;
		public String district;
	}
	
	public NewTimelineView(Context context) {
		super(context);
		mContext=context;
		// TODO Auto-generated constructor stub
	}
	
	public NewTimelineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext=context;
		// TODO Auto-generated constructor stub
	}
	
	public NewTimelineView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext=context;
		// TODO Auto-generated constructor stub
	}

	private void drawDots(Canvas canvas, Point point){
		Paint dotPaint=new Paint();
		dotPaint.setColor(Color.BLACK);
		dotPaint.setAntiAlias(true);
		canvas.drawCircle(point.x, point.y, mDefaultDotsRadius, dotPaint);
	}
	
	private void drawLine(Canvas canvas, Point fromPoint, Point toPoint, Paint paint){
		canvas.drawLine(fromPoint.x, fromPoint.y, toPoint.x, toPoint.y, paint);
	}
	
	private void drawDotLine(Canvas canvas, Point fromPoint, Point toPoint, Paint paint){
		int margin=getContext().getResources().getDimensionPixelSize(R.dimen.timeline_preview_dot_margin);
		int radius=getContext().getResources().getDimensionPixelSize(R.dimen.time_line_preview_circle_radius);
		int i=fromPoint.y+margin;
		while(i<toPoint.y){
			canvas.drawCircle(fromPoint.x, i, radius, paint);
			i+=margin;
		}
	}
	
	private void drawItem(Canvas canvas, View view, Rect rect){
		view.layout (0, 0, rect.width(), rect.height());
		
		canvas.save();
		canvas.translate(rect.left, rect.top);
		
		view.draw(canvas);
		canvas.restore();
	}
	
	public void setDataItem(ArrayList<ArrayList<TimeItem>> data){
		mData=data;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		mLastDimention=new ArrayList<Rect>();
		Point mLastPoint=new Point(mDefaultTimeLineLeftMargin, mDefaultTimeLineTopMargin);
		String mLastDistrict="";
        //smile_gao add for draw district line 2014/12/5
        boolean  drawTransport = false;
        //end smile

		for(int i=0;i<mData.size();i++){
			ArrayList<TimeItem> timeItemList=mData.get(i);
			//draw time and dot
			TimeItem timeItem=timeItemList.get(0);
			
			int activityIndex=checkHasActivityItem(timeItemList);
			int neededLineLength=0;
			
			//draw activity view
			TimeItem firstItem=timeItemList.get(activityIndex==-1?0:activityIndex);
			Rect firstItemDimention=measureDimentionRect(firstItem.view);
			int firstLeft=mLastPoint.x-firstItemDimention.width()
					, firstTop=mLastPoint.y-firstItemDimention.height()/2;
			String tag=(String) firstItem.view.getTag();
			if(!tag.matches(TAG_ACTIVTY_END)){
				Rect firstPositionRect=calculatePotisionRect(firstLeft, firstTop, firstItemDimention.width(), firstItemDimention.height());
				if(testIntersect(firstPositionRect)){
					firstPositionRect.top=mLastDimention.get(mLastDimention.size()-1).bottom;
					firstPositionRect.bottom=firstPositionRect.top+firstItemDimention.height();
					drawItem(canvas, firstItem.view, firstPositionRect);
				}
				else{
					drawItem(canvas, firstItem.view, firstPositionRect);
				}
				neededLineLength+=firstItemDimention.height();
				mLastDimention.add(firstPositionRect);	
			}
			
			//draw district
			if(drawDistrict(timeItem)){
				View location_text=LayoutInflater.from(mContext).inflate(R.layout.daily_info_location_layout, null);
				TextView districtText=(TextView)location_text.findViewById(R.id.district);
				districtText.setText(timeItem.district);
				
				Rect itemDimention=measureDimentionRect(location_text);
				Rect positionRect=calculatePotisionRect(mLastPoint.x-itemDimention.width()-mDefaultOffset
						, mLastPoint.y+itemDimention.height()/2, itemDimention.width(), itemDimention.height());
				if(testIntersect(positionRect)){
					positionRect.top=mLastDimention.get(mLastDimention.size()-1).bottom;
					positionRect.bottom=positionRect.top+itemDimention.height();
					drawItem(canvas, location_text, positionRect);
				}
				else{
					drawItem(canvas, location_text, positionRect);
				}
				neededLineLength+=itemDimention.height()*2;
				mLastDimention.add(positionRect);
			}
			
			
			//draw all ecg views.
			int startIndex=0;
			if(activityIndex==-1){//no activity item, the first item already draw.
				startIndex=1;
			}
			for(int j=startIndex;j<timeItemList.size();j++){
				TimeItem item=timeItemList.get(j);
				
				if(activityIndex!=j){
					String viewTag=(String) item.view.getTag();
					if(viewTag.matches(TAG_ACTIVTY_END)){
						continue;
					}
					Rect itemDimention=measureDimentionRect(item.view);
					neededLineLength+=itemDimention.height();
					int left=mLastPoint.x-itemDimention.width()
							, top=mLastPoint.y-itemDimention.height()/2;
					if(activityIndex!=-1){
						left-=mDefaultMarginActivityLocation;
					}
					Rect positionRect=calculatePotisionRect(left, top, itemDimention.width(), itemDimention.height());
					if(testIntersect(positionRect)){
						positionRect.top=mLastDimention.get(mLastDimention.size()-1).bottom;
						positionRect.bottom=positionRect.top+itemDimention.height();
						drawItem(canvas, item.view, positionRect);
					}
					else{
						drawItem(canvas, item.view, positionRect);
					}
					mLastDimention.add(positionRect);	
				}
			}
			
			//compare district
			Paint districtPaint=null;
			if(i+1<=mData.size()-1){
				ArrayList<TimeItem> nextTimeItem=mData.get(i+1);
				districtPaint=getDistrictPaint(timeItem.district.matches(nextTimeItem.get(0).district) 
						&& !timeItem.district.matches(WellnessLocationManager.DEFAULT_DISTRICT)
						&& !timeItem.district.matches(WellnessLocationManager.OFF_POSITIONING));
			}
			
			//draw line
			Point fromPoint=new Point(mLastPoint);
			Point endPoint=new Point(fromPoint);
			if(neededLineLength<mDefaultLineLength){
				neededLineLength=mDefaultLineLength;
			}
           //smile_gao draw fTransPoint 2014/12/5
			endPoint.y+=neededLineLength;
			if(districtPaint!=null){
                if(tag.matches(TAG_ACTIVTY_END)){
                    drawTransport=false;
                }
				if(activityIndex!=-1){
                    //if time elapse less than 1 minute
                    if(timeItemList.size() ==1 ) {
                        Point fDistrictPoint = new Point(fromPoint);
                        fDistrictPoint.x += mDefaultMarginActivityLocation;
                        Point eDistrictPoint = new Point(endPoint);
                        eDistrictPoint.x += mDefaultMarginActivityLocation;
     				    drawLine(canvas, fDistrictPoint, eDistrictPoint, districtPaint);

                        Point fTransPoint = new Point(fromPoint);
                        fTransPoint.x -= mDefaultMarginActivityLocation;
                        Point eTransPoint = new Point(endPoint);
                        eTransPoint.x -= mDefaultMarginActivityLocation;
                        if (tag.matches(TAG_ACTIVTY_START)) {
                            drawTransport = true;
                        }
                        drawLine(canvas, fTransPoint, eTransPoint, getTransportingPaint((TextView)timeItem.view.findViewById(R.id.tv_step_or_bike_type)));
                    }else{
                        drawLine(canvas, fromPoint, endPoint, districtPaint);
                    }
				}
				else{
                    if(!drawTransport)
                    {
                        drawLine(canvas, fromPoint, endPoint, districtPaint);
                    }
                    else {
                        Point fDistrictPoint=new Point(fromPoint);
                        fDistrictPoint.x += mDefaultMarginActivityLocation;
                        Point eDistrictPoint=new Point(endPoint);
                        eDistrictPoint.x += mDefaultMarginActivityLocation;
                        drawLine(canvas, fDistrictPoint, eDistrictPoint, districtPaint);

                        //draw transport
                        Point fTransPoint=new Point(fromPoint);
                        fTransPoint.x-=mDefaultMarginActivityLocation;
                        Point eTransPoint=new Point(endPoint);
                        eTransPoint.x-=mDefaultMarginActivityLocation;

                        //   Log.i("smile","transportEndPoint: "+String.valueOf(transportEndPoint.y)+" "+String.valueOf(drawtransportLength));
                        drawLine(canvas, fTransPoint, eTransPoint, getTransportingPaint((TextView)timeItem.view.findViewById(R.id.tv_step_or_bike_type)));

                    }
				}
			}
            //end smile_gao transport 2014/12/5
			if(i==mData.size()-1 && timeItemList.size()>1){
				drawLine(canvas, fromPoint, endPoint, getDistrictPaint(false));
				endPoint.y+=mDefaultLineLength;
			}
			drawTimeDot(canvas, mLastPoint, timeItem);
			
			//draw location icon
			if(drawDistrict(timeItem)){
				Bitmap bitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.asus_wellness_ic_beitou);
				canvas.drawBitmap(bitmap, mLastPoint.x-bitmap.getWidth()/2, mLastPoint.y-bitmap.getHeight()/2,new Paint());
				bitmap.recycle();
			}
			mLastPoint=new Point(endPoint);
			mLastDistrict=timeItem.district;
		}
		
		mLastPoint=new Point(addRestPreviewItem(canvas, mLastPoint, mData.size()));
		
		if(viewHeight!=mLastPoint.y-mDefaultLineLength+mDefaultTimeLineTopMargin){
			viewHeight=mLastPoint.y-mDefaultLineLength+mDefaultTimeLineTopMargin;
			LayoutParams params=(LayoutParams) getLayoutParams();
			params.height=viewHeight;
			setLayoutParams(params);
		}
	}

    private boolean drawDistrict(TimeItem timeItem) {
       // Log.i("kim_value", timeItem.district);
        if(!timeItem.district.matches(WellnessLocationManager.OFF_POSITIONING) && !timeItem.district.matches(WellnessLocationManager.DEFAULT_DISTRICT)
                && timeItem.view.getTag().equals(NewTimelineView.TAG_ACTIVTY_START)) {
            return true;
        } else {
            return false;
        }
    }

	private Point addRestPreviewItem(Canvas canvas, Point startPoint, int dataSize){
		Point point=new Point(startPoint);
		
		View heartRateLayout=LayoutInflater.from(mContext).inflate(R.layout.daily_info_heart_rate_layout, null);
		View relaxLayout=LayoutInflater.from(mContext).inflate(R.layout.daily_info_stress_energy_layout, null);
		View activityLayout=LayoutInflater.from(mContext).inflate(R.layout.daily_info_walk_bike_layout, null);
		
		Rect heartRateRect=measureDimentionRect(heartRateLayout);
		Rect relaxRect=measureDimentionRect(relaxLayout);
		Rect activityRect=measureDimentionRect(activityLayout);
		
		ImageView heartrateImage=(ImageView)heartRateLayout.findViewById(R.id.heartrate_iv);
		ImageView relaxImage=(ImageView)relaxLayout.findViewById(R.id.energy_or_stress_icon);
		ImageView activityImage=(ImageView)activityLayout.findViewById(R.id.img_step_or_bike_icon);
		heartrateImage.setImageResource(R.drawable.asus_wellness_ic_green1);
		relaxImage.setImageResource(R.drawable.asus_wellness_ic_red);
		activityImage.setImageResource(R.drawable.asus_wellness_ic_green2);
		
		heartRateLayout.findViewById(R.id.heartrate_container_up).setVisibility(View.INVISIBLE);
		heartRateLayout.findViewById(R.id.heartrate_container_down).setVisibility(View.INVISIBLE);
		relaxLayout.findViewById(R.id.relax_container_up).setVisibility(View.INVISIBLE);

        // Draw No Data TextView
        if(dataSize == 0) {
            View noDataLayout = LayoutInflater.from(mContext).inflate(R.layout.no_data_tv_layout, null);
            TextView noDataTv = (TextView) noDataLayout.findViewById(R.id.no_data_tv);
            Rect positionNodata = calculatePotisionRect(point.x-heartRateRect.width(), point.y-heartRateRect.height()/2, heartRateRect.width(), heartRateRect.height());
            drawItem(canvas, noDataTv, positionNodata);
        }

		if(mData.size()<6){
			Log.d("circle","size:"+mData.size());
			int index=mData.size();
			for(int i=index;i<6;i++){
				switch(i){
					case 0:
						Rect positionHeartRate=calculatePotisionRect(point.x-heartRateRect.width(), point.y-heartRateRect.height()/2, heartRateRect.width(), heartRateRect.height());
						drawItem(canvas, heartRateLayout, positionHeartRate);
						point.set(point.x, point.y+mDefaultLineLength);
						break;
					case 1:
						Rect positionRelax=calculatePotisionRect(point.x-relaxRect.width(), point.y-relaxRect.height()/2, relaxRect.width(), relaxRect.height());
						drawItem(canvas, relaxLayout, positionRelax);
						point.set(point.x, point.y+mDefaultLineLength);
						break;
					case 2:
						Rect positionActivity=calculatePotisionRect(point.x-activityRect.width(), point.y-activityRect.height()/2, activityRect.width(), activityRect.height());
						drawItem(canvas, activityLayout, positionActivity);
						point.set(point.x, point.y+mDefaultLineLength);
						break;
					case 3:
						positionRelax=calculatePotisionRect(point.x-relaxRect.width(), point.y-relaxRect.height()/2, relaxRect.width(), relaxRect.height());
						drawItem(canvas, relaxLayout, positionRelax);
						point.set(point.x, point.y+mDefaultLineLength);
						break;
					case 4:
						positionHeartRate=calculatePotisionRect(point.x-heartRateRect.width(), point.y-heartRateRect.height()/2, heartRateRect.width(), heartRateRect.height());
						drawItem(canvas, heartRateLayout, positionHeartRate);
						point.set(point.x, point.y+mDefaultLineLength);
						break;
					case 5:
						positionActivity=calculatePotisionRect(point.x-activityRect.width(), point.y-activityRect.height()/2, activityRect.width(), activityRect.height());
						drawItem(canvas, activityLayout, positionActivity);
						point.set(point.x, point.y+mDefaultLineLength);
						break;
				}
			}
			Paint paint=new Paint();
			paint.setColor(Color.GRAY);
			paint.setAntiAlias(true);
			Point mRealStartPoint=new Point(startPoint);
			if(mData.size()!=0){
				mRealStartPoint.set(startPoint.x, startPoint.y-mDefaultLineLength);	
			}
			drawDotLine(canvas, mRealStartPoint, point, paint);
		}
		return point;
	}
	
	private int checkHasActivityItem(ArrayList<TimeItem> ti){
		for(int i=0;i<ti.size();i++){
			String tag=(String) ti.get(i).view.getTag();
			if(tag.matches(TAG_ACTIVTY_START)){
				return i;
			}
		}
		return -1;
	}
	
	private void drawTimeDot(Canvas canvas, Point point, TimeItem timeItem){
		drawDots(canvas, point);
		
		TextView text=new TextView(mContext);
		text.setMaxWidth(mContext.getResources().getDimensionPixelSize(R.dimen.timeline_time_text_maxwidth));
		text.setText(timeItem.timeString);
		Rect dimention=measureDimentionRect(text);
		Rect position=calculatePotisionRect(point.x+mDefaultOffset, point.y-dimention.height()/2, dimention.width(), dimention.height());
		drawItem(canvas, text, position);
	}
	
	private Rect measureDimentionRect(View view){
        view.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
				MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
		return new Rect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
	}
	
	private Rect calculatePotisionRect(int left, int top, int width, int height){
		return new Rect(left, top, left+width, top+height);
	}
	
	private boolean testIntersect(Rect targetRect){
		for(int i=0;i<mLastDimention.size();i++){
			if(Rect.intersects(mLastDimention.get(i), targetRect)){
				return true;
			}
		}
		return false;
	}
	
	private Paint getTransportingPaint(TextView typeTv){
		Paint paint=new Paint();
		paint.setColor(0xff81bc0f);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(mDefaultLineWidth);
		if(typeTv!=null && typeTv.getText()!=null && typeTv.getText().toString().equals(getResources().getString(R.string.daily_info_run_type))){
			paint.setColor(0xff228d54);
		}
		return paint;
	}
	
	
	private Paint getDistrictPaint(boolean sameDistrict){
		Paint paint=new Paint();
		if(sameDistrict){
			paint.setColor(0xff556caa);
		}
		else{
			paint.setColor(0xff909090);
		}
		paint.setAntiAlias(true);
		paint.setStrokeWidth(mDefaultLineWidth);
		return paint;
	}
}
