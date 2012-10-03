package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.R;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.imageloader.RoundCornersPostProcessor;
import com.topface.topface.imageloader.RoundPostProcessor;

public class ImageViewRemote extends ImageView {

    private static final int POST_PROCESSOR_NONE = 0;
    private static final int POST_PROCESSOR_ROUNDED = 1;
    private static final int POST_PROCESSOR_ROUND_CORNERS = 2;
    private ImagePostProcessor mPostProcessor;

    public ImageViewRemote(Context context) {
        super(context);
    }

    public ImageViewRemote(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAttributes(attrs);
    }

    public ImageViewRemote(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }

    private void setAttributes(AttributeSet attrs) {
        TypedArray values = getContext().obtainStyledAttributes(attrs, R.styleable.ImageViewRemote);

        setPostProcessor(
                values.getInt(
                        R.styleable.ImageViewRemote_postProcessor,
                        POST_PROCESSOR_NONE
                ),
                values.getInt(
                        R.styleable.ImageViewRemote_cornersRadius,
                        RoundCornersPostProcessor.DEFAULT_RADIUS
                )
        );

        setRemoteSrc(
                values.getString(
                        R.styleable.ImageViewRemote_remoteSrc
                )
        );

    }

    private void setPostProcessor(int postProcessorId, int cornerRadius) {

        switch (postProcessorId) {
            case POST_PROCESSOR_ROUNDED:
                mPostProcessor = new RoundPostProcessor();
                break;
            case POST_PROCESSOR_ROUND_CORNERS:
                mPostProcessor = new RoundCornersPostProcessor(cornerRadius);
                break;
            default:
                mPostProcessor = null;
        }
    }

    public boolean setRemoteSrc(String remoteSrc) {
        boolean isCorrectSrc = true;
        if (remoteSrc != null && remoteSrc.trim().length() > 0) {
            ImagePostProcessor processor = getPostProcessor();
            setImageBitmap(null);
            if (processor != null) {
                getImageLoader().displayImage(remoteSrc, this, null, null, processor);
            } else {
                getImageLoader().displayImage(remoteSrc, this, null, null);
            }

        } else {
            isCorrectSrc = false;
        }

        return isCorrectSrc;
    }

    public DefaultImageLoader getImageLoader() {
        return DefaultImageLoader.getInstance();
    }

    public ImagePostProcessor getPostProcessor() {
        return mPostProcessor;
    }
}
