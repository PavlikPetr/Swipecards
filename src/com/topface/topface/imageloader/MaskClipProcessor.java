package com.topface.topface.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

import java.util.HashMap;

public class MaskClipProcessor implements BitmapProcessor {

    public static final int DEFAULT_MASK = R.drawable.user_mask_album;
    private final Bitmap mMask;
    private static HashMap<Integer, Bitmap> cachedMaskBitmaps = new HashMap<Integer, Bitmap>();

    public MaskClipProcessor(int mask) {
        if (cachedMaskBitmaps.containsKey(mask)) {
            mMask = cachedMaskBitmaps.get(mask);
        } else {
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
