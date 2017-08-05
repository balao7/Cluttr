package org.polaric.cluttr.comparators;

import java.io.File;
import java.util.Comparator;

public class MediaRawAlphabetical implements Comparator<File> {
    private boolean ascending = true;

    public MediaRawAlphabetical(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(File mediaItem, File t1) {
        return ascending
                ? mediaItem.getName().toLowerCase().compareTo(t1.getName().toLowerCase())
                : t1.getName().toLowerCase().compareTo(mediaItem.getName().toLowerCase());
    }
}
