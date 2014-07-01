package com.topface.framework.imageloader;

import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

class DefaultImageLoaderListener extends SimpleImageLoadingListener {

    private int mErrorResId = 0;

    DefaultImageLoaderListener(int errorResId) {
        mErrorResId = errorResId;
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        super.onLoadingFailed(imageUri, view, failReason);
        ((ImageView) view).setImageResource(mErrorResId);
    }
}
