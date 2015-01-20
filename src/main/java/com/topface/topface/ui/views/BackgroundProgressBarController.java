package com.topface.topface.ui.views;

import android.view.View;
import android.widget.ProgressBar;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Controls visibality/animation of assigned ProgressBar,
 * which usually used as background in FeedFragment
 * and visible when content is loading
 * <p/>
 * need this to stop appearing animation in 2.3 versions
 */
public class BackgroundProgressBarController {
    private static final String ANIMATION_TYPE = "alpha";
    private static final int ANIMATION_DURATION = 1500; // in millis
    private static final int ANIMATION_START_DELAY = 0; // in millis
    private static final float ALPHA_START = 0f;
    private static final float ALPHA_END = 1f;

    private ProgressBar mProgressBar;
    private ValueAnimator mAnimator;

    public void setProgressBar(ProgressBar progressBar) {
        mProgressBar = progressBar;
        initAnimator();
    }

    public void show() {
        if (isReady()) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hide() {
        if (isReady()) {
            stopAnimation();
            ViewHelper.setAlpha(mProgressBar, 0f);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    public void startAnimation() {
        if (isReady()) {
            if (isAnimatorReady()) {
                ViewHelper.setAlpha(mProgressBar, 0f);
                mProgressBar.setVisibility(View.VISIBLE);
                mAnimator.start();
            }
        }
    }

    private void initAnimator() {
        releaseAnimator();
        if (isReady()) {
            mAnimator = ObjectAnimator.ofFloat(mProgressBar, ANIMATION_TYPE, ALPHA_START, ALPHA_END);
            mAnimator.setDuration(ANIMATION_DURATION);
            mAnimator.setStartDelay(ANIMATION_START_DELAY);
        }

    }

    private void stopAnimation() {
        if (isAnimatorReady()) {
            mAnimator.end();
        }
    }

    /**
     * release animator, remove all callback here, if they are
     */
    private void releaseAnimator() {
        stopAnimation();
    }

    private boolean isReady() {
        return mProgressBar != null;
    }

    private boolean isAnimatorReady() {
        return mAnimator != null;
    }
}
