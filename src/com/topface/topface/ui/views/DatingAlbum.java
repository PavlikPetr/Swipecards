package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Gallery;

public class DatingAlbum extends Gallery {
    // Data
    //---------------------------------------------------------------------------
    public DatingAlbum(Context context,AttributeSet attrs) {
        super(context, attrs);
    }
    //---------------------------------------------------------------------------
    @Override
    public boolean onFling(MotionEvent e1,MotionEvent e2,float velocityX,float velocityY) {
        float velMax = 2500f;
        float velMin = 1000f;
        float velX = Math.abs(velocityX);
        if (velX > velMax) {
            velX = velMax;
        } else if (velX < velMin) {
            velX = velMin;
        }
        velX -= 600;
        int k = 500000;
        int speed = (int)Math.floor(1f / velX * k);
//        setAnimationDuration(speed);

        int kEvent;
        if (isScrollingLeft(e1, e2))
            kEvent = KeyEvent.KEYCODE_DPAD_LEFT; // Check if scrolling left
        else
            kEvent = KeyEvent.KEYCODE_DPAD_RIGHT; // Otherwise scrolling right

        onKeyDown(kEvent, null);

        return true;
    }
    //---------------------------------------------------------------------------
    private boolean isScrollingLeft(MotionEvent e1,MotionEvent e2) {
        return e2.getX() > e1.getX();
    }
    
    public boolean canScrollHorizontally(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 0;
        }
    }
    //---------------------------------------------------------------------------
//    @Override
//    public boolean onTouch(View v,MotionEvent event) {
//        return false;
//    }    
    //---------------------------------------------------------------------------
}
