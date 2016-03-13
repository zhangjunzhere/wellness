package com.asus.wellness.coach;

import android.content.Context;


import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.dbhelper.Coach;
import com.asus.wellness.dbhelper.CoachItem;
import com.asus.wellness.dbhelper.DaoSession;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.utils.Arith;
import com.asus.wellness.utils.ProfileHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by jz on 2015/5/22.
 */
public class CoachDataModel {
    public enum eState{
        START,PLAY,RESUME,PAUSE,STOP,FINISH;
    }

    private  eType type = eType.RUN; //run,bike,sit-up
    private  eGoal goal = eGoal.DISTANCE; //distance,time,colories
    private eState state = eState.FINISH;
    private  eUnit unit = eUnit.KM ; //km:1000. miles: 1, other 1

    private  int insertData = 0; // for insertDataFragment1,
    private long total_time = 0;        //in term of sec
    private long total_distance = 0;  //in term of m
    private long total_calories = 0;  // in term of  cal
    private long total_quantity = 0L;
    private long history_quantity = 0L;
    private long start_quantity = 0L;
    private long history_interval = 0L;
    private long start_time = 0L;

    private Coach mCoachRecord;
    private List<CoachItem> mCoachItemRecord = new ArrayList<CoachItem>();

    public  CoachDataModel(){
    }



    public void setGoal(eGoal goal){
        this.goal = goal;
    }
    public eGoal getGoal(){
        return this.goal;
    }

    public void setState(eState state){
        this.state = state;
    }
    public eState getState(){
        return this.state;
    }

    public void setType(eType type){
        this.type = type;
    }
    public eType getType(){
        return this.type;
    }

    public void setUnit(eUnit unit){
        this.unit = unit;
    }
    public eUnit getUnit(){
        return this.unit;
    }

    public void setStartQuantity(long  quantity){
        this.start_quantity = quantity;
    }

    public long getStartQuantity(){
        return this.start_quantity;
    }

    public void setHistoryInterval(long history_value){this.history_interval = history_value;}
    public long getHistoryInterval(){return this.history_interval;}

    public void setStartTime(long start_value){this.start_time = start_value;}
    public long getStartTime(){return this.start_time;}


    public void setTarget(int value){
        this.insertData = value;
    }
    public long getTarget(){
        long target = insertData;
        switch (goal){
            case DISTANCE:
                target = (long) Arith.mul((double)insertData/100,unit.value);
                break;
            case TIME:
                target = insertData * 60;
                break;
            case COLARIES:
                target = insertData;
                break;
            case NOGOAL:
                target = -1;
                break;
            default:
                break;
        }
        return target;
    }

    public void setTotalTime(long value){
        if(getGoal() == eGoal.TIME) {
            value = Math.min(value,getTarget());
        }
        this.total_time = value;
    }

    public long getTotalTime(){
        return this.total_time;
    }

    public long getTotalCalories(){
        return this.total_calories ;
    }
    public long getTotalDistance(){
        return this.total_distance;
    }
//    public long getTotalStep(){
//        return this.total_step;
//    }

    public void setTotalQuantity(long value){
        this.total_quantity = value;
    }
    public long getTotalQuantity(){
        return this.total_quantity;
    }

    public void setHistoryQuantity(long value){
        this.history_quantity = value;
    }

    public long getHistoryQuantity(){
        return this.history_quantity;
    }
//    public void setTotalStep(long value){
//        this.total_step = value;
//    }

    public void startCoach(){
        mCoachRecord = new Coach();
        mCoachRecord.setType(eType.toLong(type));
        mCoachItemRecord.clear();
    }

