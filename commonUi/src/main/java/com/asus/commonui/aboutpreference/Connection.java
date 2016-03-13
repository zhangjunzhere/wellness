package com.asus.commonui.aboutpreference;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.asus.commonui.R;

public class Connection  {

    final static int TYPE_PRIVACY_POLICY = 0;
    final static int TYPE_TERMS_OF_USE_NOTICE = 1;
    final static int TYPE_TERMS_OF_USE_DIGITAL_CONTENT = 2;

    private int mInfoType;
    private Context mContext;
    private String mConnectURL;

    public Connection(Context context, int infoType) {
        mContext = context;
        mInfoType = infoType;
    }

    public void connect() {
        if (mInfoType == TYPE_PRIVACY_POLICY) {
            mConnectURL = mContext.getResources().getString(
                    R.string.asus_commonui_privacy_policy_url);
        } else if (mInfoType == TYPE_TERMS_OF_USE_NOTICE) {
            mConnectURL = mContext.getResources().getString(
                    R.string.asus_commonui_terms_of_use_notice_url);
        } else {
            mConnectURL = mContext.getResources().getString(
                    R.string.asus_commonui_terms_of_use_digital_content_url);
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mConnectURL));
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        try {
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, mContext.getString(R.string.asus_commonui_no_url_handler),
                    Toast.LENGTH_SHORT).show();
        }
    }

}