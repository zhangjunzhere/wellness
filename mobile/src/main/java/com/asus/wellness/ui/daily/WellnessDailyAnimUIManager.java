package com.asus.wellness.ui.daily;

import com.asus.wellness.R;
import com.asus.wellness.R.string;
import com.asus.wellness.WApplication;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.utils.Utility.SIZEDIMENS;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class WellnessDailyAnimUIManager implements OnTouchListener, OnGestureListener {
    private final String TAG = "WellnessDailyAnimUIManager";

    private GestureDetector mGestureDetector;
    private ViewGroup mainLayout;

    //emily++++
    private ViewGroup mBodyLayout;
    private ViewGroup mCalendarLayout;
    private ViewGroup mMindSleepLayout;
    //emily----

    private int m_leftGroupNum = 0; //amount of L_leafs
    private int m_rightGroupNum = 0; // amount of R_leafs
    private int m_calendarGroupNum = 0; //amount of calendar_txt
    private int m_caldotGroupNum = 0;
    private List<Double> m_leftV_Ratio = new ArrayList<Double>(); //speed ration of L_leafs
    private List<ViewGroup> m_leftGroup = new ArrayList<ViewGroup>(); //left group
    private List<ViewGroup> m_L_leaveGroup = new ArrayList<ViewGroup>();//left leafs
    private List<ViewGroup> m_L_circleGroup = new ArrayList<ViewGroup>();//left circles

    private List<Double> m_rightV_Ratio = new ArrayList<Double>(); //speed of ratio of R_leafs
    private List<ViewGroup> m_rightGroup = new ArrayList<ViewGroup>(); //right group
    private List<ViewGroup> m_R_leaveGroup = new ArrayList<ViewGroup>(); //right leafs
    private List<ViewGroup> m_R_circleGroup = new ArrayList<ViewGroup>(); // right circles

    private List<PointF> m_rightGroupPos = new ArrayList<PointF>(); //position of right circle(absolute in screen)
    private List<PointF> m_leftGroupPos = new ArrayList<PointF>(); //position of left circle(absolute in screen)
    private List<PointF> m_calGroupPos = new ArrayList<PointF>(); //position of calendar textfield(absolute in screen)
    private List<PointF> m_caldotGroupPos = new ArrayList<PointF>();

    private List<View> m_calList=new ArrayList<View>(); //calendar group
    private List<View> m_caldotList = new ArrayList<View>();

    private View m_L_largeView; // left aim circle
    private View m_R_largeView; // right aim circle
    private float m_baseNumber=0; //basement of moving distance
    private float m_rubNumber =0.6f; //the rub of scroll
    private float m_rubNumber_auto=0.925f; //the rub of slowdown animation in autoback() of autoForward()
    private float m_flingSpeed_rub=0.015f; //the rub of slowdown animation in fling_animation()
    private boolean m_isRunning=false; // animation is running  or not
    private boolean m_isScroll=false; //is scrolling or not
    private List<Double> m_calRatio=new ArrayList<Double>(); //calendar moving speed ratio
    private List<Double> m_caldotRatio = new ArrayList<Double>();
    private float m_speedV=0; //animation speed
    private float m_autoSpeed=0; //dynamic animation speed
    private boolean m_isLimit=false; //back moving want to stop or not
    private boolean m_isAutoing=false; //is auto moving or not
    public  static long oneDay=1000*60*60*24;
    private long m_FPS=30;
    private float m_limitRub_Speed=2f; //when scrolling to limited position,the speed will change to slow speed
    private boolean m_isLimitRubStatus_forward=false; //arriving the  today
    private boolean m_isLimitRubStatus_previous=false; //arriving the first using day
    private float m_fadeOutTime=1.5f; //opening animation fadeout duration
    private float m_flingIgnore=200; //ignore fling min-limited speed
    private float m_AutoForwardSpeed=70; //autoforward animation speed
    private float m_AutoBackSpeed=25;//autoback animation speed
    private int m_backSpeed=100;// the speed of counting autoback animation
    private float m_ori_scale=1; //the scale ration in different dimensions
    private boolean isPadLayout = false;

    private enum MONTH {
        JAN(1, "JAN"), FEB(2, "FEB"), MAR(3, "MAR"), APR(4, "APR"), MAY(5, "MAY"), JUN(6, "JUN"),
        JUL(7, "JUL"), AUG(8, "AUG"), SEP(9, "SEP"), OCT(10, "OCT"), NOV(11, "NOV"), DEC(12, "DEC");

        private int m_index;
        private String m_name;

        MONTH(int idx, String _name) {
            this.m_index = idx;
            this.m_name = _name;
        }

        public int getIndex() {
            return m_index;
        }

        public String getName() {
            return m_name;
        }
    }

    private enum WEEK {
        MON(1, "MON"), TUE(2, "TUE"), WED(3, "WED"), THU(4, "THU"), FRI(5, "FRI"), SAT(6, "SAT"), SUN(7, "SUN");

        private int m_index;
        private String m_name;

        WEEK(int idx, String _name) {
            this.m_index = idx;
            this.m_name = _name;
        }

        public int getIndex() {
            return m_index;
        }

        public String getName() {
            return m_name;
        }

    }

    private enum DIRECTION {
        UP, DOWN, LEFT, RIGHT
    }

    public enum MOVING_TYPE {
        ORIGINAL_POS, FORWARD_END, BACK_END, FORWARD_START, BACK_START
    }

    private DIRECTION m_curDirection; //the finger moving direction
    private MOVING_TYPE m_curMoving_type = MOVING_TYPE.ORIGINAL_POS; //the current aniamtion moving type
    private HandlerThread m_hrHandlerThread; //the thread of handling animation
    private Handler m_handler; //the handler of handling animation
    private Activity m_context;
    private EventNotify m_eEventNotify; //the notification to the register
    private long m_curDate; //currrent date date in animation
    private Calendar m_calendar;
    private DisplayMetrics m_metrics;
    private float m_backrubNumber=0.86f; //autoback or autoforward animation rub
    private float m_limitSpeed=15f; //fling min speed
    private float m_back_limitSpeed=5f; //autoanimation  min speed
    private float m_limitRubAnimation_forward=0.75f; //the distance in the edge to start to reduce speed (up gesture)
    private float m_limitRubAnimation_previous=0.01f; //the distance in the edge to start to reduce speed (down gesture)
    private long m_firstUseTime;
    private int m_firstDay;
    private int m_firstMonth;
    private int m_firstYear;
    private boolean m_isBackStart=false;
    private SIZEDIMENS m_Dimens;
    private Runnable m_AutoRSDRunable; //manager auto animation how to slowdown
    private Runnable m_FlingRSDRunable; //manager fling animation how to slowdown
    private Runnable m_ForwardRunable; //manager forward animation
    private Runnable m_BackRunable; //manager back animation
    private Runnable m_BackCountRunable; //manager counting back animation
    private Runnable m_FlingRunable; //manager fling animation
    private float m_fling_dis=150; //the limited distance decide to fling animation or single day animation
    private float m_fling_speed=1500; //the limited speed decide to fling animation or single day animation in limited distance(m_fling_dis)

    public WellnessDailyAnimUIManager(Context ctxt, EventNotify eventNotify, long nowTime, long firstUseTime) {
        m_context = (Activity)ctxt;
        m_eEventNotify = eventNotify;
        setFirstUseAndNowTime(nowTime,firstUseTime);
        prepareRunnables();
        init();
    }

    public void setFirstUseAndNowTime(long nowTime, long firstUseTime){
        m_curDate = nowTime;
        m_firstUseTime = firstUseTime;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(firstUseTime);
        m_firstDay = c.getTime().getDate();
        m_firstMonth = c.getTime().getMonth();/* +1 */
        m_firstYear = c.getTime().getYear();/* +1900; */

        m_calendar = Calendar.getInstance();
        m_calendar.setTimeInMillis(m_curDate);

        m_context.getWindow().getDecorView().findViewById(R.id.wellness_daily_anim_view).setOnTouchListener(this);
        m_context.getWindow().getDecorView().findViewById(R.id.wellness_daily_anim_view).setLongClickable(true);
    }


    private void prepareRunnables(){
        m_AutoRSDRunable = new Runnable() {
            @Override
            public void run() {
                m_speedV *= m_backrubNumber;
             //   Log.i("smile","speed: "+m_speedV);
                if (m_speedV < 0) {
                    if (m_speedV >= -m_back_limitSpeed)
                        m_speedV = -m_back_limitSpeed;
                } else if (m_speedV > 0) {
                    if (m_speedV <= m_back_limitSpeed)
                        m_speedV = m_back_limitSpeed;
                }
                animationUpdate(m_speedV);
            }
        };

        m_FlingRSDRunable = new Runnable() {
            @Override
            public void run() {
                m_speedV *= m_rubNumber_auto;
                if (m_speedV < 0) {
                    if (m_speedV >= -m_limitSpeed)
                        m_speedV = -m_limitSpeed;
                } else if (m_speedV > 0) {
                    if (m_speedV <= m_limitSpeed)
                        m_speedV = m_limitSpeed;
                }
                animationUpdate(m_speedV);
            }
        };

        m_ForwardRunable = new Runnable() {
            @Override
            public void run() {
                setPreTextVisible(View.INVISIBLE);
                while (true) {
                    try {
                        Thread.sleep(m_FPS);
                        if (m_speedV == 0) {
                            break;
                        }
                        autoSlowDownAnimation();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                m_context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_curMoving_type = MOVING_TYPE.ORIGINAL_POS;
                        sendNotify();
                        ObjectAnimator anim;
                        for (int i = 0; i < m_calendarGroupNum; i++) {
                            if (isPadLayout) {
                                anim = ObjectAnimator.ofFloat(m_calList.get(i), "x",
                                        m_calList.get(i).getX() + 10f, m_calList.get(i).getX());
                            } else {
                                anim = ObjectAnimator.ofFloat(m_calList.get(i), "y",
                                        m_calList.get(i).getY() + 10f, m_calList.get(i).getY());
                            }
                            anim.setInterpolator(AnimationUtils.loadInterpolator(m_context,
                                    android.R.anim.bounce_interpolator));
                            anim.setDuration(200);
                            anim.start();
                            if (i == 0) {
                                anim.addListener(new AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        // TODO Auto-generated method stub
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {
                                        // TODO Auto-generated method stub
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        isAnimationRunning(false);
                                        m_isAutoing = false;
                                        m_isLimit = false;
                                        m_isLimitRubStatus_forward = false;
                                        setNextTextVisible(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        // TODO Auto-generated method stub
                                    }
                                });
                            }
                        }
                    }
                });
            }
        };

        m_BackRunable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (m_speedV == 0) {
                        break;
                    }
                    try {
                        Thread.sleep(m_FPS);
                        autoSlowDownAnimation();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                m_context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_isAutoing = false;
                        isAnimationRunning(false);
                        m_isLimit = true;
                        animationUpdate(m_speedV);
                        m_isLimit = false;
                        m_isLimitRubStatus_previous = false;
                        m_curMoving_type = MOVING_TYPE.ORIGINAL_POS;
                        sendNotify();
                    }
                });
            }
        };

        m_BackCountRunable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(m_FPS);
                        if (m_speedV == 0) {
                            break;
                        }
                        autoSlowDownAnimation();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                m_context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animationUpdate(m_speedV);
                        m_isAutoing = false;
                        isAnimationRunning(false);
                        m_isLimit = false;
                        m_curMoving_type = MOVING_TYPE.ORIGINAL_POS;
                        sendNotify();
                    }
                });
            }
        };

        m_FlingRunable = new Runnable() {
            @Override
            public void run() {
                Calendar c = Calendar.getInstance();
                while (true) {
                    if (m_speedV == 0) {
                        isAnimationRunning(false);
                        break;
                    }

                    try {
                        Thread.sleep(m_FPS);
                        c.setTimeInMillis(m_curDate);
                        if (c.getTime().getDate() == m_firstDay
                                && c.getTime().getMonth() == m_firstMonth
                                && c.getTime().getYear() == m_firstYear)
                            setPreTextVisible(View.INVISIBLE);
                        else
                            setPreTextVisible(View.VISIBLE);

                        if (m_isLimitRubStatus_forward ||
                                Utility.getDateOffset(c.getTimeInMillis(), m_calendar.getTimeInMillis()) > 0) {
                            setNextTextVisible(View.VISIBLE);
                            autoForward();
                            break;
                        }
                        c.setTimeInMillis(m_curDate - oneDay);
                        if (m_isLimitRubStatus_previous &&
                                Utility.getDateOffset(c.getTimeInMillis(), m_firstUseTime) < 0) {
                            m_isLimit = true;
                            autoBack();
                            return;
                        }

                        boolean needAutoRun = false;
                        if (isPadLayout) {
                            if (m_leftGroup.get(m_leftGroupNum - 2).getX() + m_speedV <= 0
                                    && m_speedV < 0
                                    || m_leftGroup.get(m_leftGroupNum - 2).getX() + m_speedV >= m_baseNumber
                                    && m_speedV > 0) {
                                needAutoRun = true;
                            }
                        } else {
                            if (m_leftGroup.get(m_leftGroupNum - 2).getX() + m_speedV <= -m_baseNumber
                                    && m_speedV < 0
                                    || m_leftGroup.get(m_leftGroupNum - 2).getX() + m_speedV >= 0
                                    && m_speedV > 0) {
                                needAutoRun = true;
                            }
                        }

                        if (needAutoRun) { // auto_forwardRun
                            if (Math.abs(m_speedV) <= m_limitSpeed) {
                                //isAnimationRunning(false);
                                m_context.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        isAnimationRunning(false);
                                        if (m_curMoving_type == MOVING_TYPE.FORWARD_START) {
                                            animationUpdate(m_speedV);
                                        } else {
                                            m_isLimit = true;
                                            animationUpdate(m_speedV);
                                            m_isLimit = false;
                                        }
                                        if(isMatchPosition()) {
                                            m_curMoving_type = MOVING_TYPE.ORIGINAL_POS;
                                            sendNotify();
                                        } else {
                                            goToMatchPosition();
                                        }
                                    }
                                });
                                break;
                            }
                        }

                        Fling_SlowDownAnimation();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        //GuestureTest();
    }

    private void init() {
        m_FPS = Math.round(((float)1 / (float)m_FPS) * 1000);
        isPadLayout = Utility.isPadLayout(m_context);
        m_Dimens = Utility.getSizeDimens(m_context);
        m_ori_scale = Utility.getScaleRatio(m_context);
        /*switch(m_Dimens) {
            case SW320DP:
                m_ori_scale = 1; // 0.9f;
                break;
            case SW360DP:
                m_ori_scale = 1;
                break;
            case SW600DP:
                m_ori_scale = 1.34f; // 1.234f;
                break;
            case SW720DP:
                m_ori_scale = 1.34f;
                break;
            default:
                m_ori_scale = 1;
        }*/

        if(isPadLayout) {
            m_limitSpeed = 50f;
            m_back_limitSpeed = 45f;
        }

        m_metrics = m_context.getResources().getDisplayMetrics();

        m_limitSpeed *= m_metrics.density / 3;
        m_backSpeed *= m_metrics.density / 3;
        m_back_limitSpeed *= m_metrics.density / 3;
        m_AutoForwardSpeed *= m_metrics.density / 3;
        m_AutoBackSpeed *= m_metrics.density / 3;


        mGestureDetector = new GestureDetector(this);
        m_hrHandlerThread = new HandlerThread("animationManager");
        m_hrHandlerThread.start();
        m_handler = new Handler(m_hrHandlerThread.getLooper());

        // DailyAnim
        // m_context.getWindow().getDecorView().findViewById(android.R.id.content).setOnTouchListener(this);
        // m_context.getWindow().getDecorView().findViewById(android.R.id.content).setLongClickable(true);
//        m_context.getWindow().getDecorView().findViewById(R.id.wellness_daily_anim_view).setOnTouchListener(this);
//        m_context.getWindow().getDecorView().findViewById(R.id.wellness_daily_anim_view).setLongClickable(true);

        mainLayout = (ViewGroup)m_context.findViewById(R.id.daily_anim_main);
        mainLayout.setScaleX(m_ori_scale);
        mainLayout.setScaleY(m_ori_scale);

        initBodyView();
        initMindView();
        initCalendarView();

        setPreTextVisible(View.INVISIBLE);
        /* Debug
        int k;
        for(k = 0; k < m_leftGroupPos.size(); k++) {
            Log.d(TAG, "m_leftGroupPos(" + k + ") = (" + m_leftGroupPos.get(k).x + " , "
                    + m_leftGroupPos.get(k).y + ")");
        }
        for(k = 0; k < m_rightGroupPos.size(); k++) {
            Log.d(TAG, "m_rightGroupPos(" + k + ") = (" + m_rightGroupPos.get(k).x + " , "
                    + m_rightGroupPos.get(k).y + ")");
        }
        for(k = 0; k < m_calGroupPos.size(); k++) {
            Log.d(TAG, "m_calGroupPos(" + k + ") = (" + m_calGroupPos.get(k).x + " , "
                    + m_calGroupPos.get(k).y + ")");
        }
        */
    }

    private void initBodyView(){
        //region body view

        //emily++++
        // ViewGroup L_body ;
        mainLayout = (ViewGroup) m_context.findViewById(R.id.daily_anim_main);
        if (!WApplication.getInstance().getConnectedDevice().getIsRobin()){
            //Log.i("emily","initBodyView()---sparrow");
            mainLayout.findViewById(R.id.L_body).setVisibility(View.GONE);
            mBodyLayout = (ViewGroup) mainLayout.findViewById(R.id.L_body_sparrow);
        }
        else {
            mainLayout.findViewById(R.id.L_body_sparrow).setVisibility(View.GONE);
            mBodyLayout = (ViewGroup) mainLayout.findViewById(R.id.L_body);
            //Log.i("emily","initBodyView()---Robin");
        }
        mBodyLayout.setVisibility(View.VISIBLE);
        // L_body = mBodyLayout;
        //emily----

        // ViewGroup L_body =  (ViewGroup)mainLayout.findViewById(R.id.L_body);
        ViewGroup L_group =  (ViewGroup)mBodyLayout.findViewById(R.id.L_group);

        m_L_largeView =L_group.findViewById(R.id.l_large);
        m_L_largeView.setVisibility(ViewGroup.GONE);

        for (int i = 0; i < L_group.getChildCount() - 1; i++) {
            m_leftGroup.add((ViewGroup)L_group.getChildAt(i));
            m_leftGroupNum++;
        }

        m_baseNumber = (((ViewGroup)m_leftGroup.get(3)).getChildAt(1).getX()
                + ((ViewGroup)m_leftGroup.get(3)).getChildAt(1).getMeasuredWidth() / 2)
                - (((ViewGroup)m_leftGroup.get(4)).getChildAt(1).getX()
                + ((ViewGroup)m_leftGroup.get(4)).getChildAt(1).getMeasuredWidth() / 2);
        m_baseNumber = Math.abs(m_baseNumber);

        for (int i = 0; i < m_leftGroupNum; i++) {
            m_L_leaveGroup.add((ViewGroup)m_leftGroup.get(i).getChildAt(0));
            m_L_circleGroup.add((ViewGroup)m_leftGroup.get(i).getChildAt(1));
            m_L_circleGroup.get(i).setPivotX(
                    m_L_circleGroup.get(i).getChildAt(1).getMeasuredWidth() / 2);
            if (isPadLayout) {
                m_L_circleGroup.get(i).setPivotY(
                        m_L_circleGroup.get(i).getChildAt(1).getMeasuredHeight());
            } else {
                m_L_circleGroup.get(i).setPivotY(0);
            }

            m_L_leaveGroup.get(i).setPivotX(
                    m_L_leaveGroup.get(i).getChildAt(1).getMeasuredWidth() / 2
                            - (m_metrics.density * ((float)5 / (float)3)));

            if (isPadLayout) {
                m_L_leaveGroup.get(i).setPivotY(
                        m_L_leaveGroup.get(i).getChildAt(1).getMeasuredHeight()
                                + (m_L_circleGroup.get(i).getChildAt(1).getMeasuredHeight() * 7/10));
            } else {
                m_L_leaveGroup.get(i).setPivotY(
                        m_L_leaveGroup.get(i).getChildAt(1).getMeasuredHeight());
            }

            m_L_leaveGroup.get(i).getChildAt(0).setAlpha(0);
            float targetPX1;
            float targetPX2;
            if (i != m_leftGroupNum - 1) {
                targetPX2 = ((ViewGroup)m_leftGroup.get(i + 1)).getChildAt(1).getX()
                        + ((ViewGroup)m_leftGroup.get(i + 1)).getChildAt(1).getMeasuredWidth() / 2;
            } else {
                targetPX2 = m_L_largeView.getX() + m_L_largeView.getMeasuredWidth() / 2;
            }

            targetPX1 = m_L_circleGroup.get(i).getX() + m_L_circleGroup.get(i).getMeasuredWidth() / 2;
            float posX = targetPX1 - targetPX2;
            m_leftV_Ratio.add(Math.abs((double)posX / (double)m_baseNumber));

            if(isPadLayout) {
                m_leftGroupPos.add(new PointF(m_L_circleGroup.get(i).getX()
                        + m_L_circleGroup.get(i).getMeasuredWidth() / 2, m_L_circleGroup.get(i).getY()));
            } else {
                m_leftGroupPos.add(new PointF(m_L_circleGroup.get(i).getX()
                        + m_L_circleGroup.get(i).getMeasuredWidth() / 2, m_L_circleGroup.get(i).getY()
                        + m_L_circleGroup.get(i).getMeasuredHeight() / 2));
            }

            m_L_leaveGroup.get(i).setX(
                    m_L_leaveGroup.get(i).getX()
                            + m_context.getResources().getDimensionPixelOffset(
                            R.dimen.l_left_offset));
            m_L_leaveGroup.get(i).setY(
                    m_L_leaveGroup.get(i).getY()
                            + m_context.getResources()
                            .getDimensionPixelOffset(R.dimen.l_top_offset));
            m_L_circleGroup.get(i).setX(
                    m_L_circleGroup.get(i).getX()
                            + m_context.getResources().getDimensionPixelOffset(
                            R.dimen.l_left_offset));
            m_L_circleGroup.get(i).setY(
                    m_L_circleGroup.get(i).getY()
                            + m_context.getResources()
                            .getDimensionPixelOffset(R.dimen.l_top_offset));
        }

        View Activity_txt= mBodyLayout.findViewById(R.id.Activity_txt);
        View activity_btn = mBodyLayout.findViewById(R.id.activity_btn);
        Activity_txt.setX(Activity_txt.getX() + m_context.getResources().getDimensionPixelOffset(R.dimen.l_left_offset));
        Activity_txt.setY(Activity_txt.getY() + m_context.getResources().getDimensionPixelOffset(R.dimen.l_top_offset));
        activity_btn.setX(activity_btn.getX() + m_context.getResources().getDimensionPixelOffset(R.dimen.l_left_offset));
        activity_btn.setY(activity_btn.getY() + m_context.getResources().getDimensionPixelOffset(R.dimen.l_top_offset));
        activity_btn.setOnTouchListener(this);
        activity_btn.setLongClickable(true);
        //#endregion body view
    }

    private void initMindView(){
        //region mind view
        ViewGroup R_mind,R_sleep;
        R_mind =  (ViewGroup) mainLayout.findViewById(R.id.R_mind);
        R_sleep  =  (ViewGroup) mainLayout.findViewById(R.id.R_sleep);

        if (WApplication.getInstance().getConnectedDevice().getIsRobin()){
            R_mind.setVisibility(View.VISIBLE);
            R_sleep.setVisibility(View.GONE);
            mMindSleepLayout = R_mind;
        }
        else {
            R_mind.setVisibility(View.GONE);
            R_sleep.setVisibility(View.VISIBLE);
            mMindSleepLayout = R_sleep;
        }
//        mBodyLayout.setVisibility(View.VISIBLE);

//        ViewGroup  R_mind = (ViewGroup)m_context.findViewById(R.id.R_mind);


        ViewGroup  R_group = (ViewGroup)mMindSleepLayout.findViewById(R.id.R_group);

        View Energy_txt = mMindSleepLayout.findViewById(R.id.Energy_txt);
        View energy_btn = mMindSleepLayout.findViewById(R.id.energy_btn);

        Energy_txt.setX(Energy_txt.getX() - m_context.getResources().getDimensionPixelOffset(R.dimen.r_right_offset));
        Energy_txt.setY(Energy_txt.getY()  + m_context.getResources().getDimensionPixelOffset(R.dimen.r_top_offset));
        energy_btn.setX(energy_btn.getX() - m_context.getResources().getDimensionPixelOffset(R.dimen.r_right_offset));
        energy_btn.setY(energy_btn.getY()  + m_context.getResources().getDimensionPixelOffset(R.dimen.r_top_offset));
        energy_btn.setOnTouchListener(this);
        energy_btn.setLongClickable(true);

        m_R_largeView = mMindSleepLayout.findViewById(R.id.r_large);
        m_R_largeView.setVisibility(ViewGroup.GONE);
        for (int i = 0; i < R_group.getChildCount() - 1; i++) {
            m_rightGroup.add((ViewGroup)R_group.getChildAt(i));
            m_rightGroupNum++;
        }
        for (int i = 0; i < m_rightGroupNum; i++) {
            m_R_leaveGroup.add((ViewGroup)m_rightGroup.get(i).getChildAt(0));
            m_R_circleGroup.add((ViewGroup)m_rightGroup.get(i).getChildAt(1));
            m_R_circleGroup.get(i).setPivotX(
                    m_R_circleGroup.get(i).getChildAt(1).getMeasuredWidth() / 2);
            if (isPadLayout) {
                m_R_circleGroup.get(i).setPivotY(
                        m_R_circleGroup.get(i).getChildAt(1).getMeasuredHeight());
            } else {
                m_R_circleGroup.get(i).setPivotY(0);
            }

            m_R_leaveGroup.get(i).setPivotX(
                    m_R_leaveGroup.get(i).getChildAt(1).getMeasuredWidth() / 2
                            + (m_metrics.density * ((float)9 / (float)3)));
            if (isPadLayout) {
                m_R_leaveGroup.get(i).setPivotY(
                        m_R_leaveGroup.get(i).getChildAt(1).getMeasuredHeight()
                                + (m_R_circleGroup.get(i).getChildAt(1).getMeasuredHeight()*5/10));
            } else {
                m_R_leaveGroup.get(i).setPivotY(
                        m_R_leaveGroup.get(i).getChildAt(1).getMeasuredHeight());
            }
            m_R_leaveGroup.get(i).getChildAt(0).setAlpha(0);
            float targetPX1;
            float targetPX2;
            if (i != m_rightGroupNum - 1) {
                targetPX2 = ((ViewGroup)m_rightGroup.get(i + 1)).getChildAt(1).getX()
                        + ((ViewGroup)m_rightGroup.get(i + 1)).getChildAt(1).getMeasuredWidth() / 2;
            } else {
                targetPX2 = (m_R_largeView.getX() + m_R_largeView.getMeasuredWidth() / 2);
            }

            targetPX1 = m_R_circleGroup.get(i).getX() + m_R_circleGroup.get(i).getMeasuredWidth() / 2;
            float posX = targetPX1 - targetPX2;
            m_rightV_Ratio.add(Math.abs((double)posX / (double)m_baseNumber));

            if(isPadLayout) {
                m_rightGroupPos.add(new PointF(m_R_circleGroup.get(i).getX()
                        + m_R_circleGroup.get(i).getMeasuredWidth() / 2, m_R_circleGroup.get(i).getY()));
            } else {
                m_rightGroupPos.add(new PointF(m_R_circleGroup.get(i).getX()
                        + m_R_circleGroup.get(i).getMeasuredWidth() / 2, m_R_circleGroup.get(i).getY()
                        + m_R_circleGroup.get(i).getMeasuredHeight() / 2));
            }

            m_R_leaveGroup.get(i).setX(
                    m_R_leaveGroup.get(i).getX()
                            - m_context.getResources().getDimensionPixelOffset(
                            R.dimen.r_right_offset));
            m_R_leaveGroup.get(i).setY(
                    m_R_leaveGroup.get(i).getY()
                            + m_context.getResources()
                            .getDimensionPixelOffset(R.dimen.r_top_offset));
            m_R_circleGroup.get(i).setX(
                    m_R_circleGroup.get(i).getX()
                            - m_context.getResources().getDimensionPixelOffset(
                            R.dimen.r_right_offset));
            m_R_circleGroup.get(i).setY(
                    m_R_circleGroup.get(i).getY()
                            + m_context.getResources()
                            .getDimensionPixelOffset(R.dimen.r_top_offset));
        }

        //emily++++
        if (!WApplication.getInstance().getConnectedDevice().getIsRobin()){
            mainLayout.findViewById(R.id.R_mind).setVisibility(View.GONE);
            mainLayout.findViewById(R.id.R_sleep).setVisibility(View.VISIBLE);
        }
        else {
            mainLayout.findViewById(R.id.R_mind).setVisibility(View.VISIBLE);
            mainLayout.findViewById(R.id.R_sleep).setVisibility(View.GONE);
        }
        //emily----
        //endregion mind view
    }

    private void initCalendarView(){
        //emily++++
        //ViewGroup M_calendar ;
        if (!WApplication.getInstance().getConnectedDevice().getIsRobin()){
            mainLayout.findViewById(R.id.M_calendar).setVisibility(View.GONE);
            mCalendarLayout = (ViewGroup) mainLayout.findViewById(R.id.M_calendar_sparrow);
        }
        else {
            mainLayout.findViewById(R.id.M_calendar_sparrow).setVisibility(View.GONE);
            mCalendarLayout = (ViewGroup) mainLayout.findViewById(R.id.M_calendar);
        }
        mCalendarLayout.setVisibility(View.VISIBLE);
        //emily----

        ViewGroup vg_calendar = (ViewGroup) mCalendarLayout.findViewById(R.id.calendar);

        //region calendar view
        for (int i = 0; i <vg_calendar.getChildCount(); i++) {
            m_calList.add( vg_calendar.getChildAt(i));
            if(isPadLayout) {
                m_calList.get(i).setX(m_calList.get(i).getX() + m_context.getResources().getDimensionPixelOffset(R.dimen.calendar_top_offset));
            } else {
                m_calList.get(i).setY(m_calList.get(i).getY() + m_context.getResources().getDimensionPixelOffset(R.dimen.calendar_top_offset));
            }
            m_calendarGroupNum++;
        }

        if (isPadLayout) {
            for (int i = 0; i < ((ViewGroup)mainLayout.findViewById(R.id.calendar_dot)).getChildCount(); i++) {
                m_caldotList.add(((ViewGroup)mainLayout.findViewById(R.id.calendar_dot)).getChildAt(i));
                m_caldotList.get(i).setX(m_caldotList.get(i).getX());
                m_caldotGroupNum++;
            }
        }


        for (int i = 0; i < m_calendarGroupNum; i++) {
            TextView tView = ((TextView)m_calList.get((m_calendarGroupNum - 1) - i));

            if (isPadLayout) {
                tView.setPivotY(tView.getMeasuredHeight() / 2);
                tView.setPivotX(0);
            } else {
                tView.setPivotX(tView.getMeasuredWidth() / 2);
                tView.setPivotY(0);
            }

            float ration = 1;
            if (m_Dimens == SIZEDIMENS.SW320DP || m_Dimens == SIZEDIMENS.SW360DP) {
                ration = 1.7f;
            } else if (m_Dimens == SIZEDIMENS.SW600DP || m_Dimens == SIZEDIMENS.SW720DP) {
                ration = 3;
            }
            float count = 0;
            if (i >= 5) {
                count = 4.5f;
            } else {
                count = i;
            }
            float textsize = ((m_context.getResources().getDimensionPixelSize(
                    R.dimen.dailyanim_c_txt_large_textsize) / m_metrics.density) - (count * ration));

            if(!isPadLayout) {
                tView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textsize);
            }

            m_calGroupPos.add(new PointF(m_calList.get(i).getX(), m_calList.get(i).getY()));

            float targetPX2;
            float targetPX1;
            if (i != m_calendarGroupNum - 1) {
                if(isPadLayout) {
                    targetPX2 = m_calList.get(i + 1).getX()
                            + m_calList.get(i + 1).getMeasuredWidth() / 2;
                } else {
                    targetPX2 = m_calList.get(i + 1).getY();
                }
            } else {
                if (isPadLayout) {
                    targetPX2 = m_calList.get(i).getX()
                            + (m_calList.get(m_calendarGroupNum - 1).getX() - m_calList.get(
                            m_calendarGroupNum - 2).getX());
                } else {
                    targetPX2 = m_calList.get(i).getY()
                            + (m_calList.get(m_calendarGroupNum - 1).getY() - m_calList.get(
                            m_calendarGroupNum - 2).getY());
                }
            }

            targetPX1 = isPadLayout ? (m_calList.get(i).getX() + m_calList.get(i)
                    .getMeasuredWidth() / 2) : m_calList.get(i).getY();
            float posX = targetPX1 - targetPX2;
            m_calRatio.add(Math.abs((double)posX / (double)m_baseNumber));

        }

        if (isPadLayout) {
            for (int i = 0; i < m_caldotGroupNum; i++) {
                View dotV = m_caldotList.get((m_caldotGroupNum - 1) - i);
                dotV.setPivotY(dotV.getMeasuredHeight() / 2);
                dotV.setPivotX(0);

                m_caldotGroupPos.add(new PointF(m_caldotList.get(i).getX(), m_caldotList.get(i).getY()));

                float targetPX2;
                float targetPX1;
                if (i != m_caldotGroupNum - 1) {
                    targetPX2 = m_caldotList.get(i + 1).getX()
                            + m_caldotList.get(i + 1).getMeasuredWidth() / 2;
                } else {
                    targetPX2 = m_caldotList.get(i).getX()
                            + (m_caldotList.get(m_caldotGroupNum - 1).getX() - m_caldotList.get(
                            m_caldotGroupNum - 2).getX());
                }
                targetPX1 = m_caldotList.get(i).getX() + m_caldotList.get(i).getMeasuredWidth() / 2;
                float posX = targetPX1 - targetPX2;
                m_caldotRatio.add(Math.abs((double)posX / (double)m_baseNumber));
            }
        }

        View white_mask = mCalendarLayout.findViewById(R.id.white_mask);
        View divide_txt = mCalendarLayout.findViewById(R.id.divide_txt);
        View month_txt = mCalendarLayout.findViewById(R.id.month_txt);
        View week_txt = mCalendarLayout.findViewById(R.id.week_txt);
        final View today_btn = mCalendarLayout.findViewById(R.id.today_btn);

        int calendar_top_offset = m_context.getResources().getDimensionPixelOffset(R.dimen.calendar_top_offset);

        white_mask.setY(white_mask.getY() +calendar_top_offset);
        divide_txt.setY(divide_txt.getY()  + calendar_top_offset);
        month_txt.setY(month_txt.getY() +calendar_top_offset);
        week_txt.setY(week_txt.getY() + calendar_top_offset);
        today_btn.setY(today_btn.getY()  + calendar_top_offset);

        today_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_isRunning) return;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(m_curDate);
                Date date = new Date();
                date.setTime(m_calendar.getTimeInMillis() - m_curDate);
                int offsetDay = Math.round((m_calendar.getTimeInMillis() - m_curDate) / oneDay);

                today_btn.animate().setListener(null);
                if (offsetDay <= 3) {
                    autoBack(offsetDay);
                    today_btn.animate().rotation(0).setDuration(250).start();
                } else {
                    m_curDate = m_calendar.getTimeInMillis() - (3 * oneDay);
                    setDate(m_curDate);
                    autoBack(3);
                    today_btn.animate().rotation(0).setDuration(700).start();
                }
            }
        });

        today_btn.setAlpha(0);
        today_btn.setPivotX( today_btn.getMeasuredWidth() / 2);
        today_btn.setPivotY(today_btn.getMeasuredHeight() / 2);
        today_btn.animate().setInterpolator(
                AnimationUtils.loadInterpolator(m_context, android.R.anim.decelerate_interpolator));
        //endregion calendar

        setDate(m_curDate);
        if (m_eEventNotify != null) {
            m_curMoving_type = MOVING_TYPE.ORIGINAL_POS;
            sendNotify();
        }

        m_curDate -= 3 * oneDay;
        setDate(m_curDate);
        m_isLimit = false;
        // mainLayout.setAlpha(0);
        mainLayout.setVisibility(View.VISIBLE);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                m_context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainLayout.animate().alpha(1).setDuration(1000).start();
                        autoBack(3);
                    }
                });
            }
        };
        timer.schedule(timerTask, 0);
    }

    public void checkAndMatchPosition() {
        goToMatchPosition();
    }

    private boolean isMatchPosition() {
        return (m_curMoving_type == MOVING_TYPE.FORWARD_END || m_curMoving_type == MOVING_TYPE.BACK_END);
    }

    private void goToMatchPosition() {
        m_autoSpeed = m_limitSpeed;
        //fixed bug: date cannot update when another day comiing
       // if (m_leftGroup.get(0).getX() != 0 ) {
            if (!m_isLimitRubStatus_forward && !m_isLimitRubStatus_previous) {
                if (isPadLayout) {
                    if (m_leftGroup.get(m_leftGroupNum - 2).getX() > (m_leftGroupPos
                            .get(m_leftGroupNum - 1).x - m_leftGroupPos.get(m_leftGroupNum - 2).x) / 2)
                        m_curMoving_type = MOVING_TYPE.FORWARD_START;
                    else
                        m_curMoving_type = MOVING_TYPE.BACK_START;
                } else {
                    if (m_leftGroup.get(m_leftGroupNum - 2).getX() < -((m_leftGroupPos
                            .get(m_leftGroupNum - 2).x - m_leftGroupPos.get(m_leftGroupNum - 1).x) / 2))
                        m_curMoving_type = MOVING_TYPE.FORWARD_START;
                    else
                        m_curMoving_type = MOVING_TYPE.BACK_START;
                }

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(m_curDate - oneDay);
                if (c.getTime().getDate() < m_firstDay
                        && c.getTime().getMonth() == m_firstMonth
                        && c.getTime().getYear() == m_firstYear) {
                    m_curMoving_type = MOVING_TYPE.BACK_START;
                }
                fling_animation(m_curMoving_type);
            } else {
                if (m_isLimitRubStatus_forward) {
                    autoForward();
                } else if (m_isLimitRubStatus_previous) {
                    autoBack();
                }
            }
//        }
    }

    /**
     * sensity test code
     * @param vis
     */
	/*private void GuestureTest(){
		final EditText editText = (EditText) m_context.findViewById(R.id.sensity_txt);
		final EditText editText1 = (EditText) m_context.findViewById(R.id.sensity_dis_txt);
		final EditText editText2 = (EditText) m_context.findViewById(R.id.scroll_value_txt);
		if(editText==null)return;

		editText.setText(m_fling_speed+"");
		editText1.setText(m_fling_dis+"");
		editText2.setText(m_rubNumber+"");
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
					boolean handled = false;
		            handled = true;
		            editText.clearFocus();
		            InputMethodManager imm = (InputMethodManager)m_context.getSystemService(
		            	      Context.INPUT_METHOD_SERVICE);
		            	imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            		try {
		            	m_fling_speed=Float.valueOf(editText.getText()+"");
					} catch (NumberFormatException e) {
						// TODO: handle exception
					}
		            return handled;
		        }
		});

		editText1.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
					boolean handled = false;
		            handled = true;
		            editText1.clearFocus();
		            InputMethodManager imm = (InputMethodManager)m_context.getSystemService(
		            	      Context.INPUT_METHOD_SERVICE);
		            	imm.hideSoftInputFromWindow(editText1.getWindowToken(), 0);
		            	try {
			            	m_fling_dis=Float.valueOf(editText1.getText()+"");
						} catch (NumberFormatException e) {
							// TODO: handle exception
						}
		            return handled;
		        }
		});

		editText2.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
		        boolean handled = false;
		            handled = true;
		            editText2.clearFocus();
		            InputMethodManager imm = (InputMethodManager)m_context.getSystemService(
		            	      Context.INPUT_METHOD_SERVICE);
		            	imm.hideSoftInputFromWindow(editText2.getWindowToken(), 0);
		            	try {
			            	m_rubNumber=Float.valueOf(editText2.getText()+"");
						} catch (NumberFormatException e) {
							// TODO: handle exception
						}
		            return handled;
		        }
		});

	}*/

    private void setPreTextVisible(final int vis) {
        m_context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)mBodyLayout.findViewById(R.id.l2_txt)).setVisibility(vis);
                ((TextView)mainLayout.findViewById(R.id.r2_txt)).setVisibility(vis);

            }
        });

    }

    private void setNextTextVisible(final int vis) {
        m_context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)mBodyLayout.findViewById(R.id.l1_txt)).setVisibility(vis);
                ((TextView)mainLayout.findViewById(R.id.r1_txt)).setVisibility(vis);
                ((TextView)mBodyLayout.findViewById(R.id.completion)).setVisibility(vis);
                ((TextView)mainLayout.findViewById(R.id.Average)).setVisibility(vis);
            }
        });
    }

    private void setDate(long _curDAte) {
        if (mCalendarLayout.findViewById(R.id.month_txt).getVisibility() == View.INVISIBLE) {
            mCalendarLayout.findViewById(R.id.month_txt).setVisibility(View.VISIBLE);
            if (m_Dimens == SIZEDIMENS.SW600DP || m_Dimens == SIZEDIMENS.SW720DP) {
                mCalendarLayout.findViewById(R.id.week_txt).setVisibility(View.INVISIBLE);
            } else {
                mCalendarLayout.findViewById(R.id.week_txt).setVisibility(View.VISIBLE);
            }
            mCalendarLayout.findViewById(R.id.divide_txt).setVisibility(View.VISIBLE);
            mCalendarLayout.findViewById(R.id.month_txt).setAlpha(0);
            mCalendarLayout.findViewById(R.id.month_txt).animate().alpha(1).setDuration(250).start();
            mCalendarLayout.findViewById(R.id.week_txt).setAlpha(0);
            mCalendarLayout.findViewById(R.id.week_txt).animate().alpha(1).setDuration(250).start();
            mCalendarLayout.findViewById(R.id.divide_txt).setAlpha(0);
            mCalendarLayout.findViewById(R.id.divide_txt).animate().alpha(1).setDuration(250).start();
            mCalendarLayout.findViewById(R.id.today_btn).setAlpha(1);
            mCalendarLayout.findViewById(R.id.today_btn).animate().setListener(null);
            mCalendarLayout.findViewById(R.id.today_btn).animate().cancel();
            mCalendarLayout.findViewById(R.id.today_btn).animate().alpha(1).rotation(-90).setDuration(250).start();
        }

        Calendar calendar = Calendar.getInstance();

        MONTH curMonth = null;
        WEEK curWeekDay = null;
        int _curMonth = 0;
        int _curWeekDay = 0;
        String month="01";

        for (int i = 0; i < m_calendarGroupNum; i++) {
            calendar.setTimeInMillis(_curDAte - (oneDay * i));
            switch (calendar.getTime().getMonth() + 1) {
                case 1:
                    curMonth = MONTH.JAN;
                    _curMonth = string.JAN;
                    month="01";
                    break;
                case 2:
                    curMonth = MONTH.FEB;
                    _curMonth = string.FEB;
                    month="02";
                    break;
                case 3:
                    curMonth = MONTH.MAR;
                    _curMonth = string.MAR;
                    month="03";
                    break;
                case 4:
                    curMonth = MONTH.APR;
                    _curMonth = string.APR;
                    month="04";
                    break;
                case 5:
                    curMonth = MONTH.MAY;
                    _curMonth = string.MAY;
                    month="05";
                    break;
                case 6:
                    curMonth = MONTH.JUN;
                    _curMonth = string.JUN;
                    month="06";
                    break;
                case 7:
                    curMonth = MONTH.JUL;
                    _curMonth = string.JUL;
                    month="07";
                    break;
                case 8:
                    curMonth = MONTH.AUG;
                    _curMonth = string.AUG;
                    month="08";
                    break;
                case 9:
                    curMonth = MONTH.SEP;
                    _curMonth = string.SEP;
                    month="09";
                    break;
                case 10:
                    curMonth = MONTH.OCT;
                    _curMonth = string.OCT;
                    month="10";
                    break;
                case 11:
                    curMonth = MONTH.NOV;
                    _curMonth = string.NOV;
                    month="11";
                    break;
                case 12:
                    curMonth = MONTH.DEC;
                    _curMonth = string.DEC;
                    month="12";
                    break;
                default:
                    break;
            }

            switch (calendar.getTime().getDay()) {
                case 0:
                    curWeekDay = WEEK.SUN;
                    _curWeekDay = string.SUN;
                    break;
                case 1:
                    curWeekDay = WEEK.MON;
                    _curWeekDay = string.MON;
                    break;
                case 2:
                    curWeekDay = WEEK.TUE;
                    _curWeekDay = string.TUE;
                    break;
                case 3:
                    curWeekDay = WEEK.WED;
                    _curWeekDay = string.WED;
                    break;
                case 4:
                    curWeekDay = WEEK.THU;
                    _curWeekDay = string.THU;
                    break;
                case 5:
                    curWeekDay = WEEK.FRI;
                    _curWeekDay = string.FRI;
                    break;
                case 6:
                    curWeekDay = WEEK.SAT;
                    _curWeekDay = string.SAT;
                    break;
                default:
                    break;
            }

            int index = m_calendarGroupNum - 1;
            if(isPadLayout) {
                index--;
                if(index < i) continue;
            }
            //TextView tView = ((TextView)m_calList.get((m_calendarGroupNum - 1) - i));
            TextView tView = ((TextView)m_calList.get(index - i));
            String dayFormat = "";
            SimpleDateFormat sdf = new SimpleDateFormat("dd");

            if (m_calendar.getTime().getDate() == calendar.getTime().getDate()
                    && m_calendar.getTime().getMonth() == calendar.getTime().getMonth()
                    && m_calendar.getTime().getYear() == calendar.getTime().getYear()) {

                tView.setText(R.string.text_today);
                tView.setTextColor(0xFF7cb328);

                if (!isPadLayout) {
                    int todayWidth = (int)m_context.getResources().getDimension(R.dimen.dailyanim_c_txt_today_width);
                    int todayMarginLeft = (int)m_context.getResources().getDimension(R.dimen.dailyanim_c_txt_today_marginLeft);

                    if (tView.getLayoutParams() instanceof ViewGroup.LayoutParams) {
                        ViewGroup.LayoutParams lp1 = (ViewGroup.LayoutParams)tView.getLayoutParams();
                        lp1.width = todayWidth;
                        tView.requestLayout();
                    }
                    if (tView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                        ViewGroup.MarginLayoutParams mlp1 = (ViewGroup.MarginLayoutParams)tView.getLayoutParams();
                        mlp1.setMargins(todayMarginLeft, mlp1.topMargin, mlp1.rightMargin, mlp1.bottomMargin); // L, T, R, B
                        tView.requestLayout();
                    }
                }

                mCalendarLayout.findViewById(R.id.month_txt).setVisibility(View.INVISIBLE);
                mCalendarLayout.findViewById(R.id.week_txt).setVisibility(View.INVISIBLE);
                mCalendarLayout.findViewById(R.id.divide_txt).setVisibility(View.INVISIBLE);

                mCalendarLayout.findViewById(R.id.today_btn).animate().rotation(0).setDuration(250)
                        .setListener(new AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                                // TODO Auto-generated method stub
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mCalendarLayout.findViewById(R.id.today_btn).animate().alpha(0)
                                        .setDuration(1000).setListener(new AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        // TODO Auto-generated method stub
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {
                                        // TODO Auto-generated method stub
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mCalendarLayout.findViewById(R.id.today_btn).setEnabled(true);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {
                                        // TODO Auto-generated method stub
                                    }
                                }).start();
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                                // TODO Auto-generated method stub
                            }
                        }).start();
            } else {
                if (calendar.getTimeInMillis() > m_firstUseTime ||
                        Utility.getDateOffset(calendar.getTimeInMillis(), m_firstUseTime) == 0) {
                    Date dd  = calendar.getTime();
                    dayFormat = sdf.format(dd);
                    
                    if (m_Dimens == SIZEDIMENS.SW600DP || m_Dimens == SIZEDIMENS.SW720DP) {
                        if (i <= 5) {
                            tView.setText(dayFormat + " "
                                    + m_context.getResources().getString(_curWeekDay));
                        } else {
                            tView.setText(dayFormat + " ");
                        }
                    } else {
                        tView.setText(dayFormat + " ");
                    }

                    if(isPadLayout) {
                        if(i == 0) tView.setTextColor(0xFF404040);
                        else if(i == 1) tView.setTextColor(0xFF4D4D4D);
                    } else {
                        int normalWidth = (int) m_context.getResources().getDimension(R.dimen.dailyanim_c_txt_width);
                        int normalMarginLeft = (int) m_context.getResources().getDimension(R.dimen.dailyanim_c_txt_marginLeft);
                        if (tView.getLayoutParams() instanceof ViewGroup.LayoutParams) {
                            ViewGroup.LayoutParams lp2 = (ViewGroup.LayoutParams) tView.getLayoutParams();
                            lp2.width = normalWidth;
                            tView.requestLayout();
                        }
                        if (tView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                            ViewGroup.MarginLayoutParams mlp2 = (ViewGroup.MarginLayoutParams) tView.getLayoutParams();
                            mlp2.setMargins(normalMarginLeft, mlp2.topMargin, mlp2.rightMargin, mlp2.bottomMargin);  // L, T, R, B
                            tView.requestLayout();
                        }
                        tView.setTextColor(0xFF404040);
                    }
                } else {
                    tView.setText("");
                }
            }

        }

        calendar.setTimeInMillis(m_curDate);
        switch (calendar.getTime().getMonth() + 1) {
            case 1:
                curMonth = MONTH.JAN;
                _curMonth = string.JAN;
                month="01";
                break;
            case 2:
                curMonth = MONTH.FEB;
                _curMonth = string.FEB;
                month="02";
                break;
            case 3:
                curMonth = MONTH.MAR;
                _curMonth = string.MAR;
                month="03";
                break;
            case 4:
                curMonth = MONTH.APR;
                _curMonth = string.APR;
                month="04";
                break;
            case 5:
                curMonth = MONTH.MAY;
                _curMonth = string.MAY;
                month="05";
                break;
            case 6:
                curMonth = MONTH.JUN;
                _curMonth = string.JUN;
                month="06";
                break;
            case 7:
                curMonth = MONTH.JUL;
                _curMonth = string.JUL;
                month="07";
                break;
            case 8:
                curMonth = MONTH.AUG;
                _curMonth = string.AUG;
                month="08";
                break;
            case 9:
                curMonth = MONTH.SEP;
                _curMonth = string.SEP;
                month="09";
                break;
            case 10:
                curMonth = MONTH.OCT;
                _curMonth = string.OCT;
                month="10";
                break;
            case 11:
                curMonth = MONTH.NOV;
                _curMonth = string.NOV;
                month="11";
                break;
            case 12:
                curMonth = MONTH.DEC;
                _curMonth = string.DEC;
                month="12";
                break;
            default:
                break;
        }

        switch (calendar.getTime().getDay()) {
            case 0:
                curWeekDay = WEEK.SUN;
                _curWeekDay = string.SUN;
                break;
            case 1:
                curWeekDay = WEEK.MON;
                _curWeekDay = string.MON;
                break;
            case 2:
                curWeekDay = WEEK.TUE;
                _curWeekDay = string.TUE;
                break;
            case 3:
                curWeekDay = WEEK.WED;
                _curWeekDay = string.WED;
                break;
            case 4:
                curWeekDay = WEEK.THU;
                _curWeekDay = string.THU;
                break;
            case 5:
                curWeekDay = WEEK.FRI;
                _curWeekDay = string.FRI;
                break;
            case 6:
                curWeekDay = WEEK.SAT;
                _curWeekDay = string.SAT;
                break;
            default:
                break;
        }

        if (m_isLimitRubStatus_forward || m_isLimitRubStatus_previous) return;
        ((TextView)mCalendarLayout.findViewById(R.id.month_txt)).setText(month + "");
        ((TextView)mCalendarLayout.findViewById(R.id.week_txt)).setText(_curWeekDay);
    }

