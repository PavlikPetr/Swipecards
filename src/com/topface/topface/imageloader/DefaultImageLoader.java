package com.topface.topface.imageloader;

import android.content.Context;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;

public class DefaultImageLoader {

    private static ImageLoader mImageLoader;
    private static DefaultImageLoader mInstance;
    private final Context mContext;

    public DefaultImageLoader(Context context) {
        mContext = context;
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(getConfig());
    }

    protected ImageLoaderConfiguration getConfig() {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(mContext);
        builder.enableLogging();
        builder.defaultDisplayImageOptions(getDisplayImageConfig());
        return builder.build();
    }

    protected DisplayImageOptions getDisplayImageConfig() {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory();
        builder.cacheOnDisc();
        builder.resetViewBeforeLoading();
        builder.showStubImage(R.drawable.loader);
        builder.showImageForEmptyUri(R.drawable.im_photo_error);
        return builder.build();
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

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options, ImageLoadingListener listener) {
        try {
            getImageLoader().displayImage(uri, imageView, options, listener);

        }
        catch (Exception e) {
            Debug.error("ImageLoader displayImage error", e);
            listener.onLoadingFailed(FailReason.UNKNOWN);
        }
    }

    public void displayImage(String uri, ImageView imageView, DisplayImageOptions options) {
        displayImage(uri, imageView, options, getDefaultImageLoaderListener(imageView));
    }

    public void displayImage(String uri, ImageView imageView, ImageLoadingListener listener) {
        displayImage(uri, imageView, null, listener);
    }

    public void displayImage(String uri, ImageView imageView) {
        displayImage(uri, imageView, null, getDefaultImageLoaderListener(imageView));
    }

    private ImageLoadingListener getDefaultImageLoaderListener(ImageView imageView) {
        return new DefaultImageLoaderListener(imageView);
    }


}
