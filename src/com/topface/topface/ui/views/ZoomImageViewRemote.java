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
        mPhotoViewAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                callOnClick();
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

                float minScale = getMinScale();
                if (scale > minScale) {
                    zoomTo(minScale, x, y);
                } else {
                    zoomTo(DOUBLE_TAP_SCALE, x, y);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // Can sometimes happen when getX() and getY() is called
            }

            return true;
        }
    }

}
