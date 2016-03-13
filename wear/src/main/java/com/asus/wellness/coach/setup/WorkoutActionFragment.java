package com.asus.wellness.coach.setup;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.CoachSetupActivity;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.coach.StartWorkoutActivity;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.EBCommandUtils;
import com.asus.wellness.utils.Utility;

import de.greenrobot.event.EventBus;

/**
 * Created by Kim_Bai on 5/14/2015.
 */
public class WorkoutActionFragment extends AbsWorkoutFragment {

    private View mRootView;
    private CountDownTimer timer ;


    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.pni_coach_fragment_workout_action, container, false);
        final RelativeLayout rl_action = (RelativeLayout)mRootView.findViewById(R.id.rl_action);
        final ImageView iv_countdown_bg = (ImageView)mRootView.findViewById(R.id.iv_countdown_bg);
        final TextView tv_count = (TextView)mRootView.findViewById(R.id.tv_count);

        timer =  new CountDownTimer(2000,100){
            @Override
            public void onTick(long millisUntilFinished) {
                tv_count.setText(String.valueOf((millisUntilFinished) / 1000 + 2));
            }
            @Override
            public void onFinish() {
                tv_count.setText("1");
                mCoachDataModel.setState(CoachDataModel.eState.START);
                EBCommandUtils.changeCoachState(this.getClass().getName());
                Utility.startSingleActivity(getActivity(), StartWorkoutActivity.class);
                getActivity().finish();
            }
        };

        setPageTitle();
        rl_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //disable viewpage scroll
                ((CoachSetupActivity) getActivity()).enablePageScroll(false);
                rl_action.setEnabled(false);

                mRootView.findViewById(R.id.iv_shadow).setVisibility(View.GONE);
                mRootView.findViewById(R.id.iv_play).setVisibility(View.GONE);
                mRootView.findViewById(R.id.tv_start).setVisibility(View.INVISIBLE);

                iv_countdown_bg.setVisibility(View.VISIBLE);
                tv_count.setVisibility(View.VISIBLE);

                getActivity().startService(new Intent(getActivity(),WorkoutDataService.class));
                timer.start();
            }
        });

        return mRootView;
    }


    @Override
    public void onEventMainThread(EBCommand command){
        if(this.getClass().getName().equals(command.receiver)){
            setPageTitle();
        }
    }

    private void setPageTitle(){
        CoachDataModel coachDataModel = WApplication.getInstance().getCoachDataModel();
        TextView tv_target_value = (TextView)mRootView.findViewById(R.id.tv_target_value);
        String targetValue = mCoachDataModel.getGoal().equals(CoachDataModel.eGoal.NOGOAL) ? getActivity().getString(R.string.go) : coachDataModel.getTargetString(getActivity());
        tv_target_value.setText(targetValue);
        TextView tv_target_unit = (TextView)mRootView.findViewById(R.id.tv_target_unit);
        tv_target_unit.setText(coachDataModel.getTargetUnitString(getActivity()));
        if(coachDataModel.getTargetUnitString(getActivity())== null || coachDataModel.getTargetUnitString(getActivity()).equals("")){
            tv_target_unit.setVisibility(View.GONE);
        } else {
            tv_target_unit.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        timer.cancel();
    }
  }



