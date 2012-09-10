package com.topface.topface.imageloader;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.utils.Utils;

/**
 * Базовый процессор изображений, скругляющий углы
 */
public class RoundPostProcessor extends ImagePostProcessor {
    public static final String CACHE_KEY = "round";
    private int mRadius;
    private String mCacheKey;

    public RoundPostProcessor(int radius) {
        mRadius = radius;
        mCacheKey = CACHE_KEY + mRadius;
    }

    @Override
    public String getCachePrefix() {
        return mCacheKey;
    }

    @Override
    public Bitmap processBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = Utils.getRoundedCornerBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), mRadius);
        }

        return bitmap;
    }
}
