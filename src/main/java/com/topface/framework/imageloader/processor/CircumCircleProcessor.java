package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;
import com.topface.framework.utils.Debug;

/**
 * Базовый процессор изображений, скругляющий углы
 */
public class CircumCircleProcessor implements BitmapProcessor {

    @Override
    public Bitmap process(Bitmap bitmap) {
        try {
            if (bitmap != null) {
                bitmap = BitmapUtils.getScaleAndRoundBitmapOut(bitmap, bitmap.getWidth(), bitmap.getWidth(), 1.2f);
            }
        } catch (OutOfMemoryError e) {
            Debug.error(e);
        }

        return bitmap;
    }
}
