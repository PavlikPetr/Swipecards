package com.topface.topface.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class SwapAnimation extends Animation {
    private View mAnimatedView;
    private int mStartY, mEndY;
    private boolean mWasEndedAlready = false;
    private static final int DURATION = 200;

    public SwapAnimation(final View view,final View hiddenView) {
        setDuration(DURATION);

        mAnimatedView = view;

        if (hiddenView == null) {
            throw new NullPointerException();
        }

        mStartY = mAnimatedView.getPaddingTop();
        mEndY = (mStartY == 0 ? (0 - hiddenView.getHeight()) : 0);
        setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mStartY != 0) {
                    hiddenView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mStartY == 0) {
                    hiddenView.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
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
