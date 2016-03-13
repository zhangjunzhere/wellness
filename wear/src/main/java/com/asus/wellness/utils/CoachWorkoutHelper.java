package com.asus.wellness.utils;

import android.content.Context;
import android.hardware.SensorManager;

import com.asus.wellness.StepCountManager;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.coach.setup.InsertDataFragment;
import com.asus.wellness.coach.setup.WorkoutActionFragment;
import com.asus.wellness.coach.setup.ChooseGoalFragment;
import com.asus.wellness.coach.setup.ChooseTypeFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jz on 2015/7/15.
 */
public class CoachWorkoutHelper {

    public static class InsertDataModel {
        public List<String> stringArray;
        public List<Integer> valueArray;
        public int defaultPosition = 0;
    }

    private static InsertDataModel mDistanceModel = null;
    private static InsertDataModel mCaloriesModel = null;
    private static InsertDataModel mTimeModel = null;
    private static InsertDataModel mQuantityModel = null;

    public static InsertDataModel getInsertDataModel(CoachDataModel.eGoal goal) {
        switch (goal) {
            case DISTANCE: //max 99.99km
                return generateInsertData(mDistanceModel,goal);
            case COLARIES: //max 9999cal max
                return generateInsertData(mCaloriesModel,goal);
            case TIME: // max 99:59:00
                return generateInsertData(mTimeModel,goal);
            case QUANTITY:  //max 999
                return generateInsertData(mQuantityModel,goal);
            default:
                break;
        }

        return null;
    }

    private static InsertDataModel generateInsertData(InsertDataModel data, CoachDataModel.eGoal goal) {
        if (data == null) {
            data = new InsertDataModel();
            data.stringArray = new ArrayList<String>();
            data.valueArray = new ArrayList<Integer>();

            switch (goal) {
                case DISTANCE: //max 99.99km,default 0.1km/miles
                    for (int i = 5; i < 10000; i+=5) {
                        data.stringArray.add(String.format("%d.%02d", i / 100, i % 100));
                        data.valueArray.add(i);
                    }
                    data.defaultPosition = 1;
                    break;
                case COLARIES: //max 9999cal max,default: 400kcal
                    for (int i = 25; i < 10000; i += 25) {
                        data.stringArray.add(String.format("%d", i));
                        data.valueArray.add(i);
                    }
                    data.defaultPosition = 15;
                    break;
                case TIME: // max 99:59:00,default 00:30
//                    for (int i = 0; i < 8; i++) {
//                        for (int j = 1; j < 60; j++) {
//                            data.valueArray.add(i * 60 + j);
//                            data.stringArray.add(String.format("%02d:%02d:00", i, j));
//                        }
//                    }

                    for (int i = 1; i < 361; i++) {
                        data.valueArray.add(i);
                        data.stringArray.add(String.valueOf(i));
                    }
                    data.defaultPosition = 29;
                    break;
                case QUANTITY:  //max 999,default 30
                    for (int i = 1; i < 100; i++) {
                        data.valueArray.add(i);
                        data.stringArray.add(String.format("%d", i));
                    }
                    data.defaultPosition = 29;
                    break;
                default:
                    break;
            }
        }
        return data;
    }

    public static List<Class<?>> getFragmentClazzByGoal(CoachDataModel.eGoal goal, boolean voiceRunning,Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        boolean hasFitnessSensor = (sensorManager.getDefaultSensor(StepCountManager.PNI_TYPE_FITNESS_ACTIVITY) != null);

        List<Class<?>> fragmentListAll = new ArrayList<Class<?>>();
        if(!voiceRunning && hasFitnessSensor ) {
            fragmentListAll.add(ChooseTypeFragment.class);
        }
        fragmentListAll.add(ChooseGoalFragment.class);
        switch (goal) {
            case DISTANCE:
            case QUANTITY:
            case COLARIES:
            case TIME:
                fragmentListAll.add(InsertDataFragment.class);
                break;
            case NOGOAL:
            default:
                break;
        }

        fragmentListAll.add(WorkoutActionFragment.class);
        return fragmentListAll;
    }

    public static boolean hasFitnessSensor(Context context){
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        boolean hasFitnessSensor = (sensorManager.getDefaultSensor(StepCountManager.PNI_TYPE_FITNESS_ACTIVITY) != null);
        return hasFitnessSensor;
    }

}
