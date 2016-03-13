package com.asus.commonui.syncprogress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.asus.commonui.R;

/**
 * Procedurally-drawn version of a horizontal indeterminate progress bar. Draws faster and more
 * frequently (by making use of the animation timer), requires minimal memory overhead, and allows
 * some configuration via attributes:
 * <ul>
 * <li>barColor (color attribute for the bar's solid color)
 * <li>barHeight (dimension attribute for the height of the solid progress bar)
 * <li>detentWidth (dimension attribute for the width of each transparent detent in the bar)
 * </ul>
 * <p>
 * This progress bar has no intrinsic height, so you must declare it with one explicitly. (It will
 * use the given height as the bar's shadow height.)
 */
public final class SyncProgressTracker {

    private static final int MIN_DISTANCE_TO_TRIGGER_SYNC = 50; // dp
    private static final int MAX_DISTANCE_TO_TRIGGER_SYNC = 300; // dp

    private static final int DISTANCE_TO_IGNORE = 15; // dp
    private static final int DISTANCE_TO_TRIGGER_CANCEL = 10; // dp

    private static final int SHOW_CHECKING_DURATION_IN_MILLIS = 1 * 1000; // 1 seconds

    private static final int SWIPE_TEXT_APPEAR_DURATION_IN_MILLIS = 200;
    private static final int SYNC_STATUS_BAR_FADE_DURATION_IN_MILLIS = 150;
    private static final int SYNC_TRIGGER_SHRINK_DURATION_IN_MILLIS = 250;

    private boolean mTrackingScrollMovement = false;
    // Y coordinate of where scroll started
    private float mTrackingScrollStartY;
    // Max Y coordinate reached since starting scroll, this is used to know whether
    // user moved back up which should cancel the current tracking state and hide the
    // sync trigger bar.
    private float mTrackingScrollMaxY;

    private final Interpolator mAccelerateInterpolator = new AccelerateInterpolator(1.5f);
    private final Interpolator mDecelerateInterpolator = new DecelerateInterpolator(1.5f);

    private float mDensity;

    // Minimum vertical distance (in dips) of swipe to trigger a sync.
    // This value can be different based on the device.
    private float mDistanceToTriggerSyncDp = MIN_DISTANCE_TO_TRIGGER_SYNC;

    private Context mContext;
    private ViewGroup mHostView;
    private RelativeLayout mSyncLayout;
    private ProgressBar mSyncTriggerBar;
    private ButteryProgressBar mSyncProgressBar;
    private Window mWindow;
    private HintText mHintText;
    private boolean mHasHintTextViewBeenAdded;
    private final AnimatorListenerAdapter mSyncDismissListener;
    private int mBarColor;
    private int mPaddingTop = 0;

    private final SyncProgressTrackerListener mListener;
    private final WindowManager mWindowManager;

    public interface SyncProgressTrackerListener {
        boolean isReadyToStartMovementTracking();
        void onTriggerSync();
        void onCancelMovementTracking();
        void onTriggerScale(float scale);
    }

