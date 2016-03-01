package com.topface.topface.ui.views;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
        super(context.getApplicationContext(), attrs);
        initViews(inflateRootView());
        getAttrs(context.getApplicationContext(), attrs, 0);
    }

    public BuyButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context.getApplicationContext(), attrs, defStyleAttr);
        initViews(inflateRootView());
        getAttrs(context.getApplicationContext(), attrs, defStyleAttr);
    }

    public BuyButton(Context context, T builder) {
        super(context.getApplicationContext());
        initViews(inflateRootView());
        if (builder != null) {
            build(builder);
        }
    }

    public abstract void startWaiting();

    public abstract void stopWaiting();

    private View inflateRootView() {
        View view = inflate(getContext(), getButtonLayout(), null);
        view.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        this.addView(view);
        return view;
    }

    abstract void initViews(View root);

    @LayoutRes
    abstract int getButtonLayout();

    abstract void build(@NotNull T builder);

    abstract void getAttrs(Context context, AttributeSet attrs, int defStyle);
}
