package com.topface.topface.utils;

import com.topface.topface.data.AbstractDataWithPhotos;

public class PhotoLinksResolver {

    // Constants
    public enum PhotoType {
        AVATAR, PHOTO
    }

    private static final String PREFIX_CROPPED = "c";
    private static final String PREFIX_RESIZED = "r";

    public static final String SIZE_ORIGIN = "original";
    public static final String SIZE_64 = "c64x64";
    public static final String SIZE_128 = "c128x128";
    public static final String SIZE_192 = "c192x192";
    public static final String SIZE_SCREEN = "r640x960";

    public static String getLink(AbstractDataWithPhotos data, PhotoType type, int width, int height) {
        //TODO
        return "";
    }

    public static String getLink(AbstractDataWithPhotos data, String sizeKey) {
        //TODO
        return "";
    }
}
