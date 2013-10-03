package com.topface.topface.imageloader;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;

/**
 * Базовый процессор изображений, скругляющий углы
 */
public class CircumCircleProcessor implements BitmapProcessor {

    @Override
    public Bitmap process(Bitmap bitmap) {
        try {
            if (bitmap != null) {
                bitmap = Utils.getScaleAndRoundBitmapOut(bitmap, bitmap.getWidth(), bitmap.getWidth(), 1.2f);
            }
        } catch (OutOfMemoryError e) {
            Debug.error(e);
        }

        return bitmap;
    }
}
