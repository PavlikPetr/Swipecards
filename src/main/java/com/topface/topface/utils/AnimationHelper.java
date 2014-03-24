package com.topface.topface.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.topface.topface.R;

public class AnimationHelper {
    private static AnimationHelper mInstance;
    private Context mContext;

    public static AnimationHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new AnimationHelper(context);
        }
        return mInstance;
    }

    private AnimationHelper(Context context) {
        mContext = context;
    }

    public Animation getFadingAnimation(View view, boolean isShowing) {
        Animation animation = AnimationUtils.loadAnimation(mContext, isShowing ? R.anim.abc_fade_in : R.anim.abc_fade_out);
        if (animation != null) {
            animation.setAnimationListener(new MultipleViewsAnimationLinstener(view, isShowing));
        }
        return animation;
    }

    public static class MultipleViewsAnimationLinstener implements Animation.AnimationListener {
        private View view;
        private boolean isShowing;

        public MultipleViewsAnimationLinstener(View view, boolean isShowing) {
            this.view = view;
            this.isShowing = isShowing;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (isShowing) {
                view.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!isShowing) {
                view.setVisibility(View.GONE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}

