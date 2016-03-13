package com.asus.wellness.sleep;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;

import com.asus.wellness.R;
import com.asus.wellness.StartActivity;
import com.asus.wellness.coach.LauncherTipsDialog;
import com.asus.wellness.utils.EBCommand;
import com.asus.wellness.utils.EBCommandUtils;
import com.asus.wellness.utils.Utility;

import de.greenrobot.event.EventBus;

public class SleepShortcutActivity extends Activity {
    public final static String KEY_REFERER= "key_referer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("smile","SleepShortcutActivity onCreate");
        final Intent intent = new Intent(this, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if(Utility.isSupportSleepSensor(this))
        {
            intent.putExtra(KEY_REFERER, true);
        }
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("smile","SleepShortcutActivity onResume");
    }
}
