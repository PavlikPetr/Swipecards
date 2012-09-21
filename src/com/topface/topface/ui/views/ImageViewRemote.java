package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.imageloader.RoundPostProcessor;

public class ImageViewRemote extends ImageView {
    public ImageViewRemote(Context context) {
        super(context);
    }

    public ImageViewRemote(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ImageViewRemote(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean setRemoteSrc(String remoteSrc) {
        boolean isCorrectSrc = true;
        if (remoteSrc != null && remoteSrc.trim().length() > 0) {
            ImagePostProcessor processor = getPostProcessor();
            if (processor != null) {
                getImageLoader().displayImage(remoteSrc, this, processor);
            }
            else {
                getImageLoader().displayImage(remoteSrc, this);
            }

        }
        else {
            isCorrectSrc = false;
        }

        return isCorrectSrc;
    }

    public DefaultImageLoader getImageLoader() {
        return DefaultImageLoader.getInstance();
    }

    public ImagePostProcessor getPostProcessor() {
        return new RoundPostProcessor();
    }
}
