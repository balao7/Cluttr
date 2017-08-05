package org.polaric.cluttr.data;

import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class MediaItem extends RealmObject {
    @PrimaryKey private String path;
    private long date;
    private String mime;
    private boolean isSelected;
    private float aspectRatio=-1;
    private String name;

    public MediaItem() {
    }

    public MediaItem(String path, long date) {
        this.path = path;
        this.date = date;
        setMIME();
        name = getName();
    }

    public MediaItem(File file) {
        path=file.getAbsolutePath();
        date=file.lastModified();
        setMIME();
        name = getName();
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return new File(path).getName();
    }

    public String getFileExtension() {
        return path.substring(path.lastIndexOf(".")+1);
    }

    public Media.MediaTypes getMediaType() {
        if (mime.endsWith("gif")) {
            return Media.MediaTypes.GIF;
        } else if (mime.startsWith("image")) {
            return Media.MediaTypes.IMAGE;
        } else if (mime.startsWith("video")) {
            return Media.MediaTypes.VIDEO;
        }
        return Media.MediaTypes.UNKNOWN;
    }

    public String getMime() {
        return mime;
    }

    private void setMIME() {
        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension().toLowerCase());
        if (mime == null) mime="unknown";
    }

    public long getDate() {
        return date;
    }

    @Override
    public boolean equals(Object obj) {
        return path.equals(((MediaItem) obj).path);
    }
}
