package com.topface.topface.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.topface.topface.R
import com.topface.topface.utils.AnimationUtils
import uk.co.senab.photoview.PhotoViewAttacher

/**
 * ImageViewRemote с возможностью зума
 */
class ZoomImageView : ImageView {

    private var mPhotoViewAttacher: PhotoViewAttacher? = null
    private var mAnimator: ValueAnimator? = null

    constructor(context: Context) : super(context) {
        initPhotoView()
    }

    private fun initPhotoView() {
        mPhotoViewAttacher = ZoomPhotoViewAttacher(this)
        mPhotoViewAttacher!!.setOnViewTapListener { view, x, y -> performClick() }
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initPhotoView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initPhotoView()
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        // try to animate appearing
//        if (isNeedAnimateOnAppear) {
        if (true) {
            mAnimator = AnimationUtils.createAppearingImageAnimator(this)
            mAnimator!!.start()
        }
        // reset flag about animation, for image reusing
//        resetNeedAnimateOnAppear()
        //Если установлено новое изображение, то нужно проинформировать об этом PhotoView
        mPhotoViewAttacher!!.update()
        mPhotoViewAttacher!!.setZoomable(true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //Если при уничтожении ImageView не снять все коллбэки, то приложение упадет
        mPhotoViewAttacher!!.cleanup()
    }

    internal class ZoomPhotoViewAttacher(imageView: ImageView) : PhotoViewAttacher(imageView) {


        /**
         * Переопределяем двойной тап по фотографии, в нашем случае зум двухуровневый, а не трех, как в оригинале
         */
        fun onDoubleTap(ev: MotionEvent): Boolean {
            try {
                val scale = scale
                val x = ev.x
                val y = ev.y

                val minScale = minimumScale
                if (scale > minScale) {
                    setScale(minScale, x, y, true)
                } else {
                    setScale(DOUBLE_TAP_SCALE, x, y, true)
                }
            } catch (e: ArrayIndexOutOfBoundsException) {
                // Can sometimes happen when getX() and getY() is called
            }

            return true
        }

        companion object {

            /**
             * При двойном тапе на фотографии увеличиваем масштаб в 2 раза
             */
            val DOUBLE_TAP_SCALE = 2f
        }
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        /*
         * В PhotoView наблюдается толи баг, то ли фича, из-за которой при попытке поставить ресурс с картинкой ошибки,
         * то он не расположен по центру, а в левом верхнем углу.
         * Дабы это побороть устаналвиваем зум (1 к 1) и после апдейта все отображается как нужно
         * В добавок отключаем зум
         */
        val isPhotoError = resId == R.drawable.im_photo_error
        mPhotoViewAttacher!!.setZoomable(!isPhotoError)
        if (isPhotoError) {
            mPhotoViewAttacher!!.setScale(1f, 0f, 0f, true)
        }
    }

//    override fun stopAppearingAnimation() {
//        super.stopAppearingAnimation()
//        if (mAnimator != null) {
//            mAnimator!!.cancel()
//        }
//    }
}
