package com.topface.topface.imageloader;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.topface.utils.Utils;

/**
 * Базовый процессор изображений, скругляющий углы
 */
public class RoundCornersProcessor implements BitmapProcessor {
    private float mRadius;
    public static final float DEFAULT_RADIUS = 3;

    public RoundCornersProcessor(float radius) {
        mRadius = radius;
    }

    public RoundCornersProcessor() {
        this(DEFAULT_RADIUS);
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = Utils.getScaleAndRoundBitmapIn(bitmap, bitmap.getWidth(), bitmap.getHeight(), mRadius);
        }

        return bitmap;
    }
}
