package org.polaric.cluttr.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;

public class MediaFilter implements FilenameFilter {

    public final static int FILTER_ALL = 0;
    public final static int FILTER_IMAGES = 1;
    public final static int FILTER_GIFS = 2;
    public final static int FILTER_VIDEO = 3;
    public final static int FILTER_NO_VIDEO = 4;

    private HashSet<String> extensions;
    private static String[] imagesExtensions = new String[] { "jpg", "png", "jpe", "jpeg", "bmp", "webp" };
    private static String[] videoExtensions = new String[] { "mp4", "mkv", "webm", "avi" };
    private static String[] gifsExtensions = new String[] { "gif"} ;

    MediaFilter(int filterMode) {
        extensions = new HashSet<String>();
        switch (filterMode) {
            case FILTER_IMAGES:
                extensions.addAll(Arrays.asList(imagesExtensions));
                break;
            case FILTER_VIDEO:
                extensions.addAll(Arrays.asList(videoExtensions));
                break;
            case FILTER_GIFS:
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
            case FILTER_NO_VIDEO:
                extensions.addAll(Arrays.asList(imagesExtensions));
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
            default:
                extensions.addAll(Arrays.asList(imagesExtensions));
                extensions.addAll(Arrays.asList(videoExtensions));
                extensions.addAll(Arrays.asList(gifsExtensions));
                break;
        }

    }

    MediaFilter() {
        extensions = new HashSet<String>();
        extensions.addAll(Arrays.asList(imagesExtensions));
        extensions.addAll(Arrays.asList(videoExtensions));
        extensions.addAll(Arrays.asList(gifsExtensions));
    }

    @Override
    public boolean accept(File file, String s) {
        for (String extension : extensions)
            if (s.toLowerCase().endsWith(extension))
                return true;
        return false;
    }
}
