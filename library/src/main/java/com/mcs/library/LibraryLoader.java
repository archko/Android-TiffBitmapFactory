package com.mcs.library;

public class LibraryLoader {
    private static boolean alreadyLoaded = false;

    public static void load() {
        if (alreadyLoaded) {
            return;
        }
        System.loadLibrary("tiff_image_jni");
        alreadyLoaded = true;
    }
}
