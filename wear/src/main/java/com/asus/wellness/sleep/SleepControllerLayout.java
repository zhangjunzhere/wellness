package com.asus.wellness.sleep;

import android.content.Context;
import android.graphics.PointF;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.asus.wellness.R;


import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.coach.setup.WorkoutControllerArcView;
import com.asus.wellness.service.CollectStepCountService;
import com.asus.wellness.utils.EBCommand;

import de.greenrobot.event.EventBus;

/**
 * Created by jz on 2015/5/28.
 */
public class SleepControllerLayout extends RelativeLayout implements View.OnTouchListener{
    public static  final String TAG ="WorkoutControllerLayout";
    private ImageView mPlayButton;
    private TextView mCountView;

    private CountDownTimer mCountdownTimer;
    private  long TotalSpan = 2000L;
    private final long ElapseSpan = 10L;
    private long mTimeElpased = 0L;

    private WorkoutControllerArcView mProgressView;

    public  enum eState{BACK,FORWORD};
    private eState state;

    public void setState(eState state){
        this.state = state;
        mTimeElpased = 0L;
        mProgressView.setProgress(0);

        mCountdownTimer.cancel();
        if(state == eState.BACK) {
            mPlayButton.setImageResource(R.drawable.pni_asus_wellness_ic_b_stop);
            mProgressView.setProgress(0);
        }else  if(SleepDataModel.getInstance().getSleepEnabled()) {
            mPlayButton.setImageResource(R.drawable.pni_asus_wellness_ic_b_stop);
            mCountdownTimer.start();
        }
    }

    public SleepControllerLayout(Context context) {
        this(context, null);
    }
    public SleepControllerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SleepControllerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.pni_coach_workout_contoller_layout, this, true);
        mPlayButton = (ImageView) rootView.findViewById(R.id.iv_play);
        mPlayButton.setOnTouchListener(this);

        mCountdownTimer = new CountDownTimer(TotalSpan, ElapseSpan) {
            @Override
            public void onTick(long millisUntilFinished) {
                onTimerTick(millisUntilFinished);
            }
            @Override
            public void onFinish() {
                finishCountTimer();
            }
        };

        mProgressView = (WorkoutControllerArcView) rootView.findViewById(R.id.iv_progress);
        mCountView = (TextView) rootView.findViewById(R.id.tv_countdown);
        mPlayButton.setImageResource(R.drawable.pni_asus_wellness_ic_b_stop);
        mProgressView.setProgress(0);
    }
    @Override
    protected  void onLayout(boolean changed, int l, int t, int r, int b){
        super.onLayout(changed, l, t, r, b);
        int left = ((r-l) - mProgressView.getMeasuredWidth())/2;
        int top  = ((b-t) - mProgressView.getMeasuredHeight())/2;
        int right = left + mProgressView.getMeasuredWidth();
        int bottom = top + mProgressView.getMeasuredHeight();
        mProgressView.layout( left, top,right,bottom);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setState(eState.FORWORD);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                setState(eState.BACK);
                break;
            default:
                break;
        }
        return true;
    }

    private void onTimerTick(long millisUntilFinished) {
        mTimeElpased = TotalSpan - millisUntilFinished;

        float degree = (float)mTimeElpased/TotalSpan;
        //int resId = R.drawable.pni_asus_wellness_ic_b_stop;

        switch(state){
            case BACK:
                //resId =  R.drawable.pni_asus_wellness_ic_b_play_bg;
                degree = -degree;
                mCountView.setVisibility(View.VISIBLE);
                mCountView.setText(String.valueOf((millisUntilFinished)/1000  + 1));
                break;
            case FORWORD:
                mCountView.setVisibility(View.GONE);
                break;
        }

        //mPlayButton.setImageResource(resId);
        mProgressView.setProgress(degree);
    }

    private void finishCountTimer() {
        mProgressView.setProgress(1.0f);
        mTimeElpased = 0;
        EBCommand ebCommand = null;
        switch(state){
            case BACK:
                mCountView.setText("1");
                SleepDataModel.getInstance().setSleepStatus(SleepDataModel.eSleep.START);
                ebCommand = new EBCommand(this.getClass().getName(), SleepActivity.class.getName(),EBCommand.COMMAND_SLEEP_STATE, null);
                break;
            case FORWORD: {
                //Context context = getContext();
                //Toast.makeText(getContext(), context.getString(R.string.toast_sleep_done), Toast.LENGTH_LONG).show();;
                SleepDataModel.getInstance().setSleepStatus(SleepDataModel.eSleep.FINISH);
                ebCommand = new EBCommand(this.getClass().getName(), CollectStepCountService.class.getName(), EBCommand.COMMAND_START_SLEEP, false);
            }
                break;
        }

        EventBus.getDefault().post(ebCommand);
    }
}
