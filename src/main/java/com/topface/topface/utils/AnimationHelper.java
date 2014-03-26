package com.topface.topface.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.topface.topface.R;

import java.util.LinkedList;
import java.util.List;

public class AnimationHelper {
    private Context mContext;

    public AnimationHelper(Context context) {
        mContext = context;
    }

    public void animateFadingViews(final List<View> viewsToAnimate, final boolean isShowing) {
        Animation animation = AnimationUtils.loadAnimation(mContext, isShowing ? R.anim.abc_fade_in : R.anim.abc_fade_out);
        if (animation != null) {
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (isShowing) {
                        for (View view : viewsToAnimate) {
                            view.setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!isShowing) {
                        for (View view : viewsToAnimate) {
                            view.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            
            for (View view : viewsToAnimate) {
                view.startAnimation(animation);
            }
        }
    }
}

