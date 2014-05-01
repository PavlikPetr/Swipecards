package com.nostra13.universalimageloader.core;

/**
 * Created by kirussell on 26.04.2014.
 */
public class ExtendedImageLoader extends ImageLoader {

    private volatile static ExtendedImageLoader instance;

    public static ImageLoader getInstance() {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ExtendedImageLoader();
                }
            }
        }
        return instance;
    }

    @Override
    protected LoadAndDisplayImageTask createLoadAndDisplayImageTask(DisplayImageOptions options, ImageLoadingInfo imageLoadingInfo) {
        return new ExtendedLoadAndDisplayImageTask(getEngine(), imageLoadingInfo, defineHandler(options));
    }
}
