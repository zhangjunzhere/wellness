/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.asus.commonui.drawerlayout;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import com.asus.commonui.colorful.ColorfulLinearLayout;

/**
 * DrawerLayout acts as a top-level container for window content that allows for
 * interactive "drawer" views to be pulled out from the edge of the window.
 *
 * <p>Drawer positioning and layout is controlled using the <code>android:layout_gravity</code>
 * attribute on child views corresponding to which side of the view you want the drawer
 * to emerge from: left or right. (Or start/end on platform versions that support layout direction.)
 * </p>
 *
 * <p>To use a DrawerLayout, position your primary content view as the first child with
 * a width and height of <code>match_parent</code>. Add drawers as child views after the main
 * content view and set the <code>layout_gravity</code> appropriately. Drawers commonly use
 * <code>match_parent</code> for height with a fixed width.</p>
 *
 * <p>{@link DrawerListener} can be used to monitor the state and motion of drawer views.
 * Avoid performing expensive operations such as layout during animation as it can cause
 * stuttering; try to perform expensive operations during the {@link #STATE_IDLE} state.
 * {@link SimpleDrawerListener} offers default/no-op implementations of each callback method.</p>
 *
 * <p>As per the Android Design guide, any drawers positioned to the left/start should
 * always contain content for navigating around the application, whereas any drawers
 * positioned to the right/end should always contain actions to take on the current content.
 * This preserves the same navigation left, actions right structure present in the Action Bar
 * and elsewhere.</p>
 */
public class DrawerLayout extends ViewGroup {
    private static final String TAG = "DrawerLayout";

    /**
     * Indicates that any drawers are in an idle, settled state. No animation is in progress.
     */
    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

    /**
     * Indicates that a drawer is currently being dragged by the user.
     */
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

    /**
     * Indicates that a drawer is in the process of settling to a final position.
     */
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

    /**
     * The drawer is unlocked.
     */
    public static final int LOCK_MODE_UNLOCKED = 0;

    /**
     * The drawer is locked closed. The user may not open it, though
     * the app may open it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_CLOSED = 1;

    /**
     * The drawer is locked open. The user may not close it, though the app
     * may close it programmatically.
     */
    public static final int LOCK_MODE_LOCKED_OPEN = 2;

    private static final int MIN_DRAWER_MARGIN = 64; // dp

    private static final int GAP_BETWEEN_MENU = 15; //dp

    private static final float DRAWER_OFFSET = 0.0f; //Offset of drawer, should be 0 to 1.

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    /**
     * Length of time to delay before peeking the drawer.
     */
    private static final int PEEK_DELAY = 160; // ms

    /**
     * Minimum velocity that will be detected as a fling
     */
    private static final int MIN_FLING_VELOCITY = 500; // dips per second

    private static final int WAIT_ACTION_BAR_LAYOUT_FINISH_COUNT = 1;

    private static final int MSG_WAIT_ACTION_BAR_LAYOUT_FINISH = 0;
    private static final int MSG_PENDING_UPDATE_PADDING = 1;

    /**
     * Experimental feature.
     */
    private static final boolean ALLOW_EDGE_LOCK = false;

    private static final boolean CHILDREN_DISALLOW_INTERCEPT = true;

    private static final float TOUCH_SLOP_SENSITIVITY = 1.f;

    private static final int[] LAYOUT_ATTRS = new int[] {
            android.R.attr.layout_gravity
    };

    private static final float CONTENT_VIEW_TARGET_SCALE = (1.0f - 0.76f);

    private int mMinDrawerMargin;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private float mScrimOpacity;
    private Paint mScrimPaint = new Paint();

    private final ViewDragHelper mLeftDragger;
    private final ViewDragHelper mRightDragger;
    private final ViewDragCallback mLeftCallback;
    private final ViewDragCallback mRightCallback;
    private int mDrawerState;
    private boolean mInLayout;
    private boolean mFirstLayout = true;
    private int mLockModeLeft;
    private int mLockModeRight;
    private boolean mChildrenCanceledTouch;

    private DrawerListener mListener;

    private float mInitialMotionX;
    private float mInitialMotionY;

    private Drawable mShadowLeft;
    private Drawable mShadowRight;

    private Activity mHostActivity;
    private ColorfulLinearLayout mHostColorful;
    private int mOldHostColorfulVisibility;
    private ViewGroup mOldDrawerLayoutParent;
    private ViewGroup mActionBar;
    private boolean mReparented;

    private int mInsetsBottom;
    private TextView mStatusBarBackground;

    private int mLastNavigationMode = -1;
    private int mRequestLayoutCount;
    private boolean mLeftEdgeDrawer;
    private boolean mShiftContent = true;

    private boolean mForceRelayout = false;
    private int mMenuPaddingTop = -1;
    private int mContentPaddingTop = -1;
    // BEGIN bob_ding@asus.com
    private static final boolean VERSION_L_OR_LATER = android.os.Build.VERSION.SDK_INT >= 21;
    private int mStatusBarColor = Color.TRANSPARENT;
    private boolean mIsDisallowIntercept = false;
    private boolean mIsScaleEnabled = true;
    // END bob_ding@asus.com

