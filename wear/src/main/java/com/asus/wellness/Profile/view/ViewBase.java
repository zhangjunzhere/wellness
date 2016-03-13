package com.asus.wellness.Profile.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.asus.wellness.Profile.IController;
import com.asus.wellness.Profile.ItemClickListener;
import com.asus.wellness.Profile.ProfileActivity;
import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;
import com.asus.wellness.utils.Utility;
import com.asus.wellness.view.MyTextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by smile_gao on 2015/5/14.
 */
public abstract class ViewBase extends Fragment implements ItemClickListener {
    protected ProfileController mProfileController;
    protected View.OnClickListener onClickListener;
    protected String Tag = "";
    protected TextView mTitleTv;
    public   static Class[] profileClasses = new Class[]{
            GenderFragment.class,
            //AgeFragment.class ,
            HeightUnitFragment.class,
            HeightFragment.class,
            WeightUnitFragment.class,
            WeightFragment.class,
            DistanceUnitFragment.class,
            ActivityGoalFragment.class,
            ProfileReviewFragment.class
    };
    public String getMyTag()
    {
        return  Tag;
    }

    public String getTitle()
    {
        return null;
    }
    public void setTitle()
    {
//        String title = getTitle();
//        if( mTitleTv ==null)
//        {
//            return;
//        }
//        int width = Utility.getScreenWidth(getActivity());
//        mTitleTv.setText(title);  //getResources().getString(R.string.heightunit)
  //      mTitleTv.setMovementMethod(new ScrollingMovementMethod());
    //    Utility.fitFontSizeForView(mTitleTv, 0, width);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitleTv= (TextView) view.findViewById(R.id.title);
        setTitle();
        updateUi();
    }
    public void onItemClick(int listviewindex,String str,int position)
    {

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mProfileController==null)
        {
            Log.i("smile","onCreateView construct");
            mProfileController =((IController)getActivity()).getController();
        }
        return inflater.inflate(getLayout(),container,false);
    }

    public  abstract int getLayout();
    public  abstract void updateUi();
    public  int getListItemSmallSize() {return  R.dimen.age_small_list_item_size;}
    public  int getListItemBigSize()  {return  R.dimen.age_big_list_item_size;}

    public boolean needMeasureListViewItem()
    {
        return  true;
    }

    public int getListItemGravity()
    {
        return Gravity.CENTER_VERTICAL| Gravity.LEFT;
    }
    public View getListItemView()
    {
        return  new ListItemView(getActivity(),getListItemSmallSize(),getListItemBigSize(),needMeasureListViewItem(),getListItemGravity());
    }

    public View getSecondListItemView()
    {
        return  new ListItemView(getActivity(),getListItemSmallSize(),getListItemBigSize(),needMeasureListViewItem(),getListItemGravity());
    }
    public int getLess10Width()
    {
        return  35;
    }
    public int getLess99Width()
    {
        return 100;
    }
    public int getMore100Width()
    {
        return 130;
    }

    public int getSelectColor(){
        return getResources().getColor(R.color.activitygoalselectcolor);
    }

    public int getBtnNormalSize()
    {
       return getResources().getDimensionPixelSize(R.dimen.btn_normal_font_size);
    }
    public  int getBtnSelectSize()
    {
        return getResources().getDimensionPixelSize(R.dimen.btn_select_font_size);
    }
    public View getLineView()
    {
        if(getView()==null)
        {
            return  null;
        }
        return  getView().findViewById(R.id.profile_title_line);
    }
    /**
     * postion to create Fragment
     * @param index, current postion in page adapter
     * @param controller  ProfileController
     * @return  viewbase
     */
    public  static ViewBase getView(int index,ProfileController controller)
    {
        if(index>=profileClasses.length||index<0)
        {
            index = 0;
        }
        ViewBase vb = null;
        Class c = profileClasses[index];
        AsusLog.i("smile","index: "+index);
        try {
            Constructor constructor = c.getConstructor(); //ProfileController.class
            vb =(ViewBase)constructor.newInstance();
        }catch (NoSuchMethodException e)
        {
            AsusLog.i("viewbase1", e.getMessage());
        }catch (InvocationTargetException e)
        {
            AsusLog.i("viewbase2", e.getMessage());
        }
        catch (IllegalAccessException e)
        {
            AsusLog.i("viewbase3", e.getMessage());
        }
        catch (java.lang.InstantiationException e)
        {
            AsusLog.i("viewbase4", e.getMessage());
        }
        if(vb == null)
        {
            vb = new GenderFragment();
        }
       // Fragment f = new Fragment();

        return  vb;
    }

}
