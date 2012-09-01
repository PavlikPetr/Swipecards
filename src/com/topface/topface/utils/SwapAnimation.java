package com.topface.topface.utils;

import com.topface.topface.Static;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class SwapAnimation extends Animation {
    private View mAnimatedView;
    private View mHiddenView;
    private int mStartY, mEndY;
    private boolean mWasEndedAlready = false;
    private static final int DURATION = 200;
   
    public SwapAnimation(View view, int hiddenView) {
        setDuration(DURATION);
        
        mAnimatedView = view;
        mHiddenView = view.findViewById(hiddenView);
        
        if(mAnimatedView == null || mHiddenView == null)
            throw new NullPointerException();
        
        mStartY = mAnimatedView.getPaddingTop();
        mEndY = (mStartY == Static.HEADER_SHADOW_SHIFT ? (0 - mHiddenView.getHeight() + Static.HEADER_SHADOW_SHIFT) : Static.HEADER_SHADOW_SHIFT);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        if (interpolatedTime < 1.0f) {
            int y = (int) ((mEndY - mStartY) * interpolatedTime);
            mAnimatedView.setPadding(mAnimatedView.getPaddingLeft(), mStartY + y, mAnimatedView.getPaddingRight(), mAnimatedView.getPaddingBottom());
            mAnimatedView.requestLayout();
         } else if (!mWasEndedAlready) {
            mAnimatedView.setPadding(mAnimatedView.getPaddingLeft(), mEndY, mAnimatedView.getPaddingRight(), mAnimatedView.getPaddingBottom());
            mAnimatedView.requestLayout();
            mWasEndedAlready = true;
        }
    }
}
