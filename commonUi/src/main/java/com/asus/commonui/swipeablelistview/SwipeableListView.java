package com.asus.commonui.swipeablelistview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.asus.commonui.R;
import com.asus.commonui.swipeablelistview.SwipeHelper.Callback;

public class SwipeableListView extends ListView implements Callback {
    private final static int DRAG_ANIM_DURATION = 150;
    private final static int BACK_ANIM_DURATION = 250;
    private ImageView mDragView;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowParams;
    private ObjectAnimator mDragAnimation;
    private int mDragPos = AdapterView.INVALID_POSITION; // which item is being dragged
    private int mFirstDragPos = AdapterView.INVALID_POSITION; // where was the dragged item originally
    private int mDragPointY; // at what offset inside the item did the user grab it
    private int mCoordOffsetY; // the difference between screen coordinates and coordinates in this view
    private int mDragPointX;
    private int mCoordOffsetX;
    private DragListener mDragListener;
    private int mUpperBound;
    private int mLowerBound;
    private int mHeight;
    private int mDividerHeight = getDividerHeight();

    private SwipeHelper mSwipeHelper;
    private boolean mEnableSwipe = false;
    private boolean mEnableDrag = false;
    private SwipeListener mSwipeListener;
    private AdapterWrapper mAdapterWrapper;
    private boolean mBlockLayoutRequests = false; //Needed for adjusting item heights from within layoutChildren

    public SwipeableListView(Context context) {
        this(context, null);
    }

    public SwipeableListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        float densityScale = getResources().getDisplayMetrics().density;
        float pagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(context, SwipeHelper.X, this, densityScale, pagingTouchSlop);
        setItemsCanFocus(true);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        float pagingTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
        endItemAnimation();
        stopDragging();
        mFirstDragPos = AdapterView.INVALID_POSITION;
        mDragPos = AdapterView.INVALID_POSITION;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mEnableDrag == true && mDragListener != null && ev.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            mFirstDragPos = mDragPos = pointToPosition(x, y);
            if (mFirstDragPos == AdapterView.INVALID_POSITION) {
                return super.onInterceptTouchEvent(ev);
            }

