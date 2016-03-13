package com.asus.wellness.Profile.view;

import com.asus.wellness.utils.Utility;
import com.asus.wellness.Profile.controller.ProfileController;
import com.asus.wellness.Profile.model.ProfileModel;
import com.asus.wellness.R;
import com.asus.wellness.utils.AsusLog;

import java.util.ArrayList;

/**
 * Created by smile_gao on 2015/5/20.
 */
public class WeightLbsFragment extends WeightKgFragment {
    protected static  final  int int_MAX = 660;
    public  static  final  float RATE =0.45359237f;
    public  static  String Tag = "WeightLbsFragment";
    public WeightLbsFragment() {
        super();
        initData();
    }

    @Override
    public String getMyTag()
    {
        return  Tag;
    }
    protected void initData()
    {
        listItems_int = new ArrayList<String>();
      //  listItems_dec = new ArrayList<String>();
        fillList(listItems_int, int_MAX, 1);
      //  fillList(listItems_dec, dec_MAX, 0);
        AsusLog.i("smile","listcount: "+listItems_int.size());
    }
    @Override
    public String getUnitString()
    {
        return  getResources().getString(R.string.lbs);
    }
    @Override
    public void onWeightChange()
    {
      // float weight = Utility.LbsToKg(int_pos);//(int_pos + dec_pos/10.0f)*RATE;

        mProfileController.onWeightChange(int_pos);
    }



    @Override
    public void updateUi() {
        ProfileModel pm = mProfileController.getmProfileModel();
        float weightlbs = pm.getWeight();///RATE;
        if(weightlbs>0&& weightlbs <int_MAX) //+dec_MAX/10.0
        {
            int_pos = (int)Math.floor(weightlbs);
            int_pos = int_pos -1;
//            dec_pos = (int)(Math.round((weightlbs - int_pos)*10));
//            if(dec_pos>=dec_MAX)
//            {
//                dec_pos = dec_MAX-1;
//            }
            int_pos = int_pos>=int_MAX ? int_MAX-1 : int_pos;
            int_pos = int_pos<=0 ? 1: int_pos;

            listView_int.scrollToPosition(int_pos);
            //listView_dec.scrollToPosition(dec_pos);
        }

    }
}
