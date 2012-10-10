package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import com.topface.topface.Recycle;
import com.topface.topface.utils.Debug;

public class ThumbView extends ImageViewRemote {
    // Data
    public int percent;
    public int age;
    public String name;
    public boolean online;
    // Constants
    public static Paint s_PaintState;
    public static Paint s_PaintLine;
    public static Paint s_PaintText;

    //---------------------------------------------------------------------------
    public ThumbView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setBackgroundColor(Color.TRANSPARENT);

        if (s_PaintState == null) {
            s_PaintState = new Paint();
            s_PaintState.setColor(Color.WHITE);
        }

        if (s_PaintLine == null) {
            s_PaintLine = new Paint();
            s_PaintLine.setColor(Color.BLACK);
            s_PaintLine.setAlpha(154);
        }

        if (s_PaintText == null) {
            s_PaintText = new Paint();
            s_PaintText.setColor(Color.WHITE);
            s_PaintText.setAntiAlias(true);
            s_PaintText.setTextSize(16);
        }
    }

    //---------------------------------------------------------------------------
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int _x = (width - Recycle.s_People.getWidth()) / 2;
        int _y = (height - Recycle.s_People.getHeight()) / 2;
        canvas.drawBitmap(Recycle.s_People, _x, _y, null);

        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            Debug.error(e);
        }

        Rect lineRect = new Rect(0, height - 32, width, height);
        canvas.drawRect(lineRect, s_PaintLine);

        // tops
        if (percent != 0) {
            float x = lineRect.left + Recycle.s_Heart.getWidth() / 2;
            float y = (lineRect.height() - Recycle.s_Heart.getHeight()) / 2;

            // heart
            canvas.drawBitmap(Recycle.s_Heart, x, lineRect.top + y, s_PaintState);
            x = x * 2 + Recycle.s_Heart.getWidth();
            canvas.drawText(percent + " %", x, (float) (lineRect.bottom - s_PaintText.getTextSize() / 1.5), s_PaintText);

            // likes
        } else {
            float x = (float) (lineRect.right - Recycle.s_Online.getWidth() * 1.25);
            float y = (lineRect.height() - Recycle.s_Online.getHeight()) / 2;

            // name
            canvas.drawText(name + ", " + age, lineRect.left + Recycle.s_Heart.getWidth() / 2, (float) (lineRect.bottom - s_PaintText.getTextSize() / 1.5), s_PaintText);

            // is online
            if (online)
                canvas.drawBitmap(Recycle.s_Online, x, lineRect.top + y, s_PaintState);
            else
                canvas.drawBitmap(Recycle.s_Offline, x, lineRect.top + y, s_PaintState);
        }
    }

    //---------------------------------------------------------------------------
    public static void release() {
        s_PaintState = null;
        s_PaintLine = null;
        s_PaintText = null;
    }
    //---------------------------------------------------------------------------
}
