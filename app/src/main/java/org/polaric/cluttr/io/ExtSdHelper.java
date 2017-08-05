package org.polaric.cluttr.io;

import android.content.Context;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import org.polaric.cluttr.Cluttr;
import org.polaric.cluttr.Util;

import java.io.File;

public class ExtSdHelper {
    public static final String PATH_KEY="extsdpath";
    private static File sd;
    public static final int SAF_REQUEST=93;

    private static String sdPath=null;

    public static void init() {
        sdPath = PreferenceManager.getDefaultSharedPreferences(Cluttr.getApplication()).getString(PATH_KEY, null);
        if (sdPath!=null) {
            sd = new File(FileUtil.getFullPathFromTreeUri(getSdUri()));
        }
    }

    public static void clear() {
        PreferenceManager.getDefaultSharedPreferences(Cluttr.getApplication()).edit().putString(PATH_KEY, null).apply();
        sdPath=null;
        sd=null;
        //TODO: CLEAR CACHE
    }

    public static boolean hasExtSd() {
        return sdPath!=null;
    }

    public static void setSdPath(String path) {
        sdPath=path;
        PreferenceManager.getDefaultSharedPreferences(Cluttr.getApplication()).edit().putString(PATH_KEY, path).apply();
        sd = new File(FileUtil.getFullPathFromTreeUri(getSdUri()));
        Log.d(Util.LOG_TAG, "SAF External SD Directory set to " + path);
        //TODO: CLEAR CACHE
    }

    public static String getSdPath() {
        return sdPath;
    }

    public static Uri getSdUri() {
        return Uri.parse(getSdPath());
    }

    public static File getRootFile() {
        return sd;
    }
}
