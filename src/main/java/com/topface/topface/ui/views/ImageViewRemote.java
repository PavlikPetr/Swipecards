package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.topface.framework.imageloader.ImageViewRemoteTemplate;
import com.topface.framework.imageloader.processor.CircumCircleProcessor;
import com.topface.framework.imageloader.processor.RoundCornersProcessor;
import com.topface.framework.imageloader.processor.RoundProcessor;
import com.topface.topface.R;
import com.topface.topface.utils.imageloader.LeftMenuClipProcessor;
import com.topface.topface.utils.imageloader.MaskClipProcessor;


public class ImageViewRemote extends ImageViewRemoteTemplate {
    protected static final int POST_PROCESSOR_NONE = 0;
    private static final int POST_PROCESSOR_ROUNDED = 1;
    private static final int POST_PROCESSOR_ROUND_CORNERS = 2;
    private static final int POST_PROCESSOR_MASK = 3;
    private static final int POST_PROCESSOR_CIRCUMCIRCLE = 4;
    private static final int POST_PROCESSOR_LEFTMENUCLIP = 5;

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

        setPostProcessor(
                values.getInt(
                        R.styleable.ImageViewRemote_postProcessor,
                        POST_PROCESSOR_NONE
                ),
                values.getDimension(
                        R.styleable.ImageViewRemote_cornersRadius,
                        RoundCornersProcessor.DEFAULT_RADIUS
                ),
                values.getResourceId(
                        R.styleable.ImageViewRemote_clipMask,
                        MaskClipProcessor.DEFAULT_MASK
                )
        );


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
    protected void setPostProcessor(int postProcessorId, float cornerRadius, int maskId) {
        if (!isInEditMode()) {
            switch (postProcessorId) {
                case POST_PROCESSOR_ROUNDED:
                    mPostProcessor = new RoundProcessor();
                    break;
                case POST_PROCESSOR_ROUND_CORNERS:
                    mPostProcessor = new RoundCornersProcessor(cornerRadius);
                    break;
                case POST_PROCESSOR_MASK:
                    mPostProcessor = new MaskClipProcessor(maskId, borderResId);
                    break;
                case POST_PROCESSOR_CIRCUMCIRCLE:
                    mPostProcessor = new CircumCircleProcessor();
                    break;
                case POST_PROCESSOR_LEFTMENUCLIP:
                    mPostProcessor = new LeftMenuClipProcessor();
                    break;
                default:
                    mPostProcessor = null;
            }
        }
    }

    @Override
    public int getPhotoErrorResourceId() {
        return R.drawable.im_photo_error;
    }
}