/*
    private void setLeave_1_Level(float value){
		ImageView l_target0=((ImageView)m_L_leaveGroup.get(m_leftGroupNum-1).getChildAt(1));
		ImageView r_target0=((ImageView)m_R_leaveGroup.get(m_rightGroupNum-1).getChildAt(1));
        int l_targetDrawable = 0;
        int r_targetDrawable = 0;

        if (value >= 0 && value <= 20) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1_5;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1_5;
        } else if (value > 20 && value <= 40) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1_4;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1_4;
        } else if (value > 40 && value <= 60) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1_3;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1_3;
        } else if (value > 60 && value <= 80) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1_2;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1_2;
        } else if (value > 80 && value <= 100) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1;
        }

        l_target0.setImageDrawable(m_context.getResources().getDrawable(l_targetDrawable));
        r_target0.setImageDrawable(m_context.getResources().getDrawable(r_targetDrawable));
    }

    private void setLeave_2_Level(float value) {
		ImageView l_target1=((ImageView)m_L_leaveGroup.get(m_leftGroupNum-2).getChildAt(0));
		ImageView r_target1=((ImageView)m_R_leaveGroup.get(m_rightGroupNum-2).getChildAt(0));
        int l_targetDrawable = 0;
        int r_targetDrawable = 0;

        if (value >= 0 && value <= 20) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1_5;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1_5;
        } else if (value > 20 && value <= 40) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1_4;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1_4;
        } else if (value > 40 && value <= 60) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1_3;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1_3;
        } else if (value > 60 && value <= 80) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1_2;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1_2;
        } else if (value > 80 && value <= 100) {
            l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_1;
            r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_1;
        }

        l_target1.setImageDrawable(m_context.getResources().getDrawable(l_targetDrawable));
        r_target1.setImageDrawable(m_context.getResources().getDrawable(r_targetDrawable));
    }
*/

