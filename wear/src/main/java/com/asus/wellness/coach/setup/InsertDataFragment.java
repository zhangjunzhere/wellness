package com.asus.wellness.coach.setup;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asus.wellness.R;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.utils.CoachWorkoutHelper;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.Utility;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Kim_Bai on 5/14/2015.
 * 1.distance  01.00
 * 2.calories:  0400
 * 3. time:     00h30m
 * 4. quantity 0030
 */
public class InsertDataFragment extends SingleListFragment {

    protected List<Integer> mValues = new ArrayList<Integer>();
    private int defaultPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  super.onCreateView(inflater, container, savedInstanceState);
        mWearableListView.scrollToPosition(defaultPosition);

        return rootView;
    }

    @Override
    protected WearableListViewAdapter.OnScrollListener getOnScrollListener() {
        return new WearableListViewAdapter.OnScrollListener() {
            @Override
            public void onCentralPositionChanged(int i) {
                Log.d("ChooseGoalFragment", "onCentralPositionChanged position:" + i);
                mCoachDataModel.setTarget(mValues.get(i));
                EBCommand ebCommand = new EBCommand(this.getClass().getName(), WorkoutActionFragment.class.getName(), EBCommand.COMMAND_COACH_TARGET_CHANGED,mCoachDataModel.getTargetString(getActivity()));
                EventBus.getDefault().post(ebCommand);
            }
        };
    }

    @Override
    protected List<String> getModelArray() {
        CoachWorkoutHelper.InsertDataModel insertDataModel = CoachWorkoutHelper.getInsertDataModel(mCoachDataModel.getGoal());
        mModelArray = insertDataModel.stringArray;
        mValues = insertDataModel.valueArray;
        defaultPosition = insertDataModel.defaultPosition;
        return mModelArray;
    }



    @Override
    protected String getPageTitle() {
         switch (mCoachDataModel.getGoal()) {
            case DISTANCE: {//max 99.99km
                Profile profile = ProfileHelper.getStandardProfile();
                String unit = getString(R.string.distance_unit);;
                switch(profile.getDistance_unit() ){
                    case ProfileTable.DISTANTCE_UNIT_MILES:
                        unit = getString(R.string.miles).toLowerCase();
                        mCoachDataModel.setUnit(CoachDataModel.eUnit.MILES);
                        break;
                    case ProfileTable.DISTANTCE_UNIT_KM:
                        mCoachDataModel.setUnit(CoachDataModel.eUnit.KM);
                        break;
                }
                return String.format(getString(R.string.insert_distance),unit);
            }
            case COLARIES: //max 9999cal max
                return String.format(getString(R.string.insert_calories) ,getString(R.string.calories_unit));
            case TIME: // max 99:59:00
                return String.format(getString(R.string.insert_time ),getString(R.string.hr_mitute_sec));
            case QUANTITY:  //max 999
                return getString(R.string.insert_quantity);
            default:
                break;
        }
        return "should not here";
    }

    @Override
    protected Bundle createAdapterArgs() {
        Bundle textSizes = new Bundle();
        textSizes.putInt(WearableListItem.KEY_MAIN_STYLE_NORMAL, R.style.coach_list_view_text);
        textSizes.putInt(WearableListItem.KEY_MAIN_STYLE_SELECTED, R.style.coach_list_view_text_selected);

        if (mCoachDataModel.getGoal() == CoachDataModel.eGoal.TIME) {
            textSizes.putBoolean(WearableListItem.KEY_SPANNABLE_STRING, true);
        }

        return textSizes;
    }

    @Override
    public void onEventMainThread(EBCommand cmdMsg) {
        if (cmdMsg.receiver.equals(getClass().getName())) {
            // Display the text we just generated within the LogView.
            mWearableListViewAdapter.setContent(getModelArray(), createAdapterArgs());
            mWearableListViewAdapter.notifyDataSetChanged();
            mWearableListView.scrollToPosition(defaultPosition);
            mPageTitle.setText(getPageTitle());
            int size = getResources().getDimensionPixelSize(R.dimen.page_title_text_size);
         //   Utility.fitFontSizeForView(mPageTitle, size, Utility.getScreenWidth(getActivity()));
           // Utility.adjustTextSize(mPageTitle, getResources().getDimension(R.dimen.page_title_text_size), getResources().getDimension(R.dimen.margin_left));
        }
    }
}




