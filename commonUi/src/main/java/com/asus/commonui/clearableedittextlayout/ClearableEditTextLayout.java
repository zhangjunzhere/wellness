package com.asus.commonui.clearableedittextlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.asus.commonui.R;

public class ClearableEditTextLayout extends LinearLayout {

    private EditText mEditText;
    private ImageView mClearButton;
    private RelativeLayout mClearButtonLayout;
    private boolean mDarkStyle;

    private int mEditTextBgResId = R.drawable.asus_commonui_textfield_clearable_selector_light;
    private static final int mEditTextPaddingStartResId = R.dimen.asus_commonui_text_padding_start;
    private static final int mEditTextPaddingTopBottomResId = R.dimen.asus_commonui_text_padding_top_bottom;
    private int mClearButtonResId = R.drawable.asus_commonui_ic_clear_light;
    private static final int mClearButtonBgResId = R.drawable.asus_commonui_textfield_clearable_clear_bg;
    private static final int mBlockWidthResId = R.dimen.asus_commonui_clear_button_padding;

    public ClearableEditTextLayout(Context context) {
        this(context, null);
    }

    public ClearableEditTextLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearableEditTextLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClearableEditTextLayout, R.attr.darkStyle, 0);
        mDarkStyle = a.getBoolean(R.styleable.ClearableEditTextLayout_darkStyle, false);
        a.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (getChildAt(0) instanceof EditText) {
            mEditText = (EditText) getChildAt(0);
            mEditText.addTextChangedListener(mTextWatcher);
            mEditText.setBackground(null);
            final int edittextPaddingStart = (int) getResources().getDimension(mEditTextPaddingStartResId);
            final int edittextPaddingTopBottom = (int) getResources().getDimension(mEditTextPaddingTopBottomResId);
            mEditText.setPaddingRelative(edittextPaddingStart ,edittextPaddingTopBottom ,0 ,edittextPaddingTopBottom);

            mClearButton = new ImageView(getContext());
            mClearButton.setBackgroundResource(mClearButtonBgResId);
            mClearButton.setOnClickListener(mOnClickListener);

            View mBlockRegion = new View(getContext());
            mBlockRegion.setOnClickListener(null);

            final int blockWidth = (int) getResources().getDimension(mBlockWidthResId);
            mClearButtonLayout = new RelativeLayout(getContext());
            mClearButtonLayout.addView(mClearButton, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mClearButtonLayout.addView(mBlockRegion, blockWidth, ViewGroup.LayoutParams.MATCH_PARENT);
            mClearButtonLayout.setVisibility(View.GONE);

            updateBackgroundResource();
            this.addView(mClearButtonLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            this.setGravity(Gravity.CENTER_VERTICAL);
            this.getViewTreeObserver().addOnGlobalFocusChangeListener(mOnGlobalFocusChangeListener);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mEditText == null) {
            return;
        }

        if (!mEditText.isFocused()) {
            mClearButtonLayout.setVisibility(View.GONE);
        }

        if (mEditText instanceof AutoCompleteTextView) {
            int dropDownWidth = ((AutoCompleteTextView) mEditText).getDropDownWidth();
            if (dropDownWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
                ((AutoCompleteTextView) mEditText).setDropDownWidth(this.getWidth());
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            updateFocusedState();
        }
    }

    private TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 0) {
                mClearButtonLayout.setVisibility(View.GONE);
            } else {
                mClearButtonLayout.setVisibility(View.VISIBLE);
            }
        }

    };

    // Do NOT set any listener directly on mEditText, because there may be another listener in app side
    private OnGlobalFocusChangeListener mOnGlobalFocusChangeListener = new OnGlobalFocusChangeListener() {

        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            updateFocusedState();
            if (oldFocus == mEditText) {
                mClearButtonLayout.setVisibility(View.GONE);
            } else if ((newFocus == mEditText) && (mEditText.getText().length() > 0)) {
                mClearButtonLayout.setVisibility(View.VISIBLE);
            }
        }
    };

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mEditText.setText(null);
            mEditText.requestFocus();
        }

    };

    private void updateFocusedState() {
        boolean focused = mEditText.hasFocus();
        this.getBackground().setState(focused ? FOCUSED_STATE_SET : EMPTY_STATE_SET);
    }

    public void setDarkStyle(boolean darkStyle) {
        if (darkStyle != mDarkStyle) {
            mDarkStyle = darkStyle;
            updateBackgroundResource();
            updateFocusedState();
        }
    }

    private void updateBackgroundResource(){
        if (!mDarkStyle) {
            mEditTextBgResId = R.drawable.asus_commonui_textfield_clearable_selector_light;
            mClearButtonResId = R.drawable.asus_commonui_ic_clear_light;
        } else {
            mEditTextBgResId = R.drawable.asus_commonui_textfield_clearable_selector_dark;
            mClearButtonResId = R.drawable.asus_commonui_ic_clear_dark;
        }

        this.setBackgroundResource(mEditTextBgResId);
        mClearButton.setImageDrawable(getResources().getDrawable(mClearButtonResId));
    }

}