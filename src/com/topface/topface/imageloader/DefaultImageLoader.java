package com.topface.topface.imageloader;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ListView;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;

public class DefaultImageLoader {

    private static ImageLoader mImageLoader;
    private static DefaultImageLoader mInstance;
    public static final int DISC_CACHE_SIZE = 10 * 1024 * 1024;
    public static final int MEMORY_CACHE_SIZE = 2 * 1024 * 1024;
    private final Context mContext;

    public DefaultImageLoader(Context context) {
        mContext = context;
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(getConfig().build());
    }

    protected ImageLoaderConfiguration.Builder getConfig() {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(mContext);
        if (App.DEBUG) {
            builder.enableLogging();
        }
        builder.discCacheSize(DISC_CACHE_SIZE);
        builder.memoryCache(new WeakMemoryCache());
        builder.memoryCacheSize(MEMORY_CACHE_SIZE);
        builder.defaultDisplayImageOptions(getDisplayImageConfig().build());
        return builder;
    }

    protected DisplayImageOptions.Builder getDisplayImageConfig() {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory();
        builder.cacheOnDisc();
        builder.imageScaleType(ImageScaleType.EXACT);
        builder.resetViewBeforeLoading();
        //builder.showStubImage(R.drawable.loader);
        builder.showImageForEmptyUri(R.drawable.im_photo_error);
        return builder;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public static DefaultImageLoader getInstance() {
        if (mInstance == null) {
            mInstance = new DefaultImageLoader(App.getContext());
        }

        return mInstance;
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener, ImagePostProcessor postProcessor) {
        try {
            getImageLoader().displayImage(uri, imageView, options, listener, postProcessor);
        } catch (Exception e) {
            Debug.error("ImageLoader displayImage error", e);
            if (listener != null) {
                listener.onLoadingFailed(FailReason.UNKNOWN);
            }
        }
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener) {
        displayImage(uri, imageView, options, listener, null);
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        displayImage(uri, imageView, options, getDefaultImageLoaderListener(imageView), null);
    }

    public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener) {
        displayImage(uri, imageView, null, listener, null);
    }

    public void displayImage(String uri, ImageView imageView) {
        displayImage(uri, imageView, null, getDefaultImageLoaderListener(imageView), null);
    }

    public void displayImage(String uri, ImageView imageView, ImagePostProcessor postProcessor) {
        displayImage(uri, imageView, null, getDefaultImageLoaderListener(imageView), postProcessor);
    }

    private ImageLoadingListener getDefaultImageLoaderListener(ImageView imageView) {
        return new DefaultImageLoaderListener(imageView);
    }

    public void preloadImage(String uri) {
        preloadImage(uri, null);
    }

    public void preloadImage(String uri, ImageLoadingListener listener) {
        ImageView fakeImageView = new ImageView(mContext);
        fakeImageView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT));
        displayImage(uri, fakeImageView, listener);
    }


}
