package com.topface.framework.imageloader.processor;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

/**
 * Create rounded bitmap without any paddings. We scale bitmap to container size and rounded it.
 */
public class InscribedCircleAvatarProcessor implements BitmapProcessor {
    private IViewSizeGetter mSizeGetter;
    private float mBorderWidth;
    private ColorStateList mBorderColor;

    public InscribedCircleAvatarProcessor(IViewSizeGetter sizeGetter, float borderWidth, ColorStateList borderColor) {
        mSizeGetter = sizeGetter;
        mBorderWidth = borderWidth;
        mBorderColor = borderColor;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            int viewHeight = mSizeGetter.getHeight();
            int viewWidth = mSizeGetter.getWidth();
            bitmap = BitmapUtils.getInscribedCircleBitmap(bitmap, viewWidth <= 0 ? bitmap.getWidth() : viewWidth, viewHeight <= 0 ? bitmap.getHeight() : viewHeight, mBorderWidth, mBorderColor);
        }
        return bitmap;
    }
}
