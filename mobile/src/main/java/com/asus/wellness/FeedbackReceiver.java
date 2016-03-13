package com.asus.wellness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.asus.wellness.ui.setting.SettingActivity;
import com.uservoice.uservoicesdk.ConfigInterface;
import com.uservoice.uservoicesdk.UserVoice;

public class FeedbackReceiver extends BroadcastReceiver {
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(SettingActivity.FEEDBACK_ACTION)) {
            mContext = context;
            final int topicId = context.getResources().getInteger(R.integer.uservoice_topic_id); //By language in resource
            final int forumId = context.getResources().getInteger(R.integer.uservoice_forum_id); //By language in resource

            ConfigInterface configInterface = new ConfigInterface() {

                @Override
                public int getTopicID() {
                    return topicId;
                }

                @Override
                public int getForumID() {
                    return forumId;
                }

                @Override
                public int getPrimaryColor() {
                     return mContext.getResources().getColor(android.R.color.white);
                }
            };
            UserVoice.init(configInterface, context);

            UserVoice.launchUserVoice(context);   // Show the UserVoice portal page
        }
    }
}
