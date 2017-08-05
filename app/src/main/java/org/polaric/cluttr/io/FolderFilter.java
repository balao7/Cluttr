package org.polaric.cluttr.io;

import java.io.File;
import java.io.FileFilter;

public class FolderFilter implements FileFilter {
    @Override
    public boolean accept(File file) {
        return file.isDirectory();
    }
}
