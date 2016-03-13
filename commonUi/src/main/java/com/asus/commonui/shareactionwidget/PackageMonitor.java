package com.asus.commonui.shareactionwidget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;

import java.util.HashSet;

/**
 * Helper class for monitoring the state of packages: adding, removing,
 * updating, and disappearing and reappearing on the SD card.
 */
public abstract class PackageMonitor extends android.content.BroadcastReceiver {
    // Noticed. Below declarations should stay in sync with Intent's definition
    public static final String ACTION_QUERY_PACKAGE_RESTART = "android.intent.action.QUERY_PACKAGE_RESTART";
    public static final String EXTRA_PACKAGES = "android.intent.extra.PACKAGES";
    public static final String ACTION_USER_STOPPED = "android.intent.action.USER_STOPPED";
    public static final String EXTRA_USER_HANDLE = "android.intent.extra.user_handle";
    public static final String EXTRA_REMOVED_FOR_ALL_USERS = "android.intent.extra.REMOVED_FOR_ALL_USERS";

    static final IntentFilter sPackageFilt = new IntentFilter();
    static final IntentFilter sNonDataFilt = new IntentFilter();
    static final IntentFilter sExternalFilt = new IntentFilter();

    static {
        sPackageFilt.addAction(Intent.ACTION_PACKAGE_ADDED);
        sPackageFilt.addAction(Intent.ACTION_PACKAGE_REMOVED);
        sPackageFilt.addAction(Intent.ACTION_PACKAGE_CHANGED);
        sPackageFilt.addAction(ACTION_QUERY_PACKAGE_RESTART);
        sPackageFilt.addAction(Intent.ACTION_PACKAGE_RESTARTED);
        sPackageFilt.addAction(Intent.ACTION_UID_REMOVED);
        sPackageFilt.addDataScheme("package");
        sNonDataFilt.addAction(Intent.ACTION_UID_REMOVED);
        sNonDataFilt.addAction(ACTION_USER_STOPPED);
        sExternalFilt.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sExternalFilt.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
    }

    final HashSet<String> mUpdatingPackages = new HashSet<String>();

    Context mRegisteredContext;
    Handler mRegisteredHandler;
    String[] mDisappearingPackages;
    String[] mAppearingPackages;
    String[] mModifiedPackages;
    int mChangeType;
    // multi-user is not supported in commonui
    //    int mChangeUserId = UserHandle.USER_NULL;
    boolean mSomePackagesChanged;

    String[] mTempArray = new String[1];

    public void register(Context context, Looper thread, boolean externalStorage) {
        register(context, thread, null, externalStorage);
    }

    public void register(Context context, Looper thread, UserHandle user,
            boolean externalStorage) {
        if (mRegisteredContext != null) {
            throw new IllegalStateException("Already registered");
        }
        mRegisteredContext = context;
        if (thread == null) {
            mRegisteredHandler = BackgroundThread.getHandler();
        } else {
            mRegisteredHandler = new Handler(thread);
        }
        // multi-user is not supported in commonui
        //        if (user != null) {
        //            context.registerReceiverAsUser(this, user, sPackageFilt, null, mRegisteredHandler);
        //            context.registerReceiverAsUser(this, user, sNonDataFilt, null, mRegisteredHandler);
        //            if (externalStorage) {
        //                context.registerReceiverAsUser(this, user, sExternalFilt, null,
        //                        mRegisteredHandler);
        //            }
        //        } else {
        context.registerReceiver(this, sPackageFilt, null, mRegisteredHandler);
        context.registerReceiver(this, sNonDataFilt, null, mRegisteredHandler);
        if (externalStorage) {
            context.registerReceiver(this, sExternalFilt, null, mRegisteredHandler);
        }
        //        }
    }

    public Handler getRegisteredHandler() {
        return mRegisteredHandler;
    }

    public void unregister() {
        if (mRegisteredContext == null) {
            throw new IllegalStateException("Not registered");
        }
        mRegisteredContext.unregisterReceiver(this);
        mRegisteredContext = null;
    }

    //not yet implemented
    boolean isPackageUpdating(String packageName) {
        synchronized (mUpdatingPackages) {
            return mUpdatingPackages.contains(packageName);
        }
    }

    public void onBeginPackageChanges() {
    }

    /**
     * Called when a package is really added (and not replaced).
     */
    public void onPackageAdded(String packageName, int uid) {
    }

