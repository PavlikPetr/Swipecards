package com.topface.topface.imageloader;

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
//        int height = (int) res.getDimension(R.dimen.left_menu_background_height);

        int tmpHeight = bitmap.getHeight() * width / bitmap.getWidth();
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, tmpHeight, true);
        bitmap.recycle();
        return scaled;
//        int dstY = scaled.getHeight() - height;
//        if (dstY > 0) {
//            Bitmap cropped = Bitmap.createBitmap(scaled, 0, dstY, width, height);
//            scaled.recycle();
//            return cropped;
//        } else {
//            return scaled;
//        }
    }
}
