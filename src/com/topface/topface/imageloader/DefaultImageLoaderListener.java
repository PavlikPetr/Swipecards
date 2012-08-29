package com.topface.topface.imageloader;

import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.topface.topface.R;

/**
 * Created with IntelliJ IDEA.
 * User: gildor
 * Date: 29.08.12
 * Time: 15:21
 * To change this template use File | Settings | File Templates.
 */
public class DefaultImageLoaderListener extends SimpleImageLoadingListener {

    private ImageView mImageView;

    public DefaultImageLoaderListener(ImageView imageView) {
        mImageView = imageView;
    }
    @Override
    public void onLoadingFailed(FailReason failReason) {
        super.onLoadingFailed(failReason);
        mImageView.setImageResource(R.drawable.im_photo_error);
    }
}