    /**
     * Called when a package is really removed (and not replaced).
     */
    public void onPackageRemoved(String packageName, int uid) {
    }

    /**
     * Called when a package is really removed (and not replaced) for
     * all users on the device.
     */
    public void onPackageRemovedAllUsers(String packageName, int uid) {
    }

    public void onPackageUpdateStarted(String packageName, int uid) {
    }

    public void onPackageUpdateFinished(String packageName, int uid) {
    }

    /**
     * Direct reflection of {@link Intent#ACTION_PACKAGE_CHANGED
     * Intent.ACTION_PACKAGE_CHANGED} being received, informing you of
     * changes to the enabled/disabled state of components in a package
     * and/or of the overall package.
     *
     * @param packageName The name of the package that is changing.
     * @param uid The user ID the package runs under.
     * @param components Any components in the package that are changing.  If
     * the overall package is changing, this will contain an entry of the
     * package name itself.
     * @return Return true to indicate you care about this change, which will
     * result in {@link #onSomePackagesChanged()} being called later.  If you
     * return false, no further callbacks will happen about this change.  The
     * default implementation returns true if this is a change to the entire
     * package.
     */
    public boolean onPackageChanged(String packageName, int uid, String[] components) {
        if (components != null) {
            for (String name : components) {
                if (packageName.equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
        return false;
    }

    public void onHandleUserStop(Intent intent, int userHandle) {
    }

    public void onUidRemoved(int uid) {
    }

    public void onPackagesAvailable(String[] packages) {
    }

    public void onPackagesUnavailable(String[] packages) {
    }

    public static final int PACKAGE_UNCHANGED = 0;
    public static final int PACKAGE_UPDATING = 1;
    public static final int PACKAGE_TEMPORARY_CHANGE = 2;
    public static final int PACKAGE_PERMANENT_CHANGE = 3;

    /**
     * Called when a package disappears for any reason.
     */
    public void onPackageDisappeared(String packageName, int reason) {
    }

    /**
     * Called when a package appears for any reason.
     */
    public void onPackageAppeared(String packageName, int reason) {
    }

    /**
     * Called when an existing package is updated or its disabled state changes.
     */
    public void onPackageModified(String packageName) {
    }

    public boolean didSomePackagesChange() {
        return mSomePackagesChanged;
    }

    public int isPackageAppearing(String packageName) {
        if (mAppearingPackages != null) {
            for (int i=mAppearingPackages.length-1; i>=0; i--) {
                if (packageName.equals(mAppearingPackages[i])) {
                    return mChangeType;
                }
            }
        }
        return PACKAGE_UNCHANGED;
    }

    public boolean anyPackagesAppearing() {
        return mAppearingPackages != null;
    }

    public int isPackageDisappearing(String packageName) {
        if (mDisappearingPackages != null) {
            for (int i=mDisappearingPackages.length-1; i>=0; i--) {
                if (packageName.equals(mDisappearingPackages[i])) {
                    return mChangeType;
                }
            }
        }
        return PACKAGE_UNCHANGED;
    }

    public boolean anyPackagesDisappearing() {
        return mDisappearingPackages != null;
    }

    public boolean isPackageModified(String packageName) {
        if (mModifiedPackages != null) {
            for (int i=mModifiedPackages.length-1; i>=0; i--) {
                if (packageName.equals(mModifiedPackages[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onSomePackagesChanged() {
    }

    public void onFinishPackageChanges() {
    }

    // multi-user is not supported in commonui
    //    public int getChangingUserId() {
    //        return mChangeUserId;
    //    }

    String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        String pkg = uri != null ? uri.getSchemeSpecificPart() : null;
        return pkg;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // multi-user is not supported in commonui
        //        mChangeUserId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE,
        //                UserHandle.USER_NULL);
        //        if (mChangeUserId == UserHandle.USER_NULL) {
        //            throw new IllegalArgumentException(
        //                    "Intent broadcast does not contain user handle: " + intent);
        //        }
        onBeginPackageChanges();

        mDisappearingPackages = mAppearingPackages = null;
        mSomePackagesChanged = false;

        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            String pkg = getPackageName(intent);
            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            // We consider something to have changed regardless of whether
            // this is just an update, because the update is now finished
            // and the contents of the package may have changed.
            mSomePackagesChanged = true;
            if (pkg != null) {
                mAppearingPackages = mTempArray;
                mTempArray[0] = pkg;
                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    mModifiedPackages = mTempArray;
                    mChangeType = PACKAGE_UPDATING;
                    onPackageUpdateFinished(pkg, uid);
                    onPackageModified(pkg);
                } else {
                    mChangeType = PACKAGE_PERMANENT_CHANGE;
                    onPackageAdded(pkg, uid);
                }
                onPackageAppeared(pkg, mChangeType);
                if (mChangeType == PACKAGE_UPDATING) {
                    synchronized (mUpdatingPackages) {
                        mUpdatingPackages.remove(pkg);
                    }
                }
            }
        } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            String pkg = getPackageName(intent);
            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            if (pkg != null) {
                mDisappearingPackages = mTempArray;
                mTempArray[0] = pkg;
                if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                    mChangeType = PACKAGE_UPDATING;
                    synchronized (mUpdatingPackages) {
                        //not used for now
                        //mUpdatingPackages.add(pkg);
                    }
                    onPackageUpdateStarted(pkg, uid);
                } else {
                    mChangeType = PACKAGE_PERMANENT_CHANGE;
                    // We only consider something to have changed if this is
                    // not a replace; for a replace, we just need to consider
                    // it when it is re-added.
                    mSomePackagesChanged = true;
                    onPackageRemoved(pkg, uid);
                    if (intent.getBooleanExtra(EXTRA_REMOVED_FOR_ALL_USERS, false)) {
                        onPackageRemovedAllUsers(pkg, uid);
                    }
                }
                onPackageDisappeared(pkg, mChangeType);
            }
        } else if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
            String pkg = getPackageName(intent);
            int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
            String[] components = intent.getStringArrayExtra(
                    Intent.EXTRA_CHANGED_COMPONENT_NAME_LIST);
            if (pkg != null) {
                mModifiedPackages = mTempArray;
                mTempArray[0] = pkg;
                mChangeType = PACKAGE_PERMANENT_CHANGE;
                if (onPackageChanged(pkg, uid, components)) {
                    mSomePackagesChanged = true;
                }
                onPackageModified(pkg);
            }
        } else if (ACTION_QUERY_PACKAGE_RESTART.equals(action)) {
            mDisappearingPackages = intent.getStringArrayExtra(EXTRA_PACKAGES);
            mChangeType = PACKAGE_TEMPORARY_CHANGE;
            boolean canRestart = onHandleForceStop(intent,
                    mDisappearingPackages,
                    intent.getIntExtra(Intent.EXTRA_UID, 0), false);
            if (canRestart) setResultCode(Activity.RESULT_OK);
        } else if (Intent.ACTION_PACKAGE_RESTARTED.equals(action)) {
            mDisappearingPackages = new String[] {getPackageName(intent)};
            mChangeType = PACKAGE_TEMPORARY_CHANGE;
            onHandleForceStop(intent, mDisappearingPackages,
                    intent.getIntExtra(Intent.EXTRA_UID, 0), true);
        } else if (Intent.ACTION_UID_REMOVED.equals(action)) {
            onUidRemoved(intent.getIntExtra(Intent.EXTRA_UID, 0));
        } else if (ACTION_USER_STOPPED.equals(action)) {
            if (intent.hasExtra(EXTRA_USER_HANDLE)) {
                onHandleUserStop(intent, intent.getIntExtra(EXTRA_USER_HANDLE, 0));
            }
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
            String[] pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            mAppearingPackages = pkgList;
            mChangeType = PACKAGE_TEMPORARY_CHANGE;
            mSomePackagesChanged = true;
            if (pkgList != null) {
                onPackagesAvailable(pkgList);
                for (int i=0; i<pkgList.length; i++) {
                    onPackageAppeared(pkgList[i], PACKAGE_TEMPORARY_CHANGE);
                }
            }
        } else if (Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
            String[] pkgList = intent.getStringArrayExtra(Intent.EXTRA_CHANGED_PACKAGE_LIST);
            mDisappearingPackages = pkgList;
            mChangeType = PACKAGE_TEMPORARY_CHANGE;
            mSomePackagesChanged = true;
            if (pkgList != null) {
                onPackagesUnavailable(pkgList);
                for (int i=0; i<pkgList.length; i++) {
                    onPackageDisappeared(pkgList[i], PACKAGE_TEMPORARY_CHANGE);
                }
            }
        }

        if (mSomePackagesChanged) {
            onSomePackagesChanged();
        }

        onFinishPackageChanges();
        // multi-user is not supported in commonui
        //        mChangeUserId = UserHandle.USER_NULL;
    }
}
