package com.topface.topface.utils;

import android.databinding.BindingAdapter;
import android.view.View;
import android.view.animation.AlphaAnimation;

import com.topface.topface.ui.views.ImageViewRemote;

/**
 * Сюда складывать все BindingAdapter
 * Created by tiberal on 18.01.16.
 */
public class BindingsAdapters {

    @BindingAdapter("bind:animate")
    public static void animateDisplaying(ImageViewRemote imageView, int duration) {
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(duration);
        imageView.setViewDisplayAnimate(animation);
    }

    @BindingAdapter("bind:onLongItemClick")
    public static void onLongItemClick(View view, View.OnLongClickListener longClickListener) {
        view.setOnLongClickListener(longClickListener);
    }

}
