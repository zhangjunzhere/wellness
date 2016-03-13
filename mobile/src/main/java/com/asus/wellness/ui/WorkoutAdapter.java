package com.asus.wellness.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.asus.wellness.ParseDataManager;
import com.asus.wellness.R;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.provider.CoachTable;
import com.asus.wellness.provider.ProfileTable;
import com.asus.wellness.utils.TimeSpanItem;
import com.asus.wellness.utils.Utility;
import java.util.ArrayList;

public class WorkoutAdapter extends BaseAdapter {

    ArrayList<ParseDataManager.WorkoutInfo> mArrayWorkoutInfo;
    private Context mContext;

    private int WORKOUT_COMPLETED =100;

    public WorkoutAdapter(Context context, ArrayList<ParseDataManager.WorkoutInfo> infos){
        mContext = context;
        mArrayWorkoutInfo = infos;
       // Log.i("emily","size  = " + mArrayWorkoutInfo.size());
    }

    @Override
    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mArrayWorkoutInfo.size();
    }

    @Override
    public ParseDataManager.WorkoutInfo getItem(int position) {
        return mArrayWorkoutInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ParseDataManager parseDM= ParseDataManager.getInstance();
        ParseDataManager.ProfileData pfd=parseDM.getProfileData(mContext);
        ParseDataManager.WorkoutInfo info = mArrayWorkoutInfo.get(position);

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.workout_item,parent,false);
        }
        ImageView imgCompleted = (ImageView)convertView.findViewById(R.id.workout_completed_img);
        ImageView imgType = (ImageView)convertView.findViewById(R.id.workout_type_img);
        TextView tvTime=(TextView)convertView.findViewById(R.id.workout_time);
        TextView tvCal=(TextView)convertView.findViewById(R.id.workout_cal);

        View accumulationView = convertView.findViewById(R.id.workout_accumulation);
        TextView tvDistance=(TextView)accumulationView.findViewById(R.id.workout_distance);
        TextView tvSteps=(TextView)accumulationView.findViewById(R.id.workout_step);

        int runCal=0, runDistance=0;
        if(info.type == CoachTable.TYPE_RUN){
            imgType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.asus_wellness_ic_medium));
            accumulationView.setVisibility(View.VISIBLE);

            int heightInCM=pfd.height;
            if(pfd.heightUnit== ProfileTable.HEIGHT_UNIT_FT){
                float ft= Utility.InchToFt(heightInCM);
                heightInCM=Math.round(Utility.ftToCm(ft));
            }
            int weightInKG=pfd.weight;
            if(pfd.weightUnit==ProfileTable.WEIGHT_UNIT_LBS){
                weightInKG=Math.round(Utility.LbsToKg(pfd.weight));
            }
            runDistance=Utility.getWalkDistanceInCM(heightInCM, (int)info.count)/100;
           // tvDistance.setText(Utility.getTwoDigitFloatString((float) runDistance / 1000.0f)+" "+mContext.getString(R.string.distance_unit) +" /");
            int res = R.string.distance_unit_miles;
            Profile profile=parseDM.getStandardProfile();
            if (profile.getDistance_unit() == 0){
                res = R.string.distance_unit;
            }
            String dis=Utility.formatDistance(runDistance, profile.getDistance_unit());
            tvDistance.setText(dis +" "+ mContext.getResources().getString(res)+" /");

            runCal=(int)Utility.getWalkCalories(heightInCM, (int)info.count, weightInKG);
            tvSteps.setText(Utility.formatNumber(info.count) + " " + mContext.getString(R.string.daily_info_walk_unit));
            tvCal.setText(Utility.formatNumber(runCal)+" "+mContext.getString(R.string.calories_unit));
        }
        else{
            imgType.setImageDrawable(info.type == CoachTable.TYPE_PUSHUP ?
                    mContext.getResources().getDrawable(R.drawable.asus_wellness_ic_pushup):mContext.getResources().getDrawable(R.drawable.asus_wellness_ic_situp));
            accumulationView.setVisibility(View.INVISIBLE);
            tvCal.setText(Utility.formatNumber(info.count)+" "+mContext.getString(R.string.count_time));
        }
        if(info.duration/60 < 1){
            tvTime.setText(Utility.formatNumber(1)+" "+mContext.getString(R.string.detail_sleep_shortmin));
        }
        else{
            TimeSpanItem timeSpan = Utility.getTimeSpan(info.duration/60);
            tvTime.setText(Utility.getHourMinStr_short_unit(mContext, timeSpan));
        }
        imgCompleted.setVisibility(info.percent == WORKOUT_COMPLETED ? View.VISIBLE : View.INVISIBLE);

        return convertView;
    }
}
