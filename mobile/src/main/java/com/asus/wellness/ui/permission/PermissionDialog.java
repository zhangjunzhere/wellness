package com.asus.wellness.ui.permission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.asus.wellness.utils.Utility;

/**
 * Created by smile_gao on 2016/1/8.
 */
public class PermissionDialog extends DialogFragment {
    public interface DialogFragmentClickImpl {
        void doPositiveClick();
        void doNegativeClick();
    }
    public  static String TAG = "PermissionDialog";
    public  static String PACKAGENAME_KEY = "packagename_key";
    public  static String TITLE_KEY = "title_key";
    public static String CONTENT_KEY = "content_key";
    private String mTitle;
    private String mContent;
    private Context mContext;
    private String packageName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bd =  getArguments();
        if(bd !=null)
        {
            mTitle = bd.getString(TITLE_KEY);
            mContent = bd.getString(CONTENT_KEY);
            packageName = bd.getString(PACKAGENAME_KEY);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setMessage(mContent)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
//                                DialogFragmentClickImpl impl = (DialogFragmentClickImpl) getActivity();
//                                impl.doPositiveClick();
                                Utility.openSettingAppInfo(packageName,getActivity());
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
//                                DialogFragmentClickImpl impl = (DialogFragmentClickImpl) getActivity();
//                                impl.doNegativeClick();
                                Log.i("smile","permission dialog dismiss");
                                dismiss();

                            }
                        }
                )
                .create();

    }
}
