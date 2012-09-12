package com.topface.topface.imageloader;

import android.content.Context;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.topface.topface.R;

public class FullSizeImageLoader extends DefaultImageLoader {
    public FullSizeImageLoader(Context context) {
        super(context);
    }

    @Override
    protected DisplayImageOptions.Builder getDisplayImageConfig() {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory();
        builder.cacheOnDisc();
        builder.resetViewBeforeLoading();
        builder.showImageForEmptyUri(R.drawable.im_photo_error);
        return builder;
    }

}