    public void endCoach(){
        if(mCoachRecord != null) {
            mCoachRecord.setDuration(total_time);
            mCoachRecord.setValue(total_quantity);
            mCoachRecord.setPercent(getPercent());
            //save coach data
            DaoSession daoSession = WApplication.getDataHelper().getDaoSession();
           int size = mCoachItemRecord.size();
            if( size > 0) {
                mCoachRecord.setStart(mCoachItemRecord.get(0).getStart());
                mCoachRecord.setEnd(mCoachItemRecord.get(size -1).getEnd());
                long coachId = daoSession.getCoachDao().insert(mCoachRecord);
                for (CoachItem item : mCoachItemRecord) {
                    item.setCoach_id(coachId);
                }
                daoSession.getCoachItemDao().insertInTx(mCoachItemRecord);
            }
            mCoachItemRecord.clear();
            mCoachRecord.resetItems();
            mCoachRecord = null;
        }
    }

    public void  addCoachItem(CoachItem item){
        if(item.getValue() > 0){
            mCoachItemRecord.add(item);
        }
    }

    public boolean isCoaching(){
        return state != eState.STOP && state != eState.FINISH ;
    }

    public void resetData(){
        type = eType.RUN;
        goal = eGoal.DISTANCE;
        state = eState.FINISH;
        unit = eUnit.KM;

        insertData = 0; // for insertDataFragment1,
        total_time = 0;        //in term of sec
        total_distance = 0;  //in term of m
        total_calories = 0;  // in term of  cal
//        total_step = 0;
        total_quantity = 0L;
        history_quantity = 0L;
        start_quantity = 0L;

        history_interval = 0L;
        start_time = 0L;
    }

    public double getValue(){
        double value = 0;
        switch (goal){
            case DISTANCE:
                value = total_distance;
                break;
            case TIME:
                value = total_time;
                break;
            case COLARIES:
                value = total_calories;
                break;
            case QUANTITY:
                value = total_quantity;
            default:
                break;
        }
        return value;
    }

    public void setCoachInfo(Profile profile, long value){
        int heightInCM = profile.getHeight();
        if(profile.getHeight_unit()== ProfileHelper.HEIGHT_UNIT_FT){
            float ft=ProfileHelper.InchToFt(heightInCM);
            heightInCM=(int) Math.round(ProfileHelper.ftToCm(ft));
        }
        int weightInKG = profile.getWeight();
        if(profile.getWeight_unit() ==ProfileHelper.WEIGHT_UNIT_LBS){
            weightInKG=(int) Math.round(ProfileHelper.LbsToKg(profile.getWeight()));
        }

//        switch (type){
//            case  RUN:
//                total_step = value;
//                break;
//            case  PUSHUP:
//            case SITUP:
//                total_quantity = value;
//                break;
//        }
        total_quantity = value;
        total_distance = ProfileHelper.getWalkDistanceInCM(heightInCM,value)/100;
        total_calories = ProfileHelper.getWalkCalories(heightInCM, weightInKG,value);
    }

    public String  getTargetString(Context context){

        String targetStr= context.getResources().getString(R.string.workout_goal_none);
        switch (goal){
            case DISTANCE:
                targetStr =  new DecimalFormat("0.00").format((double)insertData/100).toString();
                break;
            case TIME:
            case COLARIES:
            case QUANTITY:
                targetStr = String.valueOf( insertData);
                break;

            default:
                break;
        }
        return targetStr;
    }

    public String  getTargetUnitString(Context context){
        switch (goal){
            case DISTANCE:
                int resId = R.string.distance_unit;
                if(unit == eUnit.MILES){
                    resId = R.string.miles;
                }
                return context.getString(resId).toLowerCase();
            case TIME: {
//                if(insertData > 1){
//                    return  context.getString(R.string.unit_mintes_plurals);
//                }else{
                    return  context.getString(R.string.unit_mintes);
//                }
//                return context.getQuantityString(R.plurals.unit_mintes_plurals, insertData);
            }
            case COLARIES:
                return context.getString(R.string.calories_unit);
            case QUANTITY: {
                if(insertData > 1){
                    return  context.getString(R.string.unit_quantity_plurals);
                }else{
                    return  context.getString(R.string.unit_quantity);
                }
            }
            default:
                break;
        }
        return  "";
    }


