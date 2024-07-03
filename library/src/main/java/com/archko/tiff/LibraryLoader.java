package com.archko.tiff;

/**
 *
 */
public class LibraryLoader {

    private static boolean alreadyLoaded = false;

    public static void load() {
        if (alreadyLoaded) {
            return;
        }
        System.loadLibrary("tiff_image");
        alreadyLoaded = true;
    }
}
