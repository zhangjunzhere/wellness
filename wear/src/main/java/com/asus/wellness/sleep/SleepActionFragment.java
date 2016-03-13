package com.asus.wellness.sleep;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.Utility;


import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/8/27.
 */
public class SleepActionFragment extends Fragment implements View.OnClickListener{
    ImageView iv_sleep;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.pni_sleep_fragment_action,container,false);
        iv_sleep = (ImageView)v.findViewById(R.id.iv_sleep);
        iv_sleep.setOnClickListener(this);

        TextView txtView =(TextView)v.findViewById(R.id.lb_sleep);
//        int size = getResources().getDimensionPixelSize(R.dimen.tap_text_size);
//        Utility.fitFontSizeForView(txtView, size, Utility.getScreenWidth(getActivity()));
        return  v;
    }

    @Override
    public void onClick(View v) {
        SleepDataModel sleepDataModel   = SleepDataModel.getInstance();
        sleepDataModel.setSleepStatus(SleepDataModel.eSleep.START);
        Utility.startSingleActivity(getActivity(), SleepActivity.class);

        EBCommand ebCommand = new EBCommand(this.getClass().getName(), CollectStepCountService.class.getName(), EBCommand.COMMAND_START_SLEEP, true);
        EventBus.getDefault().post(ebCommand);
        getActivity().finish();
    }

}
