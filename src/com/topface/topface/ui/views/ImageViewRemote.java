package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.imageloader.MaskClipPostProcessor;
import com.topface.topface.imageloader.RoundCornersPostProcessor;
import com.topface.topface.imageloader.RoundPostProcessor;

public class ImageViewRemote extends ImageView {

    private static final int POST_PROCESSOR_NONE = 0;
    private static final int POST_PROCESSOR_ROUNDED = 1;
    private static final int POST_PROCESSOR_ROUND_CORNERS = 2;
    private static final int POST_PROCESSOR_MASK = 3;
    public static final int LOADING_COMPLETE = 0;
    private static final int LOADING_ERROR = 1;
    private ImagePostProcessor mPostProcessor;
    Animation mAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);


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
                values.getDimension(
                        R.styleable.ImageViewRemote_cornersRadius,
                        RoundCornersPostProcessor.DEFAULT_RADIUS
                ),
                values.getResourceId(
                        R.styleable.ImageViewRemote_clipMask,
                        MaskClipPostProcessor.DEFAULT_MASK
                )
        );

        setRemoteSrc(
                values.getString(
                        R.styleable.ImageViewRemote_remoteSrc
                )
        );

    }

    private void setPostProcessor(int postProcessorId, float cornerRadius, int maskId) {

        switch (postProcessorId) {
            case POST_PROCESSOR_ROUNDED:
                mPostProcessor = new RoundPostProcessor();
                break;
            case POST_PROCESSOR_ROUND_CORNERS:
                mPostProcessor = new RoundCornersPostProcessor(cornerRadius);
                break;
            case POST_PROCESSOR_MASK:
                mPostProcessor = new MaskClipPostProcessor(maskId);
                break;
            default:
                mPostProcessor = null;
        }
    }

    public boolean setRemoteSrc(String remoteSrc, Handler handler) {
        boolean isCorrectSrc = true;
        if (remoteSrc != null && remoteSrc.trim().length() > 0) {
            ImagePostProcessor processor = getPostProcessor();
            setImageBitmap(null);
            if (processor != null) {
                getImageLoader().displayImage(remoteSrc, this, null, getListener(handler), processor);
            } else {
                getImageLoader().displayImage(remoteSrc, this, null, getListener(handler));
            }

        } else {
            isCorrectSrc = false;
            setImageBitmap(null);
        }

        return isCorrectSrc;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        //Показываем анимацию только в том случае, если ImageView видно пользователю
        if (isShown()) {
            startAnimation(mAnimation);
        }

    }

    private ImageLoadingListener getListener(final Handler handler) {
        ImageLoadingListener listener = null;
        if (handler != null) {
            listener = new SimpleImageLoadingListener() {
                @Override
                public void onLoadingFailed(FailReason failReason) {
                    handler.sendEmptyMessage(LOADING_ERROR);
                    setImageResource(R.drawable.im_photo_error);
                }

                @Override
                public void onLoadingComplete(Bitmap loadedImage) {
                    handler.sendEmptyMessage(LOADING_COMPLETE);
                }

                @Override
                public void onLoadingCancelled() {
                    handler.sendEmptyMessage(LOADING_ERROR);
                }
            };
        }
        return listener;
    }

    public boolean setRemoteSrc(String remoteSrc) {
        return setRemoteSrc(remoteSrc, null);
    }

    public DefaultImageLoader getImageLoader() {
        return DefaultImageLoader.getInstance();
    }

    public ImagePostProcessor getPostProcessor() {
        return mPostProcessor;
    }

    public boolean setPhoto(Photo photo) {
        return setPhoto(photo, null);
    }

    public boolean setPhoto(Photo photo, Handler handler) {
        boolean result;
        if (photo != null) {
            int size = Math.max(getLayoutParams().height, getLayoutParams().width);
            if (size > 0) {
                result = setRemoteSrc(photo.getSuitableLink(size), handler);
            } else {
                result = setRemoteSrc(photo.getSuitableLink(Photo.SIZE_960), handler);
            }
        } else {
            result = setRemoteSrc(null);
        }

        return result;
    }
}
