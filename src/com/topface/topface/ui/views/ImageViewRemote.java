package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.imageloader.*;
import com.topface.topface.utils.Debug;

import java.util.Timer;
import java.util.TimerTask;

public class ImageViewRemote extends ImageView {

    private static final int POST_PROCESSOR_NONE = 0;
    private static final int POST_PROCESSOR_ROUNDED = 1;
    private static final int POST_PROCESSOR_ROUND_CORNERS = 2;
    private static final int POST_PROCESSOR_MASK = 3;
    private static final int POST_PROCESSOR_CIRCUMCIRCLE = 4;
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
    public static final int PHOTO_ERROR_RESOURCE = R.drawable.im_photo_error;
    private BitmapProcessor mPostProcessor;
    private String mCurrentSrc;
    private boolean isFirstTime = true;

    private int borderResId;
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


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        DefaultImageLoader.getInstance().getImageLoader().cancelDisplayTask(this);
    }

    private void setAttributes(AttributeSet attrs) {
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


        setRemoteSrc(
                values.getString(
                        R.styleable.ImageViewRemote_remoteSrc
                )
        );
    }

    private void setPostProcessor(int postProcessorId, float cornerRadius, int maskId) {

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
            default:
                mPostProcessor = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setResourceSrc(int resource) {
        DefaultImageLoader.getInstance().getImageLoader().cancelDisplayTask(this);
        setImageResource(resource);
    }

    public boolean setRemoteSrc(String remoteSrc, Handler handler) {
        return setRemoteSrc(remoteSrc, handler, false);
    }

    public boolean setRemoteSrc(String remoteSrc, Handler handler, boolean isRepeat) {
        boolean isCorrectSrc = true;
        //Отменяем текущую загрузку, если ImageViewRemote уже используется для другого изображения
        //Используем метод getInstance без указания контекста. т.к. отмена загрузки должна происходить всегда,
        //Даже если ImageViewRemote уже вне контекста активити
        //noinspection deprecation
        DefaultImageLoader.getInstance().getImageLoader().cancelDisplayTask(this);
        //Если это не повторный запрос изображения, то сбрасываем счетчик повторов
        if (!isRepeat) {
            mRepeatCounter = 0;
        }
        //Отменяем таймер повтора загрузки изображения, если он есть
        if (mRepeatTimer != null) {
            mRepeatTimer.cancel();
            mRepeatTimer = null;
        }

        //Если ссылка не пустая и мы не патаемся скачать уже установленный в View изображение, то начинаем загрузку
        if (!TextUtils.isEmpty(remoteSrc)) {
            if (!remoteSrc.equals(mCurrentSrc)) {
                mCurrentSrc = remoteSrc;
            }
            getImageLoader().displayImage(remoteSrc, this, null, getListener(handler, remoteSrc), getPostProcessor());
            if (borderResId != 0 && isFirstTime) {
                setImageResource(borderResId);
            }
        } else {
            isCorrectSrc = false;
            mCurrentSrc = null;
        }
        return isCorrectSrc;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        if (bm == null) {
            mCurrentSrc = null;
        }
        if (bm != null && mLoader != null) {
            mLoader.setVisibility(View.GONE);
        }
    }

    private ImageLoadingListener getListener(final Handler handler, final String remoteSrc) {
        return new RepeatImageLoadingListener(handler, remoteSrc);
    }

    public boolean setRemoteSrc(String remoteSrc) {
        return setRemoteSrc(remoteSrc, null);
    }

    public DefaultImageLoader getImageLoader() {
        //noinspection deprecation
        return DefaultImageLoader.getInstance();
    }

    public BitmapProcessor getPostProcessor() {
        return mPostProcessor;
    }

    public boolean setPhoto(Photo photo) {
        return setPhoto(photo, null, null);
    }

    public boolean setPhoto(Photo photo, Handler handler) {
        return setPhoto(photo, handler, null);
    }

    public boolean setPhoto(Photo photo, Handler handler, View loader) {
        boolean result = true;
        mLoader = loader;

        if (photo != null) {
            if (!photo.isFake()) {
                int size = Math.max(getLayoutParams().height, getLayoutParams().width);
                if (size > 0) {
                    //noinspection SuspiciousNameCombination
                    result = setRemoteSrc(photo.getSuitableLink(getLayoutParams().height, getLayoutParams().width), handler);
                } else {
                    result = setRemoteSrc(photo.getSuitableLink(Photo.SIZE_960), handler);
                }
            }
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
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
            isFirstTime = true;
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            super.onLoadingFailed(imageUri, view, failReason);
            if (FailReason.FailType.OUT_OF_MEMORY != failReason.getType()) {
                try {
                    if (mRepeatCounter >= MAX_REPEAT_COUNT) {
                        mRepeatCounter = 0;
                        if (mHandler != null) {
                            mHandler.sendEmptyMessage(LOADING_ERROR);
                        }
                        if (mLoader != null) {
                            setImageResource(PHOTO_ERROR_RESOURCE);
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
                    }
                } catch (OutOfMemoryError e) {
                    Debug.error("ImageViewRemote:: OnLoadingFailed " + e.toString());
                }
            }
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            super.onLoadingComplete(imageUri, view, loadedImage);

            mRepeatCounter = 0;
            isFirstTime = false;
            if (mLoader != null) {
                mLoader.setVisibility(View.GONE);
            }
            if (mHandler != null) {
                Message msg = new Message();
                msg.what = LOADING_COMPLETE;
                msg.arg1 = loadedImage.getWidth();
                msg.arg2 = loadedImage.getHeight();
                mHandler.sendMessage(msg);

            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
            super.onLoadingCancelled(imageUri, view);
            mRepeatCounter = 0;
            if (mHandler != null) {
                mHandler.sendEmptyMessage(LOADING_ERROR);
            }
        }
    }
}
