package com.topface.topface.imageloader;

import android.view.View;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.topface.topface.R;

public class DefaultImageLoaderListener extends SimpleImageLoadingListener {

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        super.onLoadingFailed(imageUri, view, failReason);

        ((ImageView) view).setImageResource(R.drawable.im_photo_error);
    }
}
