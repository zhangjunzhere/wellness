package com.asus.wellness.ui;



import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.utils.Utility;


public class TipsDialog extends DialogFragment {
    // TODO: Rename and change types of parameters

    private OnDismissListener mListener;
    private boolean mTipsDone = false;

    public TipsDialog() {
        // Required empty public constructor
    }

    public void setOnDismissListener(OnDismissListener listener){
        mListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view;
        if(Utility.isPadDevice(getActivity())){
            view = inflater.inflate(R.layout.tips_daily_layout_pad, container, false);
        }
        else {
            view = inflater.inflate(R.layout.tips_daily_layout, container, false);
            int  size = getResources().getDimensionPixelSize(R.dimen.tips_daily_p3_text_width);
            TextView tvnotice = (TextView) view.findViewById(R.id.tip3_notice_timeline);
            int fontsize = getResources().getDimensionPixelSize(R.dimen.tips_daily_content_textsize);
            Utility.fitFontSizeForView(tvnotice,fontsize,size);
        }
        final View rootView =  view;

        //emily++++
        Bitmap bmp;
        if(WApplication.getInstance().getConnectedDevice().getIsRobin()) {
            bmp = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.asus_wellness_bg_tips_p1));
        }
        else {
            bmp = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.asus_wellness_bg_tips_p1_2));
        }
        //emily----
        if (Utility.isPadDevice(getActivity())){
            View p1LeftTvs = (View) rootView.findViewById(R.id.tips_p1_l_tvs);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)getResources().getDimension(R.dimen.tips_daily_p1_text_width),
                    FrameLayout.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER;
            if(WApplication.getInstance().getConnectedDevice().getIsRobin()) {
                lp.setMargins((int) getResources().getDimension(R.dimen.tips_daily_p1_text_marginLeft),
                        (int) getResources().getDimension(R.dimen.tips_daily_p1_text_marginTop), 0, 0);
            }
            else{
                lp.setMargins((int) getResources().getDimension(R.dimen.tips_daily_p1_text_marginLeft_sparrow),
                        (int) getResources().getDimension(R.dimen.tips_daily_p1_text_marginTop), 0, 0);
            }
            p1LeftTvs.setLayoutParams(lp);

        }

        ((ImageView)rootView.findViewById(R.id.tips_p1_bg)).setImageBitmap(bmp);
        rootView.findViewById(R.id.tips_page1).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.tips_page2).setVisibility(View.GONE);
        rootView.findViewById(R.id.tips_page3).setVisibility(View.GONE);

        // phone has 3 tips, pad has 2 tips
        if(Utility.isPadDevice(getActivity())){
            rootView.findViewById(R.id.tips_p1_next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoTipsPage2_forPad(rootView);
                }
            });
        }
        else {
            rootView.findViewById(R.id.tips_p1_next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoTipsPage2(rootView);
                }
            });
        }
        return rootView;

    }

    private void gotoTipsPage2(final View rootView) {
        ((ImageView)rootView.findViewById(R.id.tips_p1_bg)).setImageBitmap(null);

        //emily++++
        Bitmap bmp;
        View p2LeftTvs = (View) rootView.findViewById(R.id.tips_p2_l_tvs);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((int)getResources().getDimension(R.dimen.tips_daily_p2_lefttext_width), FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        if(WApplication.getInstance().getConnectedDevice().getIsRobin()) {
            bmp = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.asus_wellness_bg_tips_p2));
            rootView.findViewById(R.id.tips_p2_r_tvs).setVisibility(View.VISIBLE);
            lp.setMargins((int) getResources().getDimension(R.dimen.tips_daily_p2_lefttext_marginLeft),
                    (int) getResources().getDimension(R.dimen.tips_daily_p2_lefttext_marginTop), 0, 0);

        }
        else {
            bmp = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.asus_wellness_bg_tips_p2_2));
            rootView.findViewById(R.id.tips_p2_r_tvs).setVisibility(View.GONE);
            lp.setMargins((int) getResources().getDimension(R.dimen.tips_daily_p2_lefttext_marginLeft_2),
                    (int) getResources().getDimension(R.dimen.tips_daily_p2_lefttext_marginTop), 0, 0);
        }
        p2LeftTvs.setLayoutParams(lp);
        //emily----

        ((ImageView)rootView.findViewById(R.id.tips_p2_bg)).setImageBitmap(bmp);
        rootView.findViewById(R.id.tips_page1).setVisibility(View.GONE);
        rootView.findViewById(R.id.tips_page2).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.tips_page3).setVisibility(View.GONE);
        rootView.findViewById(R.id.tips_p2_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoTipsPage3(rootView);
            }
        });
    }

    private void gotoTipsPage2_forPad(final View rootView) {
        ((ImageView)rootView.findViewById(R.id.tips_p1_bg)).setImageBitmap(null);

        Bitmap bmp;
        if(WApplication.getInstance().getConnectedDevice().getIsRobin()) {
            bmp = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.asus_wellness_bg_tips_p2));
        }
        else {
            bmp = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.asus_wellness_bg_tips_p2_2));
        }

        ((ImageView)rootView.findViewById(R.id.tips_p2_bg)).setImageBitmap(bmp);
        rootView.findViewById(R.id.tips_page1).setVisibility(View.GONE);
        rootView.findViewById(R.id.tips_page2).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.tips_page3).setVisibility(View.GONE);
        rootView.findViewById(R.id.tips_p2_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afterDailyTipsShowed();
                ((ImageView)rootView.findViewById(R.id.tips_p2_bg)).setImageBitmap(null);
                rootView.findViewById(R.id.tips_page2).setVisibility(View.GONE);
                mTipsDone = true;
                dismiss();
            }
        });
    }

    private void gotoTipsPage3(final View rootView) {
        ((ImageView)rootView.findViewById(R.id.tips_p2_bg)).setImageBitmap(null);

        //emily++++
        Bitmap bmp;
        View p3Tvs = (View) rootView.findViewById(R.id.tips_p3_tvs);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = (Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        if(!WApplication.getInstance().getConnectedDevice().getIsRobin()) {
            bmp = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.asus_wellness_bg_tips_p3_2));
            lp.setMargins((int) getResources().getDimension(R.dimen.tips_daily_p3_text_marginLeft),0,0,
                    (int) getResources().getDimension(R.dimen.tips_daily_p3_text_marginBottom_2));
        }
        else {
            bmp = BitmapFactory.decodeStream(getResources().openRawResource(R.drawable.asus_wellness_bg_tips_p3));
            lp.setMargins((int) getResources().getDimension(R.dimen.tips_daily_p3_text_marginLeft),0,0,
                    (int) getResources().getDimension(R.dimen.tips_daily_p3_text_marginBottom));
        }
        p3Tvs.setLayoutParams(lp);
        //emily----

        ((ImageView)rootView.findViewById(R.id.tips_p3_bg)).setImageBitmap(bmp);
        rootView.findViewById(R.id.tips_page1).setVisibility(View.GONE);
        rootView.findViewById(R.id.tips_page2).setVisibility(View.GONE);
        rootView.findViewById(R.id.tips_page3).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.tips_p3_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                afterDailyTipsShowed();
                ((ImageView)rootView.findViewById(R.id.tips_p3_bg)).setImageBitmap(null);
                rootView.findViewById(R.id.tips_page3).setVisibility(View.GONE);
                mTipsDone = true;

                dismiss();
            }
        });
    }
    private void afterDailyTipsShowed() {
        SharedPreferences sp = getActivity().getSharedPreferences(MainWellness.PREFERENCE_PRIVATE, 0);
        sp.edit().putBoolean(getString(R.string.pref_key_tips_daily_showed), true).commit();
    }

    @Override
    public void onDismiss(android.content.DialogInterface dialog){
        if(mListener != null){
            mListener.onDismiss(mTipsDone);
        }
    }

    public interface OnDismissListener{
        public void onDismiss(Boolean tipsDone);
    }
}
