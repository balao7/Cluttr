package org.polaric.cluttr.data;

import android.util.Log;

import org.polaric.cluttr.Util;
import org.polaric.cluttr.comparators.MediaAlphabetical;
import org.polaric.cluttr.comparators.MediaDate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.annotations.PrimaryKey;

public class Album extends RealmObject {
    @PrimaryKey private String path;
    private RealmList<MediaItem> photos = new RealmList<>();
    private int size;
    private String name;
    private String coverPath;
    private long lastModified;
    private boolean hidden=false;

    public Album() {
    }

    public Album(String path, int size, String name, String coverPath, long lastModified) {
        this.path = path;
        this.size=size;
        this.name=name;
        this.coverPath=coverPath;
        this.lastModified=lastModified;
        hidden = new File(path + "/", ".nomedia").exists();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void clearMedia() {
        photos.clear();
    }

    public List<Integer> getFilteredIndex(String query) {
        List<Integer> filter = new ArrayList<>();
        for (int index=0; index<photos.size(); index++) {
            if (photos.get(index).getName().contains(query)) {
                filter.add(index);
            }
        }
        return filter;
    }

    public boolean needsReload() {
        Log.d(Util.LOG_TAG, "Checking if " + getName() + " has been changed");
        Long modified = new File(path).lastModified();
        if (photos.size() < 1 || modified>lastModified) {
            Log.d(Util.LOG_TAG, "Album " + getName() + " has been modified, reloading");
            return true;
        }
        return false;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public int countSelected() {
        int selected=0;
        for (MediaItem i : photos) {
            if (i.isSelected()) {
                selected++;
            }
        }
        return selected;
    }

    public List<MediaItem> getSelected() {
        ArrayList<MediaItem> selected = new ArrayList<>();
        for (MediaItem item : photos) {
            if (item.isSelected()) {
                selected.add(item);
            }
        }
        return selected;
    }

    public void clearMediaSelection() {
        for (MediaItem i : photos) {
            i.setSelected(false);
        }
        Log.d(Util.LOG_TAG, "Clearing media selection");
    }

    public RealmList<MediaItem> getMedia() {
        return photos;
    }

    public void addMediaItem(MediaItem item) {
        photos.add(item);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void remove(int index) {
        photos.remove(index);
    }

    public int getSize() {
        if (photos.size()>0) {
            return photos.size();
        }
        return size;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getCoverPath() {
        return coverPath;
    }

    @Override
    public boolean equals(Object obj) {
        return path.equals(((Album) obj).path);
    }
}
