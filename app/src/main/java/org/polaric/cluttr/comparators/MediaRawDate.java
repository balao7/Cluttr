package org.polaric.cluttr.comparators;

import java.io.File;
import java.util.Comparator;

public class MediaRawDate implements Comparator<File> {
    private boolean ascending = true;

    public MediaRawDate(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(File mediaItem, File t1) {
        return ascending
                ? Long.valueOf(mediaItem.lastModified()).compareTo(t1.lastModified())
                : Long.valueOf(t1.lastModified()).compareTo(mediaItem.lastModified());
    }
}
