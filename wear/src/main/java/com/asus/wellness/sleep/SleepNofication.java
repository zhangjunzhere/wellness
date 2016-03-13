package com.asus.wellness.sleep;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.asus.wellness.R;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.utils.EBCommand;

import de.greenrobot.event.EventBus;

public class SleepNofication extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pni_sleep_notification);

        findViewById(R.id.ll_sleep_recording).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SleepDataModel sleepDataModel   = SleepDataModel.getInstance();
//                sleepDataModel.setSleepStatus(SleepDataModel.eSleep.START);
                EBCommand ebCommand = new EBCommand(SleepActivity.class.getName(), WorkoutDataService.class.getName(), EBCommand.COMMAND_START_ACTIVITY, SleepActivity.class.getName());
                EventBus.getDefault().post(ebCommand);

            }
        });


    }


}
