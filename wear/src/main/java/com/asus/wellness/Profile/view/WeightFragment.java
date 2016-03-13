package com.asus.wellness.Profile.view;

import android.app.Fragment;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.asus.wellness.Profile.EventCmd;
import com.asus.wellness.Profile.ProfileEvent;
import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/5/20.
 */
public class WeightFragment extends ViewBase {

//    protected MyAdapter myAdapter_dec;
 //   HashMap<Integer,ViewBase>  fragmentmap = new HashMap<>();
    int weightItemHeight = 258;
    int weightItemWidth1 = 50;
    int WeightItemWidth2 = 86;
    int WeightItemWidth3 = 126;
    WeightKgFragment mKgFragment;
    WeightLbsFragment mLbsFragment;
    protected int mCurrentPosition = 0;
    ViewBase viewKg;
    ViewBase viewLbs;
    public WeightFragment() {
        super();
        viewLbs = new WeightLbsFragment();
        viewKg = new WeightKgFragment();
//		 weightItemHeight = getResources().getDimensionPixelSize(R.dimen.weight_item_height);
//        weightItemWidth1 = getResources().getDimensionPixelSize(R.dimen.weight_item_width_1);
//        WeightItemWidth2 = getResources().getDimensionPixelSize(R.dimen.weight_item_width_2);
//        WeightItemWidth3 = getResources().getDimensionPixelSize(R.dimen.weight_item_width_3);

    }

    @Override
    public void onStart() {
        super.onStart();
        weightItemHeight = getResources().getDimensionPixelSize(R.dimen.weight_item_height);
        weightItemWidth1 = getResources().getDimensionPixelSize(R.dimen.weight_item_width_1);
        WeightItemWidth2 = getResources().getDimensionPixelSize(R.dimen.weight_item_width_2);
        WeightItemWidth3 = getResources().getDimensionPixelSize(R.dimen.weight_item_width_3);
    }


    /**
     * receive msg update ui
     * @param msg UPDATE_WEIGHT_VIEW
     */
    public void onEvent(ProfileEvent msg)
    {

        AsusLog.i("smile", "weight onevent " + msg);
        if(msg.getEventCmd().equals(EventCmd.UPDATE_WEIGHT_VIEW))
        {
            updateUi();
        }
//        else if(msg.getEventCmd().equals(EventCmd.UPDATE_PROFILE_WEIGHT_ITEM_VIEW))
//        {
//            if(msg.getmPrams()!=null&& listView_int!=null)
//            {
//                LinearLayout.LayoutParams oldlp = ( LinearLayout.LayoutParams)listView_int.getLayoutParams();
//                LinearLayout.LayoutParams lp = null;
//                if(msg.getmPrams().equals("3"))  // item lenght XXX, XX ,X
//                {
//
//                    lp = new LinearLayout.LayoutParams(WeightItemWidth3,weightItemHeight);
//                }else  if(msg.getmPrams().equals("2"))
//                {
//                     lp = new LinearLayout.LayoutParams(WeightItemWidth2,weightItemHeight);
//                }
//                else
//                {
//                    lp = new LinearLayout.LayoutParams(weightItemWidth1,weightItemHeight);
//                }
//                lp.setMargins(oldlp.leftMargin,oldlp.topMargin,oldlp.rightMargin,oldlp.bottomMargin);
//                listView_int.setLayoutParams(lp);
//                listView_int.getParent().requestLayout();
//            }
//           // listView_int.requestLayout();
//        }
    }

    /**
     * get item text small size
     * @return int small size
     */

    /**
     * register event bus
     */
    @Override
    public void onResume() {
        super.onResume();
        AsusLog.i("smile", "WeightView onresume ");
        EventBus.getDefault().register(this);
    }

    /**
     * unregister event bus
     */
    @Override
    public void onPause() {
        super.onPause();
        AsusLog.i("smile", "WeightView onpause ");
        EventBus.getDefault().unregister(this);
    }
//    @Override
//    public View getListItemView()
//    {
//        return  new WeightListItemView(getActivity(),getListItemSmallSize(),getListItemBigSize());
//    }
    @Override
    public int getLayout() {
        return R.layout.pni_profile_fragment_weight;
    }

    @Override
    public void updateUi() {
        AsusLog.i("smile","WeightView updateUI");
        ViewBase vb = null;
        Boolean needreplace =true;
        if(mProfileController.getmProfileModel().getWeightUnit()==0)
        {

//            vb = (ViewBase)getChildFragmentManager().findFragmentByTag(WeightKgFragment.Tag);
//            if(vb == null)
//            {
//                needreplace = true;
//                vb = new WeightKgFragment(mProfileController);
//            }
            vb = viewKg ;
        }
        else
        {
//            vb = (ViewBase)getChildFragmentManager().findFragmentByTag(WeightLbsFragment.Tag);
//            if(vb == null)
//            {
//                needreplace = true;
//                vb =  new WeightLbsFragment(mProfileController);
//
//            }
            vb = viewLbs;
        }
        String tag = vb.getMyTag();
        if(needreplace)
        {

            Log.i("smile", "Fragment null " + tag);
            getChildFragmentManager().beginTransaction().replace(R.id.weight_container, vb, tag).commit();
            getChildFragmentManager().executePendingTransactions();
        }
        else
        {
            Log.i("smile","Fragment show");
            getChildFragmentManager().beginTransaction().show(vb).commit();
        }

      //  mProfileController.getFragmentManager().beginTransaction().replace(R.id.weight_container,vb,vb.getMyTag()).commit();
    }
}