            View item = (View) getChildAt(mFirstDragPos - getFirstVisiblePosition());
            mDragPointY = y - item.getTop();
            mCoordOffsetY = ((int) ev.getRawY()) - y;
            mDragPointX = x - item.getLeft();
            mCoordOffsetX = ((int) ev.getRawX()) - x;
            View dragger = item.findViewById(R.id.asus_commonui_drag_list_item_image);
            if (dragger == null) {
                dragger = item.findViewById(R.id.drag_list_item_image);
            }
            if (dragger != null && x > dragger.getLeft() && x < dragger.getRight()) {
                item.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(item.getDrawingCache());
                item.setDrawingCacheEnabled(false);

                startDragging(bitmap, x, y);
                View draggedview = (View) getChildAt(mDragPos - getFirstVisiblePosition());
                draggedview.setVisibility(View.INVISIBLE);

                mHeight = getHeight();
                int itemHeight = item.getHeight();
                mUpperBound = itemHeight + getPaddingTop();
                mLowerBound = mHeight - itemHeight - getPaddingBottom();
                mDragListener.onDragStart(mDragPos);
                return true;
            }
            mFirstDragPos = AdapterView.INVALID_POSITION;
            mDragPos = AdapterView.INVALID_POSITION;
        }
        if (mEnableSwipe) {
            return mSwipeHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mEnableDrag == true && mDragListener != null && mDragView != null && mDragPos != INVALID_POSITION && mFirstDragPos != INVALID_POSITION) {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                View v = (View) getChildAt(mDragPos - getFirstVisiblePosition());
                if (v != null) {
                    v.setVisibility(View.VISIBLE);
                    mDragListener.onDragEnd(mFirstDragPos, mDragPos);
                }
                endItemAnimation();
                stopDragging();
                mFirstDragPos = AdapterView.INVALID_POSITION;
                mDragPos = AdapterView.INVALID_POSITION;
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (mDragPos != INVALID_POSITION) {
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    dragView(x, y);
                }
                break;
            }
            return true;
        }
        if (mEnableSwipe) {
            return mSwipeHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }

    private void startDragging(Bitmap bm, int x, int y) {
        stopDragging();

        mWindowParams = new WindowManager.LayoutParams();
        mWindowParams.gravity = Gravity.TOP | Gravity.START;
        mWindowParams.x = x - mDragPointX + mCoordOffsetX;
        mWindowParams.y = y - mDragPointY + mCoordOffsetY;

        mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mWindowParams.format = PixelFormat.TRANSLUCENT;
        mWindowParams.windowAnimations = 0;

        ImageView v = new ImageView(getContext());
        v.setImageBitmap(bm);

        mWindowManager = (WindowManager) getContext().getSystemService("window");
        mWindowManager.addView(v, mWindowParams);
        mDragView = v;
    }

    private void dragView(int x, int y) {
        // Avoid dragging out of bound
        if (y < 0 || y > mHeight) {
            return;
        }

        // Update the location of the mDragView
        mWindowParams.alpha = 0.4f;
        mWindowParams.y = y - mDragPointY + mCoordOffsetY;
        mWindowManager.updateViewLayout(mDragView, mWindowParams);

        /* Find out the touched position and change with the mDragPos
         * mDragPos Position now dragged
         * touchPos Position now touched
         */
        final int touchPos = getRealItemPos(x, y);
        if (touchPos != AdapterView.INVALID_POSITION && touchPos != mDragPos) {
            endItemAnimation();

            if (touchPos > mFirstDragPos) {
                if (mDragPos < mFirstDragPos) {
                    clearItemTranslationY(mDragPos, mFirstDragPos - 1);
                }
                int begin = (getFirstVisiblePosition() > mFirstDragPos)
                        ? getFirstVisiblePosition() : mFirstDragPos;
                translateItemY(begin, touchPos - 1, false);
                if (mDragPos > touchPos) {
                    clearItemTranslationY(mDragPos, touchPos + 1);
                    animateItemAt(touchPos + 1, false, true);
                } else {
                    animateItemAt(touchPos, false, false);
                }
            } else if (touchPos < mFirstDragPos) {
                if (mDragPos > mFirstDragPos) {
                    clearItemTranslationY(mDragPos, mFirstDragPos + 1);
                }
                int begin = (getLastVisiblePosition() < mFirstDragPos)
                        ? getLastVisiblePosition() : mFirstDragPos;
                translateItemY(begin, touchPos + 1, true);
                if (mDragPos < touchPos) {
                    clearItemTranslationY(mDragPos, touchPos - 1);
                    animateItemAt(touchPos - 1, true, true);
                } else {
                    animateItemAt(touchPos, true, false);
                }
            } else {
                // Drag back to first drag position
                clearItemTranslationY(mDragPos, touchPos);
                animateItemAt(mDragPos, (mDragPos < touchPos), true);
            }
            mDragPos = touchPos;
        }
        updateScrollView(y);
    }

    private void translateItemY(int begin, int end, boolean postive) {
        if (begin > end) {
            int temp = begin;
            begin = end;
            end = temp;
        }

        for (int i = begin; i <= end; ++i) {
            View view = (View) getChildAt(i - getFirstVisiblePosition());
            if (view != null) {
                float translation = view.getHeight() + mDividerHeight;
                if (!postive) {
                    translation = -translation;
                }
                view.setTranslationY(translation);
            }
        }
    }

    private void clearItemTranslationY(int begin, int end) {
        if (begin > end) {
            int temp = begin;
            begin = end;
            end = temp;
        }

        for (int i = begin; i <= end; ++i) {
            View view = (View) getChildAt(i - getFirstVisiblePosition());
            if (view != null) {
                view.setTranslationY(0);
            }
        }
    }

    private void animateItemAt(int index, boolean down, boolean reverse) {
        if (mDragAnimation == null) {
            mDragAnimation = new ObjectAnimator();
            mDragAnimation.setPropertyName("TranslationY");
            mDragAnimation.setDuration(DRAG_ANIM_DURATION);
        }

        View view = (View) getChildAt(index - getFirstVisiblePosition());
        if (view != null) {
            float translation = view.getHeight() + mDividerHeight;
            if (!down) {
                translation = -translation;
            }
            mDragAnimation.setTarget(view);
            mDragAnimation.setFloatValues(
                    reverse ? translation : 0f,
                    reverse ? 0f : translation);
            mDragAnimation.start();
        }
    }

    private void endItemAnimation() {
        if (mDragAnimation != null && mDragAnimation.isRunning()) {
            mDragAnimation.end();
        }
    }

    private int getRealItemPos(int x, int y) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (i < 0) {
                continue;
            }
            final View child = (View) getChildAt(i);
            if (y >= child.getTop() && y <= (child.getBottom() + mDividerHeight)) {
                return i + getFirstVisiblePosition();
            }
        }
        return -1;
    }

    private void stopDragging() {
        if (mDragView != null) {
            mWindowManager.removeView(mDragView);
            mDragView = null;
        }
    }

    private void updateScrollView(int y) {
        mBlockLayoutRequests = true;
        int speed = 0;
        if (y > mLowerBound) {
            // scroll the list up a bit
            speed = 16;
        } else if (y < mUpperBound) {
            // scroll the list down a bit
            speed = -16;
        }
        if (speed != 0) {
            int ref = getRealItemPos(0, mHeight / 2);
            View v = getChildAt(ref - getFirstVisiblePosition());
            if (v != null) {
                int pos = v.getTop();
                setSelectionFromTop(ref, pos - speed - getPaddingTop());
                layoutChildren();
            }
        }
        mBlockLayoutRequests = false;
    }

    @Override
    public void requestLayout() {
        if (!mBlockLayoutRequests) {
            super.requestLayout();
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter != null) {
            mAdapterWrapper = new AdapterWrapper(adapter);
        } else {
            mAdapterWrapper = null;
        }
        super.setAdapter(mAdapterWrapper);
    }

    /**
     * As opposed to {@link ListView#getAdapter()}, which returns
     * a heavily wrapped ListAdapter (DragSortListView wraps the
     * input ListAdapter {\emph and} ListView wraps the wrapped one).
     *
     * @return The ListAdapter set as the argument of {@link setAdapter()}
     */
    public ListAdapter getInputAdapter() {
        if (mAdapterWrapper == null) {
            return null;
        } else {
            return mAdapterWrapper.getAdapter();
        }
    }

    private class AdapterWrapper extends BaseAdapter {
        private ListAdapter mAdapter;

        public AdapterWrapper(ListAdapter adapter) {
            super();
            mAdapter = adapter;
            mAdapter.registerDataSetObserver(new DataSetObserver() {
                public void onChanged() {
                    notifyDataSetChanged();
                }

                public void onInvalidated() {
                    notifyDataSetInvalidated();
                }
            });
        }

        public ListAdapter getAdapter() {
            return mAdapter;
        }

        @Override
        public long getItemId(int position) {
            return mAdapter.getItemId(position);
        }

        @Override
        public Object getItem(int position) {
            return mAdapter.getItem(position);
        }

        @Override
        public int getCount() {
            return mAdapter.getCount();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mAdapter.areAllItemsEnabled();
        }

        @Override
        public boolean isEnabled(int position) {
            return mAdapter.isEnabled(position);
        }

        @Override
        public int getItemViewType(int position) {
            return mAdapter.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return mAdapter.getViewTypeCount();
        }

        @Override
        public boolean hasStableIds() {
            return mAdapter.hasStableIds();
        }

        @Override
        public boolean isEmpty() {
            return mAdapter.isEmpty();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View child = mAdapter.getView(position, convertView, parent);
            if (child != null) {
                int vis = View.VISIBLE;
                float translation = 0f;
                if (mFirstDragPos != AdapterView.INVALID_POSITION) {
                    if (position > mFirstDragPos && position <= mDragPos) {
                        translation = -(child.getHeight() + mDividerHeight);
                    } else if (position < mFirstDragPos && position >= mDragPos) {
                        translation = child.getHeight() + mDividerHeight;
                    } else if (position == mFirstDragPos) {
                        vis = View.INVISIBLE;
                    }
                }
                child.setVisibility(vis);
                child.setTranslationY(translation);
            }
            return child;
        }
    }

    /**
     * SwipeHelper's callback method
     */
    @Override
    public View getChildAtPosition(MotionEvent ev) {
        // find the view under the pointer, accounting for GONE views
        final int count = getChildCount();
        int touchY = (int) ev.getY();
        int childIdx = 0;
        View slidingChild;
        for (; childIdx < count; childIdx++) {
            slidingChild = getChildAt(childIdx);
            if (slidingChild.getVisibility() == GONE) {
                continue;
            }
            if (touchY >= slidingChild.getTop() && touchY <= slidingChild.getBottom()) {
                return slidingChild;
            }
        }
        return null;
    }

    @Override
    public View getChildContentView(View view) {
        return view;
    }

    @Override
    public void onScroll() {
    }

    @Override
    public boolean canChildBeDismissed(View v) {
        return true;
    }

    @Override
    public void onBeginDrag(View v) {
        // We do this so the underlying ScrollView knows that it won't get
        // the chance to intercept events anymore
        requestDisallowInterceptTouchEvent(true);
    }

    @Override
    public void onChildDismissed(View v) {
        if (v != null) {
            if (mSwipeListener != null) {
                mSwipeListener.onSwipe(v);
            }
        }
    }

    @Override
    public void onDragCancelled(View v) {
    }

    /**
     * Enable swipe gestures.
     */
    public void enableSwipe(boolean enable) {
        mEnableSwipe = enable;
    }

    public boolean isSwipeEnabled() {
        return mEnableSwipe;
    }

    /**
     * Enable drag gestures.
     */
    public void enableDrag(boolean enable) {
        mEnableDrag = enable;
    }

    public boolean isDragEnabled() {
        return mEnableDrag;
    }

    /**
     * Listener
     */
    public void setDragListener(DragListener l) {
        mDragListener = l;
    }

    public interface DragListener {
        void onDragStart(int pos);
        void onDragEnd(int from, int to);
    }

    public void setSwipeListener(SwipeListener listener) {
        mSwipeListener = listener;
    }

    public interface SwipeListener {
        public void onSwipe(View v);
    }

    /**
     * API of animation for app to use
     */
    public void animateViewBack(final View v) {
        int width = getWidth();
        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "TranslationX", width, 0f);
        anim.setDuration(BACK_ANIM_DURATION);
        anim.start();
    }

    public ValueAnimator animateViewShrink(final View v) {
        final ViewGroup.LayoutParams lp = v.getLayoutParams();
        int originalHeight = v.getHeight();
        ValueAnimator anim = ValueAnimator.ofInt(originalHeight, 0).setDuration(200);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                v.setLayoutParams(lp);
            }
        });
        return anim;
    }
}
