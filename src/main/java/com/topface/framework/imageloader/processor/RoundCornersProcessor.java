package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

/**
 * Базовый процессор изображений, скругляющий углы
 */
public class RoundCornersProcessor implements BitmapProcessor {
    public static final float DEFAULT_RADIUS = 3;
    private float mRadius;

    public RoundCornersProcessor(float radius) {
        mRadius = radius;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = BitmapUtils.getScaleAndRoundBitmapIn(bitmap, bitmap.getWidth(), bitmap.getHeight(), mRadius);
        }
        return bitmap;
    }
}
