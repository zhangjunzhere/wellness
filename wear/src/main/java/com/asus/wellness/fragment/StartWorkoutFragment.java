package com.asus.wellness.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.coach.CoachSetupActivity;
import com.asus.wellness.utils.CoachWorkoutHelper;
import com.asus.wellness.utils.Utility;

/**
 * 第三个Fragment，显示Tap to start new workout
 * Created by Kim_Bai on 5/14/2015.
 */
public class StartWorkoutFragment extends Fragment {

    /**
     * Create a new instance of CountingFragment, providing "num"
     * as an argument.
     */

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
        View v = inflater.inflate(R.layout.pni_main_fragment_start_workout, container, false);
        TextView tv_action = (TextView) v.findViewById(R.id.tv_action);
        Boolean hasFitnessSensor = CoachWorkoutHelper.hasFitnessSensor(getActivity());
        if(!hasFitnessSensor){

            tv_action.setText(getString(R.string.start_a_run));
        }
    //    int size = getResources().getDimensionPixelSize(R.dimen.tap_text_size);
    //    Utility.fitFontSizeForView(tv_action, size, Utility.getScreenWidth(getActivity()));
        v.findViewById(R.id.iv_start_workout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), CoachSetupActivity.class));
            }
        });
        return v;
    }
}
