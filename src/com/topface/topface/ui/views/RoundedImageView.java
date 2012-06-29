package com.topface.topface.ui.views;

import com.topface.topface.R;
import com.topface.topface.Recycle;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
    // Data
    private int mFrameType;
    // Frames Type
    private static final int INBOX = 0;
    private static final int CHAT = 1;
    //---------------------------------------------------------------------------
    public RoundedImageView(Context context) {
        this(context, null);
    }
    //---------------------------------------------------------------------------
    public RoundedImageView(Context context,AttributeSet attrs) {
        this(context, attrs, 0);
    }
    //---------------------------------------------------------------------------
    public RoundedImageView(Context context,AttributeSet attrs,int defStyle) {
        super(context, attrs, defStyle);
        setAttrs(attrs);
    }
    //---------------------------------------------------------------------------
    private void setAttrs(AttributeSet attrs) {
        if (attrs == null)
            return;

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RoundedImageView, 0, 0);
        mFrameType = a.getInteger(R.styleable.RoundedImageView_frame, 0);
        a.recycle();
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        if (mFrameType == INBOX)
            setMeasuredDimension(Recycle.s_InboxFrame.getWidth(), Recycle.s_InboxFrame.getHeight());
        else if (mFrameType == CHAT)
            setMeasuredDimension(Recycle.s_ChatFrame.getWidth(), Recycle.s_ChatFrame.getHeight());
        else
            setMeasuredDimension(0, 0);
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onDraw(Canvas canvas) {
        // Frame
        Bitmap frameBitmap = mFrameType == INBOX ? Recycle.s_InboxFrame : Recycle.s_ChatFrame;

        // people
        //int x = (frameBitmap.getWidth()-Recycle.s_People.getWidth())/2;
        //int y = (frameBitmap.getHeight()-Recycle.s_People.getHeight())/2;
        //canvas.drawBitmap(Recycle.s_People,x,y,null);

        // avatar
        super.onDraw(canvas);

        // фрейм с тенюшкой
        canvas.drawBitmap(frameBitmap, 0, 0, null);

    }
    //---------------------------------------------------------------------------
}
