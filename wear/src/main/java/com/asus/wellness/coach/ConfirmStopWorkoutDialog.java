package com.asus.wellness.coach;


import android.app.DialogFragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asus.wellness.R;


public class ConfirmStopWorkoutDialog extends DialogFragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match

    private Boolean mConfirmed = false;
    private OnDismissListener  mListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View rootView = inflater.inflate(R.layout.pni_fragment_confirm_stop_workout_dialog, container, false);

        rootView.findViewById(R.id.btn_ok).setOnClickListener(this);
        rootView.findViewById(R.id.btn_cancel).setOnClickListener(this);

        return rootView;
    }


    @Override
    public void onClick(View v) {
           if(v.getId() == R.id.btn_ok){
               mConfirmed = true;
           }
           this.dismiss();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mListener != null){
            mListener.onDismiss(mConfirmed);
        }
    }

    public void addOnDismissListener(OnDismissListener listener){
        mListener = listener;
    }

    public static interface OnDismissListener{
        public void onDismiss(Boolean confirmed);
    }


}
