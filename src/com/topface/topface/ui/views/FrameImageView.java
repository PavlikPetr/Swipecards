package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.topface.topface.Recycle;

public class FrameImageView extends ImageView {
    // Data
    public boolean mOnlineState;


    public FrameImageView(Context context) {
        this(context, null);
    }


    public FrameImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Recycle.s_ProfilePhotoFrame.getWidth(), Recycle.s_ProfilePhotoFrame.getHeight());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Drawable canvasDrawable = getDrawable();
        if (canvasDrawable == null) {
            int x = (Recycle.s_ProfilePhotoFrame.getWidth() - Recycle.s_People.getWidth()) / 2;
            int y = (Recycle.s_ProfilePhotoFrame.getHeight() - Recycle.s_People.getHeight()) / 2;
            canvas.drawBitmap(Recycle.s_People, x, y, null);
        }
        // фрейм с тенюшкой
        canvas.drawBitmap(Recycle.s_ProfilePhotoFrame, 0, 0, null);
        // online state                      // ЗАРАНИЕ ПРОСЧИТАТЬ КООРДИНАТЫ
        canvas.drawBitmap(mOnlineState ? Recycle.s_Online : Recycle.s_Offline, (int) (getWidth() - Recycle.s_Online.getWidth() * 1.5), Recycle.s_Online.getHeight() / 2, null);
    }


}
