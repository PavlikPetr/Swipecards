package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import java.lang.ref.WeakReference;

public class TfImageSpan extends ImageSpan{

    public static final int ALIGN_CENTER = 2;
    private final int mVerticalAlignment;

    public TfImageSpan(Context context, int resId, int verticalAlignment) {
       super(context, resId, verticalAlignment==ALIGN_CENTER?ALIGN_BASELINE:verticalAlignment);
        mVerticalAlignment = verticalAlignment;

    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
//        super.draw(canvas, text, start, end, x, top, y, bottom, paint);
        Drawable b = getCachedDrawable();

        canvas.save();

        int transY = bottom - b.getBounds().bottom;
        if (mVerticalAlignment == ALIGN_CENTER) {
            transY -= paint.getFontMetricsInt().descent;
            transY += 5;
        }

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;

        if (wr != null)
            d = wr.get();

        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<Drawable>(d);
        }

        return d;
    }

    private WeakReference<Drawable> mDrawableRef;
}
