package com.topface.framework.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.nostra13.universalimageloader.utils.L;
import com.topface.framework.utils.Debug;

public class DefaultImageLoader {
    public static final int DISC_CACHE_SIZE = 10 * 1024 * 1024;
    private static ImageLoader mImageLoader;
    private static DefaultImageLoader mInstance;
    private final Context mContext;
    private DisplayImageOptions mOptimizedConfig;
    private int mErrorResId = 0;

    private DefaultImageLoader(Context context) {
        mContext = context;
        mImageLoader = ImageLoaderStaticFactory.createImageLoader();
        mImageLoader.init(getConfig());
    }

    /**
     * Method to get instance of Image loader
     *
     * @return image loader instance
     */
    public static DefaultImageLoader getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DefaultImageLoader(context);
        }
        return mInstance;
    }

    protected ImageLoaderConfiguration getConfig() {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(mContext);
        if (Debug.isDebugLogsEnabled()) {
            builder.writeDebugLogs();
        } else {
            L.disableLogging();
        }
        builder.discCacheSize(DISC_CACHE_SIZE);
        builder.defaultDisplayImageOptions(getDisplayImageConfig(0).build());
        return builder.build();
    }

    /**
     * Этот конфиг испольхуется только для показа фотографий не требующих прозрачности
     */
    protected DisplayImageOptions getOptimizedDisplayImageConfig(int stubResId) {
        if (mOptimizedConfig == null) {
            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
            builder.cacheInMemory(true);
            builder.cacheOnDisc(true);
            builder.imageScaleType(ImageScaleType.EXACTLY);
            builder.bitmapConfig(Bitmap.Config.RGB_565);
            builder.resetViewBeforeLoading(true);
            builder.showImageForEmptyUri(mErrorResId);
            builder.showImageOnLoading(stubResId);
            mOptimizedConfig = builder.build();
        }
        return mOptimizedConfig;
    }

    protected DisplayImageOptions.Builder getDisplayImageConfig(int stubResId) {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true);
        builder.cacheOnDisc(true);
        builder.resetViewBeforeLoading(true);
        builder.considerExifParams(true);
        builder.showImageForEmptyUri(mErrorResId);
        builder.showImageOnLoading(stubResId);
        return builder;
    }

    @SuppressWarnings("unused")
    public void setErrorImageResId(int resId) {
        mErrorResId = resId;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener, BitmapProcessor processor) {
        displayImage(uri, imageView, options, listener, processor, null, 0);
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener,
                             BitmapProcessor preProcessor, BitmapProcessor postProcessor, int stubResId) {
        try {
            //Если не задан пост-процессор, то используем оптимизированную версию конфига
            if (options == null && preProcessor == null && postProcessor == null) {
                options = getOptimizedDisplayImageConfig(stubResId);
            } else if (options == null) {
                //Если же используется процессор, то собираем новую версию конфига с нужным процессором
                options = getDisplayImageConfig(stubResId)
                        .preProcessor(preProcessor)
                        .postProcessor(postProcessor)
                        .build();
            }

            getImageLoader().displayImage(
                    uri,
                    imageView,
                    options,
                    listener
            );

        } catch (Exception e) {
            Debug.error("ImageLoader displayImage error", e);
            if (listener != null) {
                listener.onLoadingFailed(uri, imageView, new FailReason(FailReason.FailType.UNKNOWN, e));
            }
        }
    }

    @SuppressWarnings("unused")
    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener) {
        displayImage(uri, imageView, options, listener, null);
    }

    @SuppressWarnings("unused")
    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        displayImage(uri, imageView, options, getDefaultImageLoaderListener(), null);
    }

    @SuppressWarnings("unused")
    public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener) {
        displayImage(uri, imageView, null, listener, null);
    }

    @SuppressWarnings("unused")
    public void displayImage(String uri, ImageView imageView) {
        displayImage(uri, imageView, null, getDefaultImageLoaderListener(), null);
    }

    @SuppressWarnings("unused")
    public void displayImage(String uri, ImageView imageView, BitmapProcessor postProcessor) {
        displayImage(uri, imageView, null, getDefaultImageLoaderListener(), postProcessor);
    }

    private ImageLoadingListener getDefaultImageLoaderListener() {
        return new DefaultImageLoaderListener(mErrorResId);
    }

    public void preloadImage(String uri, ImageLoadingListener listener) {
        getImageLoader().loadImage(uri, listener);
    }
}
