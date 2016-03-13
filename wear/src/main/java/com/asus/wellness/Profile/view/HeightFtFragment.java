package com.asus.wellness.Profile.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by smile_gao on 2015/5/19.
 */
public class HeightFtFragment extends HeightFragment {
    WearableListView listView_ft;
    WearableListView listView_in;
    MyAdapter myAdapter_ft;
    MyAdapter myAdapter_in;
    List<String> listItems_ft;
    List<String> listItems_in;
    private static  final  int ft_MAX = 10;
    private static  final  int in_MAX = 12;
    public static  float RATE = 2.54f;
    private int ft_pos = 0;
    private int in_pos = 0;
    public  static   String Tag = "HeightFtFragment";
    public HeightFtFragment() {
        super();
        initData();
    }

    @Override
    public String getMyTag()
    {
        return  Tag;
    }

    @Override
    public String getTitle()
    {
        return getResources().getString(R.string.height)+"("+getResources().getString(R.string.ft)+")";
    }
    /**
     * init ft ,in listview ,fill data
     */
    void initData()
    {
        listItems_ft = new ArrayList<String>();
        listItems_in = new ArrayList<String>();
        fillList(listItems_ft, ft_MAX, 1);
        fillList(listItems_in, in_MAX, 0);
    }

    /**
     * @param list , data list
     * @param max    max data value
     * @param start  from 0 or 1
     */
    void fillList(List<String> list,int max,int start)
    {
        for(int i=start;i<max;i++)
        {
            list.add(String.valueOf(i));
        }
    }
    @Override
    public void onItemClick(int listviewindex,String str,int position)
    {

        Log.i("smile","onItemClick "+str);
        int clicknum = Integer.parseInt(str);
        ProfileModel pm = mProfileController.getmProfileModel();
        float height = Utility.InchToFt(pm.getHeight());
        int ft = getFt(height);
        int inch = getInch(height,ft);
        if(listviewindex == 0)
        {
            if(clicknum == ft)
            {
                mProfileController.goNextPage();
            }
            else
            {
                listView_ft.smoothScrollToPosition(getFtIndex(clicknum));
            }
        }
        else
        {
            if(clicknum == inch)
            {
                mProfileController.goNextPage();
            }
            else
            {
                listView_in.smoothScrollToPosition(getInchIndex(clicknum));
            }
        }

    }
    @Override
    public int getListItemGravity()
    {
        return Gravity.CENTER;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=super.onCreateView(inflater, container, savedInstanceState);
        listView_ft = (WearableListView) v.findViewById(R.id.list_ft);
        myAdapter_ft = new MyAdapter(listItems_ft, this);
        listView_ft.setAdapter(myAdapter_ft);
        listView_ft.addOnScrollListener(new MyScrollListener(true));


        listView_in = (WearableListView) v.findViewById(R.id.list_in);
        myAdapter_in = new MyAdapter(listItems_in, this,1);
        listView_in.setAdapter(myAdapter_in);
        listView_in.addOnScrollListener(new MyScrollListener(false));

        return  v;
    }
    public boolean needMeasureListViewItem()
    {
        return  false;
    }
    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_height_ft;
    }
    @Override
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();
        float height = Utility.InchToFt(pm.getHeight());
       // int inchs = getFt(height);
        ft_pos = getFt(height);//%12;
        in_pos = getInch(height, ft_pos);
        AsusLog.i("smile",height+" "+ ft_pos+" ft : in "+in_pos);
        listView_ft.scrollToPosition(getFtIndex(ft_pos));
        listView_in.scrollToPosition(getInchIndex(in_pos));
    }
    private int getInchIndex(int inpos)
    {
        if(inpos>=0 && inpos <in_MAX)
        {
            return  inpos;
        }
        return  0;
    }
    private int getFtIndex(int ft)
    {
        if(ft>=0 && ft <ft_MAX)
        {
            int index = ft==0? 0 : ft-1;
            return  index;
        }
        return 0;
    }

  private int getFt(float height)
  {
      int inchs = (int)height;
      return inchs;
  }
    private int getInch(float height,int ft)
    {
      return   (int)(Math.round((height - ft)*12));
    }
    /**
     * list view scroll view
     */
    class  MyScrollListener implements   WearableListView.OnScrollListener{
        boolean isft=false;
        public MyScrollListener(boolean ft)
        {
            isft = ft;
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
            AsusLog.i("smile", "ft onCentralPositionChanged  " + i);
            if(isft)
            {
                ft_pos = i+1;
            }
            else
            {
                in_pos = i;
            }
            onHeightChange();

        }
    }

    /**
     * calulator height ,then notify controller height change
     */
    public void onHeightChange()
    {
        if(mProfileController.getmProfileModel().getHeightUnit()!= ProfileTable.HEIGHT_UNIT_FT)
        {
            return;
        }
       // float height =  (ft_pos*12+in_pos)*RATE;
        float height = Utility.ftToInch(ft_pos)+in_pos; //Utility.ftToCm(ft_pos+Utility.InchToFt(in_pos));
        mProfileController.onHeightChange((int)height);
    }

}
