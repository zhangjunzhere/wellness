package com.asus.wellness.sleep;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.asus.wellness.R;
import com.asus.wellness.receiver.PackageReceiver;
import com.asus.wellness.utils.EBCommand;

import de.greenrobot.event.EventBus;

/**
 * Created by smile_gao on 2015/8/27.
 */
public abstract  class WearableSleepFragment extends Fragment {

    //private TextView mTxtRecording;

    private boolean isAmbient = false;
    @Override
    public void onResume(){
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(EBCommand ebCommand){
        if(EBCommand.COMMAND_AMBIENT_MODE.equals(ebCommand.command)){
            isAmbient = (boolean)ebCommand.param;
            if(isAmbient){
                onEnterAmbient();

            }else{
                onExitAmbient();
            }
        }
    }

    public boolean isAmbient(){return  isAmbient;}

    public abstract  void onEnterAmbient();
    public abstract void onExitAmbient();

}
