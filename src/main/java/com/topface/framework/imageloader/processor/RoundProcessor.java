package com.topface.framework.imageloader.processor;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

/**
 * Базовый процессор изображений, круглая картинка
 */
public class RoundProcessor implements BitmapProcessor {

    private float mBorderWidth;
    private ColorStateList mBorderColor;

    public RoundProcessor(float borderWidth, ColorStateList borderColor) {
        mBorderWidth = borderWidth;
        mBorderColor = borderColor;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap = BitmapUtils.getRoundBitmap(bitmap, 1.2f, mBorderWidth, mBorderColor);
        }
        return bitmap;
    }
}
