package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.topface.topface.R;

/**
 * Simple switcher to use in profile settings.
 */
public class Switcher extends ImageButton {

    private int mBackgroundOn;
    private int mBackgrounOff;

    private boolean mChecked;

    public Switcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBackgroundOn = R.drawable.switch_on;
        mBackgrounOff = R.drawable.switch_off;
        syncState();
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        syncState();
    }

    private void syncState() {
        if (mChecked) {
            setBackgroundResource(mBackgroundOn);
        } else {
            setBackgroundResource(mBackgrounOff);
        }
    }

    public boolean isChecked() {
        return mChecked;
    }
}
