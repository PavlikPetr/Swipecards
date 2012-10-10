package com.topface.topface.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;

public class MaskClipPostProcessor extends ImagePostProcessor {

    public static final int DEFAULT_MASK = R.drawable.user_mask_album;
    private String mCacheKey;
    private static final String CACHE_KEY = "maskclip";
    private final Bitmap mMask;

    public MaskClipPostProcessor(int mask) {
        mCacheKey = CACHE_KEY;
        mMask = BitmapFactory.decodeResource(App.getContext().getResources(), mask);
    }

    @Override
    public String getCachePrefix() {
        return mCacheKey;
    }

    @Override
    public Bitmap processBitmap(Bitmap bitmap) {
        return Utils.getRoundedCornerBitmapByMask(bitmap, mMask);
    }
}
