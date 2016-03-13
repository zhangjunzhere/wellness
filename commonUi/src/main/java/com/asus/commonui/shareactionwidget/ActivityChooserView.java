/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.asus.commonui.shareactionwidget;

import com.asus.commonui.shareactionwidget.ActivityChooserModel.AsusActivityChooserModelClient;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.asus.commonui.R;

/**
 * This class is a view for choosing an activity for handling a given {@link Intent}.
 * <p>
 * The view is composed of two adjacent buttons:
 * <ul>
 * <li>
 * The left button is an immediate action and allows one click activity choosing.
 * Tapping this button immediately executes the intent without requiring any further
 * user input. Long press on this button shows a popup for changing the default
 * activity.
 * </li>
 * <li>
 * The right button is an overflow action and provides an optimized menu
 * of additional activities. Tapping this button shows a popup anchored to this
 * view, listing the most frequently used activities. This list is initially
 * limited to a small number of items in frequency used order. The last item,
 * "Show all..." serves as an affordance to display all available activities.
 * </li>
 * </ul>
 * </p>
 *
 * @hide
 */
public class ActivityChooserView extends ViewGroup implements AsusActivityChooserModelClient {

    private static final String LOG_TAG = "AsusActivityChooserView";

    /**
     * An adapter for displaying the activities in an {@link AdapterView}.
     */
    private final ActivityChooserViewAdapter mAdapter;

    /**
     * Implementation of various interfaces to avoid publishing them in the APIs.
     */
    private final Callbacks mCallbacks;

    /**
     * The content of this view.
     */
    private final LinearLayout mActivityChooserContent;

    /**
     * Stores the background drawable to allow hiding and latter showing.
     */
    private final Drawable mActivityChooserContentBackground;

    /**
     * The expand activities action button;
     */
    private final FrameLayout mExpandActivityOverflowButton;

    /**
     * The image for the expand activities action button;
     */
    private final ImageView mExpandActivityOverflowButtonImage;

    /**
     * The default activities action button;
     */
    private final FrameLayout mDefaultActivityButton;

    /**
     * The image for the default activities action button;
     */
    private final ImageView mDefaultActivityButtonImage;

    /**
     * The maximal width of the list popup.
     */
    private final int mListPopupMaxWidth;

    /**
     * The ActionProvider hosting this view, if applicable.
     */
    ShareActionProvider mProvider;

