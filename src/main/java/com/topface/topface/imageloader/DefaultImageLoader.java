package com.topface.topface.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ExtendedImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.nostra13.universalimageloader.utils.L;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;

@SuppressWarnings("UnusedDeclaration")
public class DefaultImageLoader {

    public static final int DISC_CACHE_SIZE = 10 * 1024 * 1024;
    private static ImageLoader mImageLoader;
    private static DefaultImageLoader mInstance;
    private final Context mContext;
    private DisplayImageOptions mOptimizedConfig;

    public DefaultImageLoader(Context context) {
        mContext = context;
        mImageLoader = ExtendedImageLoader.getInstance();
        mImageLoader.init(getConfig().build());
    }

    /**
     * Загрузчик изображений нужно использовать только с контекстом активити
     *
     * @return инстанс загрузчика изображений
     */
    public static DefaultImageLoader getInstance() {
        if (mInstance == null) {
            mInstance = new DefaultImageLoader(App.getContext());
        }

        return mInstance;
    }

    protected ImageLoaderConfiguration.Builder getConfig() {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(mContext);
        if (Debug.isDebugLogsEnabled()) {
            builder.writeDebugLogs();
        } else {
            L.disableLogging();
        }
        builder.discCacheSize(DISC_CACHE_SIZE);
        builder.defaultDisplayImageOptions(getDisplayImageConfig().build());
        return builder;
    }

    /**
     * Этот конфиг испольхуется только для показа фотографий не требующих прозрачности
     */
    protected DisplayImageOptions getOptimizedDisplayImageConfig() {
        if (mOptimizedConfig == null) {
            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
            builder.cacheInMemory(true);
            builder.cacheOnDisc(true);
            builder.imageScaleType(ImageScaleType.EXACTLY);
            builder.bitmapConfig(Bitmap.Config.RGB_565);
            builder.resetViewBeforeLoading(true);
            builder.showImageForEmptyUri(R.drawable.im_photo_error);
            mOptimizedConfig = builder.build();
        }
        return mOptimizedConfig;
    }

    protected DisplayImageOptions.Builder getDisplayImageConfig() {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true);
        builder.cacheOnDisc(true);
        builder.resetViewBeforeLoading(true);
        builder.considerExifParams(true);
        builder.showImageForEmptyUri(R.drawable.im_photo_error);
        return builder;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener, BitmapProcessor processor) {
        try {
            //Если не задан пост-процессор, то используем оптимизированную версию конфига
            if (options == null && processor == null) {
                options = getOptimizedDisplayImageConfig();
            } else if (options == null) {
                //Если же используется процессор, то собираем новую версию конфига с нужным процессором
                options = getDisplayImageConfig()
                        .preProcessor(processor)
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

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener) {
        displayImage(uri, imageView, options, listener, null);
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        displayImage(uri, imageView, options, getDefaultImageLoaderListener(), null);
    }

    public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener) {
        displayImage(uri, imageView, null, listener, null);
    }

    public void displayImage(String uri, ImageView imageView) {
        displayImage(uri, imageView, null, getDefaultImageLoaderListener(), null);
    }

    public void displayImage(String uri, ImageView imageView, BitmapProcessor postProcessor) {
        displayImage(uri, imageView, null, getDefaultImageLoaderListener(), postProcessor);
    }

    private ImageLoadingListener getDefaultImageLoaderListener() {
        return new DefaultImageLoaderListener();
    }

    public void preloadImage(String uri, ImageLoadingListener listener) {
        getImageLoader().loadImage(uri, listener);
    }


}
