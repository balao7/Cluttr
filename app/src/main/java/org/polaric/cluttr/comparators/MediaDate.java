package org.polaric.cluttr.comparators;

import org.polaric.cluttr.data.MediaItem;

public class MediaDate extends MediaComparator {
    private boolean ascending = true;

    public MediaDate(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(MediaItem mediaItem, MediaItem t1) {
        return ascending
                ? Long.valueOf(mediaItem.getDate()).compareTo(t1.getDate())
                : Long.valueOf(t1.getDate()).compareTo(mediaItem.getDate());
    }
}
