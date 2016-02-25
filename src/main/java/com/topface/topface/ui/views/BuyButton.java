package com.topface.topface.ui.views;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Петр on 25.02.2016.
 * Parent for all buy buttons
 */
public abstract class BuyButton<T> extends LinearLayout {
    public BuyButton(Context context) {
        this(context, (T) null);
    }

    public BuyButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
        getAttrs(context, attrs, 0);
    }

    public BuyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
        getAttrs(context, attrs, defStyleAttr);
    }

    public BuyButton(Context context, T builder) {
        super(context);
        initViews();
        if (builder != null) {
            build(builder);
        }
    }

    public abstract void startWaiting();

    public abstract void stopWaiting();

    public boolean setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
            return true;
        }
        return false;
    }

    public void setText(TextView textView, String text) {
        if (setViewVisibility(textView, View.VISIBLE)) {
            textView.setText(text);
        }
    }

    protected void initViews() {
        inflate(getContext(), getButtonLayout(), this);
    }

    abstract
    @LayoutRes
    int getButtonLayout();

    abstract void build(@NotNull T builder);

    abstract void getAttrs(Context context, AttributeSet attrs, int defStyle);
}