/*
    private void setLeaveGray(boolean isGray,int index){
		if(index==m_leftGroupNum-1)return;
		ImageView l_target1=((ImageView)m_L_leaveGroup.get(index).getChildAt(1));
		ImageView r_target1=((ImageView)m_R_leaveGroup.get(index).getChildAt(1));
        int l_targetDrawable = 0;
        int r_targetDrawable = 0;

        if (isGray) {
            switch (index) {
                case 0:
                    l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_5_dis;
                    r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_5_dis;
                    break;
                case 1:
                    l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_4_dis;
                    r_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_4_dis;
                    break;
                case 2:
                    l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_3_dis;
                    r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_3_dis;
                    break;
                case 3:
                    l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_2_dis;
                    r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_2_dis;
                    break;
                default:
                    break;
            }
        } else {
            switch (index) {
                case 0:
                    l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_5;
                    r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_5;
                    break;
                case 1:
                    l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_4;
                    r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_4;
                    break;
                case 2:
                    l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_3;
                    r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_3;
                    break;
                case 3:
                    l_targetDrawable = R.drawable.asus_wellness_btn_green_leaves_2;
                    r_targetDrawable = R.drawable.asus_wellness_btn_orange_leaves_2;
                    break;
                default:
                    break;
            }
        }

        l_target1.setImageDrawable(m_context.getResources().getDrawable(l_targetDrawable));
        r_target1.setImageDrawable(m_context.getResources().getDrawable(r_targetDrawable));
    }
*/

