package com.topface.framework.imageloader.processor;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.framework.imageloader.BitmapUtils;

public class SquareProcessor implements BitmapProcessor {

    private IViewSizeGetter mSizeGetter;

    public SquareProcessor(IViewSizeGetter sizeGetter) {
        this.mSizeGetter = sizeGetter;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        int width = mSizeGetter.getWidth() == 0 ? bitmap.getWidth() : mSizeGetter.getWidth();
        return BitmapUtils.squareBitmap(bitmap, width);
    }
}
