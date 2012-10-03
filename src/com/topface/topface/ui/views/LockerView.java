package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.topface.topface.R;

/**
 * LockView - descendant of RelativeLayout with internal TextView for message.
 * TextView wraps background image or stretches to parent view.
 * <p/>
 * Attribute gravity used for positioning internal TextView
 *
 * @author kirussell
 * @attr ref android.R.styleable#LockView_android_gravity
 * <p/>
 * Attributes for internal TextView
 * @attr ref android.R.styleable#LockView_android_text
 * @attr ref android.R.styleable#LockView_android_textColor
 * @attr ref android.R.styleable#LockView_android_textSize
 * @attr ref android.R.styleable#LockView_android_shadowColor
 * @attr ref android.R.styleable#LockView_android_shadowDx
 * @attr ref android.R.styleable#LockView_android_shadowDy
 * @attr ref android.R.styleable#LockView_android_drawableLeft
 * @attr ref android.R.styleable#LockView_android_drawableTop
 * @attr ref android.R.styleable#LockView_android_drawableRight
 * @attr ref android.R.styleable#LockView_android_drawableBottom
 * @attr ref android.R.styleable#LockView_messageBackground
 * @attr ref android.R.styleable#LockView_messagePadding
 * @attr ref android.R.styleable#LockView_messagePaddingLeft
 * @attr ref android.R.styleable#LockView_messagePaddingTop
 * @attr ref android.R.styleable#LockView_messagePaddingRight
 * @attr ref android.R.styleable#LockView_messagePaddingBottom
 */

public class LockerView extends RelativeLayout {
    private TextView mTextView;

    public LockerView(Context context) {
        super(context);
        mTextView = new TextView(context);
        this.addView(mTextView);
    }

    public LockerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextView = new TextView(context);
        setAttrs(attrs);
        this.addView(mTextView);
    }

    public LockerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTextView = new TextView(context);
        setAttrs(attrs);
        this.addView(mTextView);
    }

    private void setAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LockView);

        this.setGravity(a.getInteger(R.styleable.LockView_android_gravity, Gravity.CENTER));

        // setting TextView attributes
        mTextView.setText(a.getString(R.styleable.LockView_android_text));
        mTextView.setTextColor(a.getColor(R.styleable.LockView_android_textColor, Color.BLACK));
        mTextView.setTextSize(a.getDimensionPixelSize(R.styleable.LockView_android_textSize, 10));
        mTextView.setShadowLayer(a.getFloat(R.styleable.LockView_android_shadowRadius, 0.0f),
                a.getFloat(R.styleable.LockView_android_shadowDx, 0.0f),
                a.getFloat(R.styleable.LockView_android_shadowDy, 0.0f),
                a.getColor(R.styleable.LockView_android_shadowColor, Color.TRANSPARENT));

        mTextView.setCompoundDrawablesWithIntrinsicBounds(
                a.getDrawable(R.styleable.LockView_android_drawableLeft),
                a.getDrawable(R.styleable.LockView_android_drawableTop),
                a.getDrawable(R.styleable.LockView_android_drawableRight),
                a.getDrawable(R.styleable.LockView_android_drawableBottom));

        mTextView.setCompoundDrawablePadding(a.getDimensionPixelSize(
                R.styleable.LockView_android_drawablePadding, 0));

        mTextView.setBackgroundDrawable(a.getDrawable(R.styleable.LockView_messageBackground));

        int padding = a.getDimensionPixelSize(R.styleable.LockView_messagePadding, 0);
        if (a.hasValue(R.styleable.LockView_messagePadding)) {
            mTextView.setPadding(padding, padding, padding, padding);
        } else {
            mTextView.setPadding(
                    a.getDimensionPixelSize(R.styleable.LockView_messagePaddingLeft, 0),
                    a.getDimensionPixelSize(R.styleable.LockView_messagePaddingTop, 0),
                    a.getDimensionPixelSize(R.styleable.LockView_messagePaddingRight, 0),
                    a.getDimensionPixelSize(R.styleable.LockView_messagePaddingBottom, 0));
        }

        mTextView.setGravity(a.getInt(R.styleable.LockView_messageGravity, Gravity.CENTER));

        mTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        mTextView.setSingleLine(false);

        a.recycle();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            for (Drawable drawable : mTextView.getCompoundDrawables()) {
                if (drawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) drawable).stop();
                }
            }
        } else {
            for (Drawable drawable : mTextView.getCompoundDrawables()) {
                if (drawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) drawable).start();
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
