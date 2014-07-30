package com.topface.topface.utils.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.SparseArrayCompat;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;

public class MaskClipProcessor implements BitmapProcessor {

    public static final int DEFAULT_MASK = R.drawable.user_mask_album;
    private Bitmap mMask;
    private Bitmap mBorder;
    private static SparseArrayCompat<Bitmap> cachedMaskBitmaps = new SparseArrayCompat<>();

    public MaskClipProcessor(int mask, int border) {
        if (cachedMaskBitmaps != null) {
            mMask = cachedMaskBitmaps.get(mask);
        }
        if (mMask == null) {
            mMask = BitmapFactory.decodeResource(App.getContext().getResources(), mask);
            cachedMaskBitmaps.put(mask, mMask);
        }
        if (mBorder == null && border != 0) {
            mBorder = BitmapFactory.decodeResource(App.getContext().getResources(), border);
        }
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        try {
            bitmap = BitmapUtils.getRoundedCornerBitmapByMask(bitmap, mMask, mBorder);

        } catch (OutOfMemoryError e) {
            Debug.error(e);
        }
        return bitmap;
    }

}
