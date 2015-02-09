package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

public class SquareProcessor implements BitmapProcessor {

    public SquareProcessor() {
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        return BitmapUtils.clipAndScaleBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight());
    }
}
