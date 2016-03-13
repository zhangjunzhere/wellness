package com.asus.wellness.coach.setup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.asus.wellness.R;
import com.asus.wellness.WApplication;
import com.asus.wellness.coach.CoachDataModel;
import com.asus.wellness.coach.ConfirmStopWorkoutDialog;
import com.asus.wellness.coach.SummeryWorkoutActivity;
import com.asus.wellness.coach.WorkoutDataService;
import com.asus.wellness.notification.NotificaionHelper;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.EBCommandUtils;

import de.greenrobot.event.EventBus;

/**
 * Created by jz on 2015/5/28.
 */
public class WorkoutControllerLayout extends RelativeLayout implements View.OnTouchListener{
    public static  final String TAG ="WorkoutControllerLayout";
    private ImageView mPlayButton;
    private CoachDataModel mCoachDataModel;

    private int mImagePlay;
    private int mImagePause;
    private int mImageStop;

    private CountDownTimer mCountDownTimer;
    private final long TotalSpan = 2000L;
    private final long ElapseSpan = 10L;
    private long mTimeElpased = 0L;

    private PointF mDownPoint = new PointF();
    private PointF mUpPoint = new PointF();


    WorkoutControllerArcView mProgressView;

    public WorkoutControllerLayout(Context context) {
        this(context, null);
    }
    public WorkoutControllerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorkoutControllerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mCoachDataModel = WApplication.getInstance().getCoachDataModel();
        mImagePlay = R.drawable.pni_asus_wellness_ic_b_play;
        mImagePause = R.drawable.pni_asus_wellness_ic_b_pause;
        mImageStop = R.drawable.pni_asus_wellness_ic_b_stop;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.pni_coach_workout_contoller_layout, this, true);
        mPlayButton = (ImageView) rootView.findViewById(R.id.iv_play);
        mPlayButton.setOnTouchListener(this);
        mProgressView = (WorkoutControllerArcView) rootView.findViewById(R.id.iv_progress);

        mCountDownTimer = new CountDownTimer(TotalSpan, ElapseSpan) {
            @Override
            public void onTick(long millisUntilFinished) {
                onTimerTick(millisUntilFinished);
            }
            @Override
            public void onFinish() {
                finishCountTimer();
            }
        };

        updateUI();
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

//    public  void setOnWorkoutStateistener(ConfirmStopWorkoutDialog.OnDismissListener listener){
//        mOnDismissListener = listener;
//    }
    //DialogInterface.OnDismissListener
//    @Override
    public void onDismiss() {
       if(mCoachDataModel.getState() != CoachDataModel.eState.STOP){
           mCoachDataModel.setState(CoachDataModel.eState.STOP);
           EBCommand ebCommand = new EBCommand(this.getClass().getName(), WorkoutDataService.class.getName(),EBCommand.COMMAND_COACH_STATE_CHANGED,null);
           EventBus.getDefault().post(ebCommand);
       }
    }

    public void updateUI(){
        int drawable = mImagePlay;
        switch (mCoachDataModel.getState()){
            case PLAY:
            case RESUME:
                drawable = mImagePause ;
                break;
            case PAUSE:
                drawable = mImagePlay ;
                break;
            case STOP:
            case FINISH:
                drawable = mImageStop;
                break;
            default:
                break;
        }
        mPlayButton.setImageResource(drawable);
    }

    private void onStateChanged(){
        switch (mCoachDataModel.getState()){
//            case START:
//                mCoachDataModel.setState(CoachDataModel.eState.PLAY);
//                break;
            case PAUSE:
                mCoachDataModel.setState(CoachDataModel.eState.PLAY);
                break;
            case PLAY:
            case RESUME:
                mCoachDataModel.setState(CoachDataModel.eState.PAUSE);
                break;
//            case STOP:
//                mCoachDataModel.setState(CoachDataModel.eState.PAUSE);
            default:
                break;
        }

        updateUI();

        EBCommand cmdMsg = new EBCommand(this.getClass().getName(), WorkoutDataService.class.getName(), EBCommand.COMMAND_COACH_STATE_CHANGED,mCoachDataModel.getState().toString() );
        EventBus.getDefault().post(cmdMsg);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(mCoachDataModel.getState() == CoachDataModel.eState.STOP || mCoachDataModel.getState() == CoachDataModel.eState.FINISH ){
//                    NotificaionHelper.getInstance(getContext()).cancelCoachNotification();
//                    NotificaionHelper.getInstance(getContext()).showSummery();
                    EBCommandUtils.showCoachNotification(this.getClass().getName(),false);
                }else {
                    startCountTimer();
                    mDownPoint.set(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mUpPoint.set(event.getX(), event.getY());
                stopCountTimer();
                break;
            default:
                break;
        }
        return true;
    }

    private void onTimerTick(long millisUntilFinished) {
        mTimeElpased = TotalSpan - millisUntilFinished;
        if((millisUntilFinished <= mTimeElpased )  ){
            mPlayButton.setImageResource(mImageStop);
        }
        mProgressView.setProgress((float)mTimeElpased/TotalSpan);
    }
    private void startCountTimer() {
        mTimeElpased = 0;
        mCountDownTimer.start();
    }
    private void stopCountTimer() {
        mCountDownTimer.cancel();
        float distance = Math.abs(mUpPoint.x - mDownPoint.x) + Math.abs(mUpPoint.y - mDownPoint.y);

        //avoid touch not intently
        if(mTimeElpased < TotalSpan && distance < 10   && mCoachDataModel.getState() != CoachDataModel.eState.STOP){
            onStateChanged();
        }
        mProgressView.setProgress(0);
        mTimeElpased = 0;
        updateUI();
    }
    private void finishCountTimer() {
        mProgressView.setProgress(1.0f);
        mTimeElpased = 0;
        onDismiss();

//        ConfirmStopWorkoutDialog dialog = new ConfirmStopWorkoutDialog();
//        dialog.addOnDismissListener(WorkoutControllerLayout.this);
//
//        Activity activity = (Activity)getContext();
//        dialog.show(activity.getFragmentManager(),ConfirmStopWorkoutDialog.class.getName());
        //TO DO toast

        //Context context =  getContext();
        //Toast.makeText(getContext(), context.getString(R.string.toast_workout_done), Toast.LENGTH_LONG).show();

    }
}
