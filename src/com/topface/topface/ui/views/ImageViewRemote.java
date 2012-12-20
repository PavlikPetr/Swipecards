package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
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
import com.topface.topface.utils.Debug;

import java.util.Timer;
import java.util.TimerTask;

public class ImageViewRemote extends ImageView {

    private static final int POST_PROCESSOR_NONE = 0;
    private static final int POST_PROCESSOR_ROUNDED = 1;
    private static final int POST_PROCESSOR_ROUND_CORNERS = 2;
    private static final int POST_PROCESSOR_MASK = 3;
    public static final int LOADING_COMPLETE = 0;
    private static final int LOADING_ERROR = 1;
    /**
     * Максимальное количество дополнительных попыток загрузки изображения
     */
    private static final int MAX_REPEAT_COUNT = 1;
    /**
     * Задержка перед следующей попыткой загрузки изображения
     */
    private static final long REPEAT_SCHEDULE = 2000;
    private ImagePostProcessor mPostProcessor;
    private String mCurrentSrc;
    private boolean mIsAnimationEnabled;
    /**
     * Счетчик попыток загрузить фотографию
     */
    private int mRepeatCounter = 0;
    /**
     * Объект таймера с задержкой запроса но
     */
    private Timer mRepeatTimer;
    /**
     * View, которое используется в качестве индикатора загрузки
     */
    private View mLoader;


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
        return setRemoteSrc(remoteSrc, handler, false);
    }

    public boolean setRemoteSrc(String remoteSrc, Handler handler, boolean isRepeat) {
        boolean isCorrectSrc = true;
        //Отменяем текущую загрузку, если ImageViewRemote уже используется для другого изображения
        getImageLoader().getImageLoader().cancelDisplayTask(this);
        //Если это не повторный запрос изображения, то сбрасываем счетчик повторов
        if (!isRepeat) {
            mRepeatCounter = 0;
        }
        //Отменяем таймер повтора загрузки изображения, если он есть
        if (mRepeatTimer != null) {
            mRepeatTimer.cancel();
            mRepeatTimer = null;
        }


        if (remoteSrc != null && remoteSrc.trim().length() > 0) {
            if (!remoteSrc.equals(mCurrentSrc)) {
                mCurrentSrc = remoteSrc;
                mIsAnimationEnabled = true;
            } else {
                mIsAnimationEnabled = false;
            }

            if (getDrawable() != null) {
                super.setImageBitmap(null);
            }
            getImageLoader().displayImage(remoteSrc, this, null, getListener(handler, remoteSrc), getPostProcessor());

        } else {
            isCorrectSrc = false;
            super.setImageBitmap(null);
            mCurrentSrc = null;
            mIsAnimationEnabled = true;
        }

        return isCorrectSrc;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm != null && mLoader != null) {
            mLoader.setVisibility(View.GONE);
        }
        //Показываем анимацию только в том случае, если ImageView видно пользователю
        if (bm != null && mIsAnimationEnabled) {
            startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        }

    }

    private ImageLoadingListener getListener(final Handler handler, final String remoteSrc) {
        return new RepeatImageLoadingListener(handler, remoteSrc);
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
        return setPhoto(photo, handler, null);
    }

    public boolean setPhoto(Photo photo, Handler handler, View loader) {
        boolean result;
        mLoader = loader;
        if (photo != null) {
            int size = Math.max(getLayoutParams().height, getLayoutParams().width);
            if (size > 0) {
                //noinspection SuspiciousNameCombination
                result = setRemoteSrc(photo.getSuitableLink(getLayoutParams().height, getLayoutParams().width), handler);
            } else {
                result = setRemoteSrc(photo.getSuitableLink(Photo.SIZE_960), handler);
            }
        } else {
            result = setRemoteSrc(null);
        }

        return result;
    }

    class RepeatImageLoadingListener extends SimpleImageLoadingListener {
        private final Handler mHandler;
        private final String mRemoteSrc;

        public RepeatImageLoadingListener(Handler handler, String remoteSrc) {
            mRemoteSrc = remoteSrc;
            mHandler = handler;
        }

        @Override
        public void onLoadingFailed(FailReason failReason) {
            if (FailReason.OUT_OF_MEMORY != failReason) {
                try {
                    if (mRepeatCounter >= MAX_REPEAT_COUNT) {
                        mRepeatCounter = 0;
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(LOADING_ERROR);
                        }
                        if (mLoader != null) {
                            mLoader.setVisibility(View.GONE);
                        }
                        setImageResource(R.drawable.im_photo_error);
                    } else {
                        mRepeatCounter++;
                        mRepeatTimer = new Timer();
                        mRepeatTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ImageViewRemote.this.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        setRemoteSrc(mRemoteSrc, mHandler, true);
                                    }
                                });
                            }
                        }, REPEAT_SCHEDULE);
                    }
                } catch (OutOfMemoryError e) {
                    Debug.error("ImageViewRemote:: OnLoadingFailed " + e.toString());
                }
            }
        }

        @Override
        public void onLoadingComplete(Bitmap loadedImage) {
            mRepeatCounter = 0;
            if (mHandler != null) {
                mHandler.sendEmptyMessage(LOADING_COMPLETE);
            }
        }

        @Override
        public void onLoadingCancelled() {
            mRepeatCounter = 0;
            if (mHandler != null) {
                mHandler.sendEmptyMessage(LOADING_ERROR);
            }
        }
    }
}
