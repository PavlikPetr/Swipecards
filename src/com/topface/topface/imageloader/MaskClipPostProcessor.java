package com.topface.topface.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nostra13.universalimageloader.postprocessors.ImagePostProcessor;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Utils;

import java.util.HashMap;

public class MaskClipPostProcessor extends ImagePostProcessor {

    public static final int DEFAULT_MASK = R.drawable.user_mask_album;
    private String mCacheKey;
    private static final String CACHE_KEY = "maskclip";
    private final Bitmap mMask;
    private static HashMap<Integer,Bitmap> cachedMaskBitmaps = new HashMap<Integer, Bitmap>();

    public MaskClipPostProcessor(int mask) {
        mCacheKey = CACHE_KEY;
        if(cachedMaskBitmaps.containsKey(mask)) {
            mMask = cachedMaskBitmaps.get(mask);
        } else {
            mMask = BitmapFactory.decodeResource(App.getContext().getResources(), mask);
            cachedMaskBitmaps.put(mask,mMask);
        }
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
