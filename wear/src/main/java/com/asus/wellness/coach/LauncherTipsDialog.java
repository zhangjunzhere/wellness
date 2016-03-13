package com.asus.wellness.coach;


import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asus.wellness.R;


public class LauncherTipsDialog extends DialogFragment  {
    // TODO: Rename parameter arguments, choose names that match

    private String mTips;
    private boolean mConfirmed = false;
    private boolean mSplash = false;
    private View mRootView;

    public LauncherTipsDialog(){}
    public void setTips(String tips){ mTips = tips; }
    public void setSplash(Boolean splash){ mSplash = splash; }

    private ConfirmStopWorkoutDialog.OnDismissListener  mListener;
    public void setOnDismissListener(ConfirmStopWorkoutDialog.OnDismissListener listener){
        mListener = listener;
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
        mRootView  = inflater.inflate(R.layout.launcher, container, false);
        TextView tv_tips = (TextView)mRootView.findViewById(R.id.tv_tips);
        tv_tips.setText(mTips);

        View btn_ok = mRootView.findViewById(R.id.btn_ok);

        if(mSplash) {
            btn_ok.setVisibility(View.GONE);
            new CountDownTimer(3000, 100) {
                @Override
                public void onTick(long millisUntilFinished) {
                }
                @Override
                public void onFinish() {
                    dismiss();
                }
            }.start();
        }else {
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (mListener != null) {
                        mListener.onDismiss(true);
                    }
                }
            });
        }
        return mRootView;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mListener != null){
            mListener.onDismiss(mConfirmed);
        }
    }
}
