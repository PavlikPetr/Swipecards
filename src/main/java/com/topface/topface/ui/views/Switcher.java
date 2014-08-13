package com.topface.topface.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.Button;

import com.topface.topface.R;

/**
 * Simple switcher to use in profile settings.
 */
public class Switcher extends Button {

    private String mTextOn;
    private String mTextOff;

    private int mBackgroundOn;
    private int mBackgrounOff;

    private boolean mChecked;
    private Paint mMeter;

    public Switcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextOn = context.getString(R.string.settings_switch_on);
        mTextOff = context.getString(R.string.settings_switch_off);
        mBackgroundOn = R.drawable.edit_switch_on;
        mBackgrounOff = R.drawable.edit_switch_off;
        mMeter = new Paint();
        syncState();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        CharSequence defaultText = getText();

        if (mMeter.measureText(mTextOn, 0, mTextOn.length()) > mMeter.measureText(mTextOff, 0, mTextOff.length())) {
            setText(mTextOn);
        } else {
            setText(mTextOff);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setText(defaultText);
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        syncState();
    }

    private void syncState() {
        if (mChecked) {
            setBackgroundResource(mBackgroundOn);
            setTextColor(Color.parseColor("#FFFFFF"));
            setText(mTextOn);
        } else {
            setBackgroundResource(mBackgrounOff);
            setTextColor(Color.parseColor("#2F2F2F"));
            setText(mTextOff);
        }
    }

    public boolean isChecked() {
        return mChecked;
    }
}
