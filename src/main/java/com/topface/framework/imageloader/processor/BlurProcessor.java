package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

/**
 * Create rounded bitmap without any paddings. We scale bitmap to container size and rounded it.
 */
public class BlurProcessor implements BitmapProcessor {
    private int mBlurIterationCount;

    public BlurProcessor(int blurIterationCount) {
        mBlurIterationCount = blurIterationCount;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = BitmapUtils.getBluredBitmap(bitmap, mBlurIterationCount);
        }
        return bitmap;
    }
}
