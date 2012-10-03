package com.topface.topface.imageloader;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.utils.Utils;

/**
 * Базовый процессор изображений, скругляющий углы
 */
public class RoundCornersPostProcessor extends ImagePostProcessor {
    public static final String CACHE_KEY = "roundCorners";
    private String mCacheKey;
    private int mRadius;
    public static final int DEFAULT_RADIUS = 5;

    public RoundCornersPostProcessor(int radius) {
        mCacheKey = CACHE_KEY + radius;
        mRadius = radius;
    }

    public RoundCornersPostProcessor() {
        this(DEFAULT_RADIUS);
    }

    @Override
    public String getCachePrefix() {
        return mCacheKey;
    }

    @Override
    public Bitmap processBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = Utils.getScaleAndRoundBitmapIn(bitmap, bitmap.getWidth(), bitmap.getHeight(), mRadius);
        }

        return bitmap;
    }
}
