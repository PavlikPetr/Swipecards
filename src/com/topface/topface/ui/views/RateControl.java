package com.topface.topface.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

public class RateControl extends ViewGroup {
    // Data
    private InformerView mInformerView;
    private StarsView mStarsView;

    public RateControl(Context context) {
        this(context, null);
    }

    public RateControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RateControl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mInformerView = new InformerView(context);
        addView(mInformerView);
        mStarsView = new StarsView(context, mInformerView);
        addView(mStarsView);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = 0;

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
            measuredWidth += getChildAt(i).getMeasuredWidth();
        }

        setMeasuredDimension(measuredWidth, MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int stars_x = getMeasuredWidth() - mStarsView.getMeasuredWidth();
        mStarsView.layout(stars_x, 0, stars_x + mStarsView.getMeasuredWidth(), b);
        mInformerView.layout(stars_x - mInformerView.getMeasuredWidth(), 0, stars_x, b);
    }

    public void setBlock(boolean block) {
        this.setEnabled(block);
        mStarsView.setBlock(block);
        mInformerView.setBlock(block);
    }

    public void release() {
        mStarsView.release();
        mInformerView.release();
    }

}
