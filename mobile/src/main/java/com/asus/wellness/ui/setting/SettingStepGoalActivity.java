package com.asus.wellness.ui.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.asus.wellness.DataLayerManager;
import com.asus.wellness.R;
import com.asus.wellness.cm.CmHelper;
import com.asus.wellness.provider.StepGoalHelper;
import com.asus.wellness.provider.StepGoalTable;
import com.asus.wellness.ui.BaseActivity;
import com.asus.wellness.utils.GAApplication;
import com.asus.wellness.utils.RepeatingImageButton;
import com.asus.wellness.utils.RepeatingImageButton.RepeatListener;
import com.asus.wellness.utils.Utility;
import com.cmcm.common.statistics.CMAgent;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class SettingStepGoalActivity extends BaseActivity {

	private SeekBar mSeekBarStepGoal;
	private EditText mEditTextStepGoal;
	private TextView mTxtGoalSetted;
	private TextView mTxtActivityLevel;
	private TextView mTxtActivityLevelExplain;
	
	public static final int DEFAULT_STEP_GOAL=7000;
	public static final int MAX_GOAL_LIMIT = 999999999;
	public static final int MAX_LENGTH = 9;                // max length that edittext can be entered
	private static final int GOAL_SEEKBAR_INTERVAL = 100;  // 1 unit seekbar means 100 steps
	private static final int SECTION_INTERVAL = 25;        // each section has 25 unit seekbar
    public static int MAX_GOAL;                            // max steps on seekbar
	private DataLayerManager dataLayerManager;
    private  final int DEFAULT_MIN_STEP_GOAL= 1; //Change defalut min step goal from 1 to 0.
    private  final String DEFAULT_MIN_STEP_GOAL_STRING= "1";
	
	private RepeatingImageButton mAddButton;
    private RepeatingImageButton mDelButton;
    private int mAdjustInterval = GOAL_SEEKBAR_INTERVAL;
    HashMap<String,String> data=new HashMap<>();
    @Override
    public String getPageName(){
        return SettingStepGoalActivity.class.getSimpleName();
    }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dataLayerManager=new DataLayerManager(this);
		dataLayerManager.connectGoogleApiClient();
        initLayout();

        setTitle(R.string.setting_title_activity_goal);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

        Utility.trackerScreennView(getApplicationContext(), "Goto SettingStepGoal");
	}
	
	private void initLayout(){
		setContentView(R.layout.setting_step_goal_layout);
		mTxtGoalSetted=(TextView)findViewById(R.id.txt_goal_setted);
		mTxtActivityLevel=(TextView)findViewById(R.id.activity_goals_level);
		mTxtActivityLevelExplain=(TextView)findViewById(R.id.activity_goals_level_explain);
		settingEditText();
		settingSeekBar();
		settingIncludeBike();
		settingAdjustButtons();

		Cursor cursor = Utility.getStepGoalCursor(getApplicationContext());
        Cursor cursorPre = Utility.getPreStepGoalCursor(getApplicationContext());
        int stepGoal = DEFAULT_STEP_GOAL;
        if(cursor.moveToFirst()){
            stepGoal = cursor.getInt(cursor.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL));
		} else if(cursorPre.moveToFirst()){
            stepGoal = cursorPre.getInt(cursorPre.getColumnIndex(StepGoalTable.COLUMN_STEP_GOAL));
        }
        consistentGoalDataChangeInit(stepGoal);
		cursor.close();
        cursorPre.close();
	}

	/**
	 *  Increase and decrease buttons for step goal
	 */
	private void settingAdjustButtons() {
        if (Utility.getSizeDimens(SettingStepGoalActivity.this) != Utility.SIZEDIMENS.SW800DP) {
            return;
        }
        
        mAddButton = (RepeatingImageButton) findViewById(R.id.step_goal_adjust_add);
        mDelButton = (RepeatingImageButton) findViewById(R.id.step_goal_adjust_del);
        mAddButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                doAddGoal();
            }
        });
        mDelButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                doDelGoal();
            }
        });
        
        mAddButton.setRepeatListener(new RepeatListener() {
            
            @Override
            public void onRepeat(View v, long duration, int repeatcount) {
                if (mAdjustInterval < 500) {
                    mAdjustInterval += repeatcount * 100;
                }
                doAddGoal();
            }
        }, 200);
        mDelButton.setRepeatListener(new RepeatListener() {
            
            @Override
            public void onRepeat(View v, long duration, int repeatcount) {
                if (mAdjustInterval < 500) {
                    mAdjustInterval += repeatcount * 100;
                }
                doDelGoal();
            }
        }, 200);
        
        mAddButton.setOnTouchListener(mAdjustButtonTounchListener);
        mDelButton.setOnTouchListener(mAdjustButtonTounchListener);
    }
	
	private View.OnTouchListener mAdjustButtonTounchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mAdjustInterval = GOAL_SEEKBAR_INTERVAL;
            }
            return false;
        }
    };
	
	private void doAddGoal() {
        int goal = DEFAULT_MIN_STEP_GOAL;
        if (  mEditTextStepGoal.getText().toString().equals("")){
            goal = DEFAULT_MIN_STEP_GOAL;
        }else {
            goal = Integer.valueOf(mEditTextStepGoal.getText().toString());
        }

        int newGoal = goal + mAdjustInterval;
        if (newGoal < MAX_GOAL_LIMIT) {
            consistentGoal(newGoal);
        } else {
            consistentGoal(MAX_GOAL_LIMIT);
        }
	}
	
	private void doDelGoal() {
        int goal = DEFAULT_MIN_STEP_GOAL;
        if ( mEditTextStepGoal.getText().toString().equals("")){
            goal = DEFAULT_MIN_STEP_GOAL;
        }else {
            goal = Integer.valueOf(mEditTextStepGoal.getText().toString());
        }
        int newGoal = goal - mAdjustInterval;
        if (newGoal > 0) {
            consistentGoal(newGoal);
        } else {
            consistentGoal(DEFAULT_MIN_STEP_GOAL);
        }
	}

    private void settingSeekBar(){
		mSeekBarStepGoal = (SeekBar)findViewById(R.id.seekbar_adjust_step_goal);
		mSeekBarStepGoal.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
				    progress = progress * GOAL_SEEKBAR_INTERVAL;
					mEditTextStepGoal.setText(String.valueOf(progress));
			        mEditTextStepGoal.setSelection(mEditTextStepGoal.getText().length());
					mTxtGoalSetted.setText(String.valueOf(progress));
			        mTxtActivityLevel.setText(Utility.getIntensityString(SettingStepGoalActivity.this, progress));
			        mTxtActivityLevelExplain.setText(Utility.getIntensityExplainString(SettingStepGoalActivity.this, progress));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
	            mSeekBarStepGoal.getThumb().mutate().setAlpha(255);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				if (seekBar.getProgress() == 0){
                    consistentGoal(DEFAULT_MIN_STEP_GOAL);
                }else {
                    consistentGoal(seekBar.getProgress() * GOAL_SEEKBAR_INTERVAL);
                }

			}
			
		});

        mSectionColors = getResources().getIntArray(R.array.setting_step_goal_section_colors);
        MAX_GOAL = mSeekBarStepGoal.getMax() * GOAL_SEEKBAR_INTERVAL;
        
		mSeekBarStepGoal.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
			    measureSeekBarSize();
			    mSectionNum = mSeekBarStepGoal.getMax() / SECTION_INTERVAL;
			    mScalesNum = mSectionNum + 1;
			    
			    if (Utility.getSizeDimens(SettingStepGoalActivity.this) == Utility.SIZEDIMENS.SW800DP) {
	                drawSeparator();
	                drawStepGoalExplanation();
			    }

                drawSeekBarProgressBackground();
				drawScales();
				mSeekBarStepGoal.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		    
		});
	}
	
	private int mSeekBarThumbSize;
    private int mSeekBarWidth;
    private int mSeekBarHeight;
    private int mSeekBarProgressWidth;
    private int mSeekBarProgressMinHeight;
    
    private int mSectionNum;
    private int mScalesNum;
    
    private int[] mSectionColors;
	
    /**
     *  Measure and record the seekbar width, height, thumb size 
     */
	private void measureSeekBarSize() {
	    mSeekBarStepGoal.measure(MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.UNSPECIFIED),   
	            MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
	    mSeekBarThumbSize = mSeekBarStepGoal.getThumb().getIntrinsicWidth();
	    mSeekBarWidth = mSeekBarStepGoal.getWidth();
	    mSeekBarHeight = mSeekBarStepGoal.getHeight();
        mSeekBarProgressWidth = mSeekBarStepGoal.getWidth() - mSeekBarThumbSize;
	    mSeekBarProgressMinHeight = mSeekBarStepGoal.getMinimumHeight();
	}
	
	/**
	 *  Draw the dotted separator on each section (on sw800)
	 */
	private void drawSeparator() {
	    RelativeLayout seekbar_wrapper = (RelativeLayout) findViewById(R.id.seekbar_adjust_step_goal_wrapper);
	    for (int i = 0; i < mScalesNum; i++) {
	        if (i == 1 || i == mScalesNum - 1) {
                continue;
            }
	        
            LinearLayout dotted = new LinearLayout(this);

            dotted.setBackgroundResource(R.drawable.asus_wellness_ic_ling_dashed);
            dotted.measure(MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.UNSPECIFIED),   
                MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.UNSPECIFIED));
            
            int dottedWidth = dotted.getMeasuredWidth();
            dotted.setX(i * (mSeekBarProgressWidth / mSectionNum) + (mSeekBarThumbSize / 2) - dottedWidth / 2);
            
            seekbar_wrapper.addView(dotted, 0);
        }
	}
    
	/**
	 *  Draw step goal explanation on each section (on sw800)
	 */
    private void drawStepGoalExplanation() {
        RelativeLayout seekbar_wrapper = (RelativeLayout) findViewById(R.id.seekbar_adjust_step_goal_wrapper);
        CharSequence[] step_range = getResources().getTextArray(R.array.step_intensity_term_range);
        CharSequence[] step_intensity = getResources().getStringArray(R.array.step_intensity_term);
        
        int explainPaddingTop = 26;
        int rangePaddingTop = 8;
        
        for (int i = 0; i < mSectionNum; i++) {
            if (i == 1 || i > step_range.length) {
                continue;
            }
            
            int sectionWidth = mSeekBarProgressWidth / mSectionNum;
            
            TextView range = new TextView(this);
            TextView intensity = new TextView(this);
            if (i > 1) {
                range.setText(step_range[i - 1]);
                intensity.setText(step_intensity[i - 1]);
            } else if (i == 0) {
                range.setText(step_range[i]);
                intensity.setText(step_intensity[i]);
                sectionWidth = sectionWidth * 2;        // the width of the first section is twice than others
            }
            range.setTextColor(R.color.setting_step_goal_unit_light);
            if (Utility.getSizeDimens(SettingStepGoalActivity.this) == Utility.SIZEDIMENS.SW800DP) {
                intensity.setTextColor(mSectionColors[i]);
            }
            
            float scaleRatio, intensityPix, rangePix;
            scaleRatio = getResources().getDisplayMetrics().density;
            intensityPix = getResources().getDimension(R.dimen.setting_step_goal_explain_intensity_textsize);
            rangePix = getResources().getDimension(R.dimen.setting_step_goal_explain_unit_textsize);
            
            range.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (rangePix / scaleRatio));
            intensity.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (intensityPix / scaleRatio));
            range.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
                MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
            intensity.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
                    MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
            
            int rangeTextWidth = range.getMeasuredWidth();
            int intensityTextWidth = intensity.getMeasuredWidth();
            int intensityTextHeight = intensity.getMeasuredHeight();
            intensity.setX((i * sectionWidth + sectionWidth / 2) + (mSeekBarThumbSize / 2) - intensityTextWidth / 2);
            intensity.setY(mSeekBarHeight + explainPaddingTop);
            intensity.setTypeface(null, Typeface.BOLD);
            range.setX((i * sectionWidth + sectionWidth / 2) + (mSeekBarThumbSize / 2) - rangeTextWidth / 2);
            range.setY(intensity.getY() + rangePaddingTop + intensityTextHeight);
            
            seekbar_wrapper.addView(intensity, 0);
            seekbar_wrapper.addView(range, 0);
        }
    }
	
    /**
     *  Draw seekbar progress background, different color on each section
     */
	private void drawSeekBarProgressBackground() {
	    RelativeLayout seekbar_wrapper = (RelativeLayout) findViewById(R.id.seekbar_adjust_step_goal_wrapper);
	    
        for (int i = 0; i < mSectionNum; i++) {
            LinearLayout rect = new LinearLayout(this);

            rect.measure(MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.UNSPECIFIED),   
                MeasureSpec.makeMeasureSpec(LayoutParams.MATCH_PARENT, MeasureSpec.UNSPECIFIED));
            LayoutParams params = new LayoutParams(mSeekBarProgressWidth / mSectionNum, mSeekBarProgressMinHeight);
            rect.setLayoutParams(params);
            
            if (i == 0) {
                rect.setBackgroundResource(R.drawable.seekbar_activity_level_progress_left_shape);
            } else if (i == mSectionNum - 1) {
                rect.setBackgroundResource(R.drawable.seekbar_activity_level_progress_right_shape);
            } else {
                rect.setBackgroundColor(mSectionColors[i]);
            }
            
            rect.setX(i * (mSeekBarProgressWidth / mSectionNum) + (mSeekBarThumbSize / 2));
            rect.setY((mSeekBarHeight / 2) - (mSeekBarProgressMinHeight / 2));
            
            seekbar_wrapper.addView(rect, seekbar_wrapper.getChildCount() - 1);
        }
	}
	
	/**
	 *  draw scales on each section, sw800 is above the seekbar and others are below that
	 */
	private void drawScales() {
		RelativeLayout scaleArea = (RelativeLayout) findViewById(R.id.seekbar_adjust_step_goal_scales);
		for (int i = 0; i < mScalesNum; i++) {
			if (i == 1) {
				// do not show '2500' and '17500' unit
				continue;
			}
			TextView tv = new TextView(this);
            if (i == 0){
                tv.setText(DEFAULT_MIN_STEP_GOAL_STRING);
            }else {
                tv.setText(String.valueOf(i * SECTION_INTERVAL * GOAL_SEEKBAR_INTERVAL));
            }
			tv.setTextColor(R.color.setting_step_goal_unit_light);
			if (Utility.getSizeDimens(SettingStepGoalActivity.this) == Utility.SIZEDIMENS.SW800DP) {
			    tv.setTextColor(mSectionColors[i]);
	            if (i >= 6) {
	                tv.setText(String.valueOf("15000+"));
	                if (i == mScalesNum - 1) {
	                    tv.setVisibility(View.GONE);
	                }
	            }
			}
			
            float scaleRatio, dimenPix;
            scaleRatio = getResources().getDisplayMetrics().density;
            dimenPix = getResources().getDimension(R.dimen.setting_step_goal_unit_textsize);

            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (dimenPix / scaleRatio));
			tv.measure(MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED),   
				MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));
			int textViewWidth = tv.getMeasuredWidth();
			tv.setX(i * (mSeekBarProgressWidth / mSectionNum) + (mSeekBarThumbSize / 2) - textViewWidth / 2);
			scaleArea.addView(tv);
		}
	}
	
	private void settingIncludeBike(){
		final SharedPreferences sp=PreferenceManager.getDefaultSharedPreferences(this);
		Switch includeBikeSwitch=(Switch)findViewById(R.id.include_bike_activity_switch);
		includeBikeSwitch.setChecked(sp.getBoolean(getString(R.string.pref_key_include_bike)
                , getResources().getBoolean(R.bool.default_include_bike)));
		includeBikeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                Editor editor = sp.edit();
                editor.putBoolean(getString(R.string.pref_key_include_bike), isChecked);
                editor.commit();
            }

        });
	}
	
	private void settingEditText(){
		mEditTextStepGoal=(EditText)findViewById(R.id.edittext_adjust_step_goal);
		mEditTextStepGoal.setOnEditorActionListener(new OnEditorActionListener(){

            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String goal = v.getText().toString();
                    if (goal.length() <= 0) {
                        goal = DEFAULT_MIN_STEP_GOAL_STRING;
                    }
                    consistentGoal(Integer.valueOf(goal));
                }
                return false;
            }

		});
		
		mEditTextStepGoal.addTextChangedListener(new TextWatcher(){
		    
		    String beforeChangedText;

            @Override
            public void afterTextChanged(Editable editable) {
                String afterChangedText = editable.toString();
                int length = editable.length();
                if (length > MAX_LENGTH) {
                    beforeChangedText = afterChangedText;
                    afterChangedText = String.valueOf(MAX_GOAL_LIMIT);
                }
                
                if (length > 0 && Integer.valueOf(afterChangedText) > MAX_GOAL_LIMIT) {
                    afterChangedText = String.valueOf(MAX_GOAL_LIMIT);
                }
                
                if (!afterChangedText.equals(beforeChangedText) && 
                        afterChangedText.equals(String.valueOf(MAX_GOAL_LIMIT))) {
                    consistentGoal(Integer.valueOf(afterChangedText));
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int before,
                    int count) {
                beforeChangedText = mEditTextStepGoal.getText().toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString() != "" && !s.toString().equals("")){
                    String str = s.toString();
                    if(str.length() <= MAX_LENGTH){
                        int txt = Integer.parseInt(str);
                        if (txt == 0){
                            mEditTextStepGoal.setText("");
                            //mEditTextStepGoal.setSelection(mEditTextStepGoal.getText().length());
                            // mEditTextStepGoal.setSelection(1);
                        }
                    }
                }
            }
		    
		});
		
		final LinearLayout rootLayout = (LinearLayout) mEditTextStepGoal.getParent();
		rootLayout.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                rootLayout.setFocusable(true);
                rootLayout.setFocusableInTouchMode(true);
                rootLayout.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    String goal = mEditTextStepGoal.getText().toString();
                    if (goal.length() <= 0) {
                        goal = String.valueOf(DEFAULT_MIN_STEP_GOAL);
                    }
                    consistentGoal(Integer.valueOf(goal));
                    imm.hideSoftInputFromWindow(rootLayout.getWindowToken(), 0);
                }

                return false;
            }
        });

        mEditTextStepGoal.setFilters(new InputFilter[]{new InputFilterMin()});
	}

    private void consistentGoalDataChange(int goal){
        // Remove Tomorrow's goal - kim
        StepGoalHelper.getmInstance(this).deleteNextStepGoalFromDB();
        // End

        consistentGoalDataChangeInit(goal);

        data.clear();
        data.put("step_goal", String.valueOf(goal));
        data.put("step_goal_time", String.valueOf(System.currentTimeMillis()));
        CMAgent.onEvent(CmHelper.PROFILE_MSG_ID, data);
    }

    private void consistentGoalDataChangeInit(int goal){
        mSeekBarStepGoal.setProgress(goal / GOAL_SEEKBAR_INTERVAL);
        int cursorStart = mEditTextStepGoal.getSelectionStart();
        int cursorEnd = mEditTextStepGoal.getSelectionEnd();
        mEditTextStepGoal.setText(String.valueOf(goal));
        mEditTextStepGoal.setSelection(mEditTextStepGoal.getText().length());
        // TT:461511 Fix cursor on EditText with wrong position
        if (goal != 0) {
            try {
                mEditTextStepGoal.setSelection(cursorStart, cursorEnd);
            } catch (Exception e) { }
        }

        //TT:654750, by Emily
        int stepGoalET = DEFAULT_MIN_STEP_GOAL;
        if(!mEditTextStepGoal.getText().toString().equals("")){
            try {
                stepGoalET = Integer.valueOf(mEditTextStepGoal.getText().toString());
            }catch (Exception e)
            {
                stepGoalET = DEFAULT_MIN_STEP_GOAL;
            }
        }
        
        if (stepGoalET > MAX_GOAL) {
            mSeekBarStepGoal.getThumb().mutate().setAlpha(0);
        } else {
            mSeekBarStepGoal.getThumb().mutate().setAlpha(255);
        }

        mTxtGoalSetted.setText(String.valueOf(goal));
        mTxtActivityLevel.setText(Utility.getIntensityString(SettingStepGoalActivity.this, goal));
        mTxtActivityLevelExplain.setText(Utility.getIntensityExplainString(SettingStepGoalActivity.this, goal));
        StepGoalHelper.getmInstance(this).saveStepGoalToDB(goal);
    }

	private void consistentGoal(int goal){
        consistentGoalDataChange(goal);

		dataLayerManager.sendProfileToRobin();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dataLayerManager.disConnectGoogleApiClient();
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class InputFilterMin implements InputFilter{

        private int mMin = 1;
        @Override
        public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
            try{
                int input = Integer.parseInt(spanned.toString() + charSequence.toString());

                if (isInRange(input)){
                    return  null;
                }

            }catch (NumberFormatException nfe){}

            return "";
        }

        private boolean isInRange(int input){
            return  input >= mMin;
        }
    }
}
