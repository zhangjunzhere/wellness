package com.asus.commonui.aboutpreference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

public class TermsOfUseDigitalContentPreference extends Preference{

    Connection mConnection;

    public TermsOfUseDigitalContentPreference(Context context) {
        this(context, null);
    }

    public TermsOfUseDigitalContentPreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }

    public TermsOfUseDigitalContentPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mConnection = new Connection(getContext(), Connection.TYPE_TERMS_OF_USE_DIGITAL_CONTENT);
    }

    @Override
    protected void onClick() {
        mConnection.connect();
        super.onClick();
    }

}