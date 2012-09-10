package com.topface.topface.imageloader;

import android.graphics.Bitmap;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.utils.Utils;

public class ClipPostProcessor extends ImagePostProcessor {

    private int mWidth;
    private int mHeight;
    private String mCacheKey;
    private static final String CACHE_KEY = "clip";

    public ClipPostProcessor(int width, int height) {
        mWidth = width;
        mHeight = height;
        mCacheKey = CACHE_KEY + mWidth + "x" + mHeight;
    }

    @Override
    public String getCachePrefix() {
        return mCacheKey;
    }

    @Override
    public Bitmap processBitmap(Bitmap bitmap) {
        return Utils.clipping(bitmap, mWidth, mHeight);
    }
}
