package com.asus.commonui.shareactionwidget;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

/**
 * An activity that follows the visual style of an AlertDialog.
 * 
 * @see #mBuilder
 * @see #mAlertDialog
 * @see #setupAlert()
 */
public abstract class AlertActivity extends Activity {

    protected AlertDialog.Builder mBuilder;
    protected AlertDialog mAlertDialog;

    private OnCancelListener mOnCancelListener = new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
            finish();
        }
    };

    private OnDismissListener mOnDismissListener = new OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            if (!isFinishing()) {
                finish();
            }
        }
    };

    public void dismiss() {
        if (mAlertDialog != null) {
            if (mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
            }
            mAlertDialog = null;
        }

        // This is called after the click, since we finish when handling the
        // click, don't do that again here.
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBuilder = new AlertDialog.Builder(this);
        mBuilder.setOnCancelListener(mOnCancelListener);
        mBuilder.setOnDismissListener(mOnDismissListener);
    }

    @Override
    protected void onDestroy() {
        if (mAlertDialog != null) {
            if (mAlertDialog.isShowing()) {
                mAlertDialog.dismiss();
            }
            mAlertDialog = null;
        }
        super.onDestroy();
    }

    protected void setupAlert() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (mAlertDialog == null || !mAlertDialog.isShowing()) {
            mAlertDialog = mBuilder.create();
            mAlertDialog.show();
        }
    }
}
