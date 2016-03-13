package com.asus.wellness.Profile.view;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.asus.wellness.Profile.ProfileActivity;
import com.asus.wellness.Profile.ProfileEditActivity;
import com.asus.wellness.Profile.controller.SetupProfileController;
import com.asus.wellness.R;
import com.asus.wellness.dbhelper.Profile;
import com.asus.wellness.fragment.DrawerUserHeadImageView;
import com.asus.wellness.microprovider.ProfileTable;
import com.asus.wellness.notification.ProfileTableObserver;
import com.asus.wellness.utils.ProfileHelper;
import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2015/5/22.
 */
public class RealSetupProfileFragment extends ViewBase {
    DrawerUserHeadImageView setupbtn;
    public final static String Tag = "RealSetupProfileFragment";
    private String photoUrl = "";
    private ProfileTableObserver mStepGoalTableObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfileController  = new SetupProfileController(getActivity());
        mProfileController.init(false);
    }

    public String getMyTag() {
        return Tag;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupbtn = (DrawerUserHeadImageView)view.findViewById(R.id.setupbtn);
        TextView txtView = (TextView)view.findViewById(R.id.tvsetuptext);
//        int size = getResources().getDimensionPixelSize(R.dimen.tap_text_size);
//        Utility.fitFontSizeForView(txtView, size, Utility.getScreenWidth(getActivity()));

        updatePhoto(true);
        setupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("smile","onclick RealSetup");
                Intent intent   = new Intent(getActivity(), ProfileActivity.class);
                if(mProfileController.getmProfileModel().getIsProfileSet())
                {
                    intent   = new Intent(getActivity(), ProfileEditActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity(intent);
               // getActivity().finish();
            }
        });
        mStepGoalTableObserver = new ProfileTableObserver(mHandler);
        getActivity().getContentResolver().registerContentObserver(ProfileTable.TABLE_URI, true, mStepGoalTableObserver);
        super.onViewCreated(view, savedInstanceState);
    }
    void updatePhoto(boolean onviewcreated)
    {
        if(!isVisible())
        {
            Log.i(Tag,"updatePhoto not attach to activity");
            return;
        }
        Profile p = ProfileHelper.getStandardProfile();
        byte[] data = p.getPhotodata();

        if(data!=null )
        {
            if((onviewcreated ||!photoUrl.equals(p.getPhoto_path()))) {
                photoUrl = p.getPhoto_path();
                setupbtn.setUseSpeImage(true);
                setupbtn.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));

            }
        }
        else
        {
            setupbtn.setUseSpeImage(false);
            setupbtn.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.pni_asus_wellness_ic_people));

        }
    }

    public int getLayout() {
        return R.layout.pni_profile_fragment_setup_profile;
    }

    @Override
    public void updateUi() {

    }
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.i("smile","real setup onDestroy");
       // Unregister ContentObserver
        if(mStepGoalTableObserver!=null)
            getActivity().getContentResolver().unregisterContentObserver(mStepGoalTableObserver);
        mHandler.removeCallbacksAndMessages(null);
        mStepGoalTableObserver=null;
        mHandler = null;
  //      setupbtn = null;
    }
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.i("smile", "handleMessage "+Tag);
            if(msg.what == ProfileTableObserver.PROFILE_CHANGE) {
                updatePhoto(false);
            }
        }
    };

}
