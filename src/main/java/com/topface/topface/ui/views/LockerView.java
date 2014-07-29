package com.topface.topface.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
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
 * @attr ref android.R.styleable#LockerView_android_gravity
 * <p/>
 * Attributes for internal TextView
 * @attr ref android.R.styleable#LockerView_android_text
 * @attr ref android.R.styleable#LockerView_android_textColor
 * @attr ref android.R.styleable#LockerView_android_textSize
 * @attr ref android.R.styleable#LockerView_android_shadowColor
 * @attr ref android.R.styleable#LockerView_android_shadowDx
 * @attr ref android.R.styleable#LockerView_android_shadowDy
 * @attr ref android.R.styleable#LockerView_android_drawableLeft
 * @attr ref android.R.styleable#LockerView_android_drawableTop
 * @attr ref android.R.styleable#LockerView_android_drawableRight
 * @attr ref android.R.styleable#LockerView_android_drawableBottom
 * @attr ref android.R.styleable#LockerView_messageBackground
 * @attr ref android.R.styleable#LockerView_messagePadding
 * @attr ref android.R.styleable#LockerView_messagePaddingLeft
 * @attr ref android.R.styleable#LockerView_messagePaddingTop
 * @attr ref android.R.styleable#LockerView_messagePaddingRight
 * @attr ref android.R.styleable#LockerView_messagePaddingBottom
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

    @SuppressWarnings("deprecation")
    private void setAttrs(AttributeSet attrs) {
        Context context = getContext();
        if (context == null) return;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockerView);
        if (a != null) {
            this.setGravity(a.getInteger(R.styleable.LockerView_android_gravity, Gravity.CENTER));
            // setting TextView attributes
            mTextView.setText(a.getString(R.styleable.LockerView_android_text));
            mTextView.setTextColor(a.getColor(R.styleable.LockerView_android_textColor, Color.BLACK));
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimension(R.styleable.LockerView_android_textSize, 16));
            mTextView.setShadowLayer(a.getFloat(R.styleable.LockerView_android_shadowRadius, 0.0f),
                    a.getFloat(R.styleable.LockerView_android_shadowDx, 0.0f),
                    a.getFloat(R.styleable.LockerView_android_shadowDy, 0.0f),
                    a.getColor(R.styleable.LockerView_android_shadowColor, Color.TRANSPARENT));
            mTextView.setCompoundDrawablesWithIntrinsicBounds(
                    a.getDrawable(R.styleable.LockerView_android_drawableLeft),
                    a.getDrawable(R.styleable.LockerView_android_drawableTop),
                    a.getDrawable(R.styleable.LockerView_android_drawableRight),
                    a.getDrawable(R.styleable.LockerView_android_drawableBottom));
            mTextView.setCompoundDrawablePadding(a.getDimensionPixelSize(
                    R.styleable.LockerView_android_drawablePadding, 0));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mTextView.setBackground(a.getDrawable(R.styleable.LockerView_messageBackground));
            } else {
                mTextView.setBackgroundDrawable(a.getDrawable(R.styleable.LockerView_messageBackground));
            }
            int padding = a.getDimensionPixelSize(R.styleable.LockerView_messagePadding, 0);
            if (a.hasValue(R.styleable.LockerView_messagePadding)) {
                mTextView.setPadding(padding, padding, padding, padding);
            } else {
                mTextView.setPadding(
                        a.getDimensionPixelSize(R.styleable.LockerView_messagePaddingLeft, 0),
                        a.getDimensionPixelSize(R.styleable.LockerView_messagePaddingTop, 0),
                        a.getDimensionPixelSize(R.styleable.LockerView_messagePaddingRight, 0),
                        a.getDimensionPixelSize(R.styleable.LockerView_messagePaddingBottom, 0));
            }
            mTextView.setGravity(a.getInt(R.styleable.LockerView_messageGravity, Gravity.CENTER));
            mTextView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            mTextView.setSingleLine(false);
            a.recycle();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (mTextView == null) return;
        Drawable[] drawables = mTextView.getCompoundDrawables();
        if (drawables == null) return;
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            for (Drawable drawable : drawables) {
                if (drawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) drawable).stop();
                }
            }
        } else {
            for (Drawable drawable : drawables) {
                if (drawable instanceof AnimationDrawable) {
                    ((AnimationDrawable) drawable).start();
                }
            }
        }

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return true;
    }
}
