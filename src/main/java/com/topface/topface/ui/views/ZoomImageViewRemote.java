package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * ImageViewRemote с возможностью зума
 */
@SuppressWarnings("UnusedDeclaration")
public class ZoomImageViewRemote extends ImageViewRemote {

    private PhotoViewAttacher mPhotoViewAttacher;

    public ZoomImageViewRemote(Context context) {
        super(context);
        initPhotoView();
    }

    private void initPhotoView() {
        mPhotoViewAttacher = new ZoomPhotoViewAttacher(this);
        mPhotoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                performClick();
            }
        });
    }

    public ZoomImageViewRemote(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPhotoView();
    }

    public ZoomImageViewRemote(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPhotoView();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        //Если установлено новое изображение, то нужно проинформировать об этом PhotoView
        mPhotoViewAttacher.update();
        mPhotoViewAttacher.setZoomable(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //Если при уничтожении ImageView не снять все коллбэки, то приложение упадет
        mPhotoViewAttacher.cleanup();
    }

    static class ZoomPhotoViewAttacher extends PhotoViewAttacher {

        /**
         * При двойном тапе на фотографии увеличиваем масштаб в 2 раза
         */
        public static final float DOUBLE_TAP_SCALE = 2f;

        public ZoomPhotoViewAttacher(ImageView imageView) {
            super(imageView);
        }


        /**
         * Переопределяем двойной тап по фотографии, в нашем случае зум двухуровневый, а не трех, как в оригинале
         */
        public final boolean onDoubleTap(MotionEvent ev) {
            try {
                float scale = getScale();
                float x = ev.getX();
                float y = ev.getY();

                float minScale = getMinimumScale();
                if (scale > minScale) {
                    setScale(minScale, x, y, true);
                } else {
                    setScale(DOUBLE_TAP_SCALE, x, y, true);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // Can sometimes happen when getX() and getY() is called
            }

            return true;
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        /*
         * В PhotoView наблюдается толи баг, то ли фича, из-за которой при попытке поставить ресурс с картинкой ошибки,
         * то он не расположен по центру, а в левом верхнем углу.
         * Дабы это побороть устаналвиваем зум (1 к 1) и после апдейта все отображается как нужно
         * В добавок отключаем зум
         */
        boolean isPhotoError = resId == getPhotoErrorResourceId();
        mPhotoViewAttacher.setZoomable(!isPhotoError);
        if (isPhotoError) {
            mPhotoViewAttacher.setScale(1, 0, 0, true);
        }
    }
}
