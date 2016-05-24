package com.topface.topface.utils;

import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.databinding.BindingAdapter;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.topface.framework.imageloader.IPhoto;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
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

    @BindingAdapter("android:layout_marginTop")
    public static void setMarginTop(View view, float padding) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();
        lp.setMargins(lp.leftMargin, (int) padding, lp.rightMargin, lp.bottomMargin);
    }

    @BindingAdapter("android:background")
    public static void setBackgroundResource(View view, @DrawableRes int bgResource) {
        view.setBackgroundResource(bgResource);
    }

    @BindingAdapter("android:textColor")
    public static void setTextColorSelector(View view, int colorSelector) {
        try {
            XmlResourceParser xrp = App.getContext().getResources().getXml(colorSelector);
            ColorStateList csl = ColorStateList.createFromXml(App.getContext().getResources(), xrp);
            ((TextView) view).setTextColor(csl);
        } catch (Exception e) {
            Debug.error(e.toString());
        }
    }

    @BindingAdapter("app:remoteSrc")
    public static void setremoteSrc(ImageViewRemote view, String res) {
        if (!TextUtils.isEmpty(res)) {
            view.setRemoteSrc(res);
        } else {
            view.setImageDrawable(null);
        }
    }

    @BindingAdapter("app:selected")
    public static void setSelected(View view, boolean isSelected) {
        view.setSelected(isSelected);
    }

    @BindingAdapter("app:setPhoto")
    public static void setPhoto(ImageViewRemote view, Object photo) {
        if (photo instanceof IPhoto) {
            view.setPhoto((IPhoto) photo);
        } else if (photo instanceof String) {
            view.setRemoteSrc((String) photo);
        } else {
            view.setImageDrawable(null);
        }
    }
}