    /**
     * Observer for the model data.
     */
    private final DataSetObserver mModelDataSetOberver = new DataSetObserver() {

        @Override
        public void onChanged() {
            super.onChanged();
            mAdapter.notifyDataSetChanged();
        }
        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mAdapter.notifyDataSetInvalidated();
        }
    };

    private final OnGlobalLayoutListener mOnGlobalLayoutListener = new OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (isShowingPopup()) {
                if (!isShown()) {
                    getListPopupWindow().dismiss();
                } else {
                    getListPopupWindow().show();
                    if (mProvider != null) {
                        mProvider.onSubUiVisibilityChanged(true);
                    }
                }
            }
        }
    };

    /**
     * Popup window for showing the activity overflow list.
     */
    private ListPopupWindow mListPopupWindow;

    /**
     * Listener for the dismissal of the popup/alert.
     */
    private PopupWindow.OnDismissListener mOnDismissListener;

    /**
     * Flag whether a default activity currently being selected.
     */
    private boolean mIsSelectingDefaultActivity;

    /**
     * The count of activities in the popup.
     */
    private int mInitialActivityCount = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_DEFAULT;

    /**
     * Flag whether this view is attached to a window.
     */
    private boolean mIsAttachedToWindow;

    /**
     * String resource for formatting content description of the default target.
     */
    private int mDefaultActionButtonContentDescription;

    private Context mContext;
    /**
     * Create a new instance.
     *
     * @param context The application environment.
     */
    public ActivityChooserView(Context context) {
        this(context, null);
        mContext = context;
    }

    /**
     * Create a new instance.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     */
    public ActivityChooserView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    /**
     * Create a new instance.
     *
     * @param context The application environment.
     * @param attrs A collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public ActivityChooserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;

        TypedArray attributesArray = context.obtainStyledAttributes(attrs,
                R.styleable.asus_commonui_ActivityChooserView, defStyle, 0);

        mInitialActivityCount = attributesArray.getInt(
                R.styleable.asus_commonui_ActivityChooserView_asusInitialActivityCount,
                ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_DEFAULT);

        Drawable expandActivityOverflowButtonDrawable = attributesArray.getDrawable(
                R.styleable.asus_commonui_ActivityChooserView_asusExpandActivityOverflowButtonDrawable);

        attributesArray.recycle();

        LayoutInflater inflater = LayoutInflater.from(mContext);
        inflater.inflate(R.layout.asus_commonui_activity_chooser_view, this, true);

        mCallbacks = new Callbacks();

        mActivityChooserContent = (LinearLayout) findViewById(
                R.id.asus_commonui_activity_chooser_view_content);
        mActivityChooserContentBackground = mActivityChooserContent.getBackground();

        mDefaultActivityButton = (FrameLayout) findViewById(
                R.id.asus_commonui_default_activity_button);
        mDefaultActivityButton.setOnClickListener(mCallbacks);
        mDefaultActivityButton.setOnLongClickListener(mCallbacks);
        mDefaultActivityButtonImage = (ImageView) mDefaultActivityButton.findViewById(
                R.id.asus_commonui_image_default);

        final FrameLayout expandButton = (FrameLayout) findViewById(
                R.id.asus_commonui_expand_activities_button);
        expandButton.setOnClickListener(mCallbacks);
        expandButton.setOnTouchListener(new ForwardingListener(expandButton) {
            @Override
            public ListPopupWindow getPopup() {
                return getListPopupWindow();
            }

            @Override
            protected boolean onForwardingStarted() {
                showPopup();
                return true;
            }

            @Override
            protected boolean onForwardingStopped() {
                dismissPopup();
                return true;
            }
        });
        mExpandActivityOverflowButton = expandButton;

        mExpandActivityOverflowButtonImage =
                (ImageView) mExpandActivityOverflowButton.findViewById(
                        R.id.asus_commonui_image_expand);
        mExpandActivityOverflowButtonImage.setImageDrawable(expandActivityOverflowButtonDrawable);

        mAdapter = new ActivityChooserViewAdapter();
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateAppearance();
            }
        });

        Resources resources = context.getResources();
        mListPopupMaxWidth = Math.max(resources.getDisplayMetrics().widthPixels / 2,
              resources.getDimensionPixelSize(R.dimen.asus_commonui_config_prefDialogWidth));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActivityChooserModel(ActivityChooserModel dataModel) {
        mAdapter.setDataModel(dataModel);
        if (isShowingPopup()) {
            dismissPopup();
            showPopup();
        }
    }

    /**
     * Sets the background for the button that expands the activity
     * overflow list.
     *
     * <strong>Note:</strong> Clients would like to set this drawable
     * as a clue about the action the chosen activity will perform. For
     * example, if a share activity is to be chosen the drawable should
     * give a clue that sharing is to be performed.
     *
     * @param drawable The drawable.
     */
    public void setExpandActivityOverflowButtonDrawable(Drawable drawable) {
        mExpandActivityOverflowButtonImage.setImageDrawable(drawable);
    }

    /**
     * Sets the content description for the button that expands the activity
     * overflow list.
     *
     * description as a clue about the action performed by the button.
     * For example, if a share activity is to be chosen the content
     * description should be something like "Share with".
     *
     * @param resourceId The content description resource id.
     */
    public void setExpandActivityOverflowButtonContentDescription(int resourceId) {
        CharSequence contentDescription = mContext.getString(resourceId);
        mExpandActivityOverflowButtonImage.setContentDescription(contentDescription);
    }

    /**
     * Set the provider hosting this view, if applicable.
     * @hide Internal use only
     */
    public void setProvider(ShareActionProvider provider) {
        mProvider = provider;
    }

    /**
     * Shows the popup window with activities.
     *
     * @return True if the popup was shown, false if already showing.
     */
    public boolean showPopup() {
        if (isShowingPopup() || !mIsAttachedToWindow) {
            return false;
        }
        mIsSelectingDefaultActivity = false;
        showPopupUnchecked(mInitialActivityCount);
        return true;
    }

    /**
     * Shows the popup no matter if it was already showing.
     *
     * @param maxActivityCount The max number of activities to display.
     */
    private void showPopupUnchecked(int maxActivityCount) {
        if (mAdapter.getDataModel() == null) {
            throw new IllegalStateException("No data model. Did you call #setDataModel?");
        }

        getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);

        final boolean defaultActivityButtonShown =
            mDefaultActivityButton.getVisibility() == VISIBLE;

        final int activityCount = mAdapter.getActivityCount();
        final int maxActivityCountOffset = defaultActivityButtonShown ? 1 : 0;
        if (maxActivityCount != ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED
                && activityCount > maxActivityCount + maxActivityCountOffset) {
            mAdapter.setShowFooterView(true);
            mAdapter.setMaxActivityCount(maxActivityCount - 1);
        } else {
            mAdapter.setShowFooterView(false);
            mAdapter.setMaxActivityCount(maxActivityCount);
        }

        ListPopupWindow popupWindow = getListPopupWindow();
        if (!popupWindow.isShowing()) {
            if (mIsSelectingDefaultActivity || !defaultActivityButtonShown) {
                mAdapter.setShowDefaultActivity(true, defaultActivityButtonShown);
            } else {
                mAdapter.setShowDefaultActivity(false, false);
            }
            final int contentWidth = Math.min(mAdapter.measureContentWidth(), mListPopupMaxWidth);
            popupWindow.setContentWidth(contentWidth);
            popupWindow.show();
            if (mProvider != null) {
                mProvider.onSubUiVisibilityChanged(true);
            }
            popupWindow.getListView().setContentDescription(mContext.getString(
                    R.string.asus_commonui_activitychooserview_choose_application));
        }
    }

    /**
     * Dismisses the popup window with activities.
     *
     * @return True if dismissed, false if already dismissed.
     */
    public boolean dismissPopup() {
        if (isShowingPopup()) {
            getListPopupWindow().dismiss();
            ViewTreeObserver viewTreeObserver = getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            }
        }
        return true;
    }

    /**
     * Gets whether the popup window with activities is shown.
     *
     * @return True if the popup is shown.
     */
    public boolean isShowingPopup() {
        return getListPopupWindow().isShowing();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ActivityChooserModel dataModel = mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.registerObserver(mModelDataSetOberver);
        }
        mIsAttachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ActivityChooserModel dataModel = mAdapter.getDataModel();
        if (dataModel != null) {
            dataModel.unregisterObserver(mModelDataSetOberver);
        }
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }
        if (isShowingPopup()) {
            dismissPopup();
        }
        mIsAttachedToWindow = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        View child = mActivityChooserContent;
        // If the default action is not visible we want to be as tall as the
        // ActionBar so if this widget is used in the latter it will look as
        // a normal action button.
        if (mDefaultActivityButton.getVisibility() != VISIBLE) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec),
                    MeasureSpec.EXACTLY);
        }
        measureChild(child, widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mActivityChooserContent.layout(0, 0, right - left, bottom - top);
        if (!isShowingPopup()) {
            dismissPopup();
        }
    }

    public ActivityChooserModel getDataModel() {
        return mAdapter.getDataModel();
    }

    /**
     * Sets a listener to receive a callback when the popup is dismissed.
     *
     * @param listener The listener to be notified.
     */
    public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        mOnDismissListener = listener;
    }

    /**
     * Sets the initial count of items shown in the activities popup
     * i.e. the items before the popup is expanded. This is an upper
     * bound since it is not guaranteed that such number of intent
     * handlers exist.
     *
     * @param itemCount The initial popup item count.
     */
    public void setInitialActivityCount(int itemCount) {
        mInitialActivityCount = itemCount;
    }

    /**
     * Sets a content description of the default action button. This
     * resource should be a string taking one formatting argument and
     * will be used for formatting the content description of the button
     * dynamically as the default target changes. For example, a resource
     * pointing to the string "share with %1$s" will result in a content
     * description "share with Bluetooth" for the Bluetooth activity.
     *
     * @param resourceId The resource id.
     */
    public void setDefaultActionButtonContentDescription(int resourceId) {
        mDefaultActionButtonContentDescription = resourceId;
    }

    /**
     * Gets the list popup window which is lazily initialized.
     *
     * @return The popup.
     */
    private ListPopupWindow getListPopupWindow() {
        if (mListPopupWindow == null) {
            mListPopupWindow = new ListPopupWindow(getContext());
            mListPopupWindow.setAdapter(mAdapter);
            mListPopupWindow.setAnchorView(ActivityChooserView.this);
            mListPopupWindow.setModal(true);
            mListPopupWindow.setOnItemClickListener(mCallbacks);
            mListPopupWindow.setOnDismissListener(mCallbacks);
        }
        return mListPopupWindow;
    }

    /**
     * Updates the buttons state.
     */
    private void updateAppearance() {
        // Expand overflow button.
        if (mAdapter.getCount() > 0) {
            mExpandActivityOverflowButton.setEnabled(true);
        } else {
            mExpandActivityOverflowButton.setEnabled(false);
        }
        // Default activity button.
        final int activityCount = mAdapter.getActivityCount();
        final int historySize = mAdapter.getHistorySize();
        if (activityCount==1 || activityCount > 1 && historySize > 0) {
            mDefaultActivityButton.setVisibility(VISIBLE);
            ResolveInfo activity = mAdapter.getDefaultActivity();
            PackageManager packageManager = mContext.getPackageManager();
            mDefaultActivityButtonImage.setImageDrawable(activity.loadIcon(packageManager));
            if (mDefaultActionButtonContentDescription != 0) {
                CharSequence label = activity.loadLabel(packageManager);
                String contentDescription = mContext.getString(
                        mDefaultActionButtonContentDescription, label);
                mDefaultActivityButton.setContentDescription(contentDescription);
            }
        } else {
            mDefaultActivityButton.setVisibility(View.GONE);
        }
        // Activity chooser content.
        if (mDefaultActivityButton.getVisibility() == VISIBLE) {
            mActivityChooserContent.setBackground(mActivityChooserContentBackground);
        } else {
            mActivityChooserContent.setBackground(null);
        }
    }

    /**
     * Interface implementation to avoid publishing them in the APIs.
     */
    private class Callbacks implements AdapterView.OnItemClickListener,
            View.OnClickListener, View.OnLongClickListener, PopupWindow.OnDismissListener {

        // AdapterView#OnItemClickListener
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ActivityChooserViewAdapter adapter = (ActivityChooserViewAdapter) parent.getAdapter();
            final int itemViewType = adapter.getItemViewType(position);
            switch (itemViewType) {
                case ActivityChooserViewAdapter.ITEM_VIEW_TYPE_FOOTER: {
                    showPopupUnchecked(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
                } break;
                case ActivityChooserViewAdapter.ITEM_VIEW_TYPE_ACTIVITY: {
                    dismissPopup();
                    if (mIsSelectingDefaultActivity) {
                        // The item at position zero is the default already.
                        if (position > 0) {
                            mAdapter.getDataModel().setDefaultActivity(position);
                        }
                    } else {
                        // If the default target is not shown in the list, the first
                        // item in the model is default action => adjust index
                        position = mAdapter.getShowDefaultActivity() ? position : position + 1;
                        Intent launchIntent = mAdapter.getDataModel().chooseActivity(position);
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            ResolveInfo resolveInfo = mAdapter.getDataModel().getActivity(position);
                            startActivity(launchIntent, resolveInfo);
                            //                            startActivity(launchIntent);
                        }
                    }
                } break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        // View.OnClickListener
        @Override
        public void onClick(View view) {
            if (view == mDefaultActivityButton) {
                dismissPopup();
                ResolveInfo defaultActivity = mAdapter.getDefaultActivity();
                final int index = mAdapter.getDataModel().getActivityIndex(defaultActivity);
                Intent launchIntent = mAdapter.getDataModel().chooseActivity(index);
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    startActivity(launchIntent, defaultActivity);
                }
            } else if (view == mExpandActivityOverflowButton) {
                mIsSelectingDefaultActivity = false;
                showPopupUnchecked(mInitialActivityCount);
            } else {
                throw new IllegalArgumentException();
            }
        }

        // OnLongClickListener#onLongClick
        @Override
        public boolean onLongClick(View view) {
            if (view == mDefaultActivityButton) {
                if (mAdapter.getCount() > 0) {
                    mIsSelectingDefaultActivity = true;
                    showPopupUnchecked(mInitialActivityCount);
                }
            } else {
                throw new IllegalArgumentException();
            }
            return true;
        }

        // PopUpWindow.OnDismissListener#onDismiss
        @Override
        public void onDismiss() {
            notifyOnDismissListener();
            if (mProvider != null) {
                mProvider.onSubUiVisibilityChanged(false);
            }
        }

        private void notifyOnDismissListener() {
            if (mOnDismissListener != null) {
                mOnDismissListener.onDismiss();
            }
        }

        private void startActivity(Intent intent, ResolveInfo resolveInfo) {
            try {
                mContext.startActivity(intent);
            } catch (RuntimeException re) {
                CharSequence appLabel = resolveInfo.loadLabel(mContext.getPackageManager());
                String message = mContext.getString(
                        R.string.asus_commonui_activitychooserview_choose_application_error, appLabel);
                Log.e(LOG_TAG, message);
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Adapter for backing the list of activities shown in the popup.
     */
    private class ActivityChooserViewAdapter extends BaseAdapter {

        public static final int MAX_ACTIVITY_COUNT_UNLIMITED = Integer.MAX_VALUE;

        public static final int MAX_ACTIVITY_COUNT_DEFAULT = 4;

        private static final int ITEM_VIEW_TYPE_ACTIVITY = 0;

        private static final int ITEM_VIEW_TYPE_FOOTER = 1;

        private static final int ITEM_VIEW_TYPE_COUNT = 3;

        private ActivityChooserModel mDataModel;

        private int mMaxActivityCount = MAX_ACTIVITY_COUNT_DEFAULT;

        private boolean mShowDefaultActivity;

        private boolean mHighlightDefaultActivity;

        private boolean mShowFooterView;

        public void setDataModel(ActivityChooserModel dataModel) {
            ActivityChooserModel oldDataModel = mAdapter.getDataModel();
            if (oldDataModel != null && isShown()) {
                oldDataModel.unregisterObserver(mModelDataSetOberver);
            }
            mDataModel = dataModel;
            if (dataModel != null && isShown()) {
                dataModel.registerObserver(mModelDataSetOberver);
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (mShowFooterView && position == getCount() - 1) {
                return ITEM_VIEW_TYPE_FOOTER;
            } else {
                return ITEM_VIEW_TYPE_ACTIVITY;
            }
        }

        @Override
        public int getViewTypeCount() {
            return ITEM_VIEW_TYPE_COUNT;
        }

        @Override
        public int getCount() {
            int count = 0;
            int activityCount = mDataModel.getActivityCount();
            if (!mShowDefaultActivity && mDataModel.getDefaultActivity() != null) {
                activityCount--;
            }
            count = Math.min(activityCount, mMaxActivityCount);
            if (mShowFooterView) {
                count++;
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            final int itemViewType = getItemViewType(position);
            switch (itemViewType) {
                case ITEM_VIEW_TYPE_FOOTER:
                    return null;
                case ITEM_VIEW_TYPE_ACTIVITY:
                    if (!mShowDefaultActivity && mDataModel.getDefaultActivity() != null) {
                        position++;
                    }
                    return mDataModel.getActivity(position);
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final int itemViewType = getItemViewType(position);
            switch (itemViewType) {
                case ITEM_VIEW_TYPE_FOOTER:
                    if (convertView == null || convertView.getId() != ITEM_VIEW_TYPE_FOOTER) {
                        convertView = LayoutInflater.from(getContext()).inflate(
                                R.layout.asus_commonui_activity_chooser_view_list_item, parent, false);
                        convertView.setId(ITEM_VIEW_TYPE_FOOTER);
                        TextView titleView = (TextView) convertView.findViewById(R.id.asus_commonui_title);
                        titleView.setText(mContext.getString(
                                R.string.asus_commonui_activity_chooser_view_see_all));
                    }
                    return convertView;
                case ITEM_VIEW_TYPE_ACTIVITY:
                    if (convertView == null || convertView.getId() != R.id.asus_commonui_list_item) {
                        convertView = LayoutInflater.from(getContext()).inflate(
                                R.layout.asus_commonui_activity_chooser_view_list_item, parent, false);
                    }
                    PackageManager packageManager = mContext.getPackageManager();
                    // Set the icon
                    ImageView iconView = (ImageView) convertView.findViewById(R.id.asus_commonui_icon);
                    ResolveInfo activity = (ResolveInfo) getItem(position);
                    iconView.setImageDrawable(activity.loadIcon(packageManager));
                    // Set the title.
                    TextView titleView = (TextView) convertView.findViewById(R.id.asus_commonui_title);
                    titleView.setText(activity.loadLabel(packageManager));
                    // Highlight the default.
                    if (mShowDefaultActivity && position == 0 && mHighlightDefaultActivity) {
                        convertView.setActivated(true);
                    } else {
                        convertView.setActivated(false);
                    }
                    return convertView;
                default:
                    throw new IllegalArgumentException();
            }
        }

        public int measureContentWidth() {
            // The user may have specified some of the target not to be shown but we
            // want to measure all of them since after expansion they should fit.
            final int oldMaxActivityCount = mMaxActivityCount;
            mMaxActivityCount = MAX_ACTIVITY_COUNT_UNLIMITED;

            int contentWidth = 0;
            View itemView = null;

            final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            final int count = getCount();

            for (int i = 0; i < count; i++) {
                itemView = getView(i, itemView, null);
                itemView.measure(widthMeasureSpec, heightMeasureSpec);
                contentWidth = Math.max(contentWidth, itemView.getMeasuredWidth());
            }

            mMaxActivityCount = oldMaxActivityCount;

            return contentWidth;
        }

        public void setMaxActivityCount(int maxActivityCount) {
            if (mMaxActivityCount != maxActivityCount) {
                mMaxActivityCount = maxActivityCount;
                notifyDataSetChanged();
            }
        }

        public ResolveInfo getDefaultActivity() {
            return mDataModel.getDefaultActivity();
        }

        public void setShowFooterView(boolean showFooterView) {
            if (mShowFooterView != showFooterView) {
                mShowFooterView = showFooterView;
                notifyDataSetChanged();
            }
        }

        public int getActivityCount() {
            return mDataModel.getActivityCount();
        }

        public int getHistorySize() {
            return mDataModel.getHistorySize();
        }

        public ActivityChooserModel getDataModel() {
            return mDataModel;
        }

        public void setShowDefaultActivity(boolean showDefaultActivity,
                boolean highlightDefaultActivity) {
            if (mShowDefaultActivity != showDefaultActivity
                    || mHighlightDefaultActivity != highlightDefaultActivity) {
                mShowDefaultActivity = showDefaultActivity;
                mHighlightDefaultActivity = highlightDefaultActivity;
                notifyDataSetChanged();
            }
        }

        public boolean getShowDefaultActivity() {
            return mShowDefaultActivity;
        }
    }

    /**
     * Copy from android.widget.ListPopupWindow to forward touch events to
     * ListPopupWindow
     */
    private static abstract class ForwardingListener
    implements View.OnTouchListener, View.OnAttachStateChangeListener {
        /** Scaled touch slop, used for detecting movement outside bounds. */
        private final float mScaledTouchSlop;

        /** Timeout before disallowing intercept on the source's parent. */
        private final int mTapTimeout;

        /** Source view from which events are forwarded. */
        private final View mSrc;

        /** Runnable used to prevent conflicts with scrolling parents. */
        private Runnable mDisallowIntercept;

        /** Whether this listener is currently forwarding touch events. */
        private boolean mForwarding;

        /** The id of the first pointer down in the current event stream. */
        private int mActivePointerId;

        public ForwardingListener(View src) {
            mSrc = src;
            mScaledTouchSlop = ViewConfiguration.get(src.getContext()).getScaledTouchSlop();
            mTapTimeout = ViewConfiguration.getTapTimeout();

            src.addOnAttachStateChangeListener(this);
        }

        /**
         * Returns the popup to which this listener is forwarding events.
         * <p>
         * Override this to return the correct popup. If the popup is displayed
         * asynchronously, you may also need to override
         * {@link #onForwardingStopped} to prevent premature cancelation of
         * forwarding.
         *
         * @return the popup to which this listener is forwarding events
         */
        public abstract ListPopupWindow getPopup();

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final boolean wasForwarding = mForwarding;
            final boolean forwarding;
            if (wasForwarding) {
                forwarding = onTouchForwarded(event) || !onForwardingStopped();
            } else {
                forwarding = onTouchObserved(event) && onForwardingStarted();
            }

            mForwarding = forwarding;
            return forwarding || wasForwarding;
        }

        @Override
        public void onViewAttachedToWindow(View v) {
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            mForwarding = false;
            mActivePointerId = MotionEvent.INVALID_POINTER_ID;

            if (mDisallowIntercept != null) {
                mSrc.removeCallbacks(mDisallowIntercept);
            }
        }

        /**
         * Called when forwarding would like to start.
         * <p>
         * By default, this will show the popup returned by {@link #getPopup()}.
         * It may be overridden to perform another action, like clicking the
         * source view or preparing the popup before showing it.
         *
         * @return true to start forwarding, false otherwise
         */
        protected boolean onForwardingStarted() {
            final ListPopupWindow popup = getPopup();
            if (popup != null && !popup.isShowing()) {
                popup.show();
            }
            return true;
        }

        /**
         * Called when forwarding would like to stop.
         * <p>
         * By default, this will dismiss the popup returned by
         * {@link #getPopup()}. It may be overridden to perform some other
         * action.
         *
         * @return true to stop forwarding, false otherwise
         */
        protected boolean onForwardingStopped() {
            final ListPopupWindow popup = getPopup();
            if (popup != null && popup.isShowing()) {
                popup.dismiss();
            }
            return true;
        }

        /**
         * Observes motion events and determines when to start forwarding.
         *
         * @param srcEvent motion event in source view coordinates
         * @return true to start forwarding motion events, false otherwise
         */
        private boolean onTouchObserved(MotionEvent srcEvent) {
            final View src = mSrc;
            if (!src.isEnabled()) {
                return false;
            }

            final int actionMasked = srcEvent.getActionMasked();
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    mActivePointerId = srcEvent.getPointerId(0);
                    if (mDisallowIntercept == null) {
                        mDisallowIntercept = new DisallowIntercept();
                    }
                    src.postDelayed(mDisallowIntercept, mTapTimeout);
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int activePointerIndex = srcEvent.findPointerIndex(mActivePointerId);
                    if (activePointerIndex >= 0) {
                        final float x = srcEvent.getX(activePointerIndex);
                        final float y = srcEvent.getY(activePointerIndex);
                        if (!pointInView(src, x, y, mScaledTouchSlop)) {
                            // The pointer has moved outside of the view.
                            if (mDisallowIntercept != null) {
                                src.removeCallbacks(mDisallowIntercept);
                            }
                            src.getParent().requestDisallowInterceptTouchEvent(true);
                            return true;
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (mDisallowIntercept != null) {
                        src.removeCallbacks(mDisallowIntercept);
                    }
                    break;
            }

            return false;
        }

        /**
         * Handled forwarded motion events and determines when to stop
         * forwarding.
         *
         * @param srcEvent motion event in source view coordinates
         * @return true to continue forwarding motion events, false to cancel
         */
        private boolean onTouchForwarded(MotionEvent srcEvent) {
            final View src = mSrc;
            final ListPopupWindow popup = getPopup();
            if (popup == null || !popup.isShowing()) {
                return false;
            }

            final ListView dst = popup.getListView();
            if (dst == null || !dst.isShown()) {
                return false;
            }

            // Convert event to destination-local coordinates.
            final MotionEvent dstEvent = MotionEvent.obtainNoHistory(srcEvent);
            toGlobalMotionEvent(src, dstEvent);
            toLocalMotionEvent(dst, dstEvent);

            // Forward converted event to destination view, then recycle it.
            final boolean handled = onForwardedEvent(dst, dstEvent, mActivePointerId);
            dstEvent.recycle();
            return handled;
        }

        private boolean pointInView(View view, float localX, float localY, float slop) {
            return localX >= -slop && localY >= -slop && localX < ((view.getRight() - view.getLeft()) + slop) &&
                    localY < ((view.getBottom() - view.getTop()) + slop);
        }

        private boolean toGlobalMotionEvent(View view, MotionEvent ev) {
            try {
                Method methodToGlobalMotionEvent = View.class.getDeclaredMethod(
                        "toGlobalMotionEvent", MotionEvent.class);
                Boolean result = (Boolean) methodToGlobalMotionEvent.invoke(view, ev);
                return result.booleanValue();
            } catch (NoSuchMethodException e) {
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            }
            return false;
        }

        private boolean toLocalMotionEvent(View view, MotionEvent ev) {
            try {
                Method methodToLocalMotionEvent = View.class.getDeclaredMethod(
                        "toLocalMotionEvent", MotionEvent.class);
                Boolean result = (Boolean) methodToLocalMotionEvent.invoke(view, ev);
                return result.booleanValue();
            } catch (NoSuchMethodException e) {
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            }
            return false;
        }

        private boolean onForwardedEvent(Object dropDownListView,
                MotionEvent event, int activePointerId) {
            try {
                Class classDropDownListView = Class.forName(
                        "android.widget.ListPopupWindow$DropDownListView");
                Method methodOnForwardedEvent = classDropDownListView.getDeclaredMethod(
                        "onForwardedEvent", MotionEvent.class, int.class);
                Boolean result = (Boolean) methodOnForwardedEvent.invoke(
                        dropDownListView, event, activePointerId);
                return result.booleanValue();
            } catch (ClassNotFoundException e) {
            } catch (NoSuchMethodException e) {
            } catch (InvocationTargetException e) {
            } catch (IllegalAccessException e) {
            }
            return false;
        }

        private class DisallowIntercept implements Runnable {
            @Override
            public void run() {
                final ViewParent parent = mSrc.getParent();
                parent.requestDisallowInterceptTouchEvent(true);
            }
        }
    }
}
