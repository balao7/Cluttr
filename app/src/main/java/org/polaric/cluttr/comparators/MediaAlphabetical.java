package org.polaric.cluttr.comparators;

import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.MediaItem;

public class MediaAlphabetical extends MediaComparator {
    private boolean ascending = true;

    public MediaAlphabetical(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(MediaItem mediaItem, MediaItem t1) {
        return ascending
                ? mediaItem.getName().toLowerCase().compareTo(t1.getName().toLowerCase())
                : t1.getName().toLowerCase().compareTo(mediaItem.getName().toLowerCase());
    }
}
