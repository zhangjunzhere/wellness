package com.asus.wellness.coach.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asus.wellness.R;
import com.asus.wellness.coach.CoachSetupActivity;
import com.asus.wellness.utils.EBCommand;

import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Created by jz on 2015/5/20.
 */
public class WearableListViewAdapter extends WearableListView.Adapter implements  WearableListView.ClickListener {
    public final String TAG = "WearableListViewAdapter";
    private final Context mContext;
    private final LayoutInflater mInflater;

    private Bundle mTextStyles;
    private List<String> mOptions;


    public  WearableListViewAdapter(Context context,List<String> options, Bundle textStyles ) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mOptions = options ;
        mTextStyles = textStyles;
    }

    public void setContent(List<String>  options, Bundle textStyles){
        mOptions = options;
        mTextStyles = textStyles;
        notifyDataSetChanged();
    }


    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        CoachListItemView itemView =  (CoachListItemView)mInflater.inflate(R.layout.sample_my_view, null);
        WearableListItem itemView =  (WearableListItem)mInflater.inflate(R.layout.pni_coach_list_item, null);

        return new WearableListView.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        TextView tv_main = (TextView) holder.itemView.findViewById(R.id.tv_main);
        tv_main.setText(mOptions.get(position));
        ((WearableListItem)holder.itemView).setArgs(mTextStyles);

        holder.itemView.setTag(position);
    }


    @Override
    public int getItemCount() {
//        return NUMBER_OF_TIMES;
        return mOptions.size();
    }
    /**
     *
     * @param viewHolder
     */
    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Log.d(TAG, "onClick position :" + viewHolder.getPosition());
        EBCommand ebCommand = new EBCommand(this.getClass().getName(), CoachSetupActivity.class.getName(), EBCommand.COMMAND_NEXT_PAGE,null);
        EventBus.getDefault().post(ebCommand);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }

    public static  class OnScrollListener implements WearableListView.OnScrollListener{
        @Override
        public void onScroll(int i) {

        }

        @Override
        public void onAbsoluteScrollChange(int i) {

        }

        @Override
        public void onScrollStateChanged(int i) {

        }

        @Override
        public void onCentralPositionChanged(int i) {
            Log.d("junzheng", "onCentralPositionChanged position:" + i);
        }
    }

}