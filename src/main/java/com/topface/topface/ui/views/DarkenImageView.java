package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by kirussell on 14.05.2014.
 * ImageView which can draw with color(dark) in front of image
 * To set opacity for color use {@link #setDarkenFrameOpacity(float)}
 */
public class DarkenImageView extends ImageView {

    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;

    private int mScrimColor = DEFAULT_SCRIM_COLOR;
    private float mScrimOpacity;
    private Paint mScrimPaint = new Paint();

    public DarkenImageView(Context context) {
        super(context);
    }

    public DarkenImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DarkenImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Changes opacity of the front color frame
     *
     * @param opacity value from 0 to 1.0f
     */
    public void setDarkenFrameOpacity(float opacity) {
        mScrimOpacity = opacity;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mScrimOpacity > 0) {
            final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
            final int imag = (int) (baseAlpha * mScrimOpacity);
            final int color = imag << 24 | (mScrimColor & 0xffffff);
            mScrimPaint.setColor(color);
            canvas.drawRect(0, 0, getWidth(), getHeight(), mScrimPaint);
        }
    }
}
