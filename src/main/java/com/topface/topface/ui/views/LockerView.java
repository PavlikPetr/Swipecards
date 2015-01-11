package com.topface.topface.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.topface.topface.R;

public class LockerView extends RelativeLayout {

    public LockerView(Context context) {
        super(context);
        init();
    }

    public LockerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LockerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        LayoutInflater li;
        li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.layout_locker_view, this, true);

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return true;
    }
}
