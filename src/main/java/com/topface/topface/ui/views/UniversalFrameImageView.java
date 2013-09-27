package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.topface.topface.R;

public class UniversalFrameImageView extends ImageViewRemote {

    private Bitmap mFrame;

    public UniversalFrameImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray styled = context.obtainStyledAttributes(attrs, R.styleable.UniversalFrameImageView);
        int frameSrc = styled.getResourceId(R.styleable.UniversalFrameImageView_frameSrc, 0);

        if (frameSrc > 0) {
            mFrame = BitmapFactory.decodeResource(context.getResources(), frameSrc);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mFrame != null) {
            setMeasuredDimension(mFrame.getWidth(), mFrame.getHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFrame != null) {
            canvas.drawBitmap(mFrame, 0, 0, null);
        }
    }
}