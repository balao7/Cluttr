package org.polaric.cluttr;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.polaric.cluttr.io.ExtSdHelper;
import org.polaric.colorful.Colorful;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class Cluttr extends Application {
    private static Cluttr cluttr;
    private SharedPreferences prefs;
    private boolean newRelease=false;
    private Thread.UncaughtExceptionHandler defaultUEH;

    public Cluttr() {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException (Thread thread, Throwable e) {
                handleUncaughtException (thread, e);
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cluttr=this;
        Colorful.defaults().translucent(true);
        Colorful.init(this);
        ExtSdHelper.init();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        newRelease = !(BuildConfig.VERSION_CODE == prefs.getInt("current_version",0));

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .directory(getCacheDir())
                .build();
        Realm.setDefaultConfiguration(config);
    }

    public void handleUncaughtException (Thread thread, Throwable e) {
        clearCache();
        Log.e(Util.LOG_TAG, "FATAL_ERROR: CLUTTR HAS CRASHED. CACHES WILL BE WIPED");
        //TODO: BUG REPORT?

        defaultUEH.uncaughtException(thread, e);
    }

    public void clearCache() {
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .directory(getCacheDir())
                .build();
        Realm.deleteRealm(config);
    }

    public static Cluttr getApplication() {
        return cluttr;
    }

    public boolean isNewRelease() {
        if (newRelease) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("current_version", BuildConfig.VERSION_CODE).apply();
            newRelease=false;
            return true;
        }

        return false;
    }

    public void restart() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
