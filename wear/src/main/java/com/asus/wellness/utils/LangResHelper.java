package com.asus.wellness.utils;

import android.content.Context;

import com.asus.wellness.R;

/**
 * Created by smile_gao on 2015/8/18.
 */
public class LangResHelper {
    private void getResLang(Context context)
    {
        context.getResources().getString(R.string.options_relax);
        context.getResources().getString(R.string.options_stress);
        context.getResources().getString(R.string.options_energy);
        context.getResources().getString(R.string.add_five_percent);
        context.getResources().getString(R.string.waiting_text);
        context.getResources().getString(R.string.coach_done);
        context.getResources().getStringArray(R.array.distance_unit);
        context.getResources().getString(R.string.knockknock_start_stress);

         context.getResources().getString(R.string.yo);

        context.getResources().getString(R.string.ftlowcast);
        context.getResources().getString(R.string.download_on_phone_message);
        context.getResources().getString(R.string.wait_first_heart_rate_message);
        context.getResources().getString(R.string.wait_enough_rr_message);
        context.getResources().getString(R.string.predistanceunit);
        context.getResources().getString(R.string.calories_string);
        context.getResources().getString(R.string.title_activity_profile);

        context.getResources().getString(R.string.count_down_text_secs);
        context.getResources().getString(R.string.step_unit);
        context.getResources().getString(R.string.distance_unit);
        context.getResources().getString(R.string.today_target);
        context.getResources().getString(R.string.title_workout_choose);
        context.getResources().getString(R.string.title_workout_goal);
        context.getResources().getString(R.string.no_value_message);

        context.getResources().getString(R.string.tap_to_try_again);

      //  context.getResources().getDrawable(R.mipmap.asus_icon_app_wellness);
        context.getResources().getDrawable(R.mipmap.asus_wellness_icon);


    }
}
