package com.asus.wellness.notification;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.coach.setup.WorkoutControllerLayout;
import com.asus.wellness.notification.NotificaionHelper;
import com.asus.wellness.utils.EBCommand;

import de.greenrobot.event.EventBus;


public class NotificationWorkoutControllerActivity extends Activity {
    private String TAG = "StartWorkoutActivity";
    private WorkoutControllerLayout workoutControllerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pni_coach_notification_workout_controller);

        workoutControllerLayout = (WorkoutControllerLayout) findViewById(R.id.workout_controller);
//        workoutControllerLayout.setOnWorkoutStateistener(new ConfirmStopWorkoutDialog.OnDismissListener() {
//                                                             @Override
//                                                             public void onDismiss(Boolean confirmed) {
//                                  if (confirmed) {
//                                             NotificaionHelper.getInstance(getApplicationContext()).cancelCoachNotification();
////                                                                     NotificaionHelper.getInstance(getApplicationContext()).showSummery();
//                                         }
//                                     }
//                                 }
//        );

        EventBus.getDefault().register(this);

        updateUI();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }



    public void onEventMainThread(EBCommand ebCommand) {
        if (EBCommand.COMMAND_COACH_STATE_CHANGED.equals(ebCommand.command)) {
            updateUI();
        }
    }

    private void updateUI() {
        workoutControllerLayout.updateUI();
        TextView tap_to_start = (TextView) findViewById(R.id.tv_tap);
        TextView hold_to_end = (TextView) findViewById(R.id.tv_hold);

        CoachDataModel coachDataModel = WApplication.getInstance().getCoachDataModel();
        int visibility = View.VISIBLE;
        switch (coachDataModel.getState()) {
            case PLAY:
            case RESUME:
                tap_to_start.setText(R.string.tap_to_pause);
                break;
            case PAUSE:
                tap_to_start.setText(R.string.workout_start_action);
                break;
            case STOP:
            case FINISH:
            default:
                tap_to_start.setText(R.string.cancel_notification);
                visibility = View.INVISIBLE;
                break;
        }

        hold_to_end.setVisibility(visibility);
    }

}
