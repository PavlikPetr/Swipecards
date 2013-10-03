package com.topface.topface.imageloader;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.topface.utils.Utils;

/**
 * Базовый процессор изображений, скругляющий углы
 */
public class RoundProcessor implements BitmapProcessor {

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = Utils.getRoundedBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight());
        }

        return bitmap;
    }
}
