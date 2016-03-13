package com.asus.commonui.colorful;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ColorfulLinearLayout extends LinearLayout {

    private static final int WAIT_ACTION_BAR_LAYOUT_FINISH_COUNT = 1;
    private static final int MSG_WAIT_ACTION_BAR_LAYOUT_FINISH = 0;

    private int mInsetsBottom;
    private TextView mStatusBarBackground;
    private TextView mActionBarBackground;

    private int mStatusBarBackgroundVisibility;
    private int mActionBarBackgroundVisibility;

    private int mLastActionBarVisibility;
    private int mLastNavigationMode = -1;
    private int mRequestLayoutCount;

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_WAIT_ACTION_BAR_LAYOUT_FINISH: {
                    if (mRequestLayoutCount > 0) {
                        --mRequestLayoutCount;
                        requestLayout();
                    }
                } break;
            }
        }
    };

    public ColorfulLinearLayout(Context context) {
        super(context);
        initColorfulView();
    }

    public ColorfulLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initColorfulView();
    }

    public ColorfulLinearLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initColorfulView();
    }

    /**
     * Only support vertical layout
     * @param orientation Pass VERTICAL only.
     *
     * @attr ref android.R.styleable#LinearLayout_orientation
     */
    public void setOrientation(int orientation) {
        // Only support vertical layout
        super.setOrientation(LinearLayout.VERTICAL);
    }

    /**
     * Set the background color of status bar.
     * @param color color id. ex: Color.WHITE or 0xffffffff
     */
    public void setStatusBarBackgroundColor(int color) {
        if (mStatusBarBackground != null) {
            mStatusBarBackground.setBackgroundColor(color);
        }
    }

    /**
     * Set the background color of status bar.
     * @param id color resource id.
     */
    public void setStatusBarBackgroundColorResource(int resid) {
        if (mStatusBarBackground != null) {
            mStatusBarBackground.setBackgroundResource(resid);
        }
    }

    /**
     * Set the enabled state of status bar.
     *
     * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     * @attr ref android.R.styleable#View_visibility
     */
    public void setStatusBarBackgroundVisibility(int visibility) {
        if (mStatusBarBackground != null) {
            mStatusBarBackgroundVisibility = visibility;
            mStatusBarBackground.setVisibility(visibility);
        }
    }

    /**
     * Set the background color of action bar.
     * @param color color id. ex: Color.WHITE or 0xffffffff
     */
    public void setActionBarBackgroundColor(int color) {
        if (mActionBarBackground != null) {
            mActionBarBackground.setBackgroundColor(color);
        }
    }

    /**
     * Set the background color of action bar.
     * @param id color resource id.
     */
    public void setActionBarBackgroundColorResource(int resid) {
        if (mActionBarBackground != null) {
            mActionBarBackground.setBackgroundResource(resid);
        }
    }

    /**
     * Set the enabled state of action bar.
     *
     * @param visibility One of {@link #VISIBLE}, {@link #INVISIBLE}, or {@link #GONE}.
     * @attr ref android.R.styleable#View_visibility
     */
    public void setActionBarBackgroundVisibility(int visibility) {
        if (mActionBarBackground != null) {
            mActionBarBackgroundVisibility = visibility;
            mActionBarBackground.setVisibility(visibility);
        }
    }

    public TextView getStatusBarBackground() {
        return mStatusBarBackground;
    }

    public TextView getActionBarBackground() {
        return mActionBarBackground;
    }

    @Override
    public void onWindowSystemUiVisibilityChanged(int visible) {
        super.onWindowSystemUiVisibilityChanged(visible);

        boolean change = false;
        final boolean stable = (visible & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0;
        final int statusBarBackgroundVisibility = stable
                ? mStatusBarBackgroundVisibility : View.GONE;
        final int actionBarBackgroundVisibility = stable
                ? mActionBarBackgroundVisibility : View.GONE;

        if (mStatusBarBackground != null
                && mStatusBarBackground.getVisibility() != statusBarBackgroundVisibility) {
            mStatusBarBackground.setVisibility(statusBarBackgroundVisibility);
            change = true;
        }
        if (mActionBarBackground != null
                && mActionBarBackground.getVisibility() != actionBarBackgroundVisibility) {
            mActionBarBackground.setVisibility(actionBarBackgroundVisibility);
            change = true;
        }
        if (change) {
            requestFitSystemWindows();
        }
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        final boolean stable = isSystemUiLayoutStable();
        final int lastInsetsBottom = mInsetsBottom;
        if (!stable) {
            mInsetsBottom = 0;
        } else {
            mInsetsBottom = insets.bottom;
        }
        if (mInsetsBottom != lastInsetsBottom) {
            MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
            lp.bottomMargin = mInsetsBottom;
            requestLayout();
        }

        return stable;
    }

    @Override
    protected void measureChildWithMargins(View child,
            int parentWidthMeasureSpec, int widthUsed,
            int parentHeightMeasureSpec, int heightUsed) {

        if (child == mStatusBarBackground) {
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    getStatusBarHeight(), MeasureSpec.EXACTLY);
            child.measure(parentWidthMeasureSpec, childHeightMeasureSpec);
        } else if (child == mActionBarBackground) {
            final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    getActionBarHeight(), MeasureSpec.EXACTLY);
            child.measure(parentWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed,
                    parentHeightMeasureSpec, heightUsed);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (isSystemUiLayoutStable()) {
            requestWaitActionBarLayoutFinish(false, true);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (isSystemUiLayoutStable()) {
            requestWaitActionBarLayoutFinish(true, false);
        }
    }

    private void initColorfulView() {
        // Only support vertical layout
        setOrientation(LinearLayout.VERTICAL);

        mStatusBarBackground = new TextView(getContext());
        mActionBarBackground = new TextView(getContext());

        mStatusBarBackgroundVisibility = mStatusBarBackground.getVisibility();
        mActionBarBackgroundVisibility = mActionBarBackground.getVisibility();

        addView(mStatusBarBackground,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(mActionBarBackground,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private int getStatusBarHeight() {
        // We don't take the status bar into account if application is
        // not running on default display.
        final Display display = getDisplay();
        if (display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY) {
            return 0;
        }

        int h = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            h = getResources().getDimensionPixelSize(resourceId);
        }
        return h;
    }

    private int getActionBarHeight() {
        final ActionBar actionBar = getActionBar();
        int h = 0;
        if (actionBar != null) {
            h = actionBar.isShowing() ? actionBar.getHeight() : 0;
        } else {
            TypedValue tv = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            h = getResources().getDimensionPixelSize(tv.resourceId);
        }
        return h;
    }

    private ActionBar getActionBar() {
        final Context context = getContext();
        ActionBar actionBar = null;
        if (context instanceof Activity) {
            actionBar = ((Activity) context).getActionBar();
        }
        return actionBar;
    }

    private void requestWaitActionBarLayoutFinish(boolean forced, boolean sendMsg) {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            int visibility = actionBar.isShowing() ? VISIBLE : GONE;
            int mode = actionBar.getNavigationMode();
            if (forced
                    || mode != mLastNavigationMode
                    || visibility != mLastActionBarVisibility) {
                mLastActionBarVisibility = visibility;
                mLastNavigationMode = mode;
                mRequestLayoutCount = WAIT_ACTION_BAR_LAYOUT_FINISH_COUNT;
            }
            if (sendMsg && mRequestLayoutCount > 0) {
                mHandler.sendEmptyMessage(MSG_WAIT_ACTION_BAR_LAYOUT_FINISH);
            }
        }
    }

    private boolean isSystemUiLayoutStable() {
        final int vis = getWindowSystemUiVisibility();
        final boolean stable = (vis & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0;
        return stable;
    }
}