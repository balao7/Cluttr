package org.polaric.cluttr.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.preference.PreferenceManager;
import android.test.mock.MockApplication;
import android.util.Log;

import org.polaric.cluttr.Cluttr;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.comparators.AlbumAlphabetical;
import org.polaric.cluttr.comparators.AlbumMediaCount;
import org.polaric.cluttr.comparators.MediaAlphabetical;
import org.polaric.cluttr.comparators.MediaDate;
import org.polaric.cluttr.comparators.MediaRawAlphabetical;
import org.polaric.cluttr.comparators.MediaRawDate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;

public class Media {
    public static final String ALBUM_SORT_KEY="album_sort";
    public static final String MEDIA_SORT_KEY="media_sort";
    public static final String ALBUM_SORT_KEY_ASC="album_sort_asc";
    public static final String MEDIA_SORT_KEY_ASC="media_sort_asc";
    private static boolean sortAscending=true;
    private static boolean mediaSortAscending=true;
    private static AlbumSortType sortType=AlbumSortType.ALPHABETICAL;
    private static MediaSortType mediaSortType=MediaSortType.DATE;
    private static boolean showHidden=false;
    private static boolean needsRefresh=false;
    private static boolean animations=true;
    private static boolean staggered=false;
    private static int sizePortrait=2;
    private static int sizeLandscape=4;
    private static int sizeAlbumPortrait=4;
    private static int sizeAlbumLandscape=8;
    public enum MediaTypes {
        IMAGE, VIDEO, GIF, UNKNOWN
    }

    public enum AlbumSortType {
        ALPHABETICAL, SIZE
    }

    public enum MediaSortType {
        ALPHABETICAL, DATE
    }

    public static List<String> albumsNeedReloaded() {
        List<String> reloads = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        for (Album a : realm.where(Album.class).findAll()) {
            if (a.needsReload()) {
                reloads.add(a.getPath());
            }
        }
        return reloads;
    }

    public static void invalidate() {
        Log.e(Util.LOG_TAG, "Invalidate called, all caches will be wiped");
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        realm.close();
    }

    public static void init(Context context, boolean refresh) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        sizePortrait = prefs.getInt("album_grid_portrait",2);
        sizeLandscape = prefs.getInt("album_grid_landscape",4);
        sizeAlbumPortrait = prefs.getInt("media_grid_portrait",4);
        sizeAlbumLandscape = prefs.getInt("media_grid_landscape",8);
        showHidden = prefs.getBoolean("show_hidden", false);
        animations = prefs.getBoolean("animations", true);
        staggered = prefs.getBoolean("staggered",false);

        sortType = Media.AlbumSortType.values()[prefs.getInt(Media.ALBUM_SORT_KEY, Media.AlbumSortType.ALPHABETICAL.ordinal())];
        mediaSortType = Media.MediaSortType.values()[prefs.getInt(Media.MEDIA_SORT_KEY, MediaSortType.DATE.ordinal())];
        sortAscending = prefs.getBoolean(ALBUM_SORT_KEY_ASC,true);
        mediaSortAscending = prefs.getBoolean(MEDIA_SORT_KEY_ASC,false);

        if (refresh) {
            needsRefresh=true;
        }
    }

    public static void setStaggered(boolean staggered) {
        Media.staggered = staggered;
    }

    public static boolean isStaggered() {
        return staggered;
    }

    public static boolean needsRefresh() {
        return needsRefresh;
    }

    public static boolean isShowHidden() {
        return showHidden;
    }

    public static boolean useAnimations() {
        return animations;
    }

    public static void setUseAnimations(boolean animations) {
        Media.animations = animations;
    }

    public static void setShowHidden(boolean showHidden, Context context) {
        Media.showHidden = showHidden;
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("show_hidden", showHidden).apply();
    }
    public static void setAlbumSortType(Context context, AlbumSortType type, boolean ascending) {
        sortType = type;
        sortAscending=ascending;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putInt(ALBUM_SORT_KEY,sortType.ordinal())
                .putBoolean(ALBUM_SORT_KEY_ASC,sortAscending)
                .apply();
    }

    public static void setMediaSortType(Context context, MediaSortType type, boolean ascending) {
        mediaSortType = type;
        mediaSortAscending=ascending;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit()
                .putInt(MEDIA_SORT_KEY,mediaSortType.ordinal())
                .putBoolean(MEDIA_SORT_KEY_ASC,mediaSortAscending)
                .apply();
    }

    /*public static Comparator<Album> getAlbumComparator() {
        switch (sortType) {
            case ALPHABETICAL: {
                return new AlbumAlphabetical(sortAscending);
            }
            case SIZE: {
                return new AlbumMediaCount(sortAscending);
            }
        }
        return null;
    }

    public static Comparator<MediaItem> getMediaComparator() {
        switch (mediaSortType) {
            case ALPHABETICAL: {
                return new MediaAlphabetical(mediaSortAscending);
            }
            case DATE: {
                return new MediaDate(mediaSortAscending);
            }
        }
        return null;
    }*/

    public static boolean isMediaSortAscending() {
        return mediaSortAscending;
    }

    public static MediaSortType getMediaSortType() {
        return mediaSortType;
    }

    public static boolean isSortAscending() {
        return sortAscending;
    }

    public static AlbumSortType getSortType() {
        return sortType;
    }

    public static int getSizeAlbumPortrait() {
        return sizeAlbumPortrait;
    }

    public static int getSizeAlbumLandscape() {
        return sizeAlbumLandscape;
    }

    public static int getSizePortrait() {
        return sizePortrait;
    }

    public static int getSizeLandscape() {
        return sizeLandscape;
    }

    public static List<Integer> getFilteredIndex(String query) {
        //TODO
        return null;
    }

    public static void share(Context context, String album) {

        ArrayList<Uri> files = new ArrayList<Uri>();

        Realm realm = Realm.getDefaultInstance();
        for(MediaItem item : realm.where(Album.class).equalTo("path", album).findFirst().getMedia().where().equalTo("isSelected",true).findAll()) {
            File file = new File(item.getPath());
            Uri uri = Uri.fromFile(file);
            files.add(uri);
        }

        final Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);

        realm.beginTransaction();
        realm.where(Album.class).equalTo("path", album).findFirst().clearMediaSelection();
        realm.commitTransaction();
        realm.close();

        context.startActivity(Intent.createChooser(intent, "Share"));
    }

}
