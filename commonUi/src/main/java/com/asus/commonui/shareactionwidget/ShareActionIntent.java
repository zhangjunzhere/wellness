package com.asus.commonui.shareactionwidget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.asus.commonui.R;

public class ShareActionIntent {

    private static final String USE_NATIVE_SHARE_TO = "AsusUiNativeShareTo";

    public static Intent createChooser(Context context, Intent intentIn, String title) {

        if (Log.isLoggable(USE_NATIVE_SHARE_TO, Log.DEBUG)) {
            return Intent.createChooser(intentIn, title);
        }
        else {
            Intent intentTemp = initiateChooserIntent(intentIn, title);
            intentTemp.setClass(context, ChooserActivity.class);
            return intentTemp;
        }
    }

    private static Intent initiateChooserIntent(Intent target, String title) {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_INTENT, target);
        if (title != null) {
            intent.putExtra(Intent.EXTRA_TITLE, title);
        }
        //        Migrate any clip data and flags from target.
        int permFlags = target.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (permFlags != 0) {
            ClipData targetClipData = target.getClipData();
            if (targetClipData == null && target.getData() != null) {
                ClipData.Item item = new ClipData.Item(target.getData());
                String[] mimeTypes;
                if (target.getType() != null) {
                    mimeTypes = new String[] { target.getType() };
                } else {
                    mimeTypes = new String[] { };
                }
                targetClipData = new ClipData(null, mimeTypes, item);
            }
            if (targetClipData != null) {
                intent.setClipData(targetClipData);
                intent.addFlags(permFlags);
            }
        }

        return intent;
    }

}
