package com.asus.wellness.sleep;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.sharedata.SyncSleep;
import com.asus.wellness.R;
import com.asus.wellness.StartActivity;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.coach.setup.AbsWorkoutFragment;
import com.asus.wellness.dbhelper.Sleep;
import com.asus.wellness.dbhelper.SleepDao;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.greenrobot.event.EventBus;
import originator.ailin.com.smartgraph.chart.barchart.SleepBarChart;


public class SleepSummeryFragment extends WearableSleepFragment {
    private final String TAG = "SleepSummeryFragment";
    private ViewGroup mRootView;
    private SyncSleep.SleepInfo mSleepInfo = new SyncSleep.SleepInfo();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup)inflater.inflate(R.layout.pni_sleep_fragment_end, container, false);
        updateSleepStatus();
        boolean hasSleepData = analysisSleepData();
        if(hasSleepData) {
            figureoutSleepChart();
            ViewGroup ll_total_duration = (ViewGroup) mRootView.findViewById(R.id.ll_total_duration);
            updateSleepDuration(ll_total_duration, mSleepInfo.duration_total);
            updateSleepPoint();
            updateSleepDuration();
        }else{
            mRootView.findViewById(R.id.ll_statistic).setVisibility(View.GONE);
            TextView tv_nodata = (TextView) mRootView.findViewById(R.id.tv_nodata);
            tv_nodata.setHeight(Utility.getScreenHeight(getActivity()));
            tv_nodata.setVisibility(View.VISIBLE);
        }
      return mRootView;
    }


    private void updateSleepStatus(){
        SleepDataModel sleepDataModel   = SleepDataModel.getInstance();
        sleepDataModel.setSleepStatus(SleepDataModel.eSleep.AWAKE);
    }

    private boolean analysisSleepData(){
        Sleep sleep = SleepDataModel.getInstance().getSleepRecordByDate(System.currentTimeMillis());
        if(sleep == null || TextUtils.isEmpty(sleep.getData())) {
            Log.e(Utility.TAG, "analysisSleepData , sleepList is empty ");
            return false;
        }
        mSleepInfo = SyncSleep.getSleepInfo( sleep.getData(),sleep.getStart(),sleep.getEnd());
        return true;
    }

    public void figureoutSleepChart(){
        SleepBarChart simpleBarChart = (SleepBarChart)mRootView.findViewById(R.id.my_sleep_chart);
        if(mSleepInfo.sleepQuality.size()>0) {
            simpleBarChart.setSleepData(mSleepInfo.sleepQuality, mSleepInfo.start, mSleepInfo.end);
        }
    }

    @Override
    public void onEnterAmbient() {
        overrideTextColor(getActivity(), mRootView, true);
        showIcons(mRootView, true);
        mRootView.setBackground(null);
        mRootView.findViewById(R.id.my_sleep_chart).setVisibility(View.GONE);
        mRootView.findViewById(R.id.tv_title).setVisibility(View.GONE);
        mRootView.findViewById(R.id.sleep_summery_line).setBackgroundColor(getResources().getColor(R.color.sleep_summery_line_ambient));
      //  SleepBarChart simpleBarChart = (SleepBarChart) mRootView.findViewById(R.id.my_sleep_chart);
      //  simpleBarChart.setAmbientMode(true);
    }

    @Override
    public void onExitAmbient() {
        overrideTextColor(getActivity(),mRootView,false);
        showIcons(mRootView, false);
        mRootView.setBackgroundResource(R.color.white);
        mRootView.findViewById(R.id.my_sleep_chart).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.tv_title).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.sleep_summery_line).setBackgroundColor(getResources().getColor(R.color.sleep_summery_line_normal));
     //   SleepBarChart simpleBarChart = (SleepBarChart) mRootView.findViewById(R.id.my_sleep_chart);
      //  simpleBarChart.setAmbientMode(false);
    }

    private class SleepAnalysisData {
        public int layout_id;
        public int img_id;
        public int string_id;
        public long sleep_time;

        public SleepAnalysisData(int layout_id, int img_id, int string_id, long sleep_time) {
            this.layout_id = layout_id;
            this.img_id = img_id;
            this.string_id = string_id;
            this.sleep_time = sleep_time;
        }
    }

    private void updateSleepPoint(){
        SleepAnalysisData[] items = new SleepAnalysisData[]{
                new SleepAnalysisData( R.id.ll_inbed, R.drawable.asus_wellness_ic_inbed,R.string.inbed,mSleepInfo.start),
                new SleepAnalysisData( R.id.ll_wokeup, R.drawable.asus_wellness_ic_wokeup,R.string.wokeup,mSleepInfo.end)
        };

        for(SleepAnalysisData data : items){
            updateSleepDesc(data);
            ViewGroup ll_sleep_point = (ViewGroup)mRootView.findViewById(data.layout_id);
            TextView tv_am = (TextView) ll_sleep_point.findViewById(R.id.tv_am);
            TextView tv_time = (TextView) ll_sleep_point.findViewById(R.id.tv_time);
            Calendar calendar =  Calendar.getInstance();
            calendar.setTimeInMillis(data.sleep_time);
            int am = calendar.get(Calendar.AM_PM);
            String am_pm = am == Calendar.AM? getString(R.string.am) : getString(R.string.pm) ;
            String timeStr = new SimpleDateFormat("HH:mm").format(new Date(data.sleep_time));
            tv_am.setText(am_pm);
            tv_time.setText( timeStr);
        }
    }

    private void updateSleepDuration(){
        SleepAnalysisData[] items = new SleepAnalysisData[]{
                new SleepAnalysisData( R.id.ll_sleep_awake, R.drawable.asus_wellness_ic_color_g,R.string.awoke, mSleepInfo.duration_awake ),
                new SleepAnalysisData( R.id.ll_sleep_light, R.drawable.asus_wellness_ic_color_b,R.string.light, mSleepInfo.duration_light ),
                new SleepAnalysisData( R.id.ll_sleep_deep, R.drawable.asus_wellness_ic_color_d,R.string.deep, mSleepInfo.duration_deep )
        };

        for(SleepAnalysisData data : items){
            updateSleepDesc(data);
            ViewGroup ll_duration = (ViewGroup)mRootView.findViewById(data.layout_id).findViewById(R.id.ll_duration);
            updateSleepDuration(ll_duration, (int) data.sleep_time);
        }
    }

    private void updateSleepDesc(SleepAnalysisData data){
        ViewGroup ll_sleep_duration = (ViewGroup)mRootView.findViewById(data.layout_id);
        ImageView iv_inbed = (ImageView) ll_sleep_duration.findViewById(R.id.iv_inbed);
        TextView tv_inbed = (TextView) ll_sleep_duration.findViewById(R.id.tv_inbed);
        iv_inbed.setImageResource(data.img_id);
        String strInbed = getString(data.string_id);
        tv_inbed.setText(strInbed);
    }

    private void updateSleepDuration(ViewGroup ll_duration, int minutes){
        TextView tv_hour = (TextView) ll_duration.findViewById(R.id.tv_hour);
        TextView tv_minute = (TextView) ll_duration.findViewById(R.id.tv_minute);
        tv_hour.setText( String.valueOf(minutes /60));
        tv_minute.setText(  String.valueOf(minutes%60));
    }

    private void overrideTextColor(final Context context, final View v, boolean ambient) {
        HashMap<Integer,Integer> colorMap = new HashMap<Integer,Integer>();
        colorMap.put(R.id.tv_am,R.color.sleep_text_normal);
        colorMap.put(R.id.tv_inbed,R.color.sleep_text_normal);
        colorMap.put(R.id.tv_hour_unit,R.color.sleep_text_normal);
        colorMap.put(R.id.tv_minute_unit,R.color.sleep_text_normal);
        colorMap.put(R.id.tv_time,R.color.sleep_text_highlight);
        colorMap.put(R.id.tv_hour,R.color.sleep_text_highlight);
        colorMap.put(R.id.tv_minute, R.color.sleep_text_highlight);
        colorMap.put(R.id.tv_total, R.color.sleep_text_highlight);
        colorMap.put(R.id.tv_title, R.color.sleep_text_highlight);
        colorMap.put(R.id.tv_nodata, R.color.sleep_text_highlight);

        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    overrideTextColor(context, child,ambient);
                }
            } else if (v instanceof TextView ) {
                TextView tv = (TextView)v;
                if(ambient){
                    tv.setTextColor(context.getResources().getColor(R.color.white));
                }else {
                    int resId = colorMap.get(tv.getId());
                    tv.setTextColor(context.getResources().getColor(resId));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void showIcons(final View v,boolean ambient){
        try {
            if (v instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) v;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View child = vg.getChildAt(i);
                    showIcons(child, ambient);
                }
            } else if (v instanceof ImageView ) {
                int visiblity = ambient? View.GONE : View.VISIBLE;
                v.setVisibility(visiblity);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

}
