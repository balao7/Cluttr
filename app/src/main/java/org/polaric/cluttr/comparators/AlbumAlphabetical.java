package org.polaric.cluttr.comparators;

import org.polaric.cluttr.data.Album;

public class AlbumAlphabetical extends AlbumComparator {
    private boolean ascending = true;

    public AlbumAlphabetical(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(Album album, Album t1) {
        return ascending
                ? album.getName().toLowerCase().compareTo(t1.getName().toLowerCase())
                : t1.getName().toLowerCase().compareTo(album.getName().toLowerCase());
    }
}
