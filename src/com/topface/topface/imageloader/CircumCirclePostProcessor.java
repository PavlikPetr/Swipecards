package com.topface.topface.imageloader;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.utils.Utils;

/**
 * Базовый процессор изображений, скругляющий углы
 */
public class CircumCirclePostProcessor extends ImagePostProcessor {
    public static final String CACHE_KEY = "circumcircle";
    private String mCacheKey;

    public CircumCirclePostProcessor() {
        mCacheKey = CACHE_KEY;
    }

    @Override
    public String getCachePrefix() {
        return mCacheKey;
    }

    @Override
    public Bitmap processBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = Utils.getScaleAndRoundBitmapOut(bitmap, bitmap.getWidth(), bitmap.getWidth(), 1.2f);
        }

        return bitmap;
    }
}
