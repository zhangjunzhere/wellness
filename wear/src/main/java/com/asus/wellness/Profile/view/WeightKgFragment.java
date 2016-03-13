package com.asus.wellness.Profile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/5/20.
 */
public class WeightKgFragment extends ViewBase {

    protected WearableListView listView_int;
    //   protected WearableListView listView_dec;
    protected MyAdapter myAdapter_int;
    List<String> listItems_int;
    List<String> listItems_dec;
    protected static  final  int int_MAX = 300;
//    protected static  final  int dec_MAX = 10;
    protected int int_pos = 0;
 //   protected int dec_pos = 0;
    public  static   String Tag = "WeightKgFragment";
  //  protected TextView mTxtWeightUnit;
    protected int mCurrentPosition = 0;
    public WeightKgFragment() {
        super();
        initData();
    }

    @Override
    public void onItemClick(int listviewindex, String str, int position)
    {
        if( position != mCurrentPosition)
        {
            listView_int.smoothScrollToPosition(position);
        }
        else
        {
            if(mCurrentPosition == position)
            {
                mProfileController.goNextPage();
            }
        }

    }
    @Override
    public String getMyTag()
    {
        return  Tag;
    }
   protected void initData()
    {
        listItems_int = new ArrayList<String>();
     //   listItems_dec = new ArrayList<String>();
        fillList(listItems_int, int_MAX, 1);
     //   fillList(listItems_dec, dec_MAX, 0);
    }
    protected void fillList(List<String> list,int max,int start)
    {
        for(int i=start;i<max;i++)
        {
            list.add(String.valueOf(i));
        }
    }
    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_weight_lbs;
    }
    public String getUnitString()
    {
        return  getResources().getString(R.string.kg);
    }
    @Override
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();
        if(pm.getWeight()>0&&pm.getWeight()<int_MAX) //+dec_MAX/10
        {
            int_pos = (int)Math.floor(pm.getWeight());
            int_pos = int_pos -1;
//            dec_pos = (int)(Math.round((pm.getWeight() - int_pos)*10));
//            if(dec_pos>=dec_MAX)
//            {
//                dec_pos = dec_MAX-1;
//            }
            int_pos = int_pos>=int_MAX ? int_MAX-1 : int_pos;
            int_pos = int_pos<=0 ? 0: int_pos;

            listView_int.scrollToPosition(int_pos);
           // listView_dec.scrollToPosition(dec_pos);
        }

    }
    @Override
    public int getLess99Width()
    {
        return 70;
    }
//    @Override
//    public View getListItemView()
//    {
//        return  new WeightListItemView(getActivity(),getListItemSmallSize(),getListItemBigSize());
//    }
@Override
public String getTitle()
{
    return getResources().getString(R.string.weight)+"("+getUnitString()+")";
}
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=super.onCreateView(inflater, container, savedInstanceState);

        listView_int = (WearableListView) v.findViewById(R.id.list_int);
        myAdapter_int = new MyAdapter(listItems_int,this);
        listView_int.setAdapter(myAdapter_int);
        listView_int.addOnScrollListener(new MyScrollListener(true));


        //listView_dec = (WearableListView) v.findViewById(R.id.list_dec);
      //  myAdapter_dec = new MyAdapter( listItems_dec,this,1);
     //   listView_dec.setAdapter(myAdapter_dec);
    //    listView_dec.addOnScrollListener(new MyScrollListener(false));

        return  v;
    }

    class  MyScrollListener implements   WearableListView.OnScrollListener{
        boolean iskg =false;
        public MyScrollListener(boolean ft)
        {
            iskg = ft;
        }
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
            AsusLog.i("smile", "log  " + i);
           // if(iskg)

                int_pos = i+1;
            mCurrentPosition = i;
//            else
//            {
//                dec_pos = i;
//            }
//           int height= listView_int.getHeight();
//            int width= listView_int.getWidth();
//            AsusLog.i("smile",String.valueOf(height)+"  "+width);
            onWeightChange();

        }
    }

    public void onWeightChange()
    {
       float weight = int_pos ;//+ dec_pos/10.0f;
        mProfileController.onWeightChange(int_pos);
    }
}
