package com.topface.topface.utils.imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.topface.topface.App;
import com.topface.topface.R;

public class LeftMenuClipProcessor implements BitmapProcessor {
    @Override
    public Bitmap process(Bitmap bitmap) {
        Resources res = App.getContext().getResources();
        int width = (int) res.getDimension(R.dimen.left_menu_width);
        int tmpHeight = bitmap.getHeight() * width / bitmap.getWidth();
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, tmpHeight, true);
        bitmap.recycle();
        return scaled;
    }
}
