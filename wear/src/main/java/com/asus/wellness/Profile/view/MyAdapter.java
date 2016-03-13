package com.asus.wellness.Profile.view;

import android.content.Context;
import android.os.SystemClock;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asus.wellness.R;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/5/20.
 */
public class MyAdapter extends WearableListView.Adapter {
    private LayoutInflater mInflater;
    private Context mContext;
    private List<String> listItems;
    private ViewBase mViewBase;
    private int mIndex =0;
    private int mMaxMeasureWidth =0;
    public MyAdapter(List<String> list, ViewBase v) {
        super();
        init(list,v,0);
    }

    public MyAdapter(List<String> list, ViewBase v,int index) {
        super();
        init(list, v, index);
    }
    void init(List<String> list, ViewBase v, int index)
    {
        this.mInflater = LayoutInflater.from(v.getActivity());
        mContext = v.getActivity();
        listItems = list;
        mViewBase = v;
        mIndex = index;
      //  findMinWidth();
    }
    void findMinWidth()
    {
        TextView tv = new TextView(mContext);
        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int fongtsize = mContext.getResources().getDimensionPixelSize(R.dimen.age_big_list_item_size);
        for (String str : listItems)
        {
            tv.setText(str);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, fongtsize);
            tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            int width = tv.getMeasuredWidth();
            if(width > mMaxMeasureWidth)
            {
                mMaxMeasureWidth = width;
            }
        }
        Log.i("smile","mMaxMeasureWidth: "+mMaxMeasureWidth);


    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
       // return new WearableListView.ViewHolder(mIndex ==0? mViewBase.getListItemView() : mViewBase.getSecondListItemView());
        return new WearableListView.ViewHolder(mViewBase.getListItemView());
        //new ListItemView(mContext,mViewBase.getListItemSmallSize(),mViewBase.getListItemBigSize())
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, final int position) {

        TextView tv  = (TextView)holder.itemView.findViewById(R.id.name);
        tv.setText(listItems.get(position).toString());
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("smile","onclick item");
                mViewBase.onItemClick(mIndex,((TextView)v).getText().toString(),position);
                //mViewBase.mProfileController.goNextPage();
            }
        });
//        if(position< 10)
//        {
//            tv.setMinWidth(mViewBase.getLess10Width());
//        }
//        else if(position<99)
//        {
//            tv.setMinWidth(mViewBase.getLess99Width());
//        }
//        else
        tv.setGravity(mViewBase.getListItemGravity());
//        if(mMaxMeasureWidth> mViewBase.getMore100Width())
//        {
//            tv.setMinWidth(mMaxMeasureWidth);
//        }
//        else
//        {
//            tv.setMinWidth(mViewBase.getMore100Width());
//        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }
}
