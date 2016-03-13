package com.asus.commonui.aboutpreference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class TermsOfUseNoticePreference extends Preference {

    Connection mConnection;

    public TermsOfUseNoticePreference(Context context) {
        this(context, null);
    }

    public TermsOfUseNoticePreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public TermsOfUseNoticePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mConnection = new Connection(getContext(), Connection.TYPE_TERMS_OF_USE_NOTICE);
    }

    @Override
    protected void onClick() {
        mConnection.connect();
        super.onClick();
    }

}