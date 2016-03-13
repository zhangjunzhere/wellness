package com.asus.wellness.Profile;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.StartActivity;
import com.asus.wellness.R;
import com.asus.wellness.WellnessMicroAppMain;
import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2015/5/22.
 */
public class SkipProfileDialog extends DialogFragment {
    Button yesskipBtn;
    Button cancelBtn;
    boolean isFirst =false;
    public void setFirstUse(boolean first)
    {
        isFirst = first;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.pni_profile_activity_skip_profile_setup, container, false);

        yesskipBtn = (Button)rootView.findViewById(R.id.yesskipbtn);
        yesskipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileModel p = new ProfileModel();
                p.reset();
                if(isFirst)
                  goMainActivity();
                getActivity().finish();
            }
        });
        cancelBtn = (Button)rootView.findViewById(R.id.cancelbtn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        TextView mTitleTv = (TextView) view.findViewById(R.id.title);
//        int width = Utility.getScreenWidth(getActivity());
//       // mTitleTv.setText(title);  //getResources().getString(R.string.heightunit)
//        Utility.fitFontSizeForView(mTitleTv, 0, width);
    }

    /**
     * lunch main activity
     */
    void goMainActivity()
    {
        Intent i = new Intent(getActivity(), WellnessMicroAppMain.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK );
        startActivity(i);
    }
}
