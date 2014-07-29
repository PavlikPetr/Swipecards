package com.topface.framework.imageloader;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by kirussell on 26.06.2014.
 * Provide ImageLoader or it extensions
 * Used by {@link com.topface.framework.imageloader.DefaultImageLoader}
 * to create its internal ImageLoader instance
 * By default it will provide basic ImageLoader instance
 * Use {@link #setExtendedImageLoader(com.nostra13.universalimageloader.core.ImageLoader)}
 * to set your own extended ImageLoader
 */
public class ImageLoaderStaticFactory {

    private static ImageLoader extendedImageLoader;

    public static void setExtendedImageLoader(ImageLoader imageLoader) {
        extendedImageLoader = imageLoader;
    }

    public static ImageLoader createImageLoader() {
        if (extendedImageLoader != null) {
            return extendedImageLoader;
        }
        return ImageLoader.getInstance();
    }
}