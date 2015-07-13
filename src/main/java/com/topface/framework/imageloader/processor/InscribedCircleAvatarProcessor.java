package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

/**
 *
 */
public class InscribedCircleAvatarProcessor implements BitmapProcessor {
    private IViewSizeGetter mSizeGetter;

    public InscribedCircleAvatarProcessor(IViewSizeGetter sizeGetter) {
        mSizeGetter = sizeGetter;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            int viewHeight = mSizeGetter.getHeight();
            int viewWidth = mSizeGetter.getWidth();
            bitmap = BitmapUtils.getInscribedCircleBitmap(bitmap, viewWidth <= 0 ? bitmap.getWidth() : viewWidth, viewHeight <= 0 ? bitmap.getHeight() : viewHeight);
        }
        return bitmap;
    }
}