    /**
     * Provide the progress bar to show when data sync.
     * @param context Current context.
     * @param hostView Attached ViewGroup.
     * @param listener The method to call for sync.
     * @param window Current window.
     */
    public SyncProgressTracker(Context context, ViewGroup hostView,
            SyncProgressTrackerListener listener, Window window) {
        mHasHintTextViewBeenAdded = false;
        mContext = context;
        mHostView = hostView;
        mListener = listener;
        mWindow = window;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mHintText = new HintText(context);
        computeDistanceToTriggerSync();
        mBarColor = 0;

        mSyncDismissListener = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator arg0) {
                mSyncProgressBar.setVisibility(View.GONE);
                mSyncTriggerBar.setVisibility(View.GONE);
            }
        };
    }

    /**
     * Set the message when swipe down.
     * @param message String to show.
     */
    public void setSyncMessage(String message) {
        mHintText.setSyncMessage(message);
    }

    /**
     * Set the message when swipe down.
     * @param message String resource id to show.
     */
    public void setSyncMessage(int messageid) {
        mHintText.setSyncMessage(messageid);
    }

    /**
     * Set the message when progress bar running.
     * @param message String to show.
     */
    public void setCheckingMessage(String message) {
        mHintText.setCheckingMessage(message);
    }

    /**
     * Set the message when progress bar running.
     * @param messageid String resource id to show.
     */
    public void setCheckingMessage(int messageid) {
        mHintText.setCheckingMessage(messageid);
    }

    /**
     * Set the color of message textview.
     * @param color color id. ex: Color.WHITE or 0xffffffff
     */
    public void setMessageColor(int color) {
        mHintText.setTextColor(color);
    }

    /**
     * Set the color of message textview.
     * @param id color resource id.
     */
    public void setMessageColorResource(int id) {
        mHintText.setTextColorResource(id);
    }

    /**
     * Set the background resource of message.
     * @param id background resource id.
     */
    public void setBackgroundResource(int id) {
        mHintText.setBackgroundResource(id);
    }

    /**
     * Set the background color of message.
     * @param id color id. ex: Color.WHITE or 0xffffffff
     */
    public void setBackgroundColor(int id) {
        mHintText.setBackgroundColor(id);
    }

    /**
     * Set the color of progress bar.
     * @param id color id. ex: Color.WHITE or 0xffffffff
     */
    public void setBarColor(int color) {
        if (mBarColor != color) {
            mBarColor = color;
            if (mBarColor != 0 && mSyncTriggerBar != null
                    && mSyncProgressBar != null) {
                mSyncTriggerBar.getProgressDrawable().setColorFilter(mBarColor,
                        Mode.SRC_IN);
                mSyncProgressBar.setBarColor(mBarColor);
            }
        }
    }

    public void computeDistanceToTriggerSync() {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mDensity = displayMetrics.density;

        // Calculate distance threshold for triggering a sync based on
        // screen height.  Apply a min and max cutoff.
        float threshold = (displayMetrics.heightPixels) / mDensity / 3.3f;
        mDistanceToTriggerSyncDp = Math.max(
                Math.min(threshold, MAX_DISTANCE_TO_TRIGGER_SYNC),
                MIN_DISTANCE_TO_TRIGGER_SYNC);
    }

    public boolean isTrackingScrollMovement() {
        return mTrackingScrollMovement;
    }

    private void addHintTextViewIfNecessary() {
        if (!mHasHintTextViewBeenAdded) {
            mWindowManager.addView(mHintText, getRefreshHintTextLayoutParams());
            mHasHintTextViewBeenAdded = true;
        }
        ensureProgressBars();
    }

    void relayoutHintText() {
        if (mHasHintTextViewBeenAdded) {
            mWindowManager.updateViewLayout(mHintText,
                    getRefreshHintTextLayoutParams());
        }
    }

    protected void detachedHintText() {
        if (mHasHintTextViewBeenAdded) {
            mHasHintTextViewBeenAdded = false;
            mWindowManager.removeViewImmediate(mHintText);
        }
    }

    private WindowManager.LayoutParams getRefreshHintTextLayoutParams() {
        // Create the "Swipe down to refresh" text view that covers the action bar.
        Rect rect= new Rect();
        mWindow.getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;

        final TypedArray actionBarSize = mContext.obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int actionBarHeight = actionBarSize.getDimensionPixelSize(0, 0);
        actionBarSize.recycle();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                actionBarHeight,
                WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;
        params.x = 0;
        params.y = statusBarHeight;
        return params;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        addHintTextViewIfNecessary();
        float y = event.getY(0);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mListener != null) {
                    if (mListener.isReadyToStartMovementTracking()) {
                        startMovementTracking(y);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTrackingScrollMovement) {
                    // Sync is triggered when tap and drag distance goes over a certain threshold
                    float verticalDistancePx = y - mTrackingScrollStartY;
                    float verticalDistanceDp = verticalDistancePx / mDensity;
                    if (verticalDistanceDp > mDistanceToTriggerSyncDp) {
                        triggerSync();
                        break;
                    }

                    // Moving back up vertically should be handled the same as CANCEL / UP:
                    float verticalDistanceFromMaxPx = mTrackingScrollMaxY - y;
                    float verticalDistanceFromMaxDp = verticalDistanceFromMaxPx / mDensity;
                    if (verticalDistanceFromMaxDp > DISTANCE_TO_TRIGGER_CANCEL) {
                        cancelMovementTracking();
                        break;
                    }

                    // Otherwise hint how much further user needs to drag to trigger sync by
                    // expanding the sync status bar proportional to how far they have dragged.
                    if (verticalDistanceDp < DISTANCE_TO_IGNORE) {
                        // Ignore small movements such as tap
                        verticalDistanceDp = 0;
                    }
                    setTriggerScale(mAccelerateInterpolator.getInterpolation(
                            verticalDistanceDp/mDistanceToTriggerSyncDp));

                    if (y > mTrackingScrollMaxY) {
                        mTrackingScrollMaxY = y;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mTrackingScrollMovement) {
                    cancelMovementTracking();
                }
                break;
        }
        return false;
    }

    private void startMovementTracking(float y) {
        mTrackingScrollMovement = true;
        mTrackingScrollStartY = y;
        mTrackingScrollMaxY = mTrackingScrollStartY;
    }

    public void cancelMovementTracking() {
        if (mTrackingScrollMovement) {
            // Shrink the status bar when user lifts finger and no sync has happened yet
            if (mSyncTriggerBar != null) {
                mSyncTriggerBar.animate()
                        .scaleX(0f)
                        .setInterpolator(mDecelerateInterpolator)
                        .setDuration(SYNC_TRIGGER_SHRINK_DURATION_IN_MILLIS)
                        .setListener(mSyncDismissListener)
                        .start();
            }
            mTrackingScrollMovement = false;
        }

        if (mListener != null) {
            mListener.onCancelMovementTracking();
            mHintText.hide();
        }
    }

    private void setTriggerScale(float scale) {
        if (scale == 0f && mSyncTriggerBar == null) {
            // No-op. A null trigger means it's uninitialized, and setting it to zero-scale
            // means we're trying to reset state, so there's nothing to reset in this case.
            return;
        } else if (mSyncTriggerBar != null) {
            // reset any leftover trigger visual state
            mSyncTriggerBar.animate().cancel();
            mSyncTriggerBar.setVisibility(View.VISIBLE);
        }
        ensureProgressBars();
        mSyncTriggerBar.setScaleX(scale);

        if (mListener != null) {
            mListener.onTriggerScale(scale);
        }
        if (scale > 0f) {
            mHintText.displaySwipeToRefresh();
        }
    }

    private void ensureProgressBars() {
        if (mSyncTriggerBar == null || mSyncProgressBar == null) {
            final LayoutInflater inflater = LayoutInflater.from(mContext);
            inflater.inflate(R.layout.asus_commonui_sync_progress, mHostView, true /* attachToRoot */);
            mSyncTriggerBar = (ProgressBar) mHostView
                    .findViewById(R.id.asus_commonui_sync_trigger);
            mSyncProgressBar = (ButteryProgressBar) mHostView
                    .findViewById(R.id.asus_commonui_butteryprogress);
            mSyncLayout = (RelativeLayout) mHostView
                    .findViewById(R.id.asus_commonui_sync_layout);
            mSyncLayout.setPadding(0, mPaddingTop, 0, 0);
            mSyncProgressBar.setTracker(this);
            if (mBarColor != 0) {
                mSyncTriggerBar.getProgressDrawable().setColorFilter(mBarColor,
                        Mode.SRC_IN);
                mSyncProgressBar.setBarColor(mBarColor);
            }
        }
    }

    public void setSyncProgressMarginsTop(int top) {
        mPaddingTop = top;
        if (mSyncLayout != null) {
            mSyncLayout.setPadding(0, mPaddingTop, 0, 0);
        }
    }

    private void triggerSync() {
        ensureProgressBars();
        mSyncTriggerBar.setVisibility(View.GONE);

        // Any continued dragging after this should have no effect
        mTrackingScrollMovement = false;

        if (mListener != null) {
            mListener.onTriggerSync();
            mHintText.displayCheckingAndHideAfterDelay();
        }
    }

    public void showSyncStatusBar() {
        ensureProgressBars();
        mSyncTriggerBar.setVisibility(View.GONE);
        mSyncProgressBar.setVisibility(View.VISIBLE);
        mSyncProgressBar.setAlpha(1f);
    }

    public void hideSyncStatusBar() {
        // Hide both the sync progress bar and sync trigger bar
        if (mSyncProgressBar != null && mSyncTriggerBar != null
                && mHintText != null) {
            mSyncProgressBar.animate().alpha(0f)
                    .setDuration(SYNC_STATUS_BAR_FADE_DURATION_IN_MILLIS)
                    .setListener(mSyncDismissListener);
            mSyncTriggerBar.setVisibility(View.GONE);
            mHintText.hide();
        }
    }

    /**
     * A text view that covers the entire action bar, used for displaying
     * "Swipe down to refresh" hint text if user has initiated a downward swipe.
     */
    private static class HintText extends FrameLayout {

        private static final int NONE = 0;
        private static final int SWIPE_TO_REFRESH = 1;
        private static final int CHECKING = 2;

        // Can be one of NONE, SWIPE_TO_REFRESH, CHECKING
        private int mDisplay;

        private final TextView mTextView;
        private String mRefreshText = null;
        private String mCheckText = null;

        private final Interpolator mDecelerateInterpolator = new DecelerateInterpolator(1.5f);
        private final Interpolator mAccelerateInterpolator = new AccelerateInterpolator(1.5f);

        private static final int[] STYLE_ATTR = new int[] {android.R.attr.background};
        private final Runnable mHideHintTextRunnable = new Runnable() {
            @Override
            public void run() {
                hide();
            }
        };
        private final Runnable mSetVisibilityGoneRunnable = new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        };

        public HintText(final Context context) {
            this(context, null);
        }

        public HintText(final Context context, final AttributeSet attrs) {
            this(context, attrs, -1);
        }

        public HintText(final Context context, final AttributeSet attrs, final int defStyle) {
            super(context, attrs, defStyle);

            final LayoutInflater factory = LayoutInflater.from(context);
            factory.inflate(R.layout.asus_commonui_swipe_to_refresh, this);

            mTextView = (TextView) findViewById(R.id.asus_commonui_swipe_text);

            mDisplay = NONE;
            setVisibility(View.GONE);

            // Set background color to be same as action bar color
            final int actionBarRes = getActionBarBackgroundResource(context);
            setBackgroundResource(actionBarRes);
        }

        public void setSyncMessage(String message) {
            mRefreshText = message;
        }

        public void setSyncMessage(int id) {
            mRefreshText = getResources().getString(id);
        }

        public void setCheckingMessage(String message) {
            mCheckText = message;
        }

        public void setCheckingMessage(int id) {
            mCheckText = getResources().getString(id);
        }

        public void setTextColor(int color) {
            mTextView.setTextColor(color);
        }

        public void setTextColorResource(int id) {
            mTextView.setTextColor(getResources().getColor(id));
        }

        private void displaySwipeToRefresh() {
            if (mDisplay != SWIPE_TO_REFRESH) {
                mTextView.setText(mRefreshText);
                // Covers the current action bar:
                setVisibility(View.VISIBLE);
                setAlpha(1f);
                // Animate text sliding down onto action bar:
                mTextView.setY(-mTextView.getHeight());
                mTextView.animate().y(0)
                        .setInterpolator(mDecelerateInterpolator)
                        .setDuration(SWIPE_TEXT_APPEAR_DURATION_IN_MILLIS);
                mDisplay = SWIPE_TO_REFRESH;
            }
        }

        private void displayCheckingAndHideAfterDelay() {
            mTextView.setText(mCheckText);
            setVisibility(View.VISIBLE);
            mDisplay = CHECKING;
            postDelayed(mHideHintTextRunnable, SHOW_CHECKING_DURATION_IN_MILLIS);
        }

        private void hide() {
            if (mDisplay != NONE) {
                // Animate text sliding up leaving behind a blank action bar
                mTextView.animate().y(-mTextView.getHeight())
                        .setInterpolator(mAccelerateInterpolator)
                        .setDuration(SWIPE_TEXT_APPEAR_DURATION_IN_MILLIS)
                        .start();
                animate().alpha(0f)
                        .setDuration(SWIPE_TEXT_APPEAR_DURATION_IN_MILLIS);
                postDelayed(mSetVisibilityGoneRunnable, SWIPE_TEXT_APPEAR_DURATION_IN_MILLIS);
                mDisplay = NONE;
            }
        }

        /**
         * Get the background color of app's action bar.
         */
        private int getActionBarBackgroundResource(final Context context) {
            final TypedValue actionBarStyle = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarStyle, actionBarStyle, true)
                    && actionBarStyle.type == TypedValue.TYPE_REFERENCE) {
                final TypedValue backgroundValue = new TypedValue();
                final TypedArray attr = context.obtainStyledAttributes(actionBarStyle.resourceId,
                        STYLE_ATTR);
                attr.getValue(0, backgroundValue);
                attr.recycle();
                return backgroundValue.resourceId;
            } else {
                // Default color
                return context.getResources().getColor(R.color.asus_commonui_list_background_color);
            }
        }
    }
}
