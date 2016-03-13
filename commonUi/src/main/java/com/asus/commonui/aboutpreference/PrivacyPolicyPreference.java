package com.asus.commonui.aboutpreference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class PrivacyPolicyPreference extends Preference {

    Connection mConnection;

    public PrivacyPolicyPreference(Context context) {
        this(context, null);
    }

    public PrivacyPolicyPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public PrivacyPolicyPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mConnection = new Connection(getContext(), Connection.TYPE_PRIVACY_POLICY);
    }

    @Override
    protected void onClick() {
        mConnection.connect();
        super.onClick();
    }

}