/*
    public void setDailyButton(OnClickListener onClickListener) {
        // mainLayout.findViewById(R.id.Daily_btn).setOnClickListener(onClickListener);
        View v = m_context.findViewById(R.id.daily_weekly);
        v.findViewById(R.id.Daily_btn).setOnClickListener(onClickListener);
    }

    public void setWeeklyButton(OnClickListener onClickListener) {
        // mainLayout.findViewById(R.id.Weekly_btn).setOnClickListener(onClickListener);
        View v = m_context.findViewById(R.id.daily_weekly);
        v.findViewById(R.id.Weekly_btn).setOnClickListener(onClickListener);
    }
*/

    public void setActivityButton(OnClickListener onClickListener) {
        mBodyLayout.findViewById(R.id.activity_btn).setOnClickListener(onClickListener);
        mBodyLayout.findViewById(R.id.activity_btn).setTag(m_curDate);
    }

    public void setEnergyButton(OnClickListener onClickListener) {
        mMindSleepLayout.findViewById(R.id.energy_btn).setOnClickListener(onClickListener);
        mMindSleepLayout.findViewById(R.id.energy_btn).setTag(m_curDate);
    }

    public void setActivityText(String info, String info2) {
        TextView l1_txt = (TextView)mBodyLayout.findViewById(R.id.l1_txt);
        int width = (int)m_context.getResources().getDimension(R.dimen.dailyanim_L1_slave_circle_width);
        //(int)(m_context.getResources().getDimension(R.dimen.dailyanim_L1_slave_circle_width) * Utility.getScaleRatio(m_context));
        //l1_txt.getLayoutParams().width = width;
        if(info.isEmpty()) {
            l1_txt.setText("");
            ((TextView)mBodyLayout.findViewById(R.id.completion)).setText("");
        } else {
            l1_txt.setText(info);
            ((TextView)mBodyLayout.findViewById(R.id.completion)).setText(R.string.text_completion);
            if(isPadLayout) {
                ((TextView)mBodyLayout.findViewById(R.id.text_body)).setText(R.string.text_activity);
            }
        }
        if (info2.isEmpty()) {
            ((TextView)mBodyLayout.findViewById(R.id.l2_txt)).setText("");
        } else {
            ((TextView)mBodyLayout.findViewById(R.id.l2_txt)).setText(info2);
        }
    }

    public void setEnergyText(String info, String info2) {
        if(info.isEmpty()) {
            ((TextView)mMindSleepLayout.findViewById(R.id.r1_txt)).setText("");
            ((TextView)mMindSleepLayout.findViewById(R.id.Average)).setText("");
        } else {
            ((TextView)mMindSleepLayout.findViewById(R.id.r1_txt)).setText(info);
            //((TextView)mMindSleepLayout.findViewById(R.id.Average)).setText(R.string.text_average);
            if(isPadLayout) {
                if(WApplication.getInstance().getConnectedDevice().getIsRobin()){
                    ((TextView)mMindSleepLayout.findViewById(R.id.text_mind)).setText(R.string.text_energy);
                }
                else{
                    ((TextView)mMindSleepLayout.findViewById(R.id.text_mind)).setText(string.text_sleep);
                }
            }
        }
        if (info2.isEmpty()) {
            ((TextView)mMindSleepLayout.findViewById(R.id.r2_txt)).setText("");
        } else {
            ((TextView)mMindSleepLayout.findViewById(R.id.r2_txt)).setText(info2);
        }
    }

    public void addEventNotify(EventNotify eNotify) {
        m_eEventNotify = eNotify;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (m_isAutoing)
            return false;

        mGestureDetector.onTouchEvent(event);

        if (event.getAction() == event.ACTION_UP) {
            if (!m_isRunning) {
                m_autoSpeed = m_limitSpeed;
                if (m_leftGroup.get(0).getX() != 0) {
                    if (!m_isLimitRubStatus_forward && !m_isLimitRubStatus_previous) {
                        if (isPadLayout) {
                            if (m_leftGroup.get(m_leftGroupNum - 2).getX() > (m_leftGroupPos
                                    .get(m_leftGroupNum - 1).x - m_leftGroupPos.get(m_leftGroupNum - 2).x) / 2)
                                m_curMoving_type = MOVING_TYPE.FORWARD_START;
                            else
                                m_curMoving_type = MOVING_TYPE.BACK_START;
                        } else {
                            if (m_leftGroup.get(m_leftGroupNum - 2).getX() < -((m_leftGroupPos
                                    .get(m_leftGroupNum - 2).x - m_leftGroupPos.get(m_leftGroupNum - 1).x) / 2))
                                m_curMoving_type = MOVING_TYPE.FORWARD_START;
                            else
                                m_curMoving_type = MOVING_TYPE.BACK_START;
                        }

                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(m_curDate - oneDay);
                        if (c.getTime().getDate() < m_firstDay
                                && c.getTime().getMonth() == m_firstMonth
                                && c.getTime().getYear() == m_firstYear) {
                            m_curMoving_type = MOVING_TYPE.BACK_START;
                        }

                        fling_animation(m_curMoving_type);

                    } else {
                        if (m_isLimitRubStatus_forward) {
                            autoForward();
                        } else if (m_isLimitRubStatus_previous) {
                            autoBack();
                        }
                    }
                }
            }
            m_isScroll = false;
        }

        return false;
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        m_speedV = 0;
        m_isAutoing = false;
        m_isLimit = false;

        isAnimationRunning(false);
        m_limitRub_Speed = 2;

        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float velocity = 0;
        float e1pos = 0, e2pos = 0;

        if (e1 == null || e2 == null)
            return false; // i don't know why does MotionEvent is null sometimes?

        if(isPadLayout) {
            velocity = velocityX;
            e1pos = e1.getX();
            e2pos = e2.getX();
        } else {
            velocity = velocityY;
            e1pos = e1.getY();
            e2pos = e2.getY();
        }

        if (Math.abs(velocity) <= m_flingIgnore)
            return false;
        if (m_isLimitRubStatus_forward || m_isLimitRubStatus_previous) {
            return false;
        }

        isAnimationRunning(true);

        if (Math.abs((e1pos - e2pos) / m_metrics.density) >= m_fling_dis) {
            m_autoSpeed = Math.abs(velocity * m_flingSpeed_rub);
        } else {
            if ((Math.abs((e1pos - e2pos) / m_metrics.density) / (Math.abs((float)(e2
                    .getEventTime() - e2.getDownTime())) / (float)1000)) * m_rubNumber >= m_fling_speed) {
                m_autoSpeed = Math.abs(velocity * m_flingSpeed_rub);
            } else {
                m_autoSpeed = m_limitSpeed;
            }
        }

        // sensity test code
        // ((TextView)m_context.findViewById(R.id.sensity_value)).setText((Math.abs((arg0.getY()-arg1.getY())/m_metrics.density)/(Math.abs((float)(arg1.getEventTime()-arg1.getDownTime()))/(float)1000))*m_rubNumber+"");
        // ((TextView)m_context.findViewById(R.id.limited_dis_txt)).setText(Math.abs((arg0.getY()-arg1.getY())/m_metrics.density)+"");

        // m_autoSpeed=Math.abs(arg3*flingSpeed_rub);

        if (velocity > 0) {
            // autoForward();
            fling_animation(MOVING_TYPE.FORWARD_START);
        } else {
            // autoBack();
            fling_animation(MOVING_TYPE.BACK_START);
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float distance = 0;
        float speed = 0;

        if(isPadLayout) {
            distance = distanceX;
        } else {
            distance = distanceY;
        }

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(m_curDate);
        if (c.getTime().getDate() == m_firstDay && c.getTime().getMonth() == m_firstMonth
                && c.getTime().getYear() == m_firstYear) {
            setPreTextVisible(View.INVISIBLE);
        } else {
            if (m_isScroll) {
                setPreTextVisible(View.VISIBLE);
            }
        }

        if (distance < 0) {
            m_curDirection = isPadLayout ? DIRECTION.RIGHT : DIRECTION.DOWN;
        }
        if (distance > 0) {
            m_curDirection = isPadLayout ? DIRECTION.LEFT : DIRECTION.UP;
        }

        distance *= m_rubNumber/** (m_metrics.density/3) */;

        if (!m_isScroll) {
            if (m_curDirection == DIRECTION.DOWN || m_curDirection == DIRECTION.RIGHT) {
                if (m_eEventNotify != null) {
                    m_curMoving_type = MOVING_TYPE.FORWARD_START;
                    sendNotify();
                }
            } else {
                if (m_eEventNotify != null) {
                    m_isBackStart = false;
                }
            }
        }

        speed = isPadLayout ? -distance : distance;
        animationUpdate(speed);
        m_isScroll = true;

        return false;
    }

    private void animationUpdate(float V_speed) {
        if (Math.abs(V_speed) > m_baseNumber / 3) { // protect the limited animation not to be over
            if (V_speed < 0) {
                V_speed = -m_baseNumber / 3;
            } else if (V_speed > 0) {
                V_speed = m_baseNumber / 3;
            }
        }
        float left2x = m_leftGroup.get(m_leftGroupNum - 2).getX();
        if (isPadLayout) {
            if (left2x + V_speed > m_baseNumber) {
                V_speed = m_baseNumber - m_leftGroup.get(m_leftGroupNum - 2).getX();
            } else if (m_leftGroup.get(m_leftGroupNum - 2).getX() + V_speed < 0) {
                V_speed = -m_leftGroup.get(m_leftGroupNum - 2).getX();
            }
        } else {
            if (left2x + V_speed < -m_baseNumber) {
                V_speed = -m_baseNumber - m_leftGroup.get(m_leftGroupNum - 2).getX();
            } else if (m_leftGroup.get(m_leftGroupNum - 2).getX() + V_speed > 0) {
                V_speed = -m_leftGroup.get(m_leftGroupNum - 2).getX();
            }
        }

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(m_curDate);

        if (!m_isAutoing) {
            V_speed = performNoneAutoAnimation(V_speed, c);
        }

        //region update calendar view
        for (int i = 0; i < m_calendarGroupNum; i++) {
            if(isPadLayout) {
                m_calList.get(i).setX(m_calList.get(i).getX() + (float)(V_speed * m_calRatio.get(i)));
            } else {
                m_calList.get(i).setY(m_calList.get(i).getY() + (float)(-V_speed * m_calRatio.get(i)));
            }
        }

        if (isPadLayout) {
            for (int i = 0; i < m_caldotGroupNum; i++) {
                m_caldotList.get(i).setX(
                        m_caldotList.get(i).getX() + (float)(V_speed * m_caldotRatio.get(i)));
            }
        }
        //endregion calendar view

        //region body view
        for (int i = 0; i < m_leftGroupNum; i++) {
            if (isPadLayout) {
                m_leftGroup.get(i).setX(
                        m_leftGroup.get(i).getX() + (float)(V_speed * m_leftV_Ratio.get(i)));
                m_rightGroup.get(i).setX(
                        m_rightGroup.get(i).getX() + (float)(V_speed * m_rightV_Ratio.get(i)));
            } else {
                m_leftGroup.get(i).setX(
                        m_leftGroup.get(i).getX() + (float)(V_speed * m_leftV_Ratio.get(i)));
                m_rightGroup.get(i).setX(
                        m_rightGroup.get(i).getX() + (float)(-V_speed * m_rightV_Ratio.get(i)));
            }

            if (i == m_leftGroupNum - 2) {
                if (m_curMoving_type == MOVING_TYPE.FORWARD_END) {
                    m_curMoving_type = MOVING_TYPE.FORWARD_START;
                    sendNotify();
                } else {
                    if (isPadLayout) {
                        if (m_leftGroup.get(i).getX() <= m_leftGroupPos.get(i + 1).x - m_leftGroupPos.get(i).x
                                && m_leftGroup.get(i).getX() > 0) {
                            if (!m_isBackStart) {
                                m_isBackStart = true;
                                m_curMoving_type = MOVING_TYPE.BACK_START;
                                sendNotify();
                            }
                        }
                    } else {
                        if (m_leftGroup.get(i).getX() >= m_leftGroupPos.get(i + 1).x - m_leftGroupPos.get(i).x
                                && m_leftGroup.get(i).getX() < 0) {
                            if (!m_isBackStart) {
                                m_isBackStart = true;
                                m_curMoving_type = MOVING_TYPE.BACK_START;
                                sendNotify();
                            }
                        }
                    }
                }

                boolean isMove = false;
                boolean isBack = false;
                if(isPadLayout) {
                    if (m_leftGroup.get(i).getX() >= m_leftGroupPos.get(i + 1).x - m_leftGroupPos.get(i).x) {
                        isMove = true;
                        isBack = false;
                    } else if (m_leftGroup.get(i).getX() <= 0) {
                        isMove = true;
                        isBack = true;
                    }
                } else {
                    if (m_leftGroup.get(i).getX() <= m_leftGroupPos.get(i + 1).x - m_leftGroupPos.get(i).x) {
                        isMove = true;
                        isBack = false;
                    } else if (m_leftGroup.get(i).getX() >= 0) {
                        isMove = true;
                        isBack = true;
                    }
                }

                if (isMove && !isBack) {
                    for (int j = 0; j < m_calendarGroupNum; j++) {
                        if(isPadLayout) {
                            m_calList.get(j).setX(m_calGroupPos.get(j).x);
                        } else {
                            m_calList.get(j).setY(m_calGroupPos.get(j).y);
                        }
                        m_calList.get(j).setScaleX(1);
                        m_calList.get(j).setScaleY(1);
                    }

                    if (isPadLayout) {
                        for (int j = 0; j < m_caldotGroupNum; j++) {
                            m_caldotList.get(j).setX(m_caldotGroupPos.get(j).x);
                            m_caldotList.get(j).setScaleX(1);
                            m_caldotList.get(j).setScaleY(1);
                        }
                    }

                    for (int j = 0; j < m_leftGroupNum; j++) {
                        m_leftGroup.get(j).setX(0);
                        m_L_circleGroup.get(j).setScaleX(1);
                        m_L_circleGroup.get(j).setScaleY(1);
                        m_L_leaveGroup.get(j).setScaleX(1);
                        m_L_leaveGroup.get(j).setScaleY(1);
                        m_L_leaveGroup.get(j).getChildAt(0).setAlpha(0);
                        m_L_leaveGroup.get(j).getChildAt(1).setAlpha(1);
                        m_L_circleGroup.get(j).getChildAt(1).setAlpha(1);
                        m_leftGroup.get(j).setAlpha(1);

                        m_rightGroup.get(j).setX(0);
                        m_R_circleGroup.get(j).setScaleX(1);
                        m_R_circleGroup.get(j).setScaleY(1);
                        m_R_leaveGroup.get(j).setScaleX(1);
                        m_R_leaveGroup.get(j).setScaleY(1);
                        m_R_leaveGroup.get(j).getChildAt(0).setAlpha(0);
                        m_R_leaveGroup.get(j).getChildAt(1).setAlpha(1);
                        m_R_circleGroup.get(j).getChildAt(1).setAlpha(1);
                        m_rightGroup.get(j).setAlpha(1);
                    }

                    if (m_eEventNotify != null) {
                        m_curMoving_type = MOVING_TYPE.FORWARD_END;
                        m_curDate -= oneDay;
                        setDate(m_curDate);
                        sendNotify();
                        setNextTextVisible(View.VISIBLE);
                        setPreTextVisible(View.INVISIBLE);
                        c.setTimeInMillis(m_curDate);
                      //  Log.i("smile",m_calendar.getTime()+"  curr:   "+c.getTime());
                        if (m_calendar.getTime().getDate() <= c.getTime().getDate()
                                && m_calendar.getTime().getMonth() == c.getTime().getMonth()
                                && m_calendar.getTime().getYear() == c.getTime().getYear()) {
                            m_isLimitRubStatus_forward = true;
                            m_isLimit = true;
                            m_speedV = 0;
                            return;
                        }
                        else if(m_isAutoing && m_speedV<0)
                        {
                            m_speedV = -m_speedV;
                            return;
                        }
                    }
                    m_isLimit = false;
                    return;
                } else if (isMove && isBack) {
                    if (m_isLimit) {
                        m_curMoving_type = MOVING_TYPE.BACK_END;
                        sendNotify();
                        setDate(m_curDate);
                        if (m_isLimitRubStatus_forward || m_isLimitRubStatus_previous)
                            m_speedV = 0;
                        setPreTextVisible(View.INVISIBLE);

                        for (int j = 0; j < m_calendarGroupNum; j++) {
                            if(isPadLayout) {
                                m_calList.get(j).setX(m_calGroupPos.get(j).x);
                            } else {
                                m_calList.get(j).setY(m_calGroupPos.get(j).y);
                            }
                            m_calList.get(j).setScaleX(1);
                            m_calList.get(j).setScaleY(1);
                        }

                        if (isPadLayout) {
                            for (int j = 0; j < m_caldotGroupNum; j++) {
                                m_caldotList.get(j).setX(m_caldotGroupPos.get(j).x);
                                m_caldotList.get(j).setScaleX(1);
                                m_caldotList.get(j).setScaleY(1);
                            }
                        }

                        for (int j = 0; j < m_leftGroupNum; j++) {
                            m_leftGroup.get(j).setX(0);
                            m_L_circleGroup.get(j).setScaleX(1);
                            m_L_circleGroup.get(j).setScaleY(1);
                            m_L_leaveGroup.get(j).setScaleX(1);
                            m_L_leaveGroup.get(j).setScaleY(1);
                            m_L_leaveGroup.get(j).getChildAt(0).setAlpha(0);
                            m_L_leaveGroup.get(j).getChildAt(1).setAlpha(1);
                            m_L_circleGroup.get(j).getChildAt(1).setAlpha(1);
                            m_leftGroup.get(j).setAlpha(1);

                            m_rightGroup.get(j).setX(0);
                            m_R_circleGroup.get(j).setScaleX(1);
                            m_R_circleGroup.get(j).setScaleY(1);
                            m_R_leaveGroup.get(j).setScaleX(1);
                            m_R_leaveGroup.get(j).setScaleY(1);
                            m_R_leaveGroup.get(j).getChildAt(0).setAlpha(0);
                            m_R_leaveGroup.get(j).getChildAt(1).setAlpha(1);
                            m_R_circleGroup.get(j).getChildAt(1).setAlpha(1);
                            m_rightGroup.get(j).setAlpha(1);
                        }
                        return;
                    } else {
                        if (!m_isAutoing)
                            c.setTimeInMillis(m_curDate - oneDay);
                        else
                            c.setTimeInMillis(m_curDate);

                        if (m_calendar.getTime().getDate() <= c.getTime().getDate()
                                && m_calendar.getTime().getMonth() == c.getTime().getMonth()
                                && m_calendar.getTime().getYear() == c.getTime().getYear()) {
                            m_isLimitRubStatus_previous = true;
                            m_isLimit = true;
                            m_speedV = 0;
                            return;
                        }

                        m_curDate += oneDay;
                        setDate(m_curDate);
                        for (int j = 0; j < m_leftGroupNum; j++) {
                            float l_circleScaleRatio;
                            float l_leaveScaleRatio;
                            if (j == m_leftGroupNum - 1) {
                                l_circleScaleRatio = (float)m_L_largeView.getMeasuredWidth()
                                        / m_L_circleGroup.get(j).getChildAt(1).getMeasuredWidth();
                                l_leaveScaleRatio = l_circleScaleRatio;
                                m_leftGroup
                                        .get(j)
                                        .setX(-(m_leftGroupPos.get(j).x - (m_L_largeView.getX() + m_L_largeView
                                                .getMeasuredWidth() / 2)));
                                m_leftGroup.get(j).setAlpha(0);
                            } else {
                                l_circleScaleRatio = (float)m_L_circleGroup.get(j + 1)
                                        .getChildAt(1).getMeasuredWidth()
                                        / m_L_circleGroup.get(j).getChildAt(1).getMeasuredWidth();
                                l_leaveScaleRatio = (float)m_L_leaveGroup.get(j + 1).getChildAt(1)
                                        .getMeasuredWidth()
                                        / m_L_leaveGroup.get(j).getChildAt(1).getMeasuredWidth();
                                m_leftGroup.get(j).setX(
                                        m_leftGroup.get(j).getX()
                                                - (m_leftGroupPos.get(j).x - m_leftGroupPos
                                                .get(j + 1).x));
                                m_leftGroup.get(j).setAlpha(1);
                            }

                            m_L_circleGroup.get(j).setScaleX(l_circleScaleRatio);
                            m_L_circleGroup.get(j).setScaleY(l_circleScaleRatio);
                            m_L_leaveGroup.get(j).setScaleX(l_leaveScaleRatio);
                            m_L_leaveGroup.get(j).setScaleY(l_leaveScaleRatio);
                            m_L_leaveGroup.get(j).getChildAt(0).setAlpha(1);
                            m_L_leaveGroup.get(j).getChildAt(1).setAlpha(0);
                            m_L_circleGroup.get(j).getChildAt(1).setAlpha(0);

                            float r_circleScaleRatio;
                            float r_leaveScaleRatio;
                            if (j == m_rightGroupNum - 1) {
                                r_circleScaleRatio = (float)m_R_largeView.getMeasuredWidth()
                                        / m_R_circleGroup.get(j).getChildAt(1).getMeasuredWidth();
                                r_leaveScaleRatio = r_circleScaleRatio;
                                m_rightGroup
                                        .get(j)
                                        .setX(-(m_rightGroupPos.get(j).x - (m_R_largeView.getX() + m_R_largeView
                                                .getMeasuredWidth() / 2)));
                                m_rightGroup.get(j).setAlpha(0);
                            } else {
                                r_circleScaleRatio = (float)m_R_circleGroup.get(j + 1)
                                        .getChildAt(1).getMeasuredWidth()
                                        / m_R_circleGroup.get(j).getChildAt(1).getMeasuredWidth();
                                r_leaveScaleRatio = (float)m_R_leaveGroup.get(j + 1).getChildAt(1)
                                        .getMeasuredWidth()
                                        / m_R_leaveGroup.get(j).getChildAt(1).getMeasuredWidth();
                                m_rightGroup.get(j).setX(
                                        m_rightGroup.get(j).getX()
                                                - (m_rightGroupPos.get(j).x - m_rightGroupPos
                                                .get(j + 1).x));
                                m_rightGroup.get(j).setAlpha(1);
                            }
                            m_R_circleGroup.get(j).setScaleX(r_circleScaleRatio);
                            m_R_circleGroup.get(j).setScaleY(r_circleScaleRatio);
                            m_R_leaveGroup.get(j).setScaleX(r_leaveScaleRatio);
                            m_R_leaveGroup.get(j).setScaleY(r_leaveScaleRatio);
                            m_R_leaveGroup.get(j).getChildAt(0).setAlpha(1);
                            m_R_leaveGroup.get(j).getChildAt(1).setAlpha(0);
                            m_R_circleGroup.get(j).getChildAt(1).setAlpha(0);
                        }

                        for (int j = 0; j < m_calendarGroupNum; j++) {
                            float c_ScaleRatio;
                            if (j == m_calendarGroupNum - 1) {
                                c_ScaleRatio = 1;
                                if (isPadLayout) {
                                    m_calList.get(j).setX(
                                            m_calGroupPos.get(j).x
                                                    + (m_calGroupPos.get(m_calendarGroupNum - 1).x - m_calGroupPos
                                                    .get(m_calendarGroupNum - 2).x));
                                } else {
                                    m_calList.get(j).setY(
                                            m_calGroupPos.get(j).y
                                                    + (m_calGroupPos.get(m_calendarGroupNum - 1).y - m_calGroupPos
                                                    .get(m_calendarGroupNum - 2).y));
                                }
                            } else {
                                c_ScaleRatio = (float)((TextView)m_calList.get(j + 1))
                                        .getTextSize() / ((TextView)m_calList.get(j)).getTextSize();
                                if (isPadLayout) {
                                    m_calList.get(j).setX(
                                            m_calGroupPos.get(j).x
                                                    - (m_calGroupPos.get(j).x - m_calGroupPos
                                                    .get(j + 1).x));
                                } else {
                                    m_calList.get(j).setY(
                                            m_calGroupPos.get(j).y
                                                    - (m_calGroupPos.get(j).y - m_calGroupPos
                                                    .get(j + 1).y));
                                }
                            }
                            m_calList.get(j).setScaleX(c_ScaleRatio);
                            m_calList.get(j).setScaleY(c_ScaleRatio);
                        }

                        scaleMoveBackDotForPad();

                        if (m_isLimitRubStatus_forward || m_isLimitRubStatus_previous)
                            m_speedV = 0;
                        if (m_isBackStart) {
                            m_curMoving_type = MOVING_TYPE.BACK_END;
                            sendNotify();
                            m_isBackStart = false;
                        }
                        return;
                    }
                }
            }

            float l_circleScaleRatio;
            float l_leaveScaleRatio;
            float speedRatio;
            if (i == m_leftGroupNum - 1) {
                l_circleScaleRatio = (float)m_L_largeView.getMeasuredWidth()
                        / (float)m_L_circleGroup.get(i).getChildAt(1).getMeasuredWidth();
                l_leaveScaleRatio = l_circleScaleRatio;
                speedRatio = (float)Math.abs(m_leftGroup.get(i).getX()
                        / (float)(m_leftGroupPos.get(i).x - (m_L_largeView.getX() + m_L_largeView
                        .getMeasuredWidth() / 2)))
                        * m_fadeOutTime;
                m_leftGroup.get(i).setAlpha(1-speedRatio < 0 ? 0 : 1-speedRatio);
            } else {
                l_circleScaleRatio = (float)m_L_circleGroup.get(i + 1).getChildAt(1)
                        .getMeasuredWidth()
                        / (float)m_L_circleGroup.get(i).getChildAt(1).getMeasuredWidth();
                l_leaveScaleRatio = (float)m_L_leaveGroup.get(i + 1).getChildAt(1)
                        .getMeasuredWidth()
                        / (float)m_L_leaveGroup.get(i).getChildAt(1).getMeasuredWidth();
                speedRatio = (float)Math.abs(m_leftGroup.get(i).getX()
                        / (float)(m_leftGroupPos.get(i).x - m_leftGroupPos.get(i + 1).x));
            }

            m_L_circleGroup.get(i).setScaleX(1 + ((l_circleScaleRatio - 1) * speedRatio));
            m_L_circleGroup.get(i).setScaleY(1 + ((l_circleScaleRatio - 1) * speedRatio));

            m_L_leaveGroup.get(i).setScaleX(1 + ((l_leaveScaleRatio - 1) * speedRatio));
            m_L_leaveGroup.get(i).setScaleY(1 + ((l_leaveScaleRatio - 1) * speedRatio));

            if (m_curDirection == DIRECTION.DOWN || m_curDirection == DIRECTION.RIGHT) {
                if (i != m_leftGroupNum - 1) {
                    m_L_leaveGroup.get(i).getChildAt(0).setAlpha(speedRatio);
                    m_L_leaveGroup.get(i).getChildAt(1).setAlpha(1 - speedRatio);
                }
                m_L_circleGroup.get(i).getChildAt(1).setAlpha(1 - speedRatio);
            } else {
                if (i != m_leftGroupNum - 1) {
                    m_L_leaveGroup.get(i).getChildAt(0).setAlpha(speedRatio);
                    m_L_leaveGroup.get(i).getChildAt(1).setAlpha(1 - speedRatio);
                } else {
                    m_L_leaveGroup.get(i).getChildAt(0).setAlpha(0);
                    m_L_leaveGroup.get(i).getChildAt(1).setAlpha(1);
                }
                m_L_circleGroup.get(i).getChildAt(1).setAlpha(1 - speedRatio);
            }

            float r_circleScaleRatio;
            float r_leaveScaleRatio;
            float speedRatio2;
            if (i == m_rightGroupNum - 1) {
                r_circleScaleRatio = (float)m_R_largeView.getMeasuredWidth()
                        / (float)m_R_circleGroup.get(i).getChildAt(1).getMeasuredWidth();
                r_leaveScaleRatio = r_circleScaleRatio;
                speedRatio2 = (float)Math.abs(m_rightGroup.get(i).getX()
                        / (float)(m_rightGroupPos.get(i).x - (m_R_largeView.getX() + m_R_largeView
                        .getMeasuredWidth() / 2)))
                        * m_fadeOutTime;
                m_rightGroup.get(i).setAlpha(1-speedRatio2 < 0 ? 0 : 1-speedRatio2);
            } else {
                r_circleScaleRatio = (float)m_R_circleGroup.get(i + 1).getChildAt(1)
                        .getMeasuredWidth()
                        / (float)m_R_circleGroup.get(i).getChildAt(1).getMeasuredWidth();
                r_leaveScaleRatio = (float)m_R_leaveGroup.get(i + 1).getChildAt(1)
                        .getMeasuredWidth()
                        / (float)m_R_leaveGroup.get(i).getChildAt(1).getMeasuredWidth();
                speedRatio2 = (float)Math.abs(m_rightGroup.get(i).getX()
                        / (float)(m_rightGroupPos.get(i).x - m_rightGroupPos.get(i + 1).x));
            }

            m_R_circleGroup.get(i).setScaleX(1 + ((r_circleScaleRatio - 1) * speedRatio2));
            m_R_circleGroup.get(i).setScaleY(1 + ((r_circleScaleRatio - 1) * speedRatio2));
            m_R_leaveGroup.get(i).setScaleX(1 + ((r_leaveScaleRatio - 1) * speedRatio2));
            m_R_leaveGroup.get(i).setScaleY(1 + ((r_leaveScaleRatio - 1) * speedRatio2));

            if (m_curDirection == DIRECTION.DOWN || m_curDirection == DIRECTION.RIGHT) {
                if (i != m_leftGroupNum - 1) {
                    m_R_leaveGroup.get(i).getChildAt(0).setAlpha(speedRatio2);
                    m_R_leaveGroup.get(i).getChildAt(1).setAlpha(1 - speedRatio2);
                }
                m_R_circleGroup.get(i).getChildAt(1).setAlpha(1 - speedRatio2);
            } else {
                if (i != m_leftGroupNum - 1) {
                    m_R_leaveGroup.get(i).getChildAt(0).setAlpha(speedRatio2);
                    m_R_leaveGroup.get(i).getChildAt(1).setAlpha(1 - speedRatio2);
                } else {
                    m_R_leaveGroup.get(i).getChildAt(0).setAlpha(0);
                    m_R_leaveGroup.get(i).getChildAt(1).setAlpha(1);
                }
                m_R_circleGroup.get(i).getChildAt(1).setAlpha(1 - speedRatio2);
            }
        }

        scaleCalenderNum();

        scaleDotForPad();
    }

    private float performNoneAutoAnimation(float V_speed, Calendar c) {
        m_isLimitRubStatus_previous = false;
        m_isLimitRubStatus_forward = false;

        if (m_curDirection == DIRECTION.UP || m_curDirection == DIRECTION.LEFT) { // Up or Left : back
            c.setTimeInMillis(m_curDate - oneDay);
            if (c.getTime().getDate() == m_calendar.getTime().getDate()
                    && m_calendar.getTime().getMonth() == c.getTime().getMonth()
                    && m_calendar.getTime().getYear() == c.getTime().getYear()) {
                if (isPadLayout) {
                    if (V_speed < 0
                            && m_leftGroup.get(m_leftGroupNum - 2).getX() > 0
                            && m_leftGroup.get(m_leftGroupNum - 2).getX() <=
                            (m_leftGroupPos.get(m_leftGroupNum - 1).x - m_leftGroupPos.get(m_leftGroupNum - 2).x)
                                    * m_limitRubAnimation_forward) {
                        m_limitRub_Speed *= 0.96f;

                        V_speed = -m_limitRub_Speed;
                        m_isLimitRubStatus_forward = true;
                        m_isLimitRubStatus_previous = false;

                        setNextTextVisible(View.INVISIBLE);

                        if (m_leftGroup.get(m_leftGroupNum - 2).getX() <=
                                (m_leftGroupPos.get(m_leftGroupNum - 1).x
                                        - m_leftGroupPos.get(m_leftGroupNum - 2).x) * 0.3f) {
                            V_speed = 0;
                        }
                    }
                } else {
                    if (V_speed > 0
                            && m_leftGroup.get(m_leftGroupNum - 2).getX() < 0
                            && m_leftGroup.get(m_leftGroupNum - 2).getX() >=
                            -(m_leftGroupPos.get(m_leftGroupNum - 2).x - m_leftGroupPos.get(m_leftGroupNum - 1).x)
                                    * m_limitRubAnimation_forward) {
                        m_limitRub_Speed *= 0.96f;

                        V_speed = m_limitRub_Speed;
                        m_isLimitRubStatus_forward = true;
                        m_isLimitRubStatus_previous = false;

                        setNextTextVisible(View.INVISIBLE);

                        if (m_leftGroup.get(m_leftGroupNum - 2).getX() >=
                                -(m_leftGroupPos.get(m_leftGroupNum - 2).x
                                        - m_leftGroupPos.get(m_leftGroupNum - 1).x) * 0.3f) {
                            V_speed = 0;
                        }
                    }
                }
            }
        } else if (m_curDirection == DIRECTION.DOWN || m_curDirection == DIRECTION.RIGHT) { // Down or Right : forward
            if ((c.getTime().getDate() <= m_firstDay && c.getTime().getMonth() == m_firstMonth && c.getTime().getYear() == m_firstYear)
                    || (c.getTime().getYear()==m_firstYear && c.getTime().getMonth()<m_firstMonth)
                    || c.getTime().getYear()<m_firstYear) {
                if (isPadLayout) {
                    if (m_leftGroup.get(m_leftGroupNum - 2).getX() > 0
                            && m_leftGroup.get(m_leftGroupNum - 2).getX() >=
                            (m_leftGroupPos.get(m_leftGroupNum - 1).x - m_leftGroupPos.get(m_leftGroupNum - 2).x)
                                    * m_limitRubAnimation_previous) {
                        m_limitRub_Speed *= 0.98f;

                        V_speed = m_limitRub_Speed;
                        m_isLimitRubStatus_previous = true;
                        m_isLimitRubStatus_forward = false;
                        setNextTextVisible(View.VISIBLE);
                        setPreTextVisible(View.INVISIBLE);

                        if (m_leftGroup.get(m_leftGroupNum - 2).getX() <=
                                (m_leftGroupPos.get(m_leftGroupNum - 1).x
                                        - m_leftGroupPos.get(m_leftGroupNum - 2).x) * 0.6f) {
                            V_speed = 0;
                        }
                    }
                } else {
                    if (m_leftGroup.get(m_leftGroupNum - 2).getX() < 0
                            && m_leftGroup.get(m_leftGroupNum - 2).getX() <=
                            -(m_leftGroupPos.get(m_leftGroupNum - 2).x - m_leftGroupPos.get(m_leftGroupNum - 1).x)
                                    * m_limitRubAnimation_previous) {
                        m_limitRub_Speed *= 0.98f;

                        V_speed = -m_limitRub_Speed;
                        m_isLimitRubStatus_previous = true;
                        m_isLimitRubStatus_forward = false;
                        setNextTextVisible(View.VISIBLE);
                        setPreTextVisible(View.INVISIBLE);

                        if (m_leftGroup.get(m_leftGroupNum - 2).getX() <=
                                -(m_leftGroupPos.get(m_leftGroupNum - 2).x
                                        - m_leftGroupPos.get(m_leftGroupNum - 1).x) * 0.6f) {
                            V_speed = 0;
                        }
                    }
                }
            }
        }
        return V_speed;
    }

    private void scaleMoveBackDotForPad() {
        if (isPadLayout) {
            for (int j = 0; j < m_caldotGroupNum; j++) {
                float c_ScaleRatio =1;
                if (j == m_caldotGroupNum - 1) {
                    m_caldotList.get(j).setX(
                            m_caldotGroupPos.get(j).x
                                    + (m_caldotGroupPos.get(m_caldotGroupNum - 1).x - m_caldotGroupPos
                                    .get(m_caldotGroupNum - 2).x));
                } else {
                    m_caldotList.get(j).setX(
                            m_caldotGroupPos.get(j).x
                                    - (m_caldotGroupPos.get(j).x - m_caldotGroupPos.get(j + 1).x));
                }
                m_caldotList.get(j).setScaleX(c_ScaleRatio);
                m_caldotList.get(j).setScaleY(c_ScaleRatio);
            }
        }
    }

    private void scaleCalenderNum() {
        for (int j = 0; j < m_calendarGroupNum; j++) {
            float c_ScaleRatio;
            float speedRatio3;
            if (j == m_calendarGroupNum - 1) {
                c_ScaleRatio = 1;
                speedRatio3 = 1;
            } else {
                c_ScaleRatio = (float)((TextView)m_calList.get(j + 1)).getTextSize()
                        / ((TextView)m_calList.get(j)).getTextSize();
                if (isPadLayout) {
                    speedRatio3 = (float)Math.abs((float)(m_calList.get(j).getX() - m_calGroupPos.get(j).x)
                            / (float)(m_calGroupPos.get(j).x - m_calGroupPos.get(j + 1).x));
                } else {
                    speedRatio3 = (float)Math.abs((float)(m_calList.get(j).getY() - m_calGroupPos.get(j).y)
                            / (float)(m_calGroupPos.get(j).y - m_calGroupPos.get(j + 1).y));
                }
            }
            m_calList.get(j).setScaleY(1 + ((c_ScaleRatio - 1) * speedRatio3));
            m_calList.get(j).setScaleX(1 + ((c_ScaleRatio - 1) * speedRatio3));
        }
    }

    private void scaleDotForPad() {
        if (isPadLayout) {
            for (int j = 0; j < m_caldotGroupNum; j++) {
                float c_ScaleRatio;
                float speedRatio3;
                if (j == m_caldotGroupNum - 1) {
                    c_ScaleRatio = 1;
                    speedRatio3 = 1;
                } else {
                    c_ScaleRatio = 1;
                    speedRatio3 = (float)Math
                            .abs((float)(m_caldotList.get(j).getX() - m_caldotGroupPos.get(j).x)
                                    / (float)(m_caldotGroupPos.get(j).x - m_caldotGroupPos.get(j + 1).x));
                }
                m_caldotList.get(j).setScaleY(1 + ((c_ScaleRatio - 1) * speedRatio3));
                m_caldotList.get(j).setScaleX(1 + ((c_ScaleRatio - 1) * speedRatio3));
            }
        }
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        return false;
    }

    private void autoSlowDownAnimation() {
        m_context.runOnUiThread(m_AutoRSDRunable);
    }

    private void Fling_SlowDownAnimation() {
        m_context.runOnUiThread(m_FlingRSDRunable);
    }

    private void autoForward() {
        isAnimationRunning(true);
        m_isAutoing = true;
        m_curDirection = isPadLayout ? DIRECTION.RIGHT : DIRECTION.DOWN;
        m_curMoving_type = MOVING_TYPE.FORWARD_START;
        sendNotify();
        m_speedV = isPadLayout ? m_AutoForwardSpeed : -m_AutoForwardSpeed;
        m_handler.post(m_ForwardRunable);
    }

    private void autoBack() {
        isAnimationRunning(true);
        m_isAutoing = true;
        m_curDirection = isPadLayout ? DIRECTION.LEFT : DIRECTION.UP;
        m_curMoving_type = MOVING_TYPE.BACK_START;
        sendNotify();
        m_speedV = isPadLayout ? -m_AutoBackSpeed : m_AutoBackSpeed;
        m_handler.post(m_BackRunable);
    }

    private void autoBack(final int targetCount) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(m_curDate);

        if (m_calendar.getTime().getDate() == c.getTime().getDate()
                && m_calendar.getTime().getMonth() == c.getTime().getMonth()
                && m_calendar.getTime().getYear() == c.getTime().getYear()) {
            return;
        }
        isAnimationRunning(true);
        m_isAutoing = true;
        m_curDirection = isPadLayout ? DIRECTION.LEFT : DIRECTION.UP;
        setPreTextVisible(View.VISIBLE);
        m_curMoving_type = MOVING_TYPE.BACK_START;
        animationUpdate(1); // must do that
        sendNotify();
        m_speedV = isPadLayout ? -m_backSpeed : m_backSpeed;
        m_handler.post(m_BackCountRunable);
    }

    private void fling_animation(final MOVING_TYPE _curMoving_type) {
        if (_curMoving_type == MOVING_TYPE.FORWARD_START) {
            m_speedV = isPadLayout ? m_autoSpeed : -m_autoSpeed;
        } else if (_curMoving_type == MOVING_TYPE.BACK_START) {
            m_speedV = isPadLayout ? -m_autoSpeed : m_autoSpeed;
        }
        m_curMoving_type = _curMoving_type;
        m_handler.post(m_FlingRunable);
    }

    private void isAnimationRunning(final boolean allow) {
        m_context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                m_context.findViewById(R.id.activity_btn).setEnabled(!allow);
                m_context.findViewById(R.id.energy_btn).setEnabled(!allow);
                mCalendarLayout.findViewById(R.id.today_btn).setEnabled(!allow);
                m_isRunning = allow;
            }
        });
    }

    private void sendNotify() {
        m_context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (m_eEventNotify != null) {
                    m_eEventNotify.handler(WellnessDailyAnimUIManager.this, m_curMoving_type, m_curDate);
                }
                m_context.findViewById(R.id.activity_btn).setTag(m_curDate);
                m_context.findViewById(R.id.energy_btn).setTag(m_curDate);
                if (isPadLayout) {
                    if (m_curMoving_type != MOVING_TYPE.ORIGINAL_POS) {
                        mCalendarLayout.findViewById(R.id.month_txt).setAlpha(0.2f);
                        mCalendarLayout.findViewById(R.id.week_txt).setAlpha(0.2f);
                        mCalendarLayout.findViewById(R.id.divide_txt).setAlpha(0.2f);
                        mCalendarLayout.findViewById(R.id.today_btn).setAlpha(0.2f);
                    } else {
                        mCalendarLayout.findViewById(R.id.month_txt).setAlpha(1);
                        mCalendarLayout.findViewById(R.id.week_txt).setAlpha(1);
                        mCalendarLayout.findViewById(R.id.divide_txt).setAlpha(1);
                        mCalendarLayout.findViewById(R.id.today_btn).setAlpha(1);
                    }
                }
            }
        });
    }

    public interface EventNotify {
        public void handler(WellnessDailyAnimUIManager self, MOVING_TYPE m_curMoving_type, long dateTime);
    }


}
