package com.topface.topface.utils;

import android.view.View;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

public class AnimationUtils {
    /**
     * type of animations - alpha and other
     */
    public static class Type {
        public static final String ALPHA = "alpha";
    }

    /**
     * timig params, duration, start delay
     */
    public static class Timing {
        public int duration; // in millis
        public int startDelay; // in millis

        public Timing(int duration, int startDelay) {
            this.duration = duration;
            this.startDelay = startDelay;
        }
    }

    /**
     * params for alpha-animation init
     */
    public static class AlphaParams {
        public float start;
        public float end;

        public AlphaParams(float start, float end) {
            this.start = start;
            this.end = end;
        }
    }

    /**
     * some default presets, used for same animation of some kind of view
     * appearing of ProgressBar for example
     */
    public static class Defaults {
        /**
         * presets for ProgressBar
         * used in feeds, while loading content, in chat
         */
        public static class ProgressBar {
            public static final String TYPE = Type.ALPHA;
            public static final Timing TIMING = new Timing(1500, 0);
            public static final AlphaParams ALPHA_PARAMS = new AlphaParams(0f, 1f);
        }

        /**
         * presets for appearing image in galleries
         */
        public static class AppearingImage {
            public static final String TYPE = Type.ALPHA;
            public static final Timing TIMING = new Timing(300, 0);
            public static final AlphaParams ALPHA_PARAMS = new AlphaParams(0f, 1f);
        }
    }

    /**
     * Creates default for whole system alpha-animator of View
     * using settings for ProgressBar
     * usualy for ProgressBar
     *
     * @param view view to animate
     * @return new animator
     */
    public static ValueAnimator createProgressBarAnimator(View view) {
        ValueAnimator animator = ObjectAnimator.ofFloat(view, Defaults.ProgressBar.TYPE, Defaults.ProgressBar.ALPHA_PARAMS.start, Defaults.ProgressBar.ALPHA_PARAMS.end);
        animator.setDuration(Defaults.ProgressBar.TIMING.duration);
        animator.setStartDelay(Defaults.ProgressBar.TIMING.startDelay);
        return animator;
    }

    /**
     * Creates default for whole system alpha-animator of View
     * using settings for AppearingImage
     * usualy for loaded photos in galleries
     *
     * @param view view to animate
     * @return new animator
     */
    public static ValueAnimator createAppearingImageAnimator(View view) {
        ValueAnimator animator = ObjectAnimator.ofFloat(view, Defaults.AppearingImage.TYPE, Defaults.AppearingImage.ALPHA_PARAMS.start, Defaults.AppearingImage.ALPHA_PARAMS.end);
        animator.setDuration(Defaults.AppearingImage.TIMING.duration);
        animator.setStartDelay(Defaults.AppearingImage.TIMING.startDelay);
        return animator;
    }

}
