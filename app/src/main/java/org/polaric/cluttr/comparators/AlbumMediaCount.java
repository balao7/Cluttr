package org.polaric.cluttr.comparators;

import org.polaric.cluttr.data.Album;

public class AlbumMediaCount extends AlbumComparator {
    private boolean ascending = true;

    public AlbumMediaCount(boolean ascending) {
        this.ascending = ascending;
    }

    @Override
    public int compare(Album album, Album t1) {
        return ascending
                ? album.getSize() - t1.getSize()
                : t1.getSize() - album.getSize();
    }
}
