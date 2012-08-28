package com.topface.topface.imageloader;

import android.content.Context;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.topface.topface.App;
import com.topface.topface.R;

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

}
