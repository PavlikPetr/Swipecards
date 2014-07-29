package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

/**
 * Базовый процессор изображений, круглая картинка
 */
public class RoundProcessor implements BitmapProcessor {

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = BitmapUtils.getRoundedBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight());
        }
        return bitmap;
    }
}
