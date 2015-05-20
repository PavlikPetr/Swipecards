package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

/**
 * Скругление аватарки пользователя
 */
public class RoundAvatarProcessor implements BitmapProcessor {
    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = BitmapUtils.getRoundAvatarBitmap(bitmap);
        }
        return bitmap;
    }
}
