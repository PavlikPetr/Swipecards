package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.ImageViewRemoteTemplate;
import com.topface.framework.imageloader.processor.BlurProcessor;
import com.topface.framework.imageloader.processor.CropProcessor;
import com.topface.framework.imageloader.processor.IViewSizeGetter;
import com.topface.framework.imageloader.processor.InscribedCircleAvatarProcessor;
import com.topface.framework.imageloader.processor.RoundAvatarProcessor;
import com.topface.framework.imageloader.processor.RoundCornersProcessor;
import com.topface.framework.imageloader.processor.RoundProcessor;
import com.topface.framework.imageloader.processor.SquareProcessor;
import com.topface.topface.R;
import com.topface.topface.utils.imageloader.LeftMenuClipProcessor;
import com.topface.topface.utils.imageloader.MaskClipProcessor;


public class ImageViewRemote extends ImageViewRemoteTemplate implements IViewSizeGetter {
    protected static final int POST_PROCESSOR_NONE = 0;
    private static final int POST_PROCESSOR_ROUNDED = 1;
    private static final int POST_PROCESSOR_ROUND_CORNERS = 2;
    private static final int POST_PROCESSOR_MASK = 3;
    private static final int POST_PROCESSOR_LEFTMENUCLIP = 4;
    private static final int POST_PROCESSOR_SQUARED = 5;
    private static final int POST_PROCESSOR_CROPED = 6;
    private static final int POST_PROCESSOR_ROUND_AVATAR = 7;
    private static final int POST_PROCESSOR_INSCRIBED_AVATAR_IN_CIRCLE = 8;
    private static final int POST_PROCESSOR_BLUR = 9;

    private static final int BLUR_RADIUS_DEFAULT = 10;

    private int mBlurRadius;

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

        mBlurRadius = values.getInt(R.styleable.ImageViewRemote_blurRadius, BLUR_RADIUS_DEFAULT);

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

        mPreProcessor = createProcessor(processorId, cornerRadius, maskId);

        processorId = values.getInt(
                R.styleable.ImageViewRemote_postProcessor,
                POST_PROCESSOR_NONE);

        mPostProcessor = createProcessor(processorId, cornerRadius, maskId);


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
    protected BitmapProcessor createProcessor(int processorId, float cornerRadius, int maskId) {
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
                    return new SquareProcessor(this);
                case POST_PROCESSOR_CROPED:
                    return new CropProcessor();
                case POST_PROCESSOR_ROUND_AVATAR:
                    return new RoundAvatarProcessor();
                case POST_PROCESSOR_INSCRIBED_AVATAR_IN_CIRCLE:
                    return new InscribedCircleAvatarProcessor(this);
                case POST_PROCESSOR_BLUR:
                    return new BlurProcessor(mBlurRadius);
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
