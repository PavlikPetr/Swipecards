package com.topface.IllustratedTextView;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.style.ImageSpan;
import android.text.style.UpdateAppearance;

import java.lang.ref.WeakReference;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class TfImageSpan extends ImageSpan implements UpdateAppearance {

    public static final int ALIGN_CENTER = 2;
    private final int mVerticalAlignment;
    private final IllustratedTextView.IDelegatePressed mDelegatePressed;
    private static final int[] PRESSED = new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled};
    private static final int[] NORMAL = new int[]{android.R.attr.state_enabled};

    public TfImageSpan(Context context, int resId, int verticalAlignment, IllustratedTextView.IDelegatePressed delegate) {
        super(context, resId, verticalAlignment==ALIGN_CENTER?ALIGN_BASELINE:verticalAlignment);
        mVerticalAlignment = verticalAlignment;
        mDelegatePressed = delegate;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        if (mVerticalAlignment == ALIGN_CENTER) {
            Drawable b = getCachedDrawable();
            b.setState(mDelegatePressed.isPressed() ? PRESSED : NORMAL);
            canvas.save();
            int transY = bottom - b.getBounds().bottom;

                transY -= paint.getFontMetricsInt().descent;
                transY += 5;
            canvas.translate(x, transY);
            b.draw(canvas);
            canvas.restore();
        } else {
            super.draw(canvas, text, start, end, x, top, y, bottom, paint);
        }
    }

    private Drawable getCachedDrawable() {
        WeakReference<Drawable> wr = mDrawableRef;
        Drawable d = null;
        if (wr != null)
            d = wr.get();
        if (d == null) {
            d = getDrawable();
            mDrawableRef = new WeakReference<>(d);
        }
        return d;
    }

    private WeakReference<Drawable> mDrawableRef;
}
