package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.ImageViewRemoteTemplate;
import com.topface.framework.imageloader.processor.RoundCornersProcessor;
import com.topface.framework.imageloader.processor.RoundProcessor;
import com.topface.framework.imageloader.processor.SquareProcessor;
import com.topface.topface.R;
import com.topface.topface.utils.imageloader.LeftMenuClipProcessor;
import com.topface.topface.utils.imageloader.MaskClipProcessor;


public class ImageViewRemote extends ImageViewRemoteTemplate {
    protected static final int POST_PROCESSOR_NONE = 0;
    private static final int POST_PROCESSOR_ROUNDED = 1;
    private static final int POST_PROCESSOR_ROUND_CORNERS = 2;
    private static final int POST_PROCESSOR_MASK = 3;
    private static final int POST_PROCESSOR_LEFTMENUCLIP = 4;
    private static final int POST_PROCESSOR_SQUARED = 5;

    public ImageViewRemote(Context context) {
        super(context);
    }

    public ImageViewRemote(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ImageViewRemote(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void setAttributes(AttributeSet attrs) {
        TypedArray values = getContext().obtainStyledAttributes(attrs, R.styleable.ImageViewRemote);

        borderResId = values.getResourceId(R.styleable.ImageViewRemote_border, 0);

        float cornerRadius = values.getDimension(
                R.styleable.ImageViewRemote_cornersRadius,
                RoundCornersProcessor.DEFAULT_RADIUS);
        int maskId = values.getResourceId(
                R.styleable.ImageViewRemote_clipMask,
                MaskClipProcessor.DEFAULT_MASK);
        int processorId = values.getInt(
                R.styleable.ImageViewRemote_preProcessor,
                POST_PROCESSOR_NONE);

        mPreProcessor = setProcessor(processorId, cornerRadius, maskId);

        processorId = values.getInt(
                R.styleable.ImageViewRemote_postProcessor,
                POST_PROCESSOR_NONE);

        mPostProcessor = setProcessor(processorId, cornerRadius, maskId);


        if (!isInEditMode()) {
            setRemoteSrc(
                    values.getString(
                            R.styleable.ImageViewRemote_remoteSrc
                    )
            );
        }
        maxHeight = values.getDimensionPixelSize(R.styleable.ImageViewRemote_android_maxHeight, 0);
        maxWidth = values.getDimensionPixelSize(R.styleable.ImageViewRemote_android_maxWidth, 0);
        values.recycle();
    }

    @Override
    protected BitmapProcessor setProcessor(int processorId, float cornerRadius, int maskId) {
        if (!isInEditMode()) {
            switch (processorId) {
                case POST_PROCESSOR_ROUNDED:
                    return new RoundProcessor();
                case POST_PROCESSOR_ROUND_CORNERS:
                    return new RoundCornersProcessor(cornerRadius);
                case POST_PROCESSOR_MASK:
                    return new MaskClipProcessor(maskId, borderResId);
                case POST_PROCESSOR_LEFTMENUCLIP:
                    return new LeftMenuClipProcessor();
                case POST_PROCESSOR_SQUARED:
                    return new SquareProcessor();
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public int getPhotoErrorResourceId() {
        return R.drawable.im_photo_error;
    }
}