    public int getPercent(){
        double value = getValue() * 100;
        double target = getTarget();
        int percent = 0;
        if(goal != eGoal.NOGOAL) {
            percent = (int) (value / target) ;
        }
        return percent;
    }

    public String getPercentString(){
//        double value = getValue() * 100;
//        double target = getTarget();
//        String percent = "0";
//        if(goal != eGoal.NOGOAL) {
//            percent = String.valueOf((int) (value / target)) ;
//        }
//        return percent;
        return String.valueOf(getPercent());
    }

    public String  getDistanceValueString(){
        double value = Arith.div((double) total_distance, unit.value, 3);
        return new DecimalFormat("0.000").format(value);
    }

    public String  getCoachValueString(){
        String valueStr = String.valueOf((long)getValue());
        switch (type){
            case RUN:            {
                double value = Arith.div((double)total_distance, unit.value, 3);
                valueStr = new DecimalFormat("0.000").format(value);
            }
                break;
            case PUSHUP:
            case SITUP:{
                valueStr = String.valueOf(total_quantity);
            }
            break;
        }

        return valueStr;
    }

    public String  getDistanceTargetWithUnitString(Context context){
        int resId = R.string.distance_unit;
        if(unit == eUnit.MILES){
            resId = R.string.miles;
        }
        String targetValue = context.getString(resId).toLowerCase();
        if(goal == eGoal.DISTANCE){
            targetValue =  String.format("/ %s %s",new DecimalFormat("0.00").format((double)this.insertData/100), context.getString(resId));
        }
        return targetValue;
    }



    public boolean achieveTargetValue(){
        //achive target
        if(goal == eGoal.NOGOAL){
            return state == eState.STOP;
        }else {
            double targetValue = getTarget();
            double value = getValue();
            double result = Arith.sub(targetValue, value);
            return result < 0.0001f;
        }
    }


    public enum eType{
        RUN("run"),
        PUSHUP("push-up"),
        SITUP("sit-up"),
        BIKE("bike");

        private String name;
        private eType(String name){
            this.name = name;
        }

        public static eType valueOf(int i){
            eType[] types = new eType[]{RUN,PUSHUP,SITUP,BIKE};
            for(eType type : types){
                if(i == type.ordinal()){
                    return type;
                }
            }
            return  null;
        }

        public static Long toLong(eType type1){
            long value  =  type1.ordinal() + 100;
            return  value;
        }
    };

    public enum eGoal{
        DISTANCE("distance",0),
        TIME("time",1),
        COLARIES("colaries",2),
        QUANTITY("quantity",0),
        NOGOAL("nogoal",3);

        private String name;
        private int value;
        private eGoal(String name, int value){
            this.name = name;
            this.value = value;
        }

        public  int getValue(){
            return value;
        }

        public static eGoal valueOf(int i, eType type){
            eGoal goal = null;
            switch (i){
                case 0:
                    goal = DISTANCE;
                    break;
                case 1:
                    goal = TIME;
                    break;
                case 2:
                    goal = COLARIES;
                    break;
                case 3:
                    goal = NOGOAL;
                    break;
                default:
                    break;
            }
            //modify if pushup situp selected
            if(goal == DISTANCE){
                switch (type){
                    case PUSHUP:
                    case SITUP:
                        goal =  QUANTITY;
                        break;
                    default:
                        break;
                }
            }
            return  goal;
        }
    };

    public enum eUnit{
        DEFAULT("default",1f),
        KM("km",1000f), //in m
        MILES("miles",1609.344f); //in m
        public String name;
        public  double value;
        private eUnit(String name, double value){
            this.name = name;
            this.value = value;
        }

        public static eUnit valueOf(int pos,eGoal goal){
            eUnit unit = DEFAULT;
            switch(goal){
                case DISTANCE:
                    if(pos == 0){
                        return KM;
                    }else if(pos == 1){
                        return MILES;
                    }
                    break;
                default:
                    break;
            }
            return unit;
        }
    };
}
