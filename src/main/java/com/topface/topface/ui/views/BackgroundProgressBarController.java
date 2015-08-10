package com.topface.topface.ui.views;

import android.animation.ValueAnimator;
import android.view.View;
import android.widget.ProgressBar;

import com.nineoldandroids.view.ViewHelper;
import com.topface.topface.utils.AnimationUtils;

/**
 * Controls visibality/animation of assigned ProgressBar,
 * which usually used as background in FeedFragment
 * and visible when content is loading
 * <p/>
 * need this to stop appearing animation in 2.3 versions
 */
public class BackgroundProgressBarController {
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
            mAnimator = AnimationUtils.createProgressBarAnimator(mProgressBar);
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
