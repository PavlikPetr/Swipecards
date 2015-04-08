package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

/**
 * Crops rectangle image to square starting from top left corner
 */
public class CropProcessor implements BitmapProcessor {
    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = BitmapUtils.squareCrop(bitmap);
        }
        return bitmap;
    }
}
