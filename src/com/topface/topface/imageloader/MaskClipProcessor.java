package com.topface.topface.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.SparseArrayCompat;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

public class MaskClipProcessor implements BitmapProcessor {

    public static final int DEFAULT_MASK = R.drawable.user_mask_album;
    private Bitmap mMask;
    private static SparseArrayCompat<Bitmap> cachedMaskBitmaps = new SparseArrayCompat<Bitmap>();

    public MaskClipProcessor(int mask) {
        mMask = cachedMaskBitmaps.get(mask);
        if (mMask == null) {
            mMask = BitmapFactory.decodeResource(App.getContext().getResources(), mask);
            cachedMaskBitmaps.put(mask, mMask);
        }
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        try {
            bitmap = Utils.getRoundedCornerBitmapByMask(bitmap, mMask);
        } catch (OutOfMemoryError e) {
            Debug.error(e);
        }
        return bitmap;
    }

}
