package com.topface.topface.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.topface.topface.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AnimationHelper {
    private Context mContext;
    private List<View> mViewsToAnimate;

    private Animation mInAnimation;
    private Animation mOutAnimation;

    public AnimationHelper(Context context, int inResource, int outResource) {
        mContext = context;
        mViewsToAnimate = new ArrayList<>();
        generateOutAnimation(outResource);
        generateInAnimation(inResource);

    }

    private void generateOutAnimation(int outResource) {
        mOutAnimation = AnimationUtils.loadAnimation(mContext,outResource);
        if (mOutAnimation != null) {
            mOutAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    for (View view : mViewsToAnimate) {
                        view.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            for (View view : mViewsToAnimate) {
                view.setVisibility(View.GONE);
            }
        }
    }

    private void generateInAnimation(int inResource) {
        mInAnimation = AnimationUtils.loadAnimation(mContext, inResource);
        if (mInAnimation != null) {
            mInAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    for (View view : mViewsToAnimate) {
                        view.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            for (View view : mViewsToAnimate) {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    public void addView(View view) {
        mViewsToAnimate.add(view);
    }

    public void animateOut() {
        for (View view : mViewsToAnimate) {
            view.startAnimation(mOutAnimation);
        }
    }

    public void animateIn() {
        for (View view : mViewsToAnimate) {
            view.startAnimation(mInAnimation);
        }
    }
}

