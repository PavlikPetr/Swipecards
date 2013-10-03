package com.topface.topface.imageloader;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.topface.utils.Utils;

@SuppressWarnings("UnusedDeclaration")
public class ClipProcessor implements BitmapProcessor {

    private int mWidth;
    private int mHeight;

    public ClipProcessor(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        return Utils.clipAndScaleBitmap(bitmap, mWidth, mHeight);
    }
}