    private OnLayoutChangeListener mActionBarLayoutChangeListener = new OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right,
                int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (mReparented) {
                requestWaitActionBarLayoutFinish(true, true);
            }
        }
    };

    /**
     * Listener for monitoring events about drawers.
     */
    public interface DrawerListener {
        /**
         * Called when a drawer's position changes.
         * @param drawerView The child view that was moved
         * @param slideOffset The new offset of this drawer within its range, from 0-1
         */
        public void onDrawerSlide(View drawerView, float slideOffset);

        /**
         * Called when a drawer has settled in a completely open state.
         * The drawer is interactive at this point.
         *
         * @param drawerView Drawer view that is now open
         */
        public void onDrawerOpened(View drawerView);

        /**
         * Called when a drawer has settled in a completely closed state.
         *
         * @param drawerView Drawer view that is now closed
         */
        public void onDrawerClosed(View drawerView);

        /**
         * Called when the drawer motion state changes. The new state will
         * be one of {@link #STATE_IDLE}, {@link #STATE_DRAGGING} or {@link #STATE_SETTLING}.
         *
         * @param newState The new drawer motion state
         */
        public void onDrawerStateChanged(int newState);
    }

    /**
     * Stub/no-op implementations of all methods of {@link DrawerListener}.
     * Override this if you only care about a few of the available callback methods.
     */
    public static abstract class SimpleDrawerListener implements DrawerListener {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
        }

        @Override
        public void onDrawerOpened(View drawerView) {
        }

        @Override
        public void onDrawerClosed(View drawerView) {
        }

        @Override
        public void onDrawerStateChanged(int newState) {
        }
    }

    public DrawerLayout(Context context) {
        this(context, null);
    }

    public DrawerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mIsScaleEnabled = !context.getPackageManager().hasSystemFeature("asus.software.restricted_animation");
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        final float density = getResources().getDisplayMetrics().density;
        mMinDrawerMargin = (int) (MIN_DRAWER_MARGIN * density + 0.5f);
        final float minVel = MIN_FLING_VELOCITY * density;

        mLeftCallback = new ViewDragCallback(Gravity.LEFT);
        mRightCallback = new ViewDragCallback(Gravity.RIGHT);

        mLeftDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mLeftCallback);
        mLeftDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_LEFT);
        mLeftDragger.setMinVelocity(minVel);
        mLeftCallback.setDragger(mLeftDragger);

        mRightDragger = ViewDragHelper.create(this, TOUCH_SLOP_SENSITIVITY, mRightCallback);
        mRightDragger.setEdgeTrackingEnabled(ViewDragHelper.EDGE_RIGHT);
        mRightDragger.setMinVelocity(minVel);
        mRightCallback.setDragger(mRightDragger);

        // So that we can catch the back button
        setFocusableInTouchMode(true);

        setMotionEventSplittingEnabled(false);

        initColorfulView();
    }

    public void setScaleEnabled(boolean enabled) {
        mIsScaleEnabled = enabled;
    }

    public boolean isScaleEnabled() {
        return mIsScaleEnabled;
    }

    protected boolean isLeftDrawer() {
        return mLeftEdgeDrawer;
    }

    /**
     * Set a simple drawable used for the left or right shadow.
     * The drawable provided must have a nonzero intrinsic width.
     *
     * @param shadowDrawable Shadow drawable to use at the edge of a drawer
     * @param gravity Which drawer the shadow should apply to
     */
    public void setDrawerShadow(Drawable shadowDrawable, int gravity) {
        /*
         * TODO Someone someday might want to set more complex drawables here.
         * They're probably nuts, but we might want to consider registering callbacks,
         * setting states, etc. properly.
         */

        final int absGravity = Gravity.getAbsoluteGravity(gravity,
                getLayoutDirection());
        if ((absGravity & Gravity.LEFT) == Gravity.LEFT) {
            mShadowLeft = shadowDrawable;
            invalidate();
        }
        if ((absGravity & Gravity.RIGHT) == Gravity.RIGHT) {
            mShadowRight = shadowDrawable;
            invalidate();
        }
    }

    /**
     * Set a simple drawable used for the left or right shadow.
     * The drawable provided must have a nonzero intrinsic width.
     *
     * @param resId Resource id of a shadow drawable to use at the edge of a drawer
     * @param gravity Which drawer the shadow should apply to
     */
    public void setDrawerShadow(int resId, int gravity) {
        setDrawerShadow(getResources().getDrawable(resId), gravity);
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a drawer is open.
     *
     * @param color Color to use in 0xAARRGGBB format.
     */
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }

    /**
     * Set a listener to be notified of drawer events.
     *
     * @param listener Listener to notify when drawer events occur
     * @see DrawerListener
     */
    public void setDrawerListener(DrawerListener listener) {
        mListener = listener;
    }

    /**
     * Enable or disable interaction with all drawers.
     *
     * <p>This allows the application to restrict the user's ability to open or close
     * any drawer within this layout. DrawerLayout will still respond to calls to
     * {@link #openDrawer(int)}, {@link #closeDrawer(int)} and friends if a drawer is locked.</p>
     *
     * <p>Locking drawers open or closed will implicitly open or close
     * any drawers as appropriate.</p>
     *
     * @param lockMode The new lock mode for the given drawer. One of {@link #LOCK_MODE_UNLOCKED},
     *                 {@link #LOCK_MODE_LOCKED_CLOSED} or {@link #LOCK_MODE_LOCKED_OPEN}.
     */
    public void setDrawerLockMode(int lockMode) {
        setDrawerLockMode(lockMode, Gravity.LEFT);
        setDrawerLockMode(lockMode, Gravity.RIGHT);
    }

    /**
     * Enable or disable interaction with the given drawer.
     *
     * <p>This allows the application to restrict the user's ability to open or close
     * the given drawer. DrawerLayout will still respond to calls to {@link #openDrawer(int)},
     * {@link #closeDrawer(int)} and friends if a drawer is locked.</p>
     *
     * <p>Locking a drawer open or closed will implicitly open or close
     * that drawer as appropriate.</p>
     *
     * @param lockMode The new lock mode for the given drawer. One of {@link #LOCK_MODE_UNLOCKED},
     *                 {@link #LOCK_MODE_LOCKED_CLOSED} or {@link #LOCK_MODE_LOCKED_OPEN}.
     * @param edgeGravity Gravity.LEFT, RIGHT, START or END.
     *                    Expresses which drawer to change the mode for.
     *
     * @see #LOCK_MODE_UNLOCKED
     * @see #LOCK_MODE_LOCKED_CLOSED
     * @see #LOCK_MODE_LOCKED_OPEN
     */
    public void setDrawerLockMode(int lockMode, int edgeGravity) {
        final int absGravity = Gravity.getAbsoluteGravity(edgeGravity,
                getLayoutDirection());
        if (absGravity == Gravity.LEFT) {
            mLockModeLeft = lockMode;
        } else if (absGravity == Gravity.RIGHT) {
            mLockModeRight = lockMode;
        }
        if (lockMode != LOCK_MODE_UNLOCKED) {
            // Cancel interaction in progress
            final ViewDragHelper helper = absGravity == Gravity.LEFT ? mLeftDragger : mRightDragger;
            helper.cancel();
        }
        switch (lockMode) {
            case LOCK_MODE_LOCKED_OPEN:
                final View toOpen = findDrawerWithGravity(absGravity);
                if (toOpen != null) {
                    openDrawer(toOpen);
                }
                break;
            case LOCK_MODE_LOCKED_CLOSED:
                final View toClose = findDrawerWithGravity(absGravity);
                if (toClose != null) {
                    closeDrawer(toClose);
                }
                break;
            // default: do nothing
        }
    }

    /**
     * Enable or disable interaction with the given drawer.
     *
     * <p>This allows the application to restrict the user's ability to open or close
     * the given drawer. DrawerLayout will still respond to calls to {@link #openDrawer(int)},
     * {@link #closeDrawer(int)} and friends if a drawer is locked.</p>
     *
     * <p>Locking a drawer open or closed will implicitly open or close
     * that drawer as appropriate.</p>
     *
     * @param lockMode The new lock mode for the given drawer. One of {@link #LOCK_MODE_UNLOCKED},
     *                 {@link #LOCK_MODE_LOCKED_CLOSED} or {@link #LOCK_MODE_LOCKED_OPEN}.
     * @param drawerView The drawer view to change the lock mode for
     *
     * @see #LOCK_MODE_UNLOCKED
     * @see #LOCK_MODE_LOCKED_CLOSED
     * @see #LOCK_MODE_LOCKED_OPEN
     */
    public void setDrawerLockMode(int lockMode, View drawerView) {
        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a " +
                    "drawer with appropriate layout_gravity");
        }
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        setDrawerLockMode(lockMode, gravity);
    }

    /**
     * Check the lock mode of the drawer with the given gravity.
     *
     * @param edgeGravity Gravity of the drawer to check
     * @return one of {@link #LOCK_MODE_UNLOCKED}, {@link #LOCK_MODE_LOCKED_CLOSED} or
     *         {@link #LOCK_MODE_LOCKED_OPEN}.
     */
    public int getDrawerLockMode(int edgeGravity) {
        final int absGravity = Gravity.getAbsoluteGravity(
                edgeGravity, getLayoutDirection());
        if (absGravity == Gravity.LEFT) {
            return mLockModeLeft;
        } else if (absGravity == Gravity.RIGHT) {
            return mLockModeRight;
        }
        return LOCK_MODE_UNLOCKED;
    }

    /**
     * Check the lock mode of the given drawer view.
     *
     * @param drawerView Drawer view to check lock mode
     * @return one of {@link #LOCK_MODE_UNLOCKED}, {@link #LOCK_MODE_LOCKED_CLOSED} or
     *         {@link #LOCK_MODE_LOCKED_OPEN}.
     */
    public int getDrawerLockMode(View drawerView) {
        final int absGravity = getDrawerViewAbsoluteGravity(drawerView);
        if (absGravity == Gravity.LEFT) {
            return mLockModeLeft;
        } else if (absGravity == Gravity.RIGHT) {
            return mLockModeRight;
        }
        return LOCK_MODE_UNLOCKED;
    }

    /**
     * Resolve the shared state of all drawers from the component ViewDragHelpers.
     * Should be called whenever a ViewDragHelper's state changes.
     */
    void updateDrawerState(int forGravity, int activeState, View activeDrawer) {
        final int leftState = mLeftDragger.getViewDragState();
        final int rightState = mRightDragger.getViewDragState();

        final int state;
        if (leftState == STATE_DRAGGING || rightState == STATE_DRAGGING) {
            state = STATE_DRAGGING;
        } else if (leftState == STATE_SETTLING || rightState == STATE_SETTLING) {
            state = STATE_SETTLING;
        } else {
            state = STATE_IDLE;
        }

        if (activeDrawer != null && activeState == STATE_IDLE) {
            final LayoutParams lp = (LayoutParams) activeDrawer.getLayoutParams();
            if (lp.onScreen == 0) {
                dispatchOnDrawerClosed(activeDrawer);
            } else if (lp.onScreen == 1) {
                dispatchOnDrawerOpened(activeDrawer);
            }
        }

        if (state != mDrawerState) {
            mDrawerState = state;

            if (mListener != null) {
                mListener.onDrawerStateChanged(state);
            }
        }
    }

    void dispatchOnDrawerClosed(View drawerView) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (lp.knownOpen) {
            lp.knownOpen = false;
            if (mListener != null) {
                mListener.onDrawerClosed(drawerView);
            }
            sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
    }

    void dispatchOnDrawerOpened(View drawerView) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
        if (!lp.knownOpen) {
            lp.knownOpen = true;
            if (mListener != null) {
                mListener.onDrawerOpened(drawerView);
            }
            drawerView.sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
        }
    }

    void dispatchOnDrawerSlide(View drawerView, float slideOffset) {
        if (mListener != null) {
            mListener.onDrawerSlide(drawerView, slideOffset);
        }
    }

    void setDrawerViewOffset(View drawerView, float slideOffset) {
        final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();

        lp.onScreen = slideOffset;
        mLeftEdgeDrawer = checkDrawerViewAbsoluteGravity(drawerView, Gravity.LEFT);
        if (mIsScaleEnabled) {
            setContentViewOffset(drawerView, slideOffset);
        }
        dispatchOnDrawerSlide(drawerView, slideOffset);
    }

    float getDrawerViewOffset(View drawerView) {
        return ((LayoutParams) drawerView.getLayoutParams()).onScreen;
    }

    /**
     * @return the absolute gravity of the child drawerView, resolved according
     *         to the current layout direction
     */
    int getDrawerViewAbsoluteGravity(View drawerView) {
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return Gravity.getAbsoluteGravity(gravity, getLayoutDirection());
    }

    boolean checkDrawerViewAbsoluteGravity(View drawerView, int checkFor) {
        final int absGravity = getDrawerViewAbsoluteGravity(drawerView);
        return (absGravity & checkFor) == checkFor;
    }

    View findOpenDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (((LayoutParams) child.getLayoutParams()).knownOpen) {
                return child;
            }
        }
        return null;
    }

    void moveDrawerToOffset(View drawerView, float slideOffset) {
        final float oldOffset = getDrawerViewOffset(drawerView);
        final int width = drawerView.getWidth();
        final int oldPos = (int) (width * oldOffset);
        final int newPos = (int) (width * slideOffset);
        final int dx = newPos - oldPos;

        drawerView.offsetLeftAndRight(
                checkDrawerViewAbsoluteGravity(drawerView, Gravity.LEFT) ? dx : -dx);
        setDrawerViewOffset(drawerView, slideOffset);
    }

    /**
     * @param gravity the gravity of the child to return. If specified as a
     *            relative value, it will be resolved according to the current
     *            layout direction.
     * @return the drawer with the specified gravity
     */
    View findDrawerWithGravity(int gravity) {
        final int absHorizGravity = Gravity.getAbsoluteGravity(
                gravity, getLayoutDirection()) & Gravity.HORIZONTAL_GRAVITY_MASK;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final int childAbsGravity = getDrawerViewAbsoluteGravity(child);
            if ((childAbsGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == absHorizGravity) {
                return child;
            }
        }
        return null;
    }

    /**
     * Simple gravity to string - only supports LEFT and RIGHT for debugging output.
     *
     * @param gravity Absolute gravity value
     * @return LEFT or RIGHT as appropriate, or a hex string
     */
    static String gravityToString(int gravity) {
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            return "LEFT";
        }
        if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            return "RIGHT";
        }
        return Integer.toHexString(gravity);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mFirstLayout = true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mFirstLayout = true;
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        final boolean stable = isSystemUiLayoutStable();
        final int lastInsetsBottom = mInsetsBottom;
        if (!stable || !mReparented) {
            mInsetsBottom = 0;
        } else {
            mInsetsBottom = insets.bottom;
        }
        if (mInsetsBottom != lastInsetsBottom) {
            MarginLayoutParams lp = (MarginLayoutParams) getLayoutParams();
            lp.bottomMargin = mInsetsBottom;
            requestLayout();
        }

        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            if (isInEditMode()) {
                // Don't crash the layout editor. Consume all of the space if specified
                // or pick a magic number from thin air otherwise.
                // TODO Better communication with tools of this bogus state.
                // It will crash on a real device.
                if (widthMode == MeasureSpec.AT_MOST) {
                    widthMode = MeasureSpec.EXACTLY;
                } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                    widthMode = MeasureSpec.EXACTLY;
                    widthSize = 300;
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    heightMode = MeasureSpec.EXACTLY;
                }
                else if (heightMode == MeasureSpec.UNSPECIFIED) {
                    heightMode = MeasureSpec.EXACTLY;
                    heightSize = 300;
                }
            } else {
                throw new IllegalArgumentException(
                        "DrawerLayout must be measured with MeasureSpec.EXACTLY.");
            }
        }

        setMeasuredDimension(widthSize, heightSize);

        // Gravity value for each drawer we've seen. Only one of each permitted.
        int foundDrawers = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (child == mStatusBarBackground) {
                final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                        widthSize - lp.leftMargin - lp.rightMargin, MeasureSpec.EXACTLY);
                final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                        getStatusBarHeight() - lp.topMargin - lp.bottomMargin, MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, childHeightMeasureSpec);
            } else if (isContentView(child)) {
                // Content views get measured at exactly the layout's size.
                int width = mReparented ? mActionBar.getWidth() : widthSize
                        - lp.leftMargin - lp.rightMargin;
                final int contentWidthSpec = MeasureSpec.makeMeasureSpec(
                        width, MeasureSpec.EXACTLY);
                int height = mReparented ? mActionBar.getHeight() : heightSize
                        - lp.topMargin - lp.bottomMargin;
                final int contentHeightSpec = MeasureSpec.makeMeasureSpec(
                        height, MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, contentHeightSpec);
            } else if (isDrawerView(child)) {
                final int childGravity =
                        getDrawerViewAbsoluteGravity(child) & Gravity.HORIZONTAL_GRAVITY_MASK;
                if ((foundDrawers & childGravity) != 0) {
                    throw new IllegalStateException("Child drawer has absolute gravity " +
                            gravityToString(childGravity) + " but this " + TAG + " already has a " +
                            "drawer view along that edge");
                }
                final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec,
                        mMinDrawerMargin + lp.leftMargin + lp.rightMargin,
                        lp.width);
                int height = mReparented ? mActionBar.getHeight() : lp.height;
                final int drawerHeightSpec = getChildMeasureSpec(
                        heightMeasureSpec, lp.topMargin + lp.bottomMargin,
                        height);
                child.measure(drawerWidthSpec, drawerHeightSpec);
            } else {
                throw new IllegalStateException("Child " + child + " at index " + i +
                        " does not have a valid layout_gravity - must be Gravity.LEFT, " +
                        "Gravity.RIGHT or Gravity.NO_GRAVITY");
            }
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (isSystemUiLayoutStable() || mForceRelayout) {
            requestWaitActionBarLayoutFinish(true, false);
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
            case MSG_WAIT_ACTION_BAR_LAYOUT_FINISH: {
                if (mRequestLayoutCount > 0) {
                    --mRequestLayoutCount;
                    requestLayout();
                }
                break;
            }

            case MSG_PENDING_UPDATE_PADDING: {
                PaddingPair pair = (PaddingPair) message.obj;
                if (pair != null && pair.view != null) {
                    setChildPaddingIfNeed(pair.view,
                            pair.paddings.left,
                            pair.paddings.top,
                            pair.paddings.right,
                            pair.paddings.bottom);
                }
                break;
            }
            }
        }
    };

    private void requestWaitActionBarLayoutFinish(boolean forced,
            boolean sendMsg) {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            int mode = actionBar.getNavigationMode();
            if (forced || mode != mLastNavigationMode) {
                mLastNavigationMode = mode;
                mRequestLayoutCount = WAIT_ACTION_BAR_LAYOUT_FINISH_COUNT;
            }
            if (sendMsg && mRequestLayoutCount > 0) {
                mHandler.sendEmptyMessage(MSG_WAIT_ACTION_BAR_LAYOUT_FINISH);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout = true;
        final int statusBarHeight = mReparented ? getStatusBarHeight() : 0;
        final int actionBarHeight = mReparented ? getActionBarHeight() : 0;
        if (mHostColorful != null) {
            if (actionBarHeight == 0
                    && mHostColorful.getVisibility() == View.VISIBLE) {
                mHostColorful.setVisibility(View.GONE);
            } else if (actionBarHeight > 0
                    && mHostColorful.getVisibility() != View.VISIBLE) {
                mHostColorful.setVisibility(View.VISIBLE);
            }
        }
        View contentView = null;
        View drawerView = null;
        float contentOffset = 0;
        float drawerOffset = 0;
        final int width = r - l;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (child == mStatusBarBackground) {
                child.layout(lp.leftMargin, lp.topMargin,
                        lp.leftMargin + child.getMeasuredWidth(),
                        lp.topMargin + child.getMeasuredHeight());
            } else if (isContentView(child)) {
                child.layout(lp.leftMargin, lp.topMargin,
                        lp.leftMargin + child.getMeasuredWidth(),
                        lp.topMargin + child.getMeasuredHeight());
                if (mReparented) {
                    // If reparented, we need to calculate the real height of content view manually
                    setChildPaddingIfNeed(child,
                            child.getPaddingLeft(),
                            statusBarHeight + actionBarHeight,
                            child.getPaddingRight(),
                            child.getPaddingBottom());
                }
                contentView = child;
            } else { // Drawer, if it wasn't onMeasure would have thrown an exception.
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                int childLeft;
                float contentLeft = 0;

                if (checkDrawerViewAbsoluteGravity(child, Gravity.LEFT)) {
                    childLeft = -childWidth + (int) (childWidth * lp.onScreen);
                    drawerOffset = (float) (childWidth + childLeft) / childWidth;
                    contentLeft = (float) childWidth * lp.onScreen;
                } else { // Right; onMeasure checked for us.
                    childLeft = width - (int) (childWidth * lp.onScreen);
                    drawerOffset = (float) (width - childLeft) / childWidth;
                    contentLeft = (float) -childWidth * lp.onScreen;
                }

                if (contentLeft != 0) {
                    contentOffset = contentLeft;
                }

                final boolean changeOffset = drawerOffset != lp.onScreen;

                final int vgrav = lp.gravity & Gravity.VERTICAL_GRAVITY_MASK;
                switch (vgrav) {
                    default:
                    case Gravity.TOP: {
                        child.layout(0, lp.topMargin, 0 + childWidth, lp.topMargin
                                + childHeight);
                        break;
                    }

                    case Gravity.BOTTOM: {
                        final int height = b - t;
                        child.layout(
                                0,
                                height - lp.bottomMargin
                                        - child.getMeasuredHeight(),
                                0 + childWidth, height - lp.bottomMargin);
                        break;
                    }

                    case Gravity.CENTER_VERTICAL: {
                        final int height = b - t;
                        int childTop = (height - childHeight) / 2;

                        // Offset for margins. If things don't fit right because
                        // of
                        // bad measurement before, oh well.
                        if (childTop < lp.topMargin) {
                            childTop = lp.topMargin;
                        } else if (childTop + childHeight > height
                                - lp.bottomMargin) {
                            childTop = height - lp.bottomMargin - childHeight;
                        }
                        child.layout(0, childTop, 0 + childWidth, childTop
                                + childHeight);
                        break;
                    }
                }
                if (mReparented) {
                    // If reparented, we need to calculate the real height of content view manually
                    setChildPaddingIfNeed(child,
                            child.getPaddingLeft(),
                            statusBarHeight,
                            child.getPaddingRight(),
                            child.getPaddingBottom());
                }

                final int newVisibility = lp.onScreen > 0 ? VISIBLE : INVISIBLE;
                if (child.getVisibility() != newVisibility) {
                    child.setVisibility(newVisibility);
                }
                drawerView = child;
            }
        }
        if (contentView != null) {
            if (contentOffset != 0) {
                contentView.offsetLeftAndRight((int) contentOffset);
            }
            // Adjust pivot
            contentView.setPivotX((float) contentView.getWidth() / 2.0f);
            contentView.setPivotY(((float) contentView.getHeight() - statusBarHeight) / 2.0f);
        }
        if (drawerView != null) {
            setDrawerViewOffset(drawerView, drawerOffset);
        }
        if (mReparented) {
            mActionBar.setPivotX((float) mActionBar.getWidth() / 2.0f);
            mActionBar.setPivotY(((float) mActionBar.getHeight() - statusBarHeight) / 2.0f + 1);
        }
        mInLayout = false;
        mFirstLayout = false;

        if (isSystemUiLayoutStable() || mForceRelayout) {
            requestWaitActionBarLayoutFinish(false, true);
        }
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    public void computeScroll() {
        final int childCount = getChildCount();
        float scrimOpacity = 0;
        for (int i = 0; i < childCount; i++) {
            final float onscreen = ((LayoutParams) getChildAt(i).getLayoutParams()).onScreen;
            scrimOpacity = Math.max(scrimOpacity, onscreen);
        }
        mScrimOpacity = scrimOpacity;

        // "|" used on purpose; both need to run.
        if (mLeftDragger.continueSettling(true) | mRightDragger.continueSettling(true)) {
            postInvalidateOnAnimation();
        }
    }

    private static boolean hasOpaqueBackground(View v) {
        final Drawable bg = v.getBackground();
        if (bg != null) {
            return bg.getOpacity() == PixelFormat.OPAQUE;
        }
        return false;
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final int height = getHeight();
        final boolean drawingContent = isContentView(child);
        int clipLeft = 0, clipRight = getWidth();

        final int restoreCount = canvas.save();
        if (drawingContent) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View v = getChildAt(i);
                if (v == child || v.getVisibility() != VISIBLE ||
                        !hasOpaqueBackground(v) || !isDrawerView(v) ||
                        v.getHeight() < height) {
                    continue;
                }

                if (checkDrawerViewAbsoluteGravity(v, Gravity.LEFT)) {
                    clipLeft = child.getLeft();
                } else {
                    clipRight = child.getRight();
                }
            }
            canvas.clipRect(clipLeft, 0, clipRight, getHeight());
        }
        final boolean result = super.drawChild(canvas, child, drawingTime);
        canvas.restoreToCount(restoreCount);

        if (mScrimOpacity > 0 && drawingContent && mScrimColor > 0) {
            final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
            final int imag = (int) (baseAlpha * mScrimOpacity);
            final int color = imag << 24 | (mScrimColor & 0xffffff);
            mScrimPaint.setColor(color);

            canvas.drawRect(clipLeft, 0, clipRight, getHeight(), mScrimPaint);
        } else if (mShadowLeft != null && checkDrawerViewAbsoluteGravity(child, Gravity.LEFT)) {
            final int shadowWidth = mShadowLeft.getIntrinsicWidth();
            final int childRight = (int) (child.getRight() + child.getTranslationX());
            final int drawerPeekDistance = mLeftDragger.getEdgeSize();
            final float alpha =
                    Math.max(0, Math.min((float) childRight / drawerPeekDistance, 1.f));
            mShadowLeft.setBounds(childRight, child.getTop(),
                    childRight + shadowWidth, child.getBottom());
            mShadowLeft.setAlpha((int) (0xff * alpha));
            mShadowLeft.draw(canvas);
        } else if (mShadowRight != null && checkDrawerViewAbsoluteGravity(child, Gravity.RIGHT)) {
            final int shadowWidth = mShadowRight.getIntrinsicWidth();
            final int childLeft = (int) (child.getLeft() + child.getTranslationX());
            final int showing = getWidth() - childLeft;
            final int drawerPeekDistance = mRightDragger.getEdgeSize();
            final float alpha =
                    Math.max(0, Math.min((float) showing / drawerPeekDistance, 1.f));
            mShadowRight.setBounds(childLeft - shadowWidth, child.getTop(),
                    childLeft, child.getBottom());
            mShadowRight.setAlpha((int) (0xff * alpha));
            mShadowRight.draw(canvas);
        }
        return result;
    }

    boolean isContentView(View child) {
        if (child == null) {
            return false;
        }
        return ((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
    }

    boolean isDrawerView(View child) {
        if (child == null) {
            return false;
        }
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        final int absGravity = Gravity.getAbsoluteGravity(gravity,
                child.getLayoutDirection());
        return (absGravity & (Gravity.LEFT | Gravity.RIGHT)) != 0;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();

        // "|" used deliberately here; both methods should be invoked.
        final boolean interceptForDrag = mLeftDragger.shouldInterceptTouchEvent(ev) |
                mRightDragger.shouldInterceptTouchEvent(ev);

        boolean interceptForTap = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionX = x;
                mInitialMotionY = y;
                if (mScrimOpacity > 0 &&
                        isContentView(mLeftDragger.findTopChildUnder((int) x, (int) y))) {
                    interceptForTap = true;
                }
                mChildrenCanceledTouch = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // If we cross the touch slop, don't perform the delayed peek for an edge touch.
                if (mLeftDragger.checkTouchSlop(ViewDragHelper.DIRECTION_ALL)) {
                    mLeftCallback.removeCallbacks();
                    mRightCallback.removeCallbacks();
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                closeDrawers(true);
                mChildrenCanceledTouch = false;
            }
        }

        return interceptForDrag || interceptForTap || hasPeekingDrawer() || mChildrenCanceledTouch;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mLeftDragger.processTouchEvent(ev);
        mRightDragger.processTouchEvent(ev);

        boolean wantTouchEvents = true;

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                final float x = ev.getX();
                final float y = ev.getY();
                mInitialMotionX = x;
                mInitialMotionY = y;
                mChildrenCanceledTouch = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                final float x = ev.getX();
                final float y = ev.getY();
                boolean peekingOnly = true;
                final View touchedView = mLeftDragger.findTopChildUnder((int) x, (int) y);
                if (touchedView != null && isContentView(touchedView)) {
                    final float dx = x - mInitialMotionX;
                    final float dy = y - mInitialMotionY;
                    final int slop = mLeftDragger.getTouchSlop();
                    if (dx * dx + dy * dy < slop * slop) {
                        // Taps close a dimmed open drawer but only if it isn't locked open.
                        final View openDrawer = findOpenDrawer();
                        if (openDrawer != null) {
                            peekingOnly = getDrawerLockMode(openDrawer) == LOCK_MODE_LOCKED_OPEN;
                        }
                    }
                }
                closeDrawers(peekingOnly);
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                closeDrawers(true);
                mChildrenCanceledTouch = false;
                break;
            }
        }

        return wantTouchEvents;
    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (CHILDREN_DISALLOW_INTERCEPT ||
                (!mLeftDragger.isEdgeTouched(ViewDragHelper.EDGE_LEFT) &&
                !mRightDragger.isEdgeTouched(ViewDragHelper.EDGE_RIGHT))) {
            // keep the info about if the innerViews do requestDisallowInterceptTouchEvent
            mIsDisallowIntercept = disallowIntercept;
            // If we have an edge touch we want to skip this and track it for later instead.
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
        if (disallowIntercept) {
            closeDrawers(true);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // the incorrect array size will only happen in the multi-touch scenario.
        if (ev.getPointerCount() > 1 && mIsDisallowIntercept) {
            requestDisallowInterceptTouchEvent(false);
            boolean handled = super.dispatchTouchEvent(ev);
            requestDisallowInterceptTouchEvent(true);
            return handled;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    /**
     * Close all currently open drawer views by animating them out of view.
     */
    public void closeDrawers() {
        closeDrawers(false);
    }

    void closeDrawers(boolean peekingOnly) {
        boolean needsInvalidate = false;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (!isDrawerView(child) || (peekingOnly && !lp.isPeeking)) {
                continue;
            }

            View contentView = findContentView();
            if (checkDrawerViewAbsoluteGravity(child, Gravity.LEFT)) {
                needsInvalidate |= mLeftDragger.smoothSlideViewTo(contentView,
                        0, contentView.getTop());
            } else {
                needsInvalidate |= mRightDragger.smoothSlideViewTo(contentView,
                        0, contentView.getTop());
            }

            lp.isPeeking = false;
        }

        mLeftCallback.removeCallbacks();
        mRightCallback.removeCallbacks();

        if (needsInvalidate) {
            invalidate();
        }
    }

    /**
     * Open the specified drawer view by animating it into view.
     *
     * @param drawerView Drawer view to open
     */
    public void openDrawer(View drawerView) {
        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
        }

        if (mFirstLayout) {
            final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
            lp.onScreen = 1.f;
            lp.knownOpen = true;
        } else {
            final View contentView = findContentView();
            if (checkDrawerViewAbsoluteGravity(drawerView, Gravity.LEFT)) {
                mLeftDragger.smoothSlideViewTo(contentView, drawerView.getWidth(), contentView.getTop());
            } else {
                mRightDragger.smoothSlideViewTo(contentView, -drawerView.getWidth(),
                        contentView.getTop());
            }
        }
        invalidate();
    }

    /**
     * Open the specified drawer by animating it out of view.
     *
     * @param gravity Gravity.LEFT to move the left drawer or Gravity.RIGHT for the right.
     *                Gravity.START or Gravity.END may also be used.
     */
    public void openDrawer(int gravity) {
        final View drawerView = findDrawerWithGravity(gravity);
        if (drawerView == null) {
            throw new IllegalArgumentException("No drawer view found with gravity " +
                    gravityToString(gravity));
        }
        openDrawer(drawerView);
    }

    /**
     * Close the specified drawer view by animating it into view.
     *
     * @param drawerView Drawer view to close
     */
    public void closeDrawer(View drawerView) {
        if (!isDrawerView(drawerView)) {
            throw new IllegalArgumentException("View " + drawerView + " is not a sliding drawer");
        }

        if (mFirstLayout) {
            final LayoutParams lp = (LayoutParams) drawerView.getLayoutParams();
            lp.onScreen = 0.f;
            lp.knownOpen = false;
        } else {
            final View contentView = findContentView();
            if (checkDrawerViewAbsoluteGravity(drawerView, Gravity.LEFT)) {
                mLeftDragger.smoothSlideViewTo(contentView, 0,
                        contentView.getTop());
            } else {
                mRightDragger.smoothSlideViewTo(contentView, 0, contentView.getTop());
            }
        }
        invalidate();
    }

    /**
     * Close the specified drawer by animating it out of view.
     *
     * @param gravity Gravity.LEFT to move the left drawer or Gravity.RIGHT for the right.
     *                Gravity.START or Gravity.END may also be used.
     */
    public void closeDrawer(int gravity) {
        final View drawerView = findDrawerWithGravity(gravity);
        if (drawerView == null) {
            throw new IllegalArgumentException("No drawer view found with gravity " +
                    gravityToString(gravity));
        }
        closeDrawer(drawerView);
    }

    /**
     * Check if the given drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state. To check for partial visibility use
     * {@link #isDrawerVisible(android.view.View)}.
     *
     * @param drawer Drawer view to check
     * @return true if the given drawer view is in an open state
     * @see #isDrawerVisible(android.view.View)
     */
    public boolean isDrawerOpen(View drawer) {
        if (!isDrawerView(drawer)) {
            throw new IllegalArgumentException("View " + drawer + " is not a drawer");
        }
        return ((LayoutParams) drawer.getLayoutParams()).knownOpen;
    }

    /**
     * Check if the given drawer view is currently in an open state.
     * To be considered "open" the drawer must have settled into its fully
     * visible state. If there is no drawer with the given gravity this method
     * will return false.
     *
     * @param drawerGravity Gravity of the drawer to check
     * @return true if the given drawer view is in an open state
     */
    public boolean isDrawerOpen(int drawerGravity) {
        final View drawerView = findDrawerWithGravity(drawerGravity);
        if (drawerView != null) {
            return isDrawerOpen(drawerView);
        }
        return false;
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere inbetween.
     *
     * @param drawer Drawer view to check
     * @return true if the given drawer is visible on-screen
     * @see #isDrawerOpen(android.view.View)
     */
    public boolean isDrawerVisible(View drawer) {
        if (!isDrawerView(drawer)) {
            throw new IllegalArgumentException("View " + drawer + " is not a drawer");
        }
        return ((LayoutParams) drawer.getLayoutParams()).onScreen > 0;
    }

    /**
     * Check if a given drawer view is currently visible on-screen. The drawer
     * may be only peeking onto the screen, fully extended, or anywhere inbetween.
     * If there is no drawer with the given gravity this method will return false.
     *
     * @param drawerGravity Gravity of the drawer to check
     * @return true if the given drawer is visible on-screen
     */
    public boolean isDrawerVisible(int drawerGravity) {
        final View drawerView = findDrawerWithGravity(drawerGravity);
        if (drawerView != null) {
            return isDrawerVisible(drawerView);
        }
        return false;
    }

    private boolean hasPeekingDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.isPeeking) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams
                ? new LayoutParams((LayoutParams) p)
                : p instanceof ViewGroup.MarginLayoutParams
                ? new LayoutParams((MarginLayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    private boolean hasVisibleDrawer() {
        return findVisibleDrawer() != null;
    }

    private View findVisibleDrawer() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (isDrawerView(child) && isDrawerVisible(child)) {
                return child;
            }
        }
        return null;
    }

    void cancelChildViewTouch() {
        // Cancel child touches
        if (!mChildrenCanceledTouch) {
            final long now = SystemClock.uptimeMillis();
            final MotionEvent cancelEvent = MotionEvent.obtain(now, now,
                    MotionEvent.ACTION_CANCEL, 0.0f, 0.0f, 0);
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                getChildAt(i).dispatchTouchEvent(cancelEvent);
            }
            cancelEvent.recycle();
            mChildrenCanceledTouch = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && hasVisibleDrawer()) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final View visibleDrawer = findVisibleDrawer();
            if (visibleDrawer != null && getDrawerLockMode(visibleDrawer) == LOCK_MODE_UNLOCKED) {
                closeDrawers();
            }
            return visibleDrawer != null;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        if (ss.openDrawerGravity != Gravity.NO_GRAVITY) {
            final View toOpen = findDrawerWithGravity(ss.openDrawerGravity);
            if (toOpen != null) {
                openDrawer(toOpen);
            }
        }

        setDrawerLockMode(ss.lockModeLeft, Gravity.LEFT);
        setDrawerLockMode(ss.lockModeRight, Gravity.RIGHT);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        final SavedState ss = new SavedState(superState);

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (!isDrawerView(child)) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.knownOpen) {
                ss.openDrawerGravity = lp.gravity;
                // Only one drawer can be open at a time.
                break;
            }
        }

        ss.lockModeLeft = mLockModeLeft;
        ss.lockModeRight = mLockModeRight;

        return ss;
    }

    /**
     * State persisted across instances
     */
    protected static class SavedState extends BaseSavedState {
        int openDrawerGravity = Gravity.NO_GRAVITY;
        int lockModeLeft = LOCK_MODE_UNLOCKED;
        int lockModeRight = LOCK_MODE_UNLOCKED;

        public SavedState(Parcel in) {
            super(in);
            openDrawerGravity = in.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(openDrawerGravity);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        private final int mAbsGravity;
        private ViewDragHelper mDragger;
        private View mDrawerView;

        private final Runnable mPeekRunnable = new Runnable() {
            @Override public void run() {
//                peekDrawer();
            }
        };

        public ViewDragCallback(int gravity) {
            mAbsGravity = gravity;
        }

        public void setDragger(ViewDragHelper dragger) {
            mDragger = dragger;
        }

        public void removeCallbacks() {
            DrawerLayout.this.removeCallbacks(mPeekRunnable);
        }

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            // Only capture views where the gravity matches what the content view moving direction.
            if (isContentView(child)) {
                final boolean unlocked =
                        getDrawerLockMode(Gravity.LEFT) == LOCK_MODE_UNLOCKED
                        && getDrawerLockMode(Gravity.RIGHT) == LOCK_MODE_UNLOCKED;
                if (mDrawerState == STATE_IDLE || unlocked) {
                return (mAbsGravity == Gravity.LEFT && child.getLeft() > 0)
                        || (mAbsGravity == Gravity.RIGHT && child.getLeft() < 0);
            }
            }
            return false;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            ensureTargetView(mDragger.getCapturedView());

            updateDrawerState(mAbsGravity, state, mDrawerView);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            ensureTargetView(changedView);

            float offset;
            final int childWidth = mDrawerView.getWidth();

            offset = Math.abs((float) left / childWidth);
            setDrawerViewOffset(mDrawerView, offset);
            mDrawerView.setVisibility(offset == 0 ? INVISIBLE : VISIBLE);
            invalidate();
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            ensureTargetView(capturedChild);

            final LayoutParams lp = (LayoutParams) mDrawerView.getLayoutParams();
            lp.isPeeking = false;

            closeOtherDrawer();
        }

        private void closeOtherDrawer() {
            final int otherGrav = mAbsGravity == Gravity.LEFT ? Gravity.RIGHT : Gravity.LEFT;
            final View toClose = findDrawerWithGravity(otherGrav);
            if (toClose != null) {
                closeDrawer(toClose);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            ensureTargetView(releasedChild);

            // Offset is how open the drawer is, therefore left/right values
            // are reversed from one another.
            final float offset = getDrawerViewOffset(mDrawerView);
            final int childWidth = mDrawerView.getWidth();

            int left;
            if (checkDrawerViewAbsoluteGravity(mDrawerView, Gravity.LEFT)) {
                left = xvel > 0 || xvel == 0 && offset > 0.5f ? childWidth : 0;
            } else {
                left = xvel < 0 || xvel == 0 && offset > 0.5f ? -childWidth : 0;
            }

            mDragger.settleCapturedViewAt(left, releasedChild.getTop());
            invalidate();
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            postDelayed(mPeekRunnable, PEEK_DELAY);
        }

        private void peekDrawer() {
            final View toCapture;
            final int childLeft;
            final boolean leftEdge = mAbsGravity == Gravity.LEFT;
            if (leftEdge) {
                toCapture = findDrawerWithGravity(Gravity.LEFT);
                childLeft = 0;
            } else {
                toCapture = findDrawerWithGravity(Gravity.RIGHT);
                childLeft = 0;
            }
            // Only peek if it would mean making the drawer more visible and the drawer isn't locked
            if (toCapture != null && ((leftEdge && toCapture.getLeft() < childLeft) ||
                    (!leftEdge && toCapture.getLeft() > childLeft)) &&
                    getDrawerLockMode(toCapture) == LOCK_MODE_UNLOCKED) {
                mDrawerView = toCapture;
                final LayoutParams lp = (LayoutParams) toCapture.getLayoutParams();
                final View contentView = findContentView();
                mDragger.smoothSlideViewTo(contentView, childLeft, contentView.getTop());
                lp.isPeeking = true;
                invalidate();

                closeOtherDrawer();

                cancelChildViewTouch();
            }
        }

        @Override
        public boolean onEdgeLock(int edgeFlags) {
            if (ALLOW_EDGE_LOCK) {
                final View drawer = findDrawerWithGravity(mAbsGravity);
                if (drawer != null && !isDrawerOpen(drawer)) {
                    closeDrawer(drawer);
                }
                return true;
            }
            return false;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            final View toCapture;
            if ((edgeFlags & ViewDragHelper.EDGE_LEFT) == ViewDragHelper.EDGE_LEFT) {
                toCapture = findDrawerWithGravity(Gravity.LEFT);
            } else {
                toCapture = findDrawerWithGravity(Gravity.RIGHT);
            }

            if (toCapture != null && getDrawerLockMode(toCapture) == LOCK_MODE_UNLOCKED) {
                mDrawerView = toCapture;
                mDragger.captureChildView(findContentView(), pointerId);
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            ensureTargetView(child);

            return mDrawerView.getWidth();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            ensureTargetView(child);

            if (checkDrawerViewAbsoluteGravity(mDrawerView, Gravity.LEFT)) {
                return Math.min(mDrawerView.getWidth(), Math.max(left, 0));
            } else {
                return Math.max(-mDrawerView.getWidth(), Math.min(left, 0));
            }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            ensureTargetView(child);

            return mDrawerView.getTop();
        }

        private void ensureTargetView(View child) {
            if (mDrawerView == null) {
                mDrawerView = findDrawerWithGravity(mAbsGravity);
            }
            if (mDrawerView == null) {
                mDrawerView = child;
            }
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        public int gravity = Gravity.NO_GRAVITY;
        float onScreen;
        boolean isPeeking;
        boolean knownOpen;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            this(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }

    private void setContentViewOffset(View drawerView, float slideOffset) {
        final View contentView = findContentView();
        if (contentView == null) {
            return;
        }

        final float drawerWidth = (float) drawerView.getWidth();
        final float contentWidth = (float) contentView.getWidth();
        final float scale = 1.0f - (slideOffset * CONTENT_VIEW_TARGET_SCALE);
        final float drawerscale = 1.0f + CONTENT_VIEW_TARGET_SCALE * (slideOffset - 1);
        final float density = getResources().getDisplayMetrics().density;
        float px = (int) ((float)GAP_BETWEEN_MENU * density);
        px = px / CONTENT_VIEW_TARGET_SCALE;
        float gap = px * (1.0f - scale);
        float alpha = ((1.0f - scale) / CONTENT_VIEW_TARGET_SCALE);
        final float contentOffset = -(contentWidth * (1.0f - scale)) / 2;
        if (mStatusBarBackground != null) {
            if (mStatusBarBackground.getVisibility() == View.VISIBLE) {
                mStatusBarBackground.setAlpha(1.0f - alpha);
            }
        }
        // BEGIN bob_ding@asus.com
        // Set status bar color when sdk version >= L
        if (VERSION_L_OR_LATER && mStatusBarColor != Color.TRANSPARENT
                && mReparented) {
            int statusPlpha = Math.round(255 * (1.0f - alpha));
            int currentColor = (statusPlpha << 24)
                    | (((mStatusBarColor >> 16) & 0xFF) << 16)
                    | (((mStatusBarColor >> 8) & 0xFF) << 8)
                    | (mStatusBarColor & 0xFF);
            mHostActivity.getWindow().setStatusBarColor(currentColor);
        }
        // END bob_ding@asus.com
        contentView.setScaleX(scale);
        contentView.setScaleY(scale);
        contentView.setTranslationX(mLeftEdgeDrawer ? contentOffset + gap : -contentOffset - gap );
        drawerView.setAlpha(alpha);
        drawerView.setX(mLeftEdgeDrawer
                        ? (int)(drawerWidth * DRAWER_OFFSET * (slideOffset - 1))
                        : (int)(this.getWidth() - drawerWidth * (DRAWER_OFFSET * (1 + slideOffset))));
        drawerView.setScaleX(drawerscale);
        drawerView.setScaleY(drawerscale);
        if (mReparented) {
            final float actionBarWidth = mActionBar.getWidth();
            final float actionBarOffset = (drawerWidth * slideOffset)
                    - (actionBarWidth * (1.0f - scale)) / 2;
            mActionBar.setScaleX(scale);
            mActionBar.setScaleY(scale);
            mActionBar.setTranslationX(mLeftEdgeDrawer ? actionBarOffset + gap: -actionBarOffset - gap);
        }
    }

    private View findContentView() {
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
            if (gravity == Gravity.NO_GRAVITY) {
                return child;
            }
        }
        return null;
    }

    public void attachActivity(Activity hostActivity,
            ColorfulLinearLayout colorful, int color) {
        if (!mIsScaleEnabled) {
            return;
        }
        if (hostActivity == null) {
            throw new IllegalArgumentException(
                    "The host activity could not be null");
        }
        if (hostActivity.getActionBar() == null) {
            return;
        }
        if (colorful == null) {
            throw new IllegalArgumentException(
                    "The colorfullinearlayout could not be null");
        }
        mHostColorful = colorful;
        mHostColorful.setActionBarBackgroundColor(color);
        setStatusBarBackgroundColor(color);
        mOldHostColorfulVisibility = mHostColorful.getVisibility();
        if (mOldHostColorfulVisibility == View.VISIBLE) {
            mHostColorful.setStatusBarBackgroundVisibility(View.INVISIBLE);
        }

        attachActivity(hostActivity);
        }

    public void attachActivity(Activity hostActivity) {
        if (!mIsScaleEnabled) {
            return;
        }
        if (hostActivity == null) {
            throw new IllegalArgumentException(
                    "The host activity could not be null");
        }
        if (hostActivity.getActionBar() == null) {
            return;
        }
        ViewGroup decor = (ViewGroup) hostActivity.getWindow().getDecorView();
        if (mReparented || getParent() == decor) {
            throw new IllegalArgumentException(
                    "The DrawerLayout has been reparented!!!");
        }
        View drawerView = findDrawerWithGravity(Gravity.START);
        mMenuPaddingTop = drawerView.getPaddingTop();
        View contentView = findContentView();
        mContentPaddingTop = contentView.getPaddingTop();
        mHostActivity = hostActivity;
        // BEGIN bob_ding@asus.com
        if (VERSION_L_OR_LATER) {
            int statusBarColor = mHostActivity.getWindow().getStatusBarColor();
            if (statusBarColor != Color.TRANSPARENT) {
                mStatusBarColor = statusBarColor;
            }
        }
        //END bob_ding@asus.com
        mActionBar = (ViewGroup) decor.getChildAt(0);
        if (VERSION_L_OR_LATER) {
            mActionBar.addOnLayoutChangeListener(mActionBarLayoutChangeListener);
        }
        mOldDrawerLayoutParent = (ViewGroup) getParent();
        mOldDrawerLayoutParent.removeView(this);
        addView(mStatusBarBackground,
                new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        decor.addView(this, 0);
        resetScaleAndTranslation();
        mReparented = true;
    }

    public void detachActivity() {
        if (!mReparented) {
            return;
        }
        ViewGroup decor = (ViewGroup) mHostActivity.getWindow().getDecorView();
        if (decor != getParent()) {
            throw new IllegalArgumentException(
                    "The DrawerLayout does not be reparented!!!");
        }
        resetScaleAndTranslation();
        decor.removeView(this);
            removeView(mStatusBarBackground);
        mOldDrawerLayoutParent.addView(this);
        if (mHostColorful != null) {
            mHostColorful
                    .setStatusBarBackgroundVisibility(mOldHostColorfulVisibility);
            mHostColorful = null;
        }
        if (mContentPaddingTop >= 0) {
            View contentView = findContentView();
            contentView.setPadding(contentView.getPaddingLeft(),
                    mMenuPaddingTop, contentView.getPaddingRight(),
                    contentView.getPaddingBottom());
            mContentPaddingTop = -1;
        }
        if (mMenuPaddingTop >= 0) {
            View drawerView = findDrawerWithGravity(Gravity.START);
            drawerView
                    .setPadding(drawerView.getPaddingLeft(), mMenuPaddingTop,
                            drawerView.getPaddingRight(),
                            drawerView.getPaddingBottom());
            mMenuPaddingTop = -1;
        }
        if (VERSION_L_OR_LATER) {
            mActionBar.removeOnLayoutChangeListener(mActionBarLayoutChangeListener);
        }
        // BEGIN bob_ding@asus.com
        if (VERSION_L_OR_LATER && mStatusBarColor != Color.TRANSPARENT) {
            mHostActivity.getWindow().setStatusBarColor(mStatusBarColor);
            mStatusBarColor = Color.TRANSPARENT;
        }
        // END bob_ding@asus.com
        mHostActivity = null;
        mOldDrawerLayoutParent = null;
        mActionBar = null;
        mReparented = false;
    }

    public void enableShiftContent(boolean enable) {
        if (mShiftContent != enable) {
            mShiftContent = enable;
            requestLayout();
        }
    }

    public boolean isEnableShiftContent() {
        return mShiftContent;
    }

    private void resetScaleAndTranslation() {
        final View contentView = findContentView();
        if (contentView != null) {
            contentView.setPivotY(0);
            contentView.setScaleX(1.0f);
            contentView.setScaleY(1.0f);
            contentView.setTranslationX(0f);
        }
        if (mActionBar != null) {
            mActionBar.setPivotY(0);
            mActionBar.setScaleX(1.0f);
            mActionBar.setScaleY(1.0f);
            mActionBar.setTranslationX(0f);
        }
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

    private void initColorfulView() {
        mStatusBarBackground = new TextView(getContext());
    }

    private int getStatusBarHeight() {
        // We don't take the status bar into account if application is
        // not running on default display.
        final Display display = getDisplay();
        if (display != null && display.getDisplayId() != Display.DEFAULT_DISPLAY) {
            return 0;
        }

        if (mHostActivity != null
                && (mHostActivity.getWindow().getAttributes().flags
                        & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0) {
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
            if (!mShiftContent) {
                h = 0;
            } else {
                h = actionBar.isShowing() ? actionBar.getHeight() : 0;
            }
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

    private boolean isSystemUiLayoutStable() {
        final int vis = getWindowSystemUiVisibility();
        final boolean stable = (vis & SYSTEM_UI_FLAG_LAYOUT_STABLE) != 0;
        return stable;
    }

    public void enableForceRelayout(boolean enable) {
        mForceRelayout = enable;
        if (mForceRelayout) {
            requestLayout();
        }
    }

    private void setChildPaddingIfNeed(View child, int paddingLeft, int paddingTop,
            int paddingRight, int paddingBottom) {
        final int oldPaddintLeft = child.getPaddingLeft();
        final int oldPaddintTop = child.getPaddingTop();
        final int oldPaddintRight = child.getPaddingRight();
        final int oldPaddintBottom = child.getPaddingBottom();
        if (paddingLeft != oldPaddintLeft
            || paddingTop != oldPaddintTop
            || paddingRight != oldPaddintRight
            || paddingBottom != oldPaddintBottom) {

            if (mInLayout) {
                PaddingPair pair = new PaddingPair();
                pair.view = child;
                pair.paddings.set(paddingLeft,
                        paddingTop,
                        paddingRight,
                        paddingBottom);
                Message msg = mHandler.obtainMessage(MSG_PENDING_UPDATE_PADDING, pair);
                mHandler.sendMessage(msg);
            } else {
                child.setPadding(paddingLeft,
                        paddingTop,
                        paddingRight,
                        paddingBottom);
            }
        }
    }

    private class PaddingPair {
        public View view;
        public Rect paddings = new Rect();
    }
}
