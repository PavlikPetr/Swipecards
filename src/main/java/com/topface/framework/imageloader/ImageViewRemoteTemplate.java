package com.topface.framework.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.utils.Debug;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by kirussell on 26.06.2014.
 * Basic template for ImageViewRemote variations
 */
public abstract class ImageViewRemoteTemplate extends ImageView {
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
    protected BitmapProcessor mPostProcessor;
    private String mCurrentSrc;
    private boolean isFirstTime = true;

    protected int borderResId;
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
    protected int maxHeight;
    protected int maxWidth;

    public ImageViewRemoteTemplate(Context context) {
        super(context);
    }

    public ImageViewRemoteTemplate(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setAttributes(attrs);
    }

    public ImageViewRemoteTemplate(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        DefaultImageLoader.getInstance(getContext()).getImageLoader().cancelDisplayTask(this);
    }

    protected abstract void setAttributes(AttributeSet attrs);

    @SuppressWarnings("unused")
    protected abstract void setPostProcessor(int postProcessorId, float cornerRadius, int maskId);

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setResourceSrc(int resource) {
        DefaultImageLoader.getInstance(getContext()).getImageLoader().cancelDisplayTask(this);
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
        DefaultImageLoader.getInstance(getContext()).getImageLoader().cancelDisplayTask(this);
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
            getImageLoader(getContext()).displayImage(remoteSrc, this, null, getListener(handler, remoteSrc), getPostProcessor());
            if (borderResId != 0 && isFirstTime) {
                setImageResource(borderResId);
            }
        } else {
            isCorrectSrc = false;
            mCurrentSrc = null;
        }
        return isCorrectSrc;
    }

    public void setRemoteImageBitmap(Bitmap bitmap) {
        BitmapProcessor processor = getPostProcessor();
        if (processor != null) {
            setImageBitmap(processor.process(bitmap));
        } else {
            setImageBitmap(bitmap);
        }
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

    private DefaultImageLoader getImageLoader(Context context) {
        return DefaultImageLoader.getInstance(context);
    }

    private BitmapProcessor getPostProcessor() {
        return mPostProcessor;
    }

    public boolean setPhoto(IPhoto photo) {
        return setPhoto(photo, null, null);
    }

    public boolean setPhoto(IPhoto photo, Handler handler) {
        return setPhoto(photo, handler, null);
    }

    public boolean setPhoto(IPhoto photo, Handler handler, View loader) {
        boolean result = true;
        mLoader = loader;

        if (photo != null && !photo.isFake()) {
            int size = Math.max(getLayoutParams().height, getLayoutParams().width);
            if (size > 0) {
                //noinspection SuspiciousNameCombination
                result = setRemoteSrc(photo.getSuitableLink(getLayoutParams().height, getLayoutParams().width), handler);
            } else {
                result = setRemoteSrc(photo.getDefaultLink(), handler);
            }
        } else {
            setImageBitmap(null);
        }
        return result;
    }

    public int getPhotoErrorResourceId() {
        return 0;
    }

    private class RepeatImageLoadingListener extends SimpleImageLoadingListener {
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
                            setImageResource(getPhotoErrorResourceId());
                            mLoader.setVisibility(View.GONE);
                        }
                    } else {
                        mRepeatCounter++;
                        mRepeatTimer = new Timer();
                        mRepeatTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                ImageViewRemoteTemplate.this.post(new Runnable() {
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
                } catch (Exception e) {
                    Debug.error(e);
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

    public int getImageMaxHeight() {
        return maxHeight;
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getImageMaxWidth() {
        return maxWidth;
    }
}